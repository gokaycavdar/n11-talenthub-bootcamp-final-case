package com.gokaycavdar.userservice.service;

import com.gokaycavdar.userservice.dto.auth.AuthResponse;
import com.gokaycavdar.userservice.dto.auth.LoginRequest;
import com.gokaycavdar.userservice.dto.auth.RefreshAccessTokenResponse;
import com.gokaycavdar.userservice.dto.auth.RegisterRequest;
import com.gokaycavdar.userservice.dto.user.UserResponse;
import com.gokaycavdar.userservice.entity.RefreshToken;
import com.gokaycavdar.userservice.entity.Role;
import com.gokaycavdar.userservice.entity.User;
import com.gokaycavdar.userservice.exception.BusinessException;
import com.gokaycavdar.userservice.mapper.UserMapper;
import com.gokaycavdar.userservice.repository.UserRepository;
import com.gokaycavdar.userservice.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private JwtService jwtService;
    @Mock
    private RefreshTokenService refreshTokenService;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_shouldThrowBusinessException_whenEmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest(
                "Gokay",
                "Cavdar",
                "gokay@example.com",
                "123456"
        );

        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class, () -> authService.register(request));

        assertEquals("Email is already registered", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_shouldCreateUserAndReturnTokens() {
        RegisterRequest request = new RegisterRequest(
                "Gokay",
                "Cavdar",
                "gokay@example.com",
                "123456"
        );

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });
        when(jwtService.generateAccessToken(any(User.class))).thenReturn("access-token");
        when(jwtService.getAccessTokenExpirationMs()).thenReturn(900000L);
        when(refreshTokenService.create(any(User.class))).thenAnswer(invocation ->
                RefreshToken.builder()
                        .token("refresh-token")
                        .user(invocation.getArgument(0))
                        .expiresAt(LocalDateTime.now().plusDays(7))
                        .revoked(false)
                        .build()
        );
        when(userMapper.toUserResponse(any(User.class))).thenReturn(
                new UserResponse(1L, "Gokay", "Cavdar", "gokay@example.com", Role.ROLE_USER)
        );

        AuthService.AuthResult result = authService.register(request);

        assertEquals("access-token", result.response().accessToken());
        assertEquals(900000L, result.response().expiresIn());
        assertEquals("refresh-token", result.refreshToken());
        assertEquals("gokay@example.com", result.response().user().email());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertEquals("Gokay", savedUser.getFirstName());
        assertEquals("Cavdar", savedUser.getLastName());
        assertEquals("gokay@example.com", savedUser.getEmail());
        assertEquals("hashed-password", savedUser.getPasswordHash());
        assertEquals(Role.ROLE_USER, savedUser.getRole());
    }

    @Test
    void login_shouldAuthenticateAndReturnTokens() {
        LoginRequest request = new LoginRequest("gokay@example.com", "123456");

        User user = User.builder()
                .id(1L)
                .firstName("Gokay")
                .lastName("Cavdar")
                .email("gokay@example.com")
                .passwordHash("hashed-password")
                .role(Role.ROLE_USER)
                .build();

        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
        when(jwtService.generateAccessToken(user)).thenReturn("access-token");
        when(jwtService.getAccessTokenExpirationMs()).thenReturn(900000L);
        when(refreshTokenService.create(user)).thenReturn(
                RefreshToken.builder()
                        .token("refresh-token")
                        .user(user)
                        .expiresAt(LocalDateTime.now().plusDays(7))
                        .revoked(false)
                        .build()
        );
        when(userMapper.toUserResponse(user)).thenReturn(
                new UserResponse(1L, "Gokay", "Cavdar", "gokay@example.com", Role.ROLE_USER)
        );

        AuthService.AuthResult result = authService.login(request);

        ArgumentCaptor<UsernamePasswordAuthenticationToken> authCaptor =
                ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);

        verify(authenticationManager).authenticate(authCaptor.capture());
        assertEquals("gokay@example.com", authCaptor.getValue().getPrincipal());
        assertEquals("123456", authCaptor.getValue().getCredentials());

        assertEquals("access-token", result.response().accessToken());
        assertEquals("refresh-token", result.refreshToken());
    }

    @Test
    void refresh_shouldRotateTokenAndReturnNewTokens() {
        User user = User.builder()
                .id(1L)
                .email("gokay@example.com")
                .role(Role.ROLE_USER)
                .build();

        RefreshToken currentToken = RefreshToken.builder()
                .token("old-refresh-token")
                .user(user)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .revoked(false)
                .build();

        RefreshToken rotatedToken = RefreshToken.builder()
                .token("new-refresh-token")
                .user(user)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .revoked(false)
                .build();

        when(refreshTokenService.findActiveByToken("old-refresh-token")).thenReturn(Optional.of(currentToken));
        when(refreshTokenService.rotate(currentToken)).thenReturn(rotatedToken);
        when(jwtService.generateAccessToken(user)).thenReturn("new-access-token");
        when(jwtService.getAccessTokenExpirationMs()).thenReturn(900000L);

        AuthService.RefreshResult result = authService.refresh("old-refresh-token");

        assertEquals("new-access-token", result.response().accessToken());
        assertEquals(900000L, result.response().expiresIn());
        assertEquals("new-refresh-token", result.refreshToken());
    }

    @Test
    void logout_shouldRevokeRefreshToken_whenTokenIsActive() {
        RefreshToken refreshToken = RefreshToken.builder()
                .token("refresh-token")
                .expiresAt(LocalDateTime.now().plusDays(7))
                .revoked(false)
                .build();

        when(refreshTokenService.findActiveByToken("refresh-token")).thenReturn(Optional.of(refreshToken));

        authService.logout("refresh-token");

        verify(refreshTokenService).revoke(refreshToken);
    }
}
