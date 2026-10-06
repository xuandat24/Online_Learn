package com.morrow.learning.dto;

import com.morrow.learning.domain.Registration;
import com.morrow.learning.domain.RegistrationStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RegistrationView(Long id, Long courseId, String courseTitle, Long pricePackageId,
                               String pricePackageName, String fullName, String email, String phone,
                               BigDecimal amount, String currency, RegistrationStatus status,
                               boolean loginEmailSent, LocalDateTime submittedAt, LocalDateTime paidAt) {
    public static RegistrationView from(Registration registration) {
        return new RegistrationView(registration.getId(), registration.getCourse().getId(),
                registration.getCourse().getTitle(), registration.getPricePackage().getId(),
                registration.getPricePackage().getName(), registration.getFullName(), registration.getEmail(),
                registration.getPhone(), registration.getAmount(), registration.getCurrency(),
                registration.getStatus(), registration.isLoginEmailSent(), registration.getSubmittedAt(),
                registration.getPaidAt());
    }
}