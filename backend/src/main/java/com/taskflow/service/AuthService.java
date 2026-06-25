package com.taskflow.service;

import com.taskflow.dto.request.LoginRequest;
import com.taskflow.dto.request.RegisterRequest;
import com.taskflow.dto.response.AuthResponse;
import com.taskflow.entity.User;
import com.taskflow.exception.ConflictException;
import com.taskflow.repository.UserRepository;
import com.taskflow.security.JwtUtil;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // -----------------------------------------------------------------------
    // Register — returns userId + refresh token for the controller
    // -----------------------------------------------------------------------
    public RegisterResult registerFull(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            // TC4 expects HTTP 409 — ConflictException maps to that in GlobalExceptionHandler
            throw new ConflictException("Email already in use");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(User.Role.MEMBER)
                .build();

        userRepository.save(user);

        return RegisterResult.builder()
                .userId(user.getId())
                .refreshToken(jwtUtil.generateRefreshToken(user.getEmail()))
                .build();
    }

    // -----------------------------------------------------------------------
    // Login — returns AuthResponse + refresh token for the controller
    // -----------------------------------------------------------------------
    public LoginResult loginFull(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        AuthResponse authResponse = AuthResponse.builder()
                .accessToken(jwtUtil.generateAccessToken(user.getEmail()))
                .tokenType("Bearer")
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole().name())
                .build();

        return LoginResult.builder()
                .authResponse(authResponse)
                .refreshToken(jwtUtil.generateRefreshToken(user.getEmail()))
                .build();
    }

    // -----------------------------------------------------------------------
    // Refresh — validates refresh token and issues a new access token
    // -----------------------------------------------------------------------
    public String refreshAccessToken(String refreshToken) {
        if (!jwtUtil.isTokenValid(refreshToken)) {
            throw new BadCredentialsException("Invalid or expired refresh token");
        }
        String email = jwtUtil.extractEmail(refreshToken);
        // Ensure user still exists
        userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("User not found for refresh token"));
        return jwtUtil.generateAccessToken(email);
    }

    // -----------------------------------------------------------------------
    // Inner result types (avoid polluting the DTO layer)
    // -----------------------------------------------------------------------
    @Data
    @Builder
    public static class RegisterResult {
        private Long userId;
        private String refreshToken;
    }

    @Data
    @Builder
    public static class LoginResult {
        private AuthResponse authResponse;
        private String refreshToken;
    }
}
