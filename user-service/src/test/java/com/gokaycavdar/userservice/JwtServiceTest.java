package com.gokaycavdar.userservice.security;

import com.gokaycavdar.userservice.entity.Role;
import com.gokaycavdar.userservice.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private User user;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(
                "change-me-in-local-env-change-me-in-local-env-123456",
                900000L
        );

        user = User.builder()
                .id(42L)
                .email("gokay@example.com")
                .role(Role.ROLE_USER)
                .build();
    }

    @Test
    void generateAccessToken_shouldContainExpectedClaims() {
        String token = jwtService.generateAccessToken(user);

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername("gokay@example.com")
                .password("ignored")
                .authorities("ROLE_USER")
                .build();

        assertEquals("gokay@example.com", jwtService.extractSubject(token));
        assertEquals(42L, jwtService.extractUserId(token));
        assertEquals("ROLE_USER", jwtService.extractRole(token));
        assertTrue(jwtService.isAccessTokenValid(token, userDetails));
        assertEquals(900000L, jwtService.getAccessTokenExpirationMs());
    }

    @Test
    void isAccessTokenValid_shouldReturnFalseForDifferentUser() {
        String token = jwtService.generateAccessToken(user);

        UserDetails differentUser = org.springframework.security.core.userdetails.User
                .withUsername("another@example.com")
                .password("ignored")
                .authorities("ROLE_USER")
                .build();

        assertFalse(jwtService.isAccessTokenValid(token, differentUser));
    }
}
