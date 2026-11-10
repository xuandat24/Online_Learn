package vn.onlinelearn.integration;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import java.math.BigDecimal;
import vn.onlinelearn.entity.PaymentTransaction;
class SePayGatewayClientTest {
    SePayGatewayClient client(){var c=new SePayGatewayClient();c.merchant="TEST";c.secret="key";c.ipnSecret="ipn-test-key";c.checkoutUrl="https://pay-sandbox.sepay.vn/v1/checkout/init";c.apiUrl="https://pgapi-sandbox.sepay.vn";c.publicUrl="https://test.example";return c;}
    @Test void signatureMatchesIndependentHmacVector(){
        var c=client();var fields=new HashMap<String,String>();fields.put("merchant","TEST");fields.put("order_amount","100000");fields.put("lang","vi");fields.put("customer_id","0");
        assertEquals("7rWtlVZsQqbazpyrR3A0+h+nHMuZbLj+fjCTWamI8cQ=",c.signature(fields));
        fields.put("lang","en");assertEquals(c.signature(Map.of("order_amount","100000","merchant","TEST")),c.signature(fields));
    }
    @Test void checkoutUsesDongAndOrderedPostFields(){var c=client();var p=new PaymentTransaction();p.amount=new BigDecimal("199000.00");p.registrationId=1L;p.orderCode="OLtest";var result=c.checkout(p);var fields=(Map<?,?>)result.get("fields");assertEquals("199000",fields.get("order_amount"));assertEquals("BANK_TRANSFER",fields.get("payment_method"));assertEquals("POST",result.get("method"));assertEquals("order_amount",fields.keySet().iterator().next());assertEquals("https://test.example/api/payments/sepay/success",fields.get("success_url"));}
    @Test void ipnSecretIsSeparateAndRequired(){var c=client();assertTrue(c.authenticate("ipn-test-key"));assertFalse(c.authenticate("key"));assertFalse(c.authenticate(null));c.ipnSecret="";assertFalse(c.authenticate(""));}
    @Test void productionAndMissingKeysAreRejected(){var c=client();c.checkoutUrl="https://pay.sepay.vn/v1/checkout/init";assertThrows(RuntimeException.class,c::configured);c=client();c.secret="";var missing=c;assertThrows(RuntimeException.class,missing::configured);}
}
