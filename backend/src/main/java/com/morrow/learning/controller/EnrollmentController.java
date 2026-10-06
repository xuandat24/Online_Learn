package com.morrow.learning.controller;

import com.morrow.learning.dto.EnrollmentView;
import com.morrow.learning.service.EnrollmentService;
import java.security.Principal;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/enrollments")
public class EnrollmentController {
    private final EnrollmentService enrollmentService;

    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @GetMapping("/me")
    public List<EnrollmentView> mine(Principal principal) {
        return enrollmentService.myEnrollments(principal.getName());
    }
}