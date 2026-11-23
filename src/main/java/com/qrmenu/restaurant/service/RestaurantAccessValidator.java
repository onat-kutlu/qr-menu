package com.qrmenu.restaurant.service;

import com.qrmenu.auth.entity.Restaurant;
import com.qrmenu.auth.entity.CompanyUser;
import com.qrmenu.auth.repository.CompanyUserRepository;
import com.qrmenu.auth.repository.RestaurantUserRepository;
import com.qrmenu.common.exception.ForbiddenException;
import com.qrmenu.company.service.CompanyAccessValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Centralizes restaurant access checks. Restaurant-level permissions will be
 * added when the RestaurantUser model is introduced in phase 2.
 */
@Service
@RequiredArgsConstructor
public class RestaurantAccessValidator {

    private final CompanyAccessValidator companyAccessValidator;
    private final CompanyUserRepository companyUserRepository;
    private final RestaurantUserRepository restaurantUserRepository;

    public void requireRead(Long userId, Restaurant restaurant) {
        requirePermission(userId, restaurant, RestaurantPermission.VIEW_RESTAURANT);
    }

    public void requireCompanyRead(Long userId, Long companyId) {
        companyAccessValidator.requireCompanyRead(userId, companyId);
    }

    public void requirePermission(Long userId, Restaurant restaurant, RestaurantPermission permission) {
        if (companyAccessValidator.isPlatformAdmin(userId)) {
            return;
        }
        CompanyUser membership = companyUserRepository.findActiveByCompanyIdAndUserId(
                        restaurant.getCompany().getId(), userId)
                .orElseThrow(() -> new ForbiddenException("You do not have access to this restaurant"));
        if (membership.getRole() != null && membership.getRole().isCompanyAdmin()) {
            return;
        }
        if (restaurantUserRepository.findActiveByRestaurantIdAndUserId(restaurant.getId(), userId)
                .map(assignment -> assignment.hasPermission(permission.name()))
                .orElse(false)) {
            return;
        }
        throw new ForbiddenException("Restaurant permission is required: " + permission.name());
    }
}
