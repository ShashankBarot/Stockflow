package com.inventory.management.dto.response;

import com.inventory.management.entity.User;
import java.time.LocalDateTime;

public record UserResponse(Long id, String username, String email, String role, LocalDateTime createdAt) {
    public static UserResponse from(User user) {
        String roleName = user.getRole() != null ? user.getRole().getName() : null;
        return new UserResponse(user.getId(), user.getUsername(), user.getEmail(), roleName, user.getCreatedAt());
    }
}
