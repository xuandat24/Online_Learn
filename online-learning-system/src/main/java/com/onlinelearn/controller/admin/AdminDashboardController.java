// Thư mục: src/main/java/com/onlinelearn/controller/admin/AdminDashboardController.java
package com.onlinelearn.controller.admin;

import com.onlinelearn.entity.Subject;
import com.onlinelearn.repository.RegistrationRepository;
import com.onlinelearn.repository.SubjectRepository;
import com.onlinelearn.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final UserRepository userRepository;
    private final SubjectRepository subjectRepository;
    private final RegistrationRepository registrationRepository;

    /**
     * Bảng điều khiển quản trị viên (Admin Dashboard).
     */
    @GetMapping({"", "/dashboard"})
    public String dashboard(Model model) {
        long totalUsers = userRepository.count();
        long totalSubjects = subjectRepository.count();
        long totalRegistrations = registrationRepository.count();
        List<Subject> subjects = subjectRepository.findAll();

        // Truyền các số liệu thống kê sang View
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalSubjects", totalSubjects);
        model.addAttribute("totalRegistrations", totalRegistrations);
        model.addAttribute("subjects", subjects);

        return "admin/dashboard";
    }
}
