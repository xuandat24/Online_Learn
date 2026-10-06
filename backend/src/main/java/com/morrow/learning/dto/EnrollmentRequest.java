package com.morrow.learning.dto;

import jakarta.validation.constraints.NotNull;

public record EnrollmentRequest(@NotNull Long courseId) {
}