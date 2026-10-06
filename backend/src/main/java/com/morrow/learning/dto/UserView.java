package com.morrow.learning.dto;

import com.morrow.learning.domain.Role;

public record UserView(Long id, String fullName, String email, Role role) {
}