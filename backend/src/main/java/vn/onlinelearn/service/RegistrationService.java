package vn.onlinelearn.service;

import java.util.*;
import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.onlinelearn.repository.Data;
import vn.onlinelearn.entity.*;
import vn.onlinelearn.dto.Requests.*;
import vn.onlinelearn.integration.*;
import vn.onlinelearn.exception.ApiException;

@Service @Transactional
public class RegistrationService {
    @Autowired PaymentService sepay;
    @Autowired Data db; @Autowired Rules rules; @Autowired VnpayClient gateway; @Autowired AuthService auth; @Autowired MailService mail;
    public Object create(RegistrationInput input) {
        var actor=rules.optional(); if(actor!=null&&!Set.of("Customer","Sale","Admin").contains(actor.role())) throw ApiException.forbidden();
        var p=db.packages.findById(input.packageId()).filter(x->x.status.equals("Active")).orElseThrow(ApiException::missing);
        db.subjects.findById(p.subjectId).filter(x->x.publishStatus.equals("Published")).orElseThrow(ApiException::missing);
        var email=Rules.email(input.email()); if(actor!=null&&actor.role().equals("Customer")&&!actor.email().equals(email)) throw ApiException.forbidden();
        var existing=db.users.findByEmail(email); if(existing.isPresent()&&!existing.get().roleCode.equals("Customer")) throw ApiException.bad("Email này không phải tài khoản học viên.");
        var r=new Registration(); r.packageId=p.packageId; r.fullName=input.fullName(); r.email=email; r.mobile=input.mobile(); r.gender=input.gender(); r.totalCost=p.salePrice; r.durationMonths=p.durationMonths;
        r.customerUserId=existing.map(u->u.userId).orElse(null); r.createdBy=actor==null?null:actor.id();
        String token=Rules.randomToken(); r.publicTokenHash=Rules.hash(token); db.registrations.save(r);
        return Rules.map("registration",r,"token",token);
    }
    public Registration authorized(Long id,String token) {
        var r=db.registrations.lockById(id).orElseThrow(ApiException::missing); var a=rules.optional();
        if(a!=null&&(Set.of("Admin","Sale").contains(a.role())||a.role().equals("Customer")&&a.id().equals(r.customerUserId))) return r;
        if(token!=null&&r.publicTokenHash!=null&&java.security.MessageDigest.isEqual(Rules.hash(token).getBytes(),r.publicTokenHash.getBytes())) return r;
        throw ApiException.forbidden();
    }
    public Object detail(Long id,String token) { var r=authorized(id,token); return Rules.map("registration",r,"payments",db.payments.findByRegistrationIdOrderByCreatedAtDesc(id).stream().map(p->Rules.map("paymentId",p.paymentId,"status",p.status,"amount",p.amount,"createdAt",p.createdAt,"orderCode",p.orderCode)).toList()); }
    public Object mine() { return db.registrations.findByCustomerUserIdOrderByRegisteredAtDesc(rules.role("Customer").id()); }
    public Object sales(String q,String status,int page,int size) { rules.role("Admin","Sale"); return Rules.page(db.registrations.findAll().stream().filter(r->q==null||r.email.contains(q.toLowerCase())).filter(r->status==null||status.equals(r.status)).sorted(Comparator.comparing((Registration r)->r.registeredAt).reversed()).toList(),page,size); }
    public Object cancel(Long id,String token) {
        var r=authorized(id,token); if(!r.status.equals("Submitted")) throw ApiException.conflict("Chỉ hủy đăng ký Submitted.");
        if(db.payments.findByRegistrationIdOrderByCreatedAtDesc(id).stream().anyMatch(p->p.status.equals("Pending"))) throw ApiException.conflict("Giao dịch đang chờ xác minh; chưa thể hủy đăng ký.");
        r.status="Cancelled"; r.updatedAt=Rules.now(); return r;
    }
    public Object payment(Long id,String token,String ip) {
        return sepay.create(authorized(id,token));
    }
    public Map<String,String> callback(Map<String,String> values) {
        if(!gateway.verify(values)) return reply("97","Invalid signature");
        return processVerified(values);
    }
    public Object recheck(Long id,String ip){
        rules.role("Admin","Sale");var r=db.registrations.lockById(id).orElseThrow(ApiException::missing);
        if(db.payments.findByRegistrationIdOrderByCreatedAtDesc(id).stream().anyMatch(x->"SEPAY".equals(x.provider)&&"Pending".equals(x.status)))return sepay.recheck(r);
        var p=db.payments.findByRegistrationIdOrderByCreatedAtDesc(id).stream().filter(x->x.status.equals("Pending")).findFirst().orElseThrow(()->ApiException.conflict("Không có giao dịch chờ xác minh."));
        var verified=gateway.query(p,ip);return processVerified(verified);
    }
    private Map<String,String> processVerified(Map<String,String> values){
        var found=db.payments.findByOrderCode(values.getOrDefault("vnp_TxnRef","")); if(found.isEmpty()) return reply("01","Order not found");
        var p=found.get(); var r=db.registrations.lockById(p.registrationId).orElseThrow(ApiException::missing);
        if("SEPAY".equals(p.provider)) return reply("01","Wrong provider");
        try { if(new BigDecimal(values.getOrDefault("vnp_Amount","-1")).compareTo(p.amount.movePointRight(2))!=0||p.amount.compareTo(r.totalCost)!=0) return reply("04","Invalid amount"); } catch(NumberFormatException e) { return reply("04","Invalid amount"); }
        if(!p.status.equals("Pending")) return reply("02","Already confirmed");
        var result=values.get("vnp_ResponseCode"); var status=values.get("vnp_TransactionStatus");
        if("01".equals(status)) return reply("00","Still pending");
        if("00".equals(result)&&"00".equals(status)) {
            String reference=values.get("vnp_TransactionNo"); if(reference==null||!reference.matches("[0-9]{1,15}")||reference.equals("0")) return reply("99","Invalid transaction reference");
            if(db.payments.existsByProviderTxnRef(reference)) return reply("02","Transaction already processed");
            if(!r.status.equals("Submitted")) return reply("02","Registration already processed");
            var user=db.users.findByEmail(r.email).orElseGet(()->{ var u=new User(); u.email=r.email; u.fullName=r.fullName; u.mobile=r.mobile; u.roleCode="Customer"; db.users.saveAndFlush(u); auth.issueLink(u,"RESET_PASSWORD"); return u; });
            if(!user.roleCode.equals("Customer")) return reply("99","Account requires review");
            p.providerTxnRef=reference; p.status="Success"; p.paidAt=Rules.now(); r.status="Paid"; r.customerUserId=user.userId; r.validFrom=Rules.now(); r.validTo=r.validFrom.plusMonths(r.durationMonths);
            var access=new CourseAccess(); access.registrationId=r.registrationId; access.userId=user.userId; access.subjectId=db.packages.findById(r.packageId).orElseThrow().subjectId; access.validFrom=r.validFrom; access.validTo=r.validTo; db.access.save(access);
            mail.enqueue(user.email,"OnlineLearn — Thanh toán đã được xác nhận","Đăng ký #"+r.registrationId+" đã được thanh toán. Quyền học có hiệu lực đến "+r.validTo+" UTC.");
        } else if("24".equals(result)) p.status="Cancelled";
        else if(Set.of("02","04","07").contains(status==null?"":status)) p.status="Failed";
        else return reply("99","Unrecognized result; pending review");
        p.rawResponse=new TreeMap<>(values).toString(); p.updatedAt=Rules.now(); r.updatedAt=Rules.now(); return reply("00","Confirm success");
    }
    private Map<String,String> reply(String code,String message) { return Map.of("RspCode",code,"Message",message); }
    public Object edit(Long id,RegistrationEdit input){
        var r=authorized(id,null);var actor=rules.actor();if(!r.status.equals("Submitted"))throw ApiException.conflict("Chỉ chỉnh sửa đăng ký Submitted.");
        if(actor.role().equals("Sale")&&!actor.id().equals(r.createdBy))throw ApiException.forbidden();
        r.fullName=input.fullName();r.mobile=input.mobile();r.gender=input.gender();r.notes=input.notes();r.updatedBy=actor.id();r.updatedAt=Rules.now();return r;
    }
}
