package com.gokaycavdar.userservice.dto.auth;

import com.gokaycavdar.userservice.dto.user.UserResponse;

public record AuthResponse(
        String accessToken,
        long expiresIn,
        UserResponse user
) {
}
