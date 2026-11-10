package vn.onlinelearn.integration;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import vn.onlinelearn.entity.PaymentTransaction;
import vn.onlinelearn.service.Rules;
import vn.onlinelearn.exception.ApiException;
class VnpayClientTest {
    VnpayClient client;
    @BeforeEach void setup(){client=new VnpayClient();client.merchant="TESTONLY";client.secret="unit-test-secret-not-a-provider-credential";client.paymentUrl="https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";client.returnUrl="http://localhost:5173/payment/return";}
    @Test void validatesIndependentSignatureAndRejectsTampering() throws Exception {
        var values=new TreeMap<String,String>(); values.put("vnp_TmnCode","TESTONLY");values.put("vnp_Amount","123400");values.put("vnp_TxnRef","order1");
        var mac=Mac.getInstance("HmacSHA512");mac.init(new SecretKeySpec(client.secret.getBytes(StandardCharsets.UTF_8),"HmacSHA512"));
        values.put("vnp_SecureHash",HexFormat.of().formatHex(mac.doFinal("vnp_Amount=123400&vnp_TmnCode=TESTONLY&vnp_TxnRef=order1".getBytes(StandardCharsets.UTF_8))));
        assertTrue(client.verify(values)); values.put("vnp_Amount","999900");assertFalse(client.verify(values));
    }
    @Test void rejectsWrongMerchant(){var map=new HashMap<String,String>();map.put("vnp_TmnCode","OTHER");map.put("vnp_SecureHash",client.signature(client.canonical(map)));assertFalse(client.verify(map));}
    @Test void missingCredentialsFailClosed(){client.secret="";assertThrows(ApiException.class,client::configured);assertFalse(client.verify(Map.of()));}
    @Test void checkoutUsesMinorUnitsAndSandbox(){var p=new PaymentTransaction();p.registrationId=1L;p.orderCode="order1";p.amount=new java.math.BigDecimal("150000");p.expiresAt=Rules.now().plusMinutes(15);var url=client.checkout(p,"127.0.0.1");assertTrue(url.startsWith("https://sandbox.vnpayment.vn/"));assertTrue(url.contains("vnp_Amount=15000000"));assertTrue(url.contains("vnp_SecureHash="));}
    @Test void queryResponseUsesPipeSignatureAndOrderBinding() throws Exception {
        var values=new LinkedHashMap<String,String>();String[] keys={"vnp_ResponseId","vnp_Command","vnp_ResponseCode","vnp_Message","vnp_TmnCode","vnp_TxnRef","vnp_Amount","vnp_BankCode","vnp_PayDate","vnp_TransactionNo","vnp_TransactionType","vnp_TransactionStatus","vnp_OrderInfo","vnp_PromotionCode","vnp_PromotionAmount"};
        String[] fields={"test-response","querydr","00","Success","TESTONLY","order1","15000000","NCB","20261110090000","123456","01","00","OnlineLearn test","",""};
        for(int i=0;i<keys.length;i++)values.put(keys[i],fields[i]);
        var mac=Mac.getInstance("HmacSHA512");mac.init(new SecretKeySpec(client.secret.getBytes(StandardCharsets.UTF_8),"HmacSHA512"));values.put("vnp_SecureHash",HexFormat.of().formatHex(mac.doFinal(String.join("|",fields).getBytes(StandardCharsets.UTF_8))));
        assertTrue(client.verifyQuery(values,"order1"));assertFalse(client.verifyQuery(values,"different-order"));values.put("vnp_TransactionStatus","02");assertFalse(client.verifyQuery(values,"order1"));
    }
}
