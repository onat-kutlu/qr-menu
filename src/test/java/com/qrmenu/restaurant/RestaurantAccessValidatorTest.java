package com.qrmenu.restaurant;

import com.qrmenu.auth.entity.Company;
import com.qrmenu.auth.entity.CompanyUser;
import com.qrmenu.auth.entity.Restaurant;
import com.qrmenu.auth.entity.RestaurantUser;
import com.qrmenu.auth.entity.User;
import com.qrmenu.auth.repository.CompanyUserRepository;
import com.qrmenu.auth.repository.RestaurantUserRepository;
import com.qrmenu.common.enums.UserRole;
import com.qrmenu.common.exception.ForbiddenException;
import com.qrmenu.company.service.CompanyAccessValidator;
import com.qrmenu.restaurant.service.RestaurantAccessValidator;
import com.qrmenu.restaurant.service.RestaurantPermission;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RestaurantAccessValidatorTest {

    @Mock
    private CompanyAccessValidator companyAccessValidator;
    @Mock
    private CompanyUserRepository companyUserRepository;
    @Mock
    private RestaurantUserRepository restaurantUserRepository;

    @InjectMocks
    private RestaurantAccessValidator validator;

    @Test
    void shouldAllowAssignedRestaurantPermission() {
        Restaurant restaurant = restaurant();
        User user = User.builder().id(5L).build();
        when(companyAccessValidator.isPlatformAdmin(5L)).thenReturn(false);
        when(companyUserRepository.findActiveByCompanyIdAndUserId(3L, 5L))
                .thenReturn(Optional.of(CompanyUser.builder().user(user).company(restaurant.getCompany())
                        .role(UserRole.COMPANY_USER).isActive(true).build()));
        when(restaurantUserRepository.findActiveByRestaurantIdAndUserId(9L, 5L))
                .thenReturn(Optional.of(RestaurantUser.builder().restaurant(restaurant).user(user)
                        .permissions("VIEW_MENU,UPLOAD_MENU").isActive(true).build()));

        assertDoesNotThrow(() -> validator.requirePermission(5L, restaurant, RestaurantPermission.UPLOAD_MENU));
    }

    @Test
    void shouldRejectCompanyUserWithoutRestaurantPermission() {
        Restaurant restaurant = restaurant();
        when(companyAccessValidator.isPlatformAdmin(5L)).thenReturn(false);
        when(companyUserRepository.findActiveByCompanyIdAndUserId(3L, 5L))
                .thenReturn(Optional.of(CompanyUser.builder().role(UserRole.COMPANY_USER).isActive(true).build()));
        when(restaurantUserRepository.findActiveByRestaurantIdAndUserId(9L, 5L)).thenReturn(Optional.empty());

        assertThrows(ForbiddenException.class,
                () -> validator.requirePermission(5L, restaurant, RestaurantPermission.VIEW_MENU));
    }

    private Restaurant restaurant() {
        return Restaurant.builder().id(9L)
                .company(Company.builder().id(3L).name("Acme").slug("acme").build())
                .name("Cafe").slug("cafe").build();
    }
}
