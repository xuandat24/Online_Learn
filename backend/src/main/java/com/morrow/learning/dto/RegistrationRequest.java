package com.morrow.learning.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegistrationRequest(
        @NotNull Long courseId,
        @NotNull Long pricePackageId,
        @NotBlank @Size(min = 2, max = 120) String fullName,
        @NotBlank @Email @Size(max = 190) String email,
        @NotBlank @Pattern(regexp = "[+0-9() .-]{7,24}") String phone
) {
}