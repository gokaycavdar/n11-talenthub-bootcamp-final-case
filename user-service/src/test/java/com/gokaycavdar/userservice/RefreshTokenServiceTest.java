package com.gokaycavdar.userservice.service;

import com.gokaycavdar.userservice.entity.RefreshToken;
import com.gokaycavdar.userservice.entity.Role;
import com.gokaycavdar.userservice.entity.User;
import com.gokaycavdar.userservice.repository.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(refreshTokenService, "refreshTokenExpirationMs", 604800000L);
    }

    @Test
    void create_shouldPersistActiveRefreshToken() {
        User user = User.builder()
                .id(1L)
                .email("gokay@example.com")
                .role(Role.ROLE_USER)
                .build();

        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RefreshToken refreshToken = refreshTokenService.create(user);

        assertNotNull(refreshToken.getToken());
        assertFalse(refreshToken.isRevoked());
        assertEquals(user, refreshToken.getUser());
        assertTrue(refreshToken.getExpiresAt().isAfter(LocalDateTime.now()));
    }

    @Test
    void findActiveByToken_shouldReturnEmpty_whenTokenRevoked() {
        RefreshToken refreshToken = RefreshToken.builder()
                .token("token")
                .expiresAt(LocalDateTime.now().plusDays(1))
                .revoked(true)
                .build();

        when(refreshTokenRepository.findByToken("token")).thenReturn(Optional.of(refreshToken));

        Optional<RefreshToken> result = refreshTokenService.findActiveByToken("token");

        assertTrue(result.isEmpty());
    }

    @Test
    void rotate_shouldRevokeCurrentTokenAndCreateNewOne() {
        User user = User.builder()
                .id(1L)
                .email("gokay@example.com")
                .role(Role.ROLE_USER)
                .build();

        RefreshToken currentToken = RefreshToken.builder()
                .token("current-token")
                .user(user)
                .expiresAt(LocalDateTime.now().plusDays(1))
                .revoked(false)
                .build();

        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RefreshToken rotatedToken = refreshTokenService.rotate(currentToken);

        assertTrue(currentToken.isRevoked());
        assertFalse(rotatedToken.isRevoked());
        assertEquals(user, rotatedToken.getUser());
        assertNotEquals("current-token", rotatedToken.getToken());
        verify(refreshTokenRepository, atLeast(2)).save(any(RefreshToken.class));
    }

    @Test
    void revoke_shouldMarkTokenAsRevoked() {
        RefreshToken refreshToken = RefreshToken.builder()
                .token("token")
                .expiresAt(LocalDateTime.now().plusDays(1))
                .revoked(false)
                .build();

        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        refreshTokenService.revoke(refreshToken);

        assertTrue(refreshToken.isRevoked());
        verify(refreshTokenRepository).save(refreshToken);
    }
}
