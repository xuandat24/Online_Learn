package com.morrow.learning.dto;

import com.morrow.learning.domain.Role;
import com.morrow.learning.domain.User;
import java.time.LocalDateTime;

public record UserProfileView(
        Long id,
        String fullName,
        String email,
        String phone,
        String gender,
        String address,
        Role role,
        String status,
        LocalDateTime createdAt
) {
    public static UserProfileView from(User user) {
        return new UserProfileView(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getPhone() != null ? user.getPhone() : "",
                user.getGender() != null ? user.getGender() : "Nam",
                user.getAddress() != null ? user.getAddress() : "",
                user.getRole(),
                user.getStatus() != null ? user.getStatus() : "ACTIVE",
                user.getCreatedAt()
        );
    }
}
