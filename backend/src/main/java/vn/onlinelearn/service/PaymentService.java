package vn.onlinelearn.service;

import java.util.*;
import java.math.BigDecimal;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.onlinelearn.entity.*;
import vn.onlinelearn.repository.Data;
import vn.onlinelearn.integration.*;
import vn.onlinelearn.exception.ApiException;

@Service @Transactional
public class PaymentService {
    @Autowired Data db; @Autowired SePayGatewayClient gateway; @Autowired AuthService auth; @Autowired MailService mail;
    @Value("${app.sepay.ipn-max-age-seconds:86400}") long maxAge;
    public Object create(Registration r) {
        if(!"Submitted".equals(r.status))throw ApiException.conflict("Đăng ký không thể thanh toán.");gateway.configured();
        var pending=db.payments.findByRegistrationIdOrderByCreatedAtDesc(r.registrationId).stream().filter(p->"Pending".equals(p.status)).findFirst();
        if(pending.isPresent()) {
            var p=pending.get();if(!"SEPAY".equals(p.provider))throw ApiException.conflict("Đăng ký còn giao dịch của nhà cung cấp cũ chờ xác minh.");
            return gateway.checkout(p);
        }
        if(r.totalCost.signum()<=0||r.totalCost.stripTrailingZeros().scale()>0)throw ApiException.bad("Giá thanh toán VND phải là số nguyên dương.");
        var p=new PaymentTransaction();p.registrationId=r.registrationId;p.provider="SEPAY";p.orderCode="OL"+UUID.randomUUID().toString().replace("-","");p.amount=r.totalCost;
        p.checkoutUrl=(String)gateway.checkout(p).get("checkoutUrl");db.payments.save(p);return gateway.checkout(p);
    }
    public Object ipn(String key,JsonNode body) {
        if(!gateway.authenticate(key))throw new ApiException(401,"IPN authentication failed.");
        long timestamp=body.path("timestamp").asLong(0),now=java.time.Instant.now().getEpochSecond();
        if(timestamp<=0||timestamp>now+300||timestamp<now-maxAge)throw ApiException.bad("IPN timestamp expired or invalid.");
        return process(body.path("notification_type").asText(),body.path("order"),body.path("transaction"));
    }
    public Object recheck(Registration r) {
        var p=db.payments.findByRegistrationIdOrderByCreatedAtDesc(r.registrationId).stream().filter(x->"Pending".equals(x.status)&&"SEPAY".equals(x.provider)).findFirst().orElseThrow(()->ApiException.conflict("Không có giao dịch SePay chờ xác minh."));
        var order=gateway.query(p);
        if("CANCELLED".equals(order.path("order_status").asText()))return process("ORDER_CANCELLED",order,com.fasterxml.jackson.databind.node.JsonNodeFactory.instance.objectNode());
        for(var txn:order.path("transactions"))if("PAYMENT".equals(txn.path("transaction_type").asText())&&"APPROVED".equals(txn.path("transaction_status").asText()))return process("ORDER_PAID",order,txn);
        return Map.of("success",true,"status","Pending");
    }
    private Object process(String event,JsonNode order,JsonNode txn) {
        String invoice=order.path("order_invoice_number").asText();
        // Lock registration first in every path, then reload payment under its own lock.
        var registrationId=db.payments.sepayRegistrationId(invoice).orElseThrow(ApiException::missing);
        var r=db.registrations.lockById(registrationId).orElseThrow(ApiException::missing);
        var p=db.payments.findByOrderCode(invoice).orElseThrow(ApiException::missing);
        if(!"SEPAY".equals(p.provider)||!invoice.equals(p.orderCode))throw ApiException.bad("Invalid order.");
        amount(order,"order_amount",p.amount);if(!"VND".equals(order.path("order_currency").asText())||p.amount.compareTo(r.totalCost)!=0)throw ApiException.bad("Invalid currency or registration amount.");
        if("ORDER_CANCELLED".equals(event)&&"CANCELLED".equals(order.path("order_status").asText())) {
            if("Pending".equals(p.status)){p.status="Cancelled";p.updatedAt=Rules.now();}return Map.of("success",true,"status",p.status);
        }
        if(!"ORDER_PAID".equals(event)||!"CAPTURED".equals(order.path("order_status").asText())||!"APPROVED".equals(txn.path("transaction_status").asText())||!"PAYMENT".equals(txn.path("transaction_type").asText()))
            throw ApiException.bad("Unsupported or unconfirmed payment event; no access granted.");
        amount(txn,"transaction_amount",p.amount);if(!"VND".equals(txn.path("transaction_currency").asText()))throw ApiException.bad("Invalid transaction currency.");
        String id=txn.path("id").asText();if(!id.matches("[A-Za-z0-9_-]{1,90}"))throw ApiException.bad("Invalid transaction reference.");
        String reference="SEPAY:"+id;
        if("Success".equals(p.status)) {if(!reference.equals(p.providerTxnRef))throw ApiException.conflict("Order already paid by a different transaction.");return Map.of("success",true,"status","Success");}
        if(!"Pending".equals(p.status)||!"Submitted".equals(r.status)||db.payments.existsByProviderTxnRef(reference))throw ApiException.conflict("Payment already processed.");
        var user=db.users.findByEmail(r.email).orElseGet(()->{var u=new User();u.email=r.email;u.fullName=r.fullName;u.mobile=r.mobile;u.roleCode="Customer";db.users.saveAndFlush(u);auth.issueLink(u,"RESET_PASSWORD");return u;});
        if(!"Customer".equals(user.roleCode))throw ApiException.conflict("Account requires review.");
        p.status="Success";p.providerTxnRef=reference;p.paidAt=Rules.now();p.updatedAt=Rules.now();p.rawResponse=order.toString();
        r.status="Paid";r.customerUserId=user.userId;r.validFrom=p.paidAt;r.validTo=r.validFrom.plusMonths(r.durationMonths);r.updatedAt=Rules.now();
        var access=new CourseAccess();access.registrationId=r.registrationId;access.userId=user.userId;access.subjectId=db.packages.findById(r.packageId).orElseThrow().subjectId;access.validFrom=r.validFrom;access.validTo=r.validTo;db.access.save(access);
        mail.enqueue(user.email,"OnlineLearn — Thanh toán SePay đã xác nhận","Đăng ký #"+r.registrationId+" đã thanh toán. Quyền học đến "+r.validTo+" UTC.");
        return Map.of("success",true,"status","Success");
    }
    private void amount(JsonNode node,String name,BigDecimal expected) {
        try{if(new BigDecimal(node.path(name).asText()).compareTo(expected)!=0)throw ApiException.bad("Invalid amount.");}
        catch(NumberFormatException e){throw ApiException.bad("Invalid amount.");}
    }
}
