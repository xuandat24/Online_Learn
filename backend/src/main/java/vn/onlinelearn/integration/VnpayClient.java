package vn.onlinelearn.integration;

import java.util.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import vn.onlinelearn.entity.PaymentTransaction;
import vn.onlinelearn.exception.ApiException;

/** VNPAY 2.1.0 sandbox protocol. Return-page parameters never grant access. */
@Component
public class VnpayClient {
    @Value("${app.vnpay.tmn-code}") String merchant;
    @Value("${app.vnpay.hash-secret}") String secret;
    @Value("${app.vnpay.payment-url}") String paymentUrl;
    @Value("${app.vnpay.return-url}") String returnUrl;
    @Value("${app.vnpay.query-url}") String queryUrl;
    @org.springframework.beans.factory.annotation.Autowired com.fasterxml.jackson.databind.ObjectMapper json;
    public void configured() { if(merchant.isBlank()||secret.isBlank()) throw new ApiException(503,"Chưa cấu hình VNPAY sandbox. Đăng ký được giữ ở trạng thái Submitted."); }
    public String signature(String data) {
        try { var mac=Mac.getInstance("HmacSHA512"); mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8),"HmacSHA512")); return HexFormat.of().formatHex(mac.doFinal(data.getBytes(StandardCharsets.UTF_8))); }
        catch(Exception e) { throw new IllegalStateException(e); }
    }
    public String canonical(Map<String,String> fields) {
        return new TreeMap<>(fields).entrySet().stream().filter(e->e.getKey().startsWith("vnp_")&&!Set.of("vnp_SecureHash","vnp_SecureHashType").contains(e.getKey())&&e.getValue()!=null&&!e.getValue().isEmpty())
            .map(e->e.getKey()+"="+URLEncoder.encode(e.getValue(),StandardCharsets.US_ASCII)).collect(java.util.stream.Collectors.joining("&"));
    }
    public boolean verify(Map<String,String> fields) {
        if(secret.isBlank()||!merchant.equals(fields.get("vnp_TmnCode"))) return false;
        String hash=fields.getOrDefault("vnp_SecureHash","");
        return hash.matches("[a-fA-F0-9]{128}") && MessageDigest.isEqual(signature(canonical(fields)).getBytes(StandardCharsets.US_ASCII),hash.toLowerCase(Locale.ROOT).getBytes(StandardCharsets.US_ASCII));
    }
    public String checkout(PaymentTransaction p,String ip) {
        configured(); var fmt=DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        var fields=new TreeMap<String,String>();
        fields.put("vnp_Version","2.1.0"); fields.put("vnp_Command","pay"); fields.put("vnp_TmnCode",merchant);
        fields.put("vnp_Amount",p.amount.movePointRight(2).toBigIntegerExact().toString()); fields.put("vnp_CurrCode","VND");
        fields.put("vnp_TxnRef",p.orderCode); fields.put("vnp_OrderInfo","OnlineLearn registration "+p.registrationId); fields.put("vnp_OrderType","other");
        fields.put("vnp_Locale","vn"); fields.put("vnp_ReturnUrl",returnUrl); fields.put("vnp_IpAddr",ip.contains(":")?"127.0.0.1":ip);
        fields.put("vnp_CreateDate",p.createdAt.atOffset(ZoneOffset.UTC).withOffsetSameInstant(ZoneOffset.ofHours(7)).format(fmt));
        fields.put("vnp_ExpireDate",p.expiresAt.atOffset(ZoneOffset.UTC).withOffsetSameInstant(ZoneOffset.ofHours(7)).format(fmt));
        var query=canonical(fields); return paymentUrl+"?"+query+"&vnp_SecureHash="+signature(query);
    }
    public Map<String,String> query(PaymentTransaction payment,String ip) {
        configured();
        var fmt=DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        var request=new LinkedHashMap<String,String>();
        request.put("vnp_RequestId",UUID.randomUUID().toString().replace("-",""));request.put("vnp_Version","2.1.0");
        request.put("vnp_Command","querydr");request.put("vnp_TmnCode",merchant);request.put("vnp_TxnRef",payment.orderCode);
        request.put("vnp_TransactionDate",payment.createdAt.atOffset(ZoneOffset.UTC).withOffsetSameInstant(ZoneOffset.ofHours(7)).format(fmt));
        request.put("vnp_CreateDate",java.time.ZonedDateTime.now(ZoneOffset.ofHours(7)).format(fmt));
        request.put("vnp_IpAddr",ip.contains(":")?"127.0.0.1":ip);request.put("vnp_OrderInfo","Query OnlineLearn registration "+payment.registrationId);
        request.put("vnp_SecureHash",signature(String.join("|",request.values())));
        try {
            var http=java.net.http.HttpClient.newBuilder().connectTimeout(java.time.Duration.ofSeconds(5)).build();
            var httpRequest=java.net.http.HttpRequest.newBuilder(java.net.URI.create(queryUrl)).timeout(java.time.Duration.ofSeconds(15))
                .header("Content-Type","application/json").POST(java.net.http.HttpRequest.BodyPublishers.ofString(json.writeValueAsString(request))).build();
            var response=http.send(httpRequest,java.net.http.HttpResponse.BodyHandlers.ofString());
            if(response.statusCode()!=200)throw new ApiException(503,"VNPAY chưa trả kết quả. Giao dịch tiếp tục chờ xác minh.");
            var raw=json.readTree(response.body());var values=new TreeMap<String,String>();raw.fields().forEachRemaining(e->values.put(e.getKey(),e.getValue().isNull()?"":e.getValue().asText()));
            if(!verifyQuery(values,payment.orderCode))throw new ApiException(502,"Không xác thực được phản hồi VNPAY. Giao dịch tiếp tục chờ xác minh.");
            if(!"00".equals(values.get("vnp_ResponseCode")))throw new ApiException(409,"VNPAY chưa xác nhận kết quả giao dịch. Vui lòng kiểm tra lại sau.");
            return values;
        }catch(InterruptedException e){Thread.currentThread().interrupt();throw new ApiException(503,"Truy vấn VNPAY bị gián đoạn.");}
        catch(java.io.IOException e){throw new ApiException(503,"Không kết nối được VNPAY. Giao dịch tiếp tục chờ xác minh.");}
    }
    public boolean verifyQuery(Map<String,String> values,String orderCode){
        if(secret.isBlank()||!merchant.equals(values.get("vnp_TmnCode"))||!orderCode.equals(values.get("vnp_TxnRef"))||!"querydr".equals(values.get("vnp_Command")))return false;
        var keys=List.of("vnp_ResponseId","vnp_Command","vnp_ResponseCode","vnp_Message","vnp_TmnCode","vnp_TxnRef","vnp_Amount","vnp_BankCode","vnp_PayDate","vnp_TransactionNo","vnp_TransactionType","vnp_TransactionStatus","vnp_OrderInfo","vnp_PromotionCode","vnp_PromotionAmount");
        var data=keys.stream().map(k->values.getOrDefault(k,"")).collect(java.util.stream.Collectors.joining("|"));
        var hash=values.getOrDefault("vnp_SecureHash","");
        return hash.matches("[a-fA-F0-9]{128}")&&MessageDigest.isEqual(signature(data).getBytes(StandardCharsets.US_ASCII),hash.toLowerCase(Locale.ROOT).getBytes(StandardCharsets.US_ASCII));
    }
}
