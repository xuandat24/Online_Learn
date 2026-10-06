package com.morrow.learning.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SubjectWriteRequest(
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Size(max = 1000) String description,
        @NotNull Boolean active
) {
}