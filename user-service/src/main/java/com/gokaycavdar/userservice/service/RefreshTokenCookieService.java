package com.gokaycavdar.userservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class RefreshTokenCookieService {

    private final String cookieName;

    public RefreshTokenCookieService(
            @Value("${auth.refresh-token.cookie-name}") String cookieName
    ) {
        this.cookieName = cookieName;
    }

    public ResponseCookie createCookie(String refreshToken, long maxAgeSeconds) {
        return ResponseCookie.from(cookieName, refreshToken)
                .httpOnly(true)
                .secure(false)
                .path("/api/v1/auth")
                .sameSite("Lax")
                .maxAge(maxAgeSeconds)
                .build();
    }

    public ResponseCookie deleteCookie() {
        return ResponseCookie.from(cookieName, "")
                .httpOnly(true)
                .secure(false)
                .path("/api/v1/auth")
                .sameSite("Lax")
                .maxAge(0)
                .build();
    }

    public String extractFromRequest(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }

        for (var cookie : request.getCookies()) {
            if (cookieName.equals(cookie.getName()) && StringUtils.hasText(cookie.getValue())) {
                return cookie.getValue();
            }
        }

        return null;
    }

    public String getSetCookieHeaderName() {
        return HttpHeaders.SET_COOKIE;
    }
}
