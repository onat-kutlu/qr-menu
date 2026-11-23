package com.qrmenu.auth.service;

import com.qrmenu.auth.dto.LoginRequest;
import com.qrmenu.auth.dto.LoginResponse;
import com.qrmenu.auth.entity.CompanyUser;
import com.qrmenu.auth.entity.User;
import com.qrmenu.auth.repository.CompanyUserRepository;
import com.qrmenu.auth.repository.UserRepository;
import com.qrmenu.common.exception.UnauthorizedException;
import com.qrmenu.common.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Authentication service handling login, logout, and token management.
 *
 * Features:
 * - User login with email and password
 * - JWT token generation
 * - Token validation via Redis
 * - Logout with token invalidation
 * - Token refresh
 * - Mock email verification and password reset
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final CompanyUserRepository companyUserRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, String> redisTemplate;

    private static final String TOKEN_BLACKLIST_PREFIX = "token:blacklist:";
    private static final String VERIFICATION_CODE_PREFIX = "verification:";
    private static final String RESET_TOKEN_PREFIX = "reset:";
    private static final long TOKEN_EXPIRATION_HOURS = 24;
    private static final String MOCK_VERIFICATION_CODE = "123456";

    /**
     * Authenticate user with email and password.
     * Returns JWT token and user information.
     *
     * @param request Login request with email and password
     * @return LoginResponse with token and user details
     * @throws UnauthorizedException if credentials are invalid
     */
    public LoginResponse login(LoginRequest request) {
        log.debug("Login attempt for email: {}", request.getEmail());

        // Find user by email
        User user = userRepository.findActiveByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.warn("Login failed: user not found or inactive - {}", request.getEmail());
                    return new UnauthorizedException("Invalid email or password");
                });

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            log.warn("Login failed: invalid password - {}", request.getEmail());
            throw new UnauthorizedException("Invalid email or password");
        }

        // Get user's companies
        List<CompanyUser> companyUsers = companyUserRepository.findActiveByUserId(user.getId());

        // Generate JWT token
        String token = jwtTokenProvider.generateToken(user, companyUsers);

        // Store token in Redis for validation (with TTL)
        String tokenKey = "token:" + token;
        redisTemplate.opsForValue().set(tokenKey, user.getId().toString(), TOKEN_EXPIRATION_HOURS, TimeUnit.HOURS);

        // Update last login
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        log.info("Successful login for user: {} (ID: {})", request.getEmail(), user.getId());

        return LoginResponse.builder()
                .token(token)
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .highestRole(user.getHighestRole().name())
                .companies(companyUsers.stream()
                        .map(cu -> new LoginResponse.CompanyInfo(
                                cu.getCompany().getId(),
                                cu.getCompany().getName(),
                                cu.getRole().name()
                        ))
                        .toList())
                .expiresIn(Math.max(0, jwtTokenProvider.getExpirationDateFromToken(token).getTime() - System.currentTimeMillis()))
                .build();
    }

    /**
     * Logout user by invalidating their token.
     *
     * @param token JWT token to invalidate
     */
    public void logout(String token) {
        log.debug("Logout: invalidating token");

        // Remove token from valid tokens
        String tokenKey = "token:" + token;
        redisTemplate.delete(tokenKey);

        // Add token to blacklist (in case it's still valid)
        String blacklistKey = TOKEN_BLACKLIST_PREFIX + token;
        long ttl = TOKEN_EXPIRATION_HOURS;
        redisTemplate.opsForValue().set(blacklistKey, "true", ttl, TimeUnit.HOURS);

        log.info("Token invalidated successfully");
    }

    /**
     * Validate if a token is still valid (not blacklisted and in Redis).
     *
     * @param token JWT token to validate
     * @return true if token is valid
     */
    public boolean isTokenValid(String token) {
        // Check if token is in blacklist
        String blacklistKey = TOKEN_BLACKLIST_PREFIX + token;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(blacklistKey))) {
            log.debug("Token is blacklisted");
            return false;
        }

        // Check if token exists in Redis (active tokens)
        String tokenKey = "token:" + token;
        return Boolean.TRUE.equals(redisTemplate.hasKey(tokenKey));
    }

    /**
     * Generate a verification code (mock - always returns 123456).
     *
     * @param userId User ID to generate code for
     * @return Verification code
     */
    public String generateVerificationCode(Long userId) {
        log.debug("Generating verification code for user: {}", userId);
        String key = VERIFICATION_CODE_PREFIX + userId;
        redisTemplate.opsForValue().set(key, MOCK_VERIFICATION_CODE, 24, TimeUnit.HOURS);
        return MOCK_VERIFICATION_CODE;
    }

    /**
     * Verify email with verification code.
     *
     * @param userId User ID
     * @param code Verification code
     * @return true if verification successful
     */
    public boolean verifyEmail(Long userId, String code) {
        log.debug("Verifying email for user: {}", userId);

        String key = VERIFICATION_CODE_PREFIX + userId;
        String storedCode = redisTemplate.opsForValue().get(key);

        if (storedCode == null || !storedCode.equals(code)) {
            log.warn("Email verification failed: invalid code for user {}", userId);
            return false;
        }

        User user = userRepository.findActiveById(userId)
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        user.setEmailVerified(true);
        user.setEmailVerifiedAt(LocalDateTime.now());
        userRepository.save(user);

        redisTemplate.delete(key);
        log.info("Email verified for user: {}", userId);

        return true;
    }

    /**
     * Generate a password reset token (mock).
     *
     * @param email User email
     * @return Password reset token
     */
    public String generatePasswordResetToken(String email) {
        log.debug("Generating password reset token for email: {}", email);

        User user = userRepository.findActiveByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        String resetToken = generateRandomToken();
        String key = RESET_TOKEN_PREFIX + resetToken;
        redisTemplate.opsForValue().set(key, user.getId().toString(), 1, TimeUnit.HOURS); // 1 hour validity

        log.info("Password reset token generated for user: {}", user.getId());
        return resetToken;
    }

    /**
     * Reset password with reset token.
     *
     * @param resetToken Password reset token
     * @param newPassword New password
     * @return true if password reset successful
     */
    public boolean resetPassword(String resetToken, String newPassword) {
        log.debug("Attempting password reset with token: {}", resetToken);

        String key = RESET_TOKEN_PREFIX + resetToken;
        String userIdStr = redisTemplate.opsForValue().get(key);

        if (userIdStr == null) {
            log.warn("Password reset failed: invalid or expired token");
            return false;
        }

        Long userId = Long.parseLong(userIdStr);
        User user = userRepository.findActiveById(userId)
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        redisTemplate.delete(key);
        log.info("Password reset successful for user: {}", userId);

        return true;
    }

    /**
     * Refresh an existing token.
     *
     * @param oldToken Existing JWT token
     * @return New JWT token
     */
    public String refreshToken(String oldToken) {
        if (!jwtTokenProvider.validateToken(oldToken) || !isTokenValid(oldToken)) {
            throw new UnauthorizedException("Invalid token");
        }

        Long userId = jwtTokenProvider.getUserIdFromToken(oldToken);
        User user = userRepository.findActiveById(userId)
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        List<CompanyUser> companyUsers = companyUserRepository.findActiveByUserId(userId);
        String newToken = jwtTokenProvider.generateToken(user, companyUsers);

        // Store new token in Redis
        String tokenKey = "token:" + newToken;
        redisTemplate.opsForValue().set(tokenKey, user.getId().toString(), TOKEN_EXPIRATION_HOURS, TimeUnit.HOURS);

        // Invalidate old token
        logout(oldToken);

        log.info("Token refreshed for user: {}", userId);
        return newToken;
    }

    /**
     * Generate a random token string.
     */
    private String generateRandomToken() {
        return java.util.UUID.randomUUID().toString();
    }
}
