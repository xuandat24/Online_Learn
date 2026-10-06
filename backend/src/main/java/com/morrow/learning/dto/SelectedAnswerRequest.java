package com.morrow.learning.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record SelectedAnswerRequest(@NotNull @Min(0) @Max(7) Integer selectedOptionIndex) {
}