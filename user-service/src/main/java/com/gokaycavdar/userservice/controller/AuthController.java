package com.gokaycavdar.userservice.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gokaycavdar.userservice.dto.auth.AuthResponse;
import com.gokaycavdar.userservice.dto.auth.LoginRequest;
import com.gokaycavdar.userservice.dto.auth.MessageResponse;
import com.gokaycavdar.userservice.dto.auth.RefreshAccessTokenResponse;
import com.gokaycavdar.userservice.dto.auth.RegisterRequest;
import com.gokaycavdar.userservice.dto.user.UserResponse;
import com.gokaycavdar.userservice.exception.BusinessException;
import com.gokaycavdar.userservice.service.AuthService;
import com.gokaycavdar.userservice.service.RefreshTokenCookieService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;


import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Authentication and current user endpoints")
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenCookieService refreshTokenCookieService;

    @Value("${auth.refresh-token.expiration-ms}")
    private long refreshTokenExpirationMs;

    @PostMapping("/auth/register")
    @Operation(summary = "Register", description = "Creates a new user account and returns access token")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Registration successful"),
            @ApiResponse(responseCode = "400", description = "Validation or business error")
    })
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthService.AuthResult result = authService.register(request);

        return ResponseEntity.ok()
                .header(
                        refreshTokenCookieService.getSetCookieHeaderName(),
                        refreshTokenCookieService
                                .createCookie(result.refreshToken(), refreshTokenExpirationMs / 1000)
                                .toString()
                )
                .body(result.response());
    }

    @PostMapping("/auth/login")
    @Operation(summary = "Login", description = "Authenticates user and returns access token")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthService.AuthResult result = authService.login(request);

        return ResponseEntity.ok()
                .header(
                        refreshTokenCookieService.getSetCookieHeaderName(),
                        refreshTokenCookieService
                                .createCookie(result.refreshToken(), refreshTokenExpirationMs / 1000)
                                .toString()
                )
                .body(result.response());
    }

    @PostMapping("/auth/refresh")
    @Operation(summary = "Refresh access token", description = "Uses refresh token cookie and returns a rotated access token")
    public ResponseEntity<RefreshAccessTokenResponse> refresh(HttpServletRequest request) {
        String refreshToken = refreshTokenCookieService.extractFromRequest(request);

        if (!StringUtils.hasText(refreshToken)) {
            throw new BusinessException("Refresh token cookie is missing");
        }

        AuthService.RefreshResult result = authService.refresh(refreshToken);

        return ResponseEntity.ok()
                .header(
                        refreshTokenCookieService.getSetCookieHeaderName(),
                        refreshTokenCookieService
                                .createCookie(result.refreshToken(), refreshTokenExpirationMs / 1000)
                                .toString()
                )
                .body(result.response());
    }

    @PostMapping("/auth/logout")
    @Operation(summary = "Logout", description = "Revokes refresh token and clears refresh token cookie")
    public ResponseEntity<MessageResponse> logout(HttpServletRequest request) {
        String refreshToken = refreshTokenCookieService.extractFromRequest(request);

        if (StringUtils.hasText(refreshToken)) {
            authService.logout(refreshToken);
        }

        return ResponseEntity.ok()
                .header(
                        refreshTokenCookieService.getSetCookieHeaderName(),
                        refreshTokenCookieService.deleteCookie().toString()
                )
                .body(new MessageResponse("Logged out successfully"));
    }

    @GetMapping("/users/me")
    public ResponseEntity<UserResponse> me(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(authService.getCurrentUser(email));
    }
}
