package com.morrow.learning.dto;

import com.morrow.learning.domain.Enrollment;
import java.time.LocalDateTime;

public record EnrollmentView(Long id, Long courseId, String courseTitle, String status, LocalDateTime createdAt) {
    public static EnrollmentView from(Enrollment enrollment) {
        return new EnrollmentView(enrollment.getId(), enrollment.getCourse().getId(),
                enrollment.getCourse().getTitle(), enrollment.getStatus(), enrollment.getCreatedAt());
    }
}