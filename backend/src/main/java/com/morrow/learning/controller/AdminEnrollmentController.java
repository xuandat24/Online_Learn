package com.morrow.learning.controller;

import com.morrow.learning.dto.AdminEnrollmentView;
import com.morrow.learning.service.EnrollmentService;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/enrollments")
@PreAuthorize("hasAnyRole('ADMIN', 'SALE')")
public class AdminEnrollmentController {
    private final EnrollmentService enrollmentService;

    public AdminEnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @GetMapping
    public List<AdminEnrollmentView> list() {
        return enrollmentService.allEnrollments();
    }
}