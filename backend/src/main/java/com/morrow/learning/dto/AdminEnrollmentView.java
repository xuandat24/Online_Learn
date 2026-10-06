package com.morrow.learning.dto;

import com.morrow.learning.domain.Enrollment;
import java.time.LocalDateTime;

public record AdminEnrollmentView(Long id, Long customerId, String customerName, String customerEmail,
                                  Long courseId, String courseTitle, String status, LocalDateTime createdAt) {
    public static AdminEnrollmentView from(Enrollment enrollment) {
        return new AdminEnrollmentView(enrollment.getId(), enrollment.getUser().getId(),
                enrollment.getUser().getFullName(), enrollment.getUser().getEmail(),
                enrollment.getCourse().getId(), enrollment.getCourse().getTitle(),
                enrollment.getStatus(), enrollment.getCreatedAt());
    }
}