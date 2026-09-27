package com.ford.vehicleapi.dto.user;

import com.ford.vehicleapi.domain.Role;
import com.ford.vehicleapi.domain.User;

import java.time.Instant;

public record UserResponse(Long id, String name, String email, Role role, Instant createdAt) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole(), user.getCreatedAt());
    }
}
