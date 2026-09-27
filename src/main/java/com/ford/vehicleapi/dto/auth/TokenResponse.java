package com.ford.vehicleapi.dto.auth;

import com.ford.vehicleapi.dto.user.UserResponse;

import java.time.Instant;

public record TokenResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        Instant expiresAt,
        UserResponse user
) {
}
