package vn.onlinelearn.integration;

import java.util.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.*;
import java.net.http.*;
import java.time.Duration;
import com.fasterxml.jackson.databind.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.Component;
import vn.onlinelearn.entity.PaymentTransaction;
import vn.onlinelearn.exception.ApiException;

/** SePay Payment Gateway sandbox protocol, distinct from the bank-transfer webhook API. */
@Component
public class SePayGatewayClient {
    @Value("${app.sepay.merchant-id:}") String merchant;
    @Value("${app.sepay.secret-key:}") String secret;
    @Value("${app.sepay.ipn-secret-key:}") String ipnSecret;
    @Value("${app.sepay.checkout-url:https://pay-sandbox.sepay.vn/v1/checkout/init}") String checkoutUrl;
    @Value("${app.sepay.api-url:https://pgapi-sandbox.sepay.vn}") String apiUrl;
    @Value("${app.public-backend-url:http://127.0.0.1:8801}") String publicUrl;
    @Autowired ObjectMapper json;
    private static final List<String> SIGNED = List.of("order_amount","merchant","currency","operation","order_description","order_invoice_number","customer_id","payment_method","success_url","error_url","cancel_url");
    public void configured() {
        if(merchant.isBlank()||secret.isBlank()||ipnSecret.isBlank()) throw new ApiException(503,"Chưa cấu hình Merchant ID, Secret Key và IPN Secret Key của SePay sandbox.");
        sandboxUrls();
    }
    private void sandboxUrls() {
        if(!"https://pay-sandbox.sepay.vn/v1/checkout/init".equals(checkoutUrl)||!"https://pgapi-sandbox.sepay.vn".equals(apiUrl))
            throw new ApiException(503,"OnlineLearn chỉ cho phép endpoint SePay sandbox.");
    }
    public String signature(Map<String,String> fields) {
        String data=SIGNED.stream().filter(k->fields.get(k)!=null&&!fields.get(k).isEmpty()&&!fields.get(k).equals("0"))
            .map(k->k+"="+fields.get(k)).collect(java.util.stream.Collectors.joining(","));
        try {var mac=Mac.getInstance("HmacSHA256");mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8),"HmacSHA256"));return Base64.getEncoder().encodeToString(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));}
        catch(Exception e){throw new IllegalStateException(e);}
    }
    public boolean authenticate(String supplied) {
        return supplied!=null&&!ipnSecret.isBlank()&&MessageDigest.isEqual(ipnSecret.getBytes(StandardCharsets.UTF_8),supplied.getBytes(StandardCharsets.UTF_8));
    }
    public Map<String,Object> checkout(PaymentTransaction p) {
        configured();var fields=new LinkedHashMap<String,String>();
        fields.put("order_amount",p.amount.toBigIntegerExact().toString());fields.put("merchant",merchant);fields.put("currency","VND");fields.put("operation","PURCHASE");
        fields.put("order_description","OnlineLearn registration "+p.registrationId);fields.put("order_invoice_number",p.orderCode);
        fields.put("payment_method","BANK_TRANSFER");
        for(String outcome:List.of("success","error","cancel"))fields.put(outcome+"_url",publicUrl.replaceAll("/+$","")+"/api/payments/sepay/"+outcome);
        fields.put("lang","vi");fields.put("signature",signature(fields));
        return Map.of("provider","SEPAY","checkoutUrl",checkoutUrl,"method","POST","fields",fields);
    }
    private JsonNode get(String path) {
        configured();
        try {
            String basic=Base64.getEncoder().encodeToString((merchant+":"+secret).getBytes(StandardCharsets.UTF_8));
            var request=HttpRequest.newBuilder(URI.create(apiUrl+path)).timeout(Duration.ofSeconds(15)).header("Authorization","Basic "+basic).header("Accept","application/json").GET().build();
            var response=HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build().send(request,HttpResponse.BodyHandlers.ofString());
            if(response.statusCode()!=200)throw new ApiException(503,"SePay chưa trả kết quả xác minh; giao dịch tiếp tục chờ xử lý.");
            return json.readTree(response.body()).path("data");
        } catch(InterruptedException e){Thread.currentThread().interrupt();throw new ApiException(503,"Truy vấn SePay bị gián đoạn.");}
        catch(java.io.IOException e){throw new ApiException(503,"Không kết nối được SePay sandbox.");}
    }
    public JsonNode query(PaymentTransaction p) {
        return get("/v1/order/detail/"+URLEncoder.encode(p.orderCode,StandardCharsets.UTF_8));
    }
}
