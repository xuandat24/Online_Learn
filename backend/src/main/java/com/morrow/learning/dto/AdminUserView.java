package com.morrow.learning.dto;

import com.morrow.learning.domain.Role;
import com.morrow.learning.domain.User;
import java.time.LocalDateTime;

public record AdminUserView(
        Long id,
        String fullName,
        String email,
        String phone,
        String gender,
        Role role,
        String status,
        LocalDateTime createdAt) {
    public static AdminUserView from(User user) {
        return new AdminUserView(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getPhone() != null ? user.getPhone() : "-",
                user.getGender() != null ? user.getGender() : "-",
                user.getRole(),
                user.getStatus() != null ? user.getStatus() : "ACTIVE",
                user.getCreatedAt());
    }
}