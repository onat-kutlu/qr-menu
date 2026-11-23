package com.qrmenu.company.service;

import com.qrmenu.audit.service.AuditService;
import com.qrmenu.auth.entity.Company;
import com.qrmenu.auth.entity.CompanyUser;
import com.qrmenu.auth.entity.User;
import com.qrmenu.auth.repository.CompanyRepository;
import com.qrmenu.auth.repository.CompanyUserRepository;
import com.qrmenu.auth.repository.UserRepository;
import com.qrmenu.common.enums.UserRole;
import com.qrmenu.common.exception.BadRequestException;
import com.qrmenu.common.exception.ResourceNotFoundException;
import com.qrmenu.company.dto.CompanyResponse;
import com.qrmenu.company.dto.CreateCompanyRequest;
import com.qrmenu.company.dto.CreateCompanyUserRequest;
import com.qrmenu.auth.dto.UserResponse;
import com.qrmenu.common.exception.ForbiddenException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final CompanyUserRepository companyUserRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;
    private final CompanyAccessValidator companyAccessValidator;
    private final PasswordEncoder passwordEncoder;

    public CompanyResponse createCompany(CreateCompanyRequest request, Long currentUserId) {
        if (companyRepository.slugExists(request.getSlug())) {
            throw new BadRequestException("Company slug already exists");
        }

        User creator = userRepository.findActiveById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Company company = Company.builder()
                .name(request.getName())
                .slug(request.getSlug())
                .description(request.getDescription())
                .status(Company.CompanyStatus.ACTIVE)
                .createdBy(creator)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Company saved = companyRepository.save(company);

        CompanyUser companyUser = CompanyUser.builder()
                .company(saved)
                .user(creator)
                .role(UserRole.COMPANY_ADMIN)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        companyUserRepository.save(companyUser);
        auditService.log(creator.getId(), creator.getEmail(), "CREATE_COMPANY", "Created company " + saved.getName(), "Company", saved.getId(), null, null);

        return mapToResponse(saved);
    }

    public List<CompanyResponse> getCompaniesForUser(Long currentUserId) {
        return companyUserRepository.findActiveByUserId(currentUserId).stream()
                .map(companyUser -> mapToResponse(companyUser.getCompany()))
                .toList();
    }

    public CompanyResponse getCompanyById(Long companyId, Long currentUserId) {
        companyAccessValidator.requireCompanyRead(currentUserId, companyId);
        Company company = companyRepository.findById(companyId)
            .orElseThrow(() -> new ResourceNotFoundException("Company not found"));
        return mapToResponse(company);
    }

    public void addUserToCompany(Long companyId, Long userId, UserRole role, Long currentUserId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));
        User user = userRepository.findActiveById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Optional<CompanyUser> existing = companyUserRepository.findActiveByCompanyIdAndUserId(companyId, userId);
        if (existing.isPresent()) {
            throw new BadRequestException("User already belongs to this company");
        }

        Optional<CompanyUser> current = companyUserRepository.findActiveByCompanyIdAndUserId(companyId, currentUserId);
        if (current.isEmpty() || !canManageCompanyUsers(current.get().getRole())) {
            throw new ForbiddenException("Only admins can manage company users");
        }

        CompanyUser companyUser = CompanyUser.builder()
                .company(company)
                .user(user)
                .role(role)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        companyUserRepository.save(companyUser);
        auditService.log(currentUserId, user.getEmail(), "ADD_USER_TO_COMPANY", "Added user to company", "Company", companyId, null, null);
    }

        public UserResponse createCompanyUser(Long companyId, CreateCompanyUserRequest request, Long currentUserId) {
        companyAccessValidator.requireCompanyAdmin(currentUserId, companyId);
        Company company = companyRepository.findById(companyId)
            .orElseThrow(() -> new ResourceNotFoundException("Company not found"));
        if (userRepository.emailExists(request.getEmail())) {
            throw new BadRequestException("Email already exists");
        }
        User user = User.builder().email(request.getEmail()).passwordHash(passwordEncoder.encode(request.getPassword()))
            .firstName(request.getFirstName()).lastName(request.getLastName()).phone(request.getPhone())
            .isActive(true).emailVerified(false).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();
        User saved = userRepository.save(user);
        companyUserRepository.save(CompanyUser.builder().company(company).user(saved)
            .role(UserRole.COMPANY_USER).isActive(true).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build());
        auditService.log(currentUserId, actorEmail(currentUserId), "CREATE_COMPANY_USER",
            "Created company user " + saved.getEmail(), "User", saved.getId(), null, null);
        return mapUser(saved);
        }

        public List<UserResponse> getUsersByCompany(Long companyId, Long currentUserId) {
        companyAccessValidator.requireCompanyRead(currentUserId, companyId);
        return companyUserRepository.findActiveByCompanyId(companyId).stream()
            .map(membership -> mapUser(membership.getUser())).toList();
        }

    public void removeUserFromCompany(Long companyId, Long userId, Long currentUserId) {
        companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));
        User targetUser = userRepository.findActiveById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Optional<CompanyUser> current = companyUserRepository.findActiveByCompanyIdAndUserId(companyId, currentUserId);
        if (current.isEmpty() || !canManageCompanyUsers(current.get().getRole())) {
            throw new ForbiddenException("Only admins can manage company users");
        }

        CompanyUser membership = companyUserRepository.findActiveByCompanyIdAndUserId(companyId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("User is not associated with this company"));

        membership.deactivate();
        companyUserRepository.save(membership);
        auditService.log(currentUserId, targetUser.getEmail(), "REMOVE_USER_FROM_COMPANY", "Removed user from company", "Company", companyId, null, null);
    }

    private boolean canManageCompanyUsers(UserRole role) {
        return role.isAdmin() || role.isCompanyAdmin();
    }

    private String actorEmail(Long userId) {
        return userRepository.findById(userId).map(User::getEmail).orElse("unknown");
    }

    private UserResponse mapUser(User user) {
        return UserResponse.builder().id(user.getId()).email(user.getEmail()).firstName(user.getFirstName())
                .lastName(user.getLastName()).fullName(user.getFullName()).phone(user.getPhone())
                .isActive(user.getIsActive()).emailVerified(user.getEmailVerified()).globalRole(user.getGlobalRole())
                .companies(companyUserRepository.findActiveByUserId(user.getId()).stream()
                        .map(membership -> new UserResponse.CompanyInfo(membership.getCompany().getId(), membership.getCompany().getName(), membership.getRole().name())).toList())
                .lastLoginAt(user.getLastLoginAt()).createdAt(user.getCreatedAt()).build();
    }

    private CompanyResponse mapToResponse(Company company) {
        return CompanyResponse.builder()
                .id(company.getId())
                .name(company.getName())
                .slug(company.getSlug())
                .description(company.getDescription())
                .status(company.getStatus().name())
                .createdAt(company.getCreatedAt())
                .updatedAt(company.getUpdatedAt())
                .build();
    }
}
