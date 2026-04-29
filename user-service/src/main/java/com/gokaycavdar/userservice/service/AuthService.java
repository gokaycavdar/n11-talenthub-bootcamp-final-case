package com.gokaycavdar.userservice.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gokaycavdar.userservice.dto.auth.AuthResponse;
import com.gokaycavdar.userservice.dto.auth.LoginRequest;
import com.gokaycavdar.userservice.dto.auth.RefreshAccessTokenResponse;
import com.gokaycavdar.userservice.dto.auth.RegisterRequest;
import com.gokaycavdar.userservice.dto.user.UserResponse;
import com.gokaycavdar.userservice.entity.RefreshToken;
import com.gokaycavdar.userservice.entity.Role;
import com.gokaycavdar.userservice.entity.User;
import com.gokaycavdar.userservice.exception.BusinessException;
import com.gokaycavdar.userservice.exception.ResourceNotFoundException;
import com.gokaycavdar.userservice.mapper.UserMapper;
import com.gokaycavdar.userservice.repository.UserRepository;
import com.gokaycavdar.userservice.security.JwtService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final AuthenticationManager authenticationManager;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Transactional
    public AuthResult register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException("Email is already registered");
        }

        User user = User.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(Role.ROLE_USER)
                .build();

        User savedUser = userRepository.save(user);

        String accessToken = jwtService.generateAccessToken(savedUser);
        RefreshToken refreshToken = refreshTokenService.create(savedUser);
        UserResponse userResponse = userMapper.toUserResponse(savedUser);

        return new AuthResult(
                new AuthResponse(accessToken, jwtService.getAccessTokenExpirationMs(), userResponse),
                refreshToken.getToken()
        );
    }

    @Transactional
    public AuthResult login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String accessToken = jwtService.generateAccessToken(user);
        RefreshToken refreshToken = refreshTokenService.create(user);
        UserResponse userResponse = userMapper.toUserResponse(user);

        return new AuthResult(
                new AuthResponse(accessToken, jwtService.getAccessTokenExpirationMs(), userResponse),
                refreshToken.getToken()
        );
    }

    @Transactional
    public RefreshResult refresh(String refreshTokenValue) {
        RefreshToken refreshToken = refreshTokenService.findActiveByToken(refreshTokenValue)
                .orElseThrow(() -> new BusinessException("Refresh token is invalid or expired"));

        RefreshToken rotatedToken = refreshTokenService.rotate(refreshToken);
        String accessToken = jwtService.generateAccessToken(refreshToken.getUser());

        return new RefreshResult(
                new RefreshAccessTokenResponse(accessToken, jwtService.getAccessTokenExpirationMs()),
                rotatedToken.getToken()
        );
    }

    @Transactional
    public void logout(String refreshTokenValue) {
        refreshTokenService.findActiveByToken(refreshTokenValue)
                .ifPresent(refreshTokenService::revoke);
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return userMapper.toUserResponse(user);
    }

    public record AuthResult(
            AuthResponse response,
            String refreshToken
    ) {
    }

    public record RefreshResult(
            RefreshAccessTokenResponse response,
            String refreshToken
    ) {
    }
}
