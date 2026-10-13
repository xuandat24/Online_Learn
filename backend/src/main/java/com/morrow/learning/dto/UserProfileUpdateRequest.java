package com.morrow.learning.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserProfileUpdateRequest(
        @NotBlank @Size(max = 120) String fullName,
        @Size(max = 30) String phone,
        @Size(max = 20) String gender,
        @Size(max = 255) String address
) {}
