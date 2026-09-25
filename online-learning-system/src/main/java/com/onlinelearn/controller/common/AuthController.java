// Thư mục: src/main/java/com/onlinelearn/controller/common/AuthController.java
package com.onlinelearn.controller.common;

import com.onlinelearn.entity.Role;
import com.onlinelearn.entity.User;
import com.onlinelearn.entity.enums.UserStatus;
import com.onlinelearn.repository.RoleRepository;
import com.onlinelearn.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Hiển thị trang đăng nhập.
     */
    @GetMapping("/login")
    public String loginPage() {
        return "common/login";
    }

    /**
     * Hiển thị trang đăng ký tài khoản.
     */
    @GetMapping("/register")
    public String registerPage() {
        return "common/register";
    }

    /**
     * Xử lý đăng ký tài khoản mới cho học viên (Customer).
     */
    @PostMapping("/register")
    public String handleRegister(@RequestParam("fullName") String fullName,
                                 @RequestParam("email") String email,
                                 @RequestParam("mobile") String mobile,
                                 @RequestParam("password") String password,
                                 Model model) {
        if (userRepository.existsByEmail(email)) {
            model.addAttribute("errorMessage", "Email này đã được sử dụng. Vui lòng chọn email khác.");
            return "common/register";
        }

        Role customerRole = roleRepository.findByCode("CUSTOMER")
                .orElseThrow(() -> new IllegalStateException("Vai trò CUSTOMER chưa được tạo trong hệ thống"));

        User newUser = User.builder()
                .fullName(fullName)
                .email(email)
                .mobile(mobile)
                .password(passwordEncoder.encode(password))
                .role(customerRole)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .build();

        userRepository.save(newUser);

        return "redirect:/login?registered=true";
    }

    /**
     * Hiển thị trang lỗi 403 Forbidden.
     */
    @GetMapping("/access-denied")
    public String accessDenied() {
        return "common/error/403";
    }
}
