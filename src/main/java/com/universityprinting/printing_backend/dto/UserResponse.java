package com.universityprinting.printing_backend.dto;

import com.universityprinting.printing_backend.model.Role;
import com.universityprinting.printing_backend.model.User;
import java.time.Instant;

public record UserResponse(
    String id,
    String name,
    String email,
    String phone,
    Role role,
    Instant createdAt,
    Instant updatedAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getPhone(),
            user.getRole(),
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
    }
}
