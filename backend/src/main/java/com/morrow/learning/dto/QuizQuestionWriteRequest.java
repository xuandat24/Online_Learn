package com.morrow.learning.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record QuizQuestionWriteRequest(
        @NotBlank @Size(max = 1000) String prompt,
        @NotEmpty @Size(min = 2, max = 8) List<@NotBlank @Size(max = 500) String> options,
        @Min(0) @Max(7) int correctOptionIndex
) {
}