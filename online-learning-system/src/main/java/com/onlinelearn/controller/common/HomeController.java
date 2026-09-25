package com.onlinelearn.controller.common;

import com.onlinelearn.entity.Subject;
import com.onlinelearn.entity.enums.SubjectStatus;
import com.onlinelearn.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final SubjectRepository subjectRepository;

    /**
     * Hiển thị trang chủ với danh sách các khóa học nổi bật.
     */
    @GetMapping({"/", "/home"})
    public String index(Model model) {
        List<Subject> featuredCourses = subjectRepository.findByFeaturedTrueAndStatus(SubjectStatus.PUBLISHED);

        if (featuredCourses.isEmpty()) {
            featuredCourses = subjectRepository.findByStatus(SubjectStatus.PUBLISHED);
        }

        model.addAttribute("featuredCourses", featuredCourses);
        return "common/home";
    }

    /**
     * Redirect /content → /content/subjects (landing mặc định của khu vực quản lý nội dung)
     */
    @GetMapping("/content")
    public String contentRedirect() {
        return "redirect:/content/subjects";
    }
}
