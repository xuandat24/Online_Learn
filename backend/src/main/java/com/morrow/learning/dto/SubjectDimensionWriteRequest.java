package com.morrow.learning.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SubjectDimensionWriteRequest(
        @NotBlank @Size(max = 100) String name,
        @Size(max = 1000) String description,
        @Min(0) @Max(10000) int displayOrder,
        boolean active) {
}
