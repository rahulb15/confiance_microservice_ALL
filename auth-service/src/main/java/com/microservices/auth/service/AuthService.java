package com.microservices.auth.service;

import com.microservices.auth.dto.*;
import com.microservices.auth.entity.Role;
import com.microservices.auth.entity.User;
import com.microservices.auth.repository.RoleRepository;
import com.microservices.auth.repository.UserRepository;
import com.microservices.common.core.dto.UserPrincipal;
import com.microservices.common.core.enums.ResponseCode;
import com.microservices.common.core.exception.AuthenticationException;
import com.microservices.common.core.exception.BusinessException;
import com.microservices.common.core.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Value("${app.max-login-attempts:5}")
    private int maxLoginAttempts;

    @Value("${app.account-lock-duration-minutes:30}")
    private long accountLockDurationMinutes;

    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for username: {}", request.getUsername());

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AuthenticationException(ResponseCode. INVALID_CREDENTIALS, "Invalid username or password"));

        // Check if account is locked
        if (!user.isAccountNonLocked()) {
            if (user.getLockedAt() != null &&
                    user.getLockedAt().plusMinutes(accountLockDurationMinutes).isBefore(LocalDateTime.now())) {
                // Unlock account after lock duration
                unlockAccount(user);
            } else {
                throw new AuthenticationException(ResponseCode.ACCOUNT_LOCKED, "Account is locked due to multiple failed login attempts");
            }
        }

        // Check if account is enabled
        if (!user.isEnabled()) {
            throw new AuthenticationException(ResponseCode.ACCOUNT_DISABLED, "Account is disabled");
        }

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            handleFailedLogin(user);
            throw new AuthenticationException(ResponseCode.INVALID_CREDENTIALS, "Invalid username or password");
        }

        // Reset login attempts on successful login
        if (user.getLoginAttempts() > 0) {
            userRepository.updateLoginAttempts(user.getUsername(), 0);
        }

        // Update last login time
        userRepository.updateLastLoginTime(user.getUsername(), LocalDateTime.now());

        // Generate tokens
        Set<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        String accessToken = jwtUtil.generateToken(user.getUsername(), roles);
        String refreshToken = jwtUtil.generateRefreshToken(user.getUsername());

        log.info("Successful login for user: {}", user.getUsername());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(86400) // 24 hours in seconds
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(roles)
                .loginTime(LocalDateTime.now())
                .build();
    }

    @Transactional
    public UserDto register(RegisterRequest request) {
        log.info("Registration attempt for username: {}", request.getUsername());

        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException(ResponseCode.USERNAME_ALREADY_EXISTS, "Username already exists");
        }

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(ResponseCode.EMAIL_ALREADY_EXISTS, "Email already exists");
        }

        // Create new user
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEnabled(true);
        user.setAccountNonExpired(true);
        user.setAccountNonLocked(true);
        user.setCredentialsNonExpired(true);

        // Add default USER role
        Role userRole = roleRepository.findByName("USER")
                .orElseGet(() -> {
                    Role newRole = new Role("USER", "Default user role");
                    return roleRepository.save(newRole);
                });
        user.addRole(userRole);

        User savedUser = userRepository.save(user);

        log.info("User registered successfully: {}", savedUser.getUsername());

        return convertToUserDto(savedUser);
    }

    public AuthResponse refreshToken(RefreshTokenRequest request) {
        log.info("Refresh token request");

        try {
            if (!jwtUtil.validateToken(request.getRefreshToken())) {
                throw new AuthenticationException(ResponseCode.INVALID_TOKEN, "Invalid refresh token");
            }

            String username = jwtUtil.extractUsername(request.getRefreshToken());
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new AuthenticationException(ResponseCode.USER_NOT_FOUND, "User not found"));

            if (!user.isEnabled()) {
                throw new AuthenticationException(ResponseCode.ACCOUNT_DISABLED, "Account is disabled");
            }

            Set<String> roles = user.getRoles().stream()
                    .map(Role::getName)
                    .collect(Collectors.toSet());

            String newAccessToken = jwtUtil.generateToken(user.getUsername(), roles);
            String newRefreshToken = jwtUtil.generateRefreshToken(user.getUsername());

            return AuthResponse.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(newRefreshToken)
                    .tokenType("Bearer")
                    .expiresIn(86400)
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .roles(roles)
                    .loginTime(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("Refresh token validation failed: {}", e.getMessage());
            throw new AuthenticationException(ResponseCode.INVALID_TOKEN, "Invalid refresh token");
        }
    }

    public UserPrincipal validateToken(String token) {
        if (!jwtUtil.validateToken(token)) {
            throw new AuthenticationException(ResponseCode.INVALID_TOKEN, "Invalid token");
        }

        String username = jwtUtil.extractUsername(token);
        Set<String> roles = jwtUtil.extractRoles(token);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AuthenticationException(ResponseCode.USER_NOT_FOUND, "User not found"));

        return new UserPrincipal(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                roles,
                user.isEnabled(),
                user.getLastLoginAt()
        );
    }

    private void handleFailedLogin(User user) {
        int attempts = user.getLoginAttempts() + 1;
        userRepository.updateLoginAttempts(user.getUsername(), attempts);

        if (attempts >= maxLoginAttempts) {
            userRepository.updateAccountLocked(user.getUsername(), false, LocalDateTime.now());
            log.warn("Account locked for user: {} after {} failed attempts", user.getUsername(), attempts);
        }
    }

    @Transactional
    private void unlockAccount(User user) {
        userRepository.updateAccountLocked(user.getUsername(), true, null);
        userRepository.updateLoginAttempts(user.getUsername(), 0);
        log.info("Account unlocked for user: {}", user.getUsername());
    }

    private UserDto convertToUserDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .enabled(user.isEnabled())
                .roles(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()))
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
