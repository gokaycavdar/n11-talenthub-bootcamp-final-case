package com.gokaycavdar.userservice.dto.auth;

public record RefreshAccessTokenResponse(
        String accessToken,
        long expiresIn
) {
}
