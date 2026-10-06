package com.morrow.learning.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record QuizWriteRequest(
        @NotBlank @Size(max = 160) String title,
        @NotBlank @Size(max = 1500) String description,
        @Min(0) @Max(100) int passingScore,
        boolean published,
        @NotEmpty @Size(max = 100) List<@Valid QuizQuestionWriteRequest> questions
) {
}