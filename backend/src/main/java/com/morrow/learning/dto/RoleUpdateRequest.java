package com.morrow.learning.dto;

import com.morrow.learning.domain.Role;
import jakarta.validation.constraints.NotNull;

public record RoleUpdateRequest(@NotNull Role role) {
}