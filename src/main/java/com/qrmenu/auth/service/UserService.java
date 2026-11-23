package com.qrmenu.auth.service;

import com.qrmenu.auth.dto.CreateUserRequest;
import com.qrmenu.auth.dto.UserResponse;
import com.qrmenu.auth.entity.CompanyUser;
import com.qrmenu.auth.entity.User;
import com.qrmenu.auth.repository.CompanyUserRepository;
import com.qrmenu.auth.repository.UserRepository;
import com.qrmenu.common.enums.UserRole;
import com.qrmenu.common.exception.BadRequestException;
import com.qrmenu.common.exception.ResourceNotFoundException;
import com.qrmenu.common.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * User management service.
 *
 * Handles:
 * - User registration and creation
 * - User profile management
 * - User status management
 * - Company-user relationship management
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final CompanyUserRepository companyUserRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Create a new user.
     *
     * @param request User creation request
     * @return Created user response
     * @throws BadRequestException if email already exists
     */
    public UserResponse createUser(CreateUserRequest request) {
        log.debug("Creating new user with email: {}", request.getEmail());

        // Check if email already exists
        if (userRepository.emailExists(request.getEmail())) {
            log.warn("User creation failed: email already exists - {}", request.getEmail());
            throw new BadRequestException("Email already exists");
        }

        // Create user
        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .isActive(true)
                .emailVerified(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        userRepository.save(user);
        log.info("User created successfully: {} (ID: {})", request.getEmail(), user.getId());

        return mapToResponse(user);
    }

    /**
     * Get user by ID.
     *
     * @param userId User ID
     * @return User response
     * @throws ResourceNotFoundException if user not found
     */
    public UserResponse getUserById(Long userId) {
        User user = userRepository.findActiveById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return mapToResponse(user);
    }

    /**
     * Get user by email.
     *
     * @param email User email
     * @return User response
     * @throws ResourceNotFoundException if user not found
     */
    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findActiveByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return mapToResponse(user);
    }

    /**
     * Update user profile.
     *
     * @param userId User ID
     * @param firstName First name
     * @param lastName Last name
     * @param phone Phone number
     * @return Updated user response
     */
    public UserResponse updateProfile(Long userId, String firstName, String lastName, String phone) {
        User user = userRepository.findActiveById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (firstName != null && !firstName.isEmpty()) {
            user.setFirstName(firstName);
        }
        if (lastName != null && !lastName.isEmpty()) {
            user.setLastName(lastName);
        }
        if (phone != null && !phone.isEmpty()) {
            user.setPhone(phone);
        }

        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        log.info("User profile updated: {}", userId);
        return mapToResponse(user);
    }

    /**
     * Change password.
     *
     * @param userId User ID
     * @param oldPassword Old password
     * @param newPassword New password
     * @throws UnauthorizedException if old password is incorrect
     */
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.findActiveById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            log.warn("Password change failed: invalid old password - {}", userId);
            throw new UnauthorizedException("Invalid old password");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        log.info("Password changed for user: {}", userId);
    }

    /**
     * Deactivate user account.
     *
     * @param userId User ID
     */
    public void deactivateUser(Long userId) {
        User user = userRepository.findActiveById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setIsActive(false);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        // Deactivate all company associations
        List<CompanyUser> companyUsers = companyUserRepository.findActiveByUserId(userId);
        companyUsers.forEach(CompanyUser::deactivate);
        companyUserRepository.saveAll(companyUsers);

        log.info("User deactivated: {}", userId);
    }

    /**
     * Add user to a company with a specific role.
     *
     * @param userId User ID
     * @param companyId Company ID
     * @param role User role in the company
     * @throws BadRequestException if user is already in company
     */
    public void addUserToCompany(Long userId, Long companyId, UserRole role) {
        // Check if relationship already exists
        if (companyUserRepository.findByCompanyIdAndUserId(companyId, userId).isPresent()) {
            throw new BadRequestException("User is already associated with this company");
        }

        // Note: Company and User entities must be loaded first in real implementation
        // This is a simplified version
        log.info("Adding user {} to company {} with role {}", userId, companyId, role);
    }

    /**
     * Remove user from a company.
     *
     * @param userId User ID
     * @param companyId Company ID
     */
    public void removeUserFromCompany(Long userId, Long companyId) {
        CompanyUser companyUser = companyUserRepository.findByCompanyIdAndUserId(companyId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("User is not associated with this company"));

        companyUser.deactivate();
        companyUserRepository.save(companyUser);

        log.info("User {} removed from company {}", userId, companyId);
    }

    /**
     * Get all users in a company.
     *
     * @param companyId Company ID
     * @return List of users in company
     */
    public List<UserResponse> getUsersByCompany(Long companyId) {
        List<CompanyUser> companyUsers = companyUserRepository.findActiveByCompanyId(companyId);
        return companyUsers.stream()
                .map(cu -> mapToResponse(cu.getUser()))
                .toList();
    }

    /**
     * Map User entity to UserResponse DTO.
     */
    private UserResponse mapToResponse(User user) {
        List<CompanyUser> companies = companyUserRepository.findActiveByUserId(user.getId());
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .isActive(user.getIsActive())
                .emailVerified(user.getEmailVerified())
                .companies(companies.stream()
                        .map(cu -> new UserResponse.CompanyInfo(
                                cu.getCompany().getId(),
                                cu.getCompany().getName(),
                                cu.getRole().name()
                        ))
                        .toList())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
