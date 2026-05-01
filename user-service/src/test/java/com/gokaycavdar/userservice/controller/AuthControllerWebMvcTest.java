package com.gokaycavdar.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gokaycavdar.userservice.dto.auth.AuthResponse;
import com.gokaycavdar.userservice.dto.auth.LoginRequest;
import com.gokaycavdar.userservice.dto.auth.RefreshAccessTokenResponse;
import com.gokaycavdar.userservice.dto.auth.RegisterRequest;
import com.gokaycavdar.userservice.dto.user.UserResponse;
import com.gokaycavdar.userservice.entity.Role;
import com.gokaycavdar.userservice.exception.GlobalExceptionHandler;
import com.gokaycavdar.userservice.service.AuthService;
import com.gokaycavdar.userservice.service.RefreshTokenCookieService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerWebMvcTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private AuthService authService;

    @Mock
    private RefreshTokenCookieService refreshTokenCookieService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authController, "refreshTokenExpirationMs", 604800000L);

        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();
    }

    @Test
    void register_shouldReturnAuthResponseAndSetCookie() throws Exception {
        RegisterRequest request = new RegisterRequest("Gokay", "Cavdar", "gokay@example.com", "123456");
        AuthResponse response = new AuthResponse(
                "access-token",
                900000L,
                new UserResponse(1L, "Gokay", "Cavdar", "gokay@example.com", Role.ROLE_USER)
        );

        when(authService.register(any(RegisterRequest.class)))
                .thenReturn(new AuthService.AuthResult(response, "refresh-token"));
        when(refreshTokenCookieService.getSetCookieHeaderName()).thenReturn(HttpHeaders.SET_COOKIE);
        when(refreshTokenCookieService.createCookie("refresh-token", 604800L)).thenReturn(
                ResponseCookie.from("refreshToken", "refresh-token")
                        .httpOnly(true)
                        .path("/api/v1/auth")
                        .build()
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(header().exists(HttpHeaders.SET_COOKIE))
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.user.email").value("gokay@example.com"));
    }

    @Test
    void login_shouldReturnAuthResponseAndSetCookie() throws Exception {
        LoginRequest request = new LoginRequest("gokay@example.com", "123456");
        AuthResponse response = new AuthResponse(
                "access-token",
                900000L,
                new UserResponse(1L, "Gokay", "Cavdar", "gokay@example.com", Role.ROLE_USER)
        );

        when(authService.login(any(LoginRequest.class)))
                .thenReturn(new AuthService.AuthResult(response, "refresh-token"));
        when(refreshTokenCookieService.getSetCookieHeaderName()).thenReturn(HttpHeaders.SET_COOKIE);
        when(refreshTokenCookieService.createCookie("refresh-token", 604800L)).thenReturn(
                ResponseCookie.from("refreshToken", "refresh-token")
                        .httpOnly(true)
                        .path("/api/v1/auth")
                        .build()
        );

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(header().exists(HttpHeaders.SET_COOKIE))
                .andExpect(jsonPath("$.accessToken").value("access-token"));
    }

    @Test
    void refresh_shouldReturnBadRequest_whenRefreshCookieMissing() throws Exception {
        mockMvc.perform(post("/api/v1/auth/refresh"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("BUSINESS_ERROR"))
                .andExpect(jsonPath("$.message").value("Refresh token cookie is missing"));
    }

    @Test
    void refresh_shouldReturnNewAccessTokenAndRotatedCookie() throws Exception {
        when(refreshTokenCookieService.extractFromRequest(any(HttpServletRequest.class)))
                .thenReturn("refresh-token");
        when(authService.refresh("refresh-token"))
                .thenReturn(new AuthService.RefreshResult(
                        new RefreshAccessTokenResponse("new-access-token", 900000L),
                        "rotated-refresh-token"
                ));
        when(refreshTokenCookieService.getSetCookieHeaderName()).thenReturn(HttpHeaders.SET_COOKIE);
        when(refreshTokenCookieService.createCookie("rotated-refresh-token", 604800L)).thenReturn(
                ResponseCookie.from("refreshToken", "rotated-refresh-token")
                        .httpOnly(true)
                        .path("/api/v1/auth")
                        .build()
        );

        mockMvc.perform(post("/api/v1/auth/refresh"))
                .andExpect(status().isOk())
                .andExpect(header().exists(HttpHeaders.SET_COOKIE))
                .andExpect(jsonPath("$.accessToken").value("new-access-token"));
    }

    @Test
    void logout_shouldDeleteRefreshCookie() throws Exception {
        when(refreshTokenCookieService.extractFromRequest(any(HttpServletRequest.class)))
                .thenReturn("refresh-token");
        when(refreshTokenCookieService.getSetCookieHeaderName()).thenReturn(HttpHeaders.SET_COOKIE);
        when(refreshTokenCookieService.deleteCookie()).thenReturn(
                ResponseCookie.from("refreshToken", "")
                        .httpOnly(true)
                        .path("/api/v1/auth")
                        .maxAge(0)
                        .build()
        );

        mockMvc.perform(post("/api/v1/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(header().exists(HttpHeaders.SET_COOKIE))
                .andExpect(jsonPath("$.message").value("Logged out successfully"));

        verify(authService).logout("refresh-token");
    }
}
