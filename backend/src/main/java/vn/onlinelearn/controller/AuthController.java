package vn.onlinelearn.controller;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.web.bind.annotation.*;
import vn.onlinelearn.dto.Requests.*;
import vn.onlinelearn.service.AuthService;

@RestController @RequestMapping("/api/auth")
public class AuthController {
    private final AuthService auth;
    public AuthController(AuthService auth) { this.auth=auth; }
    private Map<String,String> ok() { return Map.of("message","Thao tác thành công."); }
    @PostMapping("/register") public Object register(@Valid @RequestBody SignUp r) { return auth.register(r); }
    @PostMapping("/login") public Object login(@Valid @RequestBody Login r) { return auth.login(r); }
    @PostMapping("/verify") public Object verify(@RequestBody Map<String,String> r) { auth.verify(r.getOrDefault("token","")); return ok(); }
    @PostMapping("/forgot") public Object forgot(@Valid @RequestBody EmailRequest r) { auth.forgot(r.email(),false); return Map.of("message","Nếu email tồn tại, hệ thống sẽ gửi liên kết khôi phục."); }
    @PostMapping("/resend") public Object resend(@Valid @RequestBody EmailRequest r) { auth.forgot(r.email(),true); return Map.of("message","Nếu tài khoản cần xác thực, hệ thống sẽ gửi lại liên kết."); }
    @PostMapping("/reset") public Object reset(@Valid @RequestBody Reset r) { auth.reset(r); return ok(); }
    @GetMapping("/me") public Object me() { return auth.me(); }
    @PutMapping("/profile") public Object profile(@Valid @RequestBody Profile r) { return auth.profile(r); }
    @PostMapping("/password") public Object password(@Valid @RequestBody PasswordChange r) { auth.change(r); return ok(); }
    @PostMapping("/logout") public Object logout() { auth.logout(); return ok(); }
}
