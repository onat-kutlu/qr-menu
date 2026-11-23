package com.qrmenu.auth.controller;

import com.qrmenu.auth.dto.CreateUserRequest;
import com.qrmenu.auth.dto.LoginRequest;
import com.qrmenu.auth.dto.LoginResponse;
import com.qrmenu.auth.dto.UserResponse;
import com.qrmenu.auth.service.AuthService;
import com.qrmenu.auth.service.UserService;
import com.qrmenu.common.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication controller.
 *
 * Provides endpoints for:
 * - User login and logout
 * - User registration
 * - Token refresh
 * - Email verification (mock)
 * - Password reset (mock)
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@Validated
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    /**
     * User login endpoint.
     *
     * @param request Login credentials
     * @return LoginResponse with JWT token
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.debug("Login endpoint called for email: {}", request.getEmail());
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    /**
     * User logout endpoint.
     * Requires Authorization header with Bearer token.
     *
     * @param request HTTP request containing auth header
     * @return Success response
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
        String token = extractTokenFromRequest(request);
        authService.logout(token);
        return ResponseEntity.ok(ApiResponse.success("Logout successful"));
    }

    /**
     * User registration endpoint.
     *
     * @param request Registration data
     * @return Created user response
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody CreateUserRequest request) {
        log.debug("Registration endpoint called for email: {}", request.getEmail());
        UserResponse response = userService.createUser(request);
        return ResponseEntity.ok(ApiResponse.success("User registered successfully", response));
    }

    /**
     * Refresh token endpoint.
     * Requires Authorization header with Bearer token.
     *
     * @param request HTTP request containing auth header
     * @return New JWT token
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<String>> refreshToken(HttpServletRequest request) {
        String oldToken = extractTokenFromRequest(request);
        String newToken = authService.refreshToken(oldToken);
        return ResponseEntity.ok(ApiResponse.success("Token refreshed", newToken));
    }

    /**
     * Verify email endpoint (mock).
     * Default verification code: 123456
     *
     * @param userId User ID
     * @param code Verification code
     * @return Success/failure response
     */
    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(
            @RequestParam @NotNull Long userId,
            @RequestParam @NotBlank @Size(min = 6, max = 6) String code
    ) {
        log.debug("Email verification attempt for user: {}", userId);
        boolean verified = authService.verifyEmail(userId, code);
        if (verified) {
            return ResponseEntity.ok(ApiResponse.success("Email verified successfully"));
        } else {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Email verification failed", "VERIFICATION_FAILED"));
        }
    }

    /**
     * Forgot password endpoint (mock).
     * Generates a password reset token.
     *
     * @param email User email
     * @return Success response with message
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@RequestParam @NotBlank @Email String email) {
        log.debug("Forgot password request for email: {}", email);
        authService.generatePasswordResetToken(email);
        return ResponseEntity.ok(ApiResponse.success("Password reset instructions sent to email"));
    }

    /**
     * Reset password endpoint (mock).
     * Default reset code: 123456
     *
     * @param resetToken Password reset token
     * @param newPassword New password
     * @return Success/failure response
     */
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @RequestParam @NotBlank String resetToken,
            @RequestParam @NotBlank @Size(min = 8, max = 128) String newPassword
    ) {
        log.debug("Password reset attempt with token: {}", resetToken);
        boolean success = authService.resetPassword(resetToken, newPassword);
        if (success) {
            return ResponseEntity.ok(ApiResponse.success("Password reset successfully"));
        } else {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Password reset failed", "RESET_FAILED"));
        }
    }

    /**
     * Request email verification code (mock).
     * Always sends 123456.
     *
     * @param userId User ID
     * @return Success response with code
     */
    @PostMapping("/request-verification-code")
    public ResponseEntity<ApiResponse<String>> requestVerificationCode(@RequestParam @NotNull Long userId) {
        log.debug("Verification code request for user: {}", userId);
        String code = authService.generateVerificationCode(userId);
        return ResponseEntity.ok(ApiResponse.success("Verification code sent (mock: " + code + ")", code));
    }

    /**
     * Extract JWT token from Authorization header.
     *
     * @param request HTTP request
     * @return JWT token
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        throw new IllegalArgumentException("Invalid authorization header");
    }
}
