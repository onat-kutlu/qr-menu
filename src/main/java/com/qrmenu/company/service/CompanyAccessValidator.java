package com.qrmenu.company.service;

import com.qrmenu.auth.entity.CompanyUser;
import com.qrmenu.auth.repository.CompanyUserRepository;
import com.qrmenu.auth.repository.UserRepository;
import com.qrmenu.common.exception.ForbiddenException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Centralizes company-level authorization decisions.
 */
@Service
@RequiredArgsConstructor
public class CompanyAccessValidator {

    private final CompanyUserRepository companyUserRepository;
    private final UserRepository userRepository;

    public void requireCompanyRead(Long userId, Long companyId) {
        if (isPlatformAdmin(userId)) {
            return;
        }

        boolean member = companyUserRepository.findActiveByCompanyIdAndUserId(companyId, userId).isPresent();
        if (!member) {
            throw new ForbiddenException("You do not have access to this company");
        }
    }

    public void requireCompanyAdmin(Long userId, Long companyId) {
        if (isPlatformAdmin(userId)) {
            return;
        }

        CompanyUser membership = companyUserRepository.findActiveByCompanyIdAndUserId(companyId, userId)
                .orElseThrow(() -> new ForbiddenException("You do not have access to this company"));
        if (membership.getRole() == null || !membership.getRole().isCompanyAdmin()) {
            throw new ForbiddenException("Company administrator access is required");
        }
    }

    public boolean isPlatformAdmin(Long userId) {
        if (userRepository.findActiveById(userId).map(user -> user.getGlobalRole() != null && user.getGlobalRole().isAdmin()).orElse(false)) {
            return true;
        }
        return companyUserRepository.findActiveByUserId(userId).stream()
                .anyMatch(membership -> membership.getRole() != null && membership.getRole().isAdmin());
    }
}
