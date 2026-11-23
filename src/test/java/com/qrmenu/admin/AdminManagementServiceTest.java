package com.qrmenu.admin;

import com.qrmenu.admin.dto.PageResponse;
import com.qrmenu.admin.dto.UpdateCompanyStatusRequest;
import com.qrmenu.admin.dto.UpdateRestaurantRequest;
import com.qrmenu.admin.service.AdminManagementService;
import com.qrmenu.audit.repository.AuditLogRepository;
import com.qrmenu.audit.service.AuditService;
import com.qrmenu.auth.entity.Company;
import com.qrmenu.auth.entity.CompanyUser;
import com.qrmenu.auth.entity.Restaurant;
import com.qrmenu.auth.entity.User;
import com.qrmenu.auth.repository.CompanyRepository;
import com.qrmenu.auth.repository.CompanyUserRepository;
import com.qrmenu.auth.repository.UserRepository;
import com.qrmenu.common.enums.UserRole;
import com.qrmenu.menu.repository.MenuVersionRepository;
import com.qrmenu.restaurant.repository.RestaurantRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminManagementServiceTest {

    @Mock
    private CompanyRepository companyRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CompanyUserRepository companyUserRepository;
    @Mock
    private RestaurantRepository restaurantRepository;
    @Mock
    private MenuVersionRepository menuVersionRepository;
    @Mock
    private AuditLogRepository auditLogRepository;
    @Mock
    private AuditService auditService;

    @InjectMocks
    private AdminManagementService service;

    @Test
    void shouldReturnPagedCompaniesWithSafePageSize() {
        Company company = Company.builder().id(1L).name("Acme").slug("acme")
                .status(Company.CompanyStatus.ACTIVE).createdAt(LocalDateTime.now()).build();
        when(companyRepository.searchByStatusAndTerm(any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(company), PageRequest.of(0, 100), 1));

        PageResponse<?> response = service.listCompanies("ACTIVE", "acme", 0, 500, "unsafe");

        assertEquals(1, response.getTotalElements());
        assertEquals(100, response.getSize());
        assertEquals("Acme", ((com.qrmenu.company.dto.CompanyResponse) response.getItems().get(0)).getName());
    }

    @Test
    void shouldUpdateRestaurantQuotaAndAuditChange() {
        User admin = User.builder().id(7L).email("admin@example.com").isActive(true).build();
        Company company = Company.builder().id(3L).name("Acme").slug("acme").build();
        Restaurant restaurant = Restaurant.builder().id(10L).company(company).name("Cafe")
                .slug("cafe").status(Restaurant.RestaurantStatus.ACTIVE).monthlyUploadQuota(20)
                .createdBy(admin).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();
        UpdateRestaurantRequest request = new UpdateRestaurantRequest();
        request.setMonthlyUploadQuota(50);

        when(restaurantRepository.findById(10L)).thenReturn(Optional.of(restaurant));
        when(restaurantRepository.save(any(Restaurant.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.findById(7L)).thenReturn(Optional.of(admin));

        service.updateRestaurant(10L, request, 7L);

        assertEquals(50, restaurant.getMonthlyUploadQuota());
        verify(auditService).log(7L, "admin@example.com", "UPDATE_RESTAURANT",
                "Updated restaurant Cafe", "Restaurant", 10L, null, null);
    }
}
