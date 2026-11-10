package vn.onlinelearn.service;

import java.util.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.onlinelearn.repository.Data;
import vn.onlinelearn.entity.*;
import vn.onlinelearn.dto.Requests.*;
import vn.onlinelearn.integration.MailService;
import vn.onlinelearn.security.JwtService;
import vn.onlinelearn.exception.ApiException;

@Service @Transactional
public class AuthService {
    @Autowired Data db; @Autowired Rules rules; @Autowired PasswordEncoder passwords; @Autowired JwtService jwt; @Autowired MailService mail;
    @Value("${app.frontend-url}") String frontend;
    @Value("${app.verify-hours}") long verifyHours;
    @Value("${app.reset-minutes}") long resetMinutes;
    public Map<String,Object> register(SignUp input) {
        Rules.password(input.password(),input.password());
        String email=Rules.email(input.email());
        if(db.users.existsByEmail(email)) throw ApiException.conflict("Email đã tồn tại. Hãy đăng nhập hoặc khôi phục mật khẩu.");
        var u=new User(); u.fullName=input.fullName().strip(); u.email=email; u.passwordHash=passwords.encode(input.password()); u.roleCode="Customer";
        db.users.save(u); issueLink(u,"VERIFY_EMAIL"); return Rules.map("message","Tài khoản đã được tạo. Kiểm tra email để xác thực trước khi đăng nhập.");
    }
    public Map<String,Object> login(Login input) {
        var u=db.users.findByEmail(Rules.email(input.email())).orElse(null);
        if(u==null || u.passwordHash==null || !passwords.matches(input.password(),u.passwordHash)) throw new ApiException(401,"Email hoặc mật khẩu không đúng.");
        if(!u.status.equals("Active")) throw new ApiException(403,"Tài khoản đã bị vô hiệu hóa.");
        if(u.emailVerifiedAt==null) throw new ApiException(403,"Vui lòng xác thực email trước khi đăng nhập.");
        return Rules.map("token",jwt.issue(u),"user",u);
    }
    public void issueLink(User u,String purpose) {
        // One active token per purpose. Locking the user serializes reset/verify requests.
        db.users.lockById(u.userId).orElseThrow(ApiException::missing);
        var token=Rules.randomToken(); var t=new AuthToken(); t.userId=u.userId; t.tokenHash=Rules.hash(token); t.purpose=purpose;
        t.expiresAt=purpose.equals("VERIFY_EMAIL")?Rules.now().plusHours(verifyHours):Rules.now().plusMinutes(resetMinutes); db.tokens.save(t);
        var path=purpose.equals("VERIFY_EMAIL")?"/verify-email":"/reset-password";
        mail.enqueue(u.email,purpose.equals("VERIFY_EMAIL")?"OnlineLearn — Xác thực email":"OnlineLearn — Thiết lập mật khẩu",
            "Xin chào "+u.fullName+",\n\nMở liên kết sau để tiếp tục:\n"+frontend+path+"?token="+token+"\n\nLiên kết có thời hạn và chỉ sử dụng một lần. Nếu không yêu cầu, bạn có thể bỏ qua email này.");
    }
    public void forgot(String email,boolean verify) {
        db.users.findByEmail(Rules.email(email)).filter(u->u.status.equals("Active")).ifPresent(u->{ if(!verify || u.emailVerifiedAt==null) issueLink(u,verify?"VERIFY_EMAIL":"RESET_PASSWORD"); });
    }
    private AuthToken token(String raw,String purpose) {
        var t=db.tokens.findByTokenHash(Rules.hash(raw)).orElseThrow(()->ApiException.bad("Liên kết không hợp lệ."));
        if(!t.purpose.equals(purpose) || t.consumedAt!=null || !Rules.now().isBefore(t.expiresAt)) throw ApiException.bad("Liên kết đã hết hạn hoặc đã được sử dụng.");
        t.consumedAt=Rules.now(); return t;
    }
    public void verify(String raw) {
        var t=token(raw,"VERIFY_EMAIL"); var u=db.users.lockById(t.userId).orElseThrow(ApiException::missing); u.emailVerifiedAt=Rules.now();
        db.registrations.findByEmail(u.email).stream().filter(r->r.customerUserId==null).forEach(r->r.customerUserId=u.userId);
    }
    public void reset(Reset input) {
        Rules.password(input.password(),input.confirmPassword()); var t=token(input.token(),"RESET_PASSWORD");
        var u=db.users.lockById(t.userId).orElseThrow(ApiException::missing); u.passwordHash=passwords.encode(input.password()); u.emailVerifiedAt=Rules.now(); u.tokenVersion++;
        // Consume all outstanding reset links after a password change.
        db.tokens.findAll().stream().filter(x->x.userId.equals(u.userId) && x.purpose.equals("RESET_PASSWORD") && x.consumedAt==null).forEach(x->x.consumedAt=Rules.now());
        db.registrations.findByEmail(u.email).stream().filter(r->r.customerUserId==null).forEach(r->r.customerUserId=u.userId);
    }
    public User me() { return db.users.findById(rules.actor().id()).orElseThrow(ApiException::missing); }
    public User profile(Profile input) {
        var u=me(); u.fullName=input.fullName().strip(); u.gender=input.gender(); u.mobile=input.mobile(); u.address=input.address(); u.avatarUrl=Rules.url(input.avatarUrl()); return u;
    }
    public void change(PasswordChange input) {
        var u=db.users.lockById(rules.actor().id()).orElseThrow(ApiException::missing);
        if(u.passwordHash==null || !passwords.matches(input.currentPassword(),u.passwordHash)) throw ApiException.bad("Mật khẩu hiện tại không đúng.");
        Rules.password(input.password(),input.confirmPassword()); u.passwordHash=passwords.encode(input.password()); u.tokenVersion++;
        db.tokens.findAll().stream().filter(x->x.userId.equals(u.userId) && x.purpose.equals("RESET_PASSWORD") && x.consumedAt==null).forEach(x->x.consumedAt=Rules.now());
    }
    public void logout() { var u=db.users.lockById(rules.actor().id()).orElseThrow(ApiException::missing); u.tokenVersion++; }
}
