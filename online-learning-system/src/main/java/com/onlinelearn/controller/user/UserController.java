// Thư mục: src/main/java/com/onlinelearn/controller/user/UserController.java
package com.onlinelearn.controller.user;

import com.onlinelearn.entity.CourseAccess;
import com.onlinelearn.entity.User;
import com.onlinelearn.repository.CourseAccessRepository;
import com.onlinelearn.repository.UserRepository;
import com.onlinelearn.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final CourseAccessRepository courseAccessRepository;
    private final UserRepository userRepository;

    /**
     * Bảng điều khiển học viên: hiển thị các khóa học đã được cấp quyền truy cập.
     */
    @GetMapping({"", "/dashboard"})
    public String dashboard(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if (userDetails != null && userDetails.getUser() != null) {
            List<CourseAccess> accesses = courseAccessRepository.findByCustomerId(userDetails.getId());
            model.addAttribute("enrolledCourses", accesses);
        } else {
            model.addAttribute("enrolledCourses", Collections.emptyList());
        }

        return "user/dashboard";
    }

    /**
     * Hiển thị thông tin cá nhân của học viên.
     */
    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if (userDetails != null) {
            User user = userRepository.findById(userDetails.getId())
                    .orElse(userDetails.getUser());
            model.addAttribute("user", user);
        }

        return "user/profile";
    }

    /**
     * Cập nhật thông tin cá nhân của học viên.
     */
    @PostMapping("/profile")
    public String updateProfile(@AuthenticationPrincipal CustomUserDetails userDetails,
                                @ModelAttribute("user") User formUser,
                                Model model) {
        if (userDetails != null) {
            User currentUser = userRepository.findById(userDetails.getId()).orElse(null);
            if (currentUser != null) {
                currentUser.setFullName(formUser.getFullName());
                currentUser.setMobile(formUser.getMobile());
                currentUser.setAddress(formUser.getAddress());
                userRepository.save(currentUser);

                model.addAttribute("user", currentUser);
                model.addAttribute("successMessage", "Thông tin hồ sơ đã được cập nhật thành công!");
                return "user/profile";
            }
        }

        return "redirect:/user/dashboard";
    }
}
