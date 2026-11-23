package com.qrmenu.restaurant;

import com.qrmenu.audit.service.AuditService;
import com.qrmenu.auth.entity.Company;
import com.qrmenu.auth.entity.CompanyUser;
import com.qrmenu.auth.entity.User;
import com.qrmenu.auth.repository.CompanyRepository;
import com.qrmenu.auth.repository.CompanyUserRepository;
import com.qrmenu.auth.repository.RestaurantApprovalRequestRepository;
import com.qrmenu.auth.repository.SlugHistoryRepository;
import com.qrmenu.auth.repository.UserRepository;
import com.qrmenu.common.enums.UserRole;
import com.qrmenu.common.exception.BadRequestException;
import com.qrmenu.common.exception.ForbiddenException;
import com.qrmenu.common.exception.ResourceNotFoundException;
import com.qrmenu.restaurant.dto.CreateRestaurantRequest;
import com.qrmenu.restaurant.dto.RestaurantResponse;
import com.qrmenu.restaurant.repository.RestaurantRepository;
import com.qrmenu.restaurant.service.RestaurantService;
import com.qrmenu.storage.service.StoragePathValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RestaurantServiceTest {

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private CompanyUserRepository companyUserRepository;

        @Mock
        private RestaurantApprovalRequestRepository restaurantApprovalRequestRepository;

        @Mock
        private SlugHistoryRepository slugHistoryRepository;

        @Mock
        private StoragePathValidator storagePathValidator;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private RestaurantService restaurantService;

    private User companyAdmin;
    private Company company;

    @BeforeEach
    void setUp() {
                lenient().when(storagePathValidator.resolve(any(String.class)))
                        .thenAnswer(invocation -> Path.of((String) invocation.getArgument(0)));
        companyAdmin = User.builder()
                .id(1L)
                .email("admin@example.com")
                .passwordHash("hash")
                .isActive(true)
                .emailVerified(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        companyAdmin.setCompanies(new java.util.HashSet<>());

        company = Company.builder()
                .id(10L)
                .name("Test Company")
                .slug("test-company")
                .status(Company.CompanyStatus.ACTIVE)
                .createdBy(companyAdmin)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void shouldCreateRestaurantWhenCompanyAdminIsAuthorized() {
        CreateRestaurantRequest request = CreateRestaurantRequest.builder()
                .companyId(10L)
                .name("Test Restaurant")
                .slug("test-restaurant")
                .address("Test Address")
                .phone("123456")
                .description("Test Description")
                .build();

        when(companyRepository.findById(10L)).thenReturn(Optional.of(company));
        when(companyUserRepository.findActiveByCompanyIdAndUserId(10L, 1L)).thenReturn(Optional.of(CompanyUser.builder()
                .company(company)
                .user(companyAdmin)
                .role(UserRole.COMPANY_ADMIN)
                .isActive(true)
                .build()));
        when(userRepository.findActiveById(1L)).thenReturn(Optional.of(companyAdmin));
        when(restaurantRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        RestaurantResponse response = restaurantService.createRestaurant(request, 1L);

        assertNotNull(response);
        assertEquals("Test Restaurant", response.getName());
        assertEquals("test-restaurant", response.getSlug());
        assertEquals("PENDING_APPROVAL", response.getStatus());
    }

    @Test
    void shouldRejectCreationWhenSlugAlreadyExists() {
        CreateRestaurantRequest request = CreateRestaurantRequest.builder()
                .companyId(10L)
                .name("Test Restaurant")
                .slug("existing-slug")
                .build();

        when(companyRepository.findById(10L)).thenReturn(Optional.of(company));
        when(companyUserRepository.findActiveByCompanyIdAndUserId(10L, 1L)).thenReturn(Optional.of(CompanyUser.builder()
                .company(company)
                .user(companyAdmin)
                .role(UserRole.COMPANY_ADMIN)
                .isActive(true)
                .build()));
        when(restaurantService.isSlugTaken("existing-slug")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> restaurantService.createRestaurant(request, 1L));
    }

    @Test
    void shouldApproveRestaurantAndActivateIt() {
        companyAdmin.setCompanies(new java.util.HashSet<>(java.util.List.of(CompanyUser.builder()
                .company(company)
                .user(companyAdmin)
                .role(UserRole.ADMIN)
                .isActive(true)
                .build())));

        when(userRepository.findActiveById(1L)).thenReturn(Optional.of(companyAdmin));
        when(restaurantRepository.findById(100L)).thenReturn(Optional.of(com.qrmenu.auth.entity.Restaurant.builder()
                .id(100L)
                .company(company)
                .name("Pending Restaurant")
                .slug("pending-restaurant")
                .status(com.qrmenu.auth.entity.Restaurant.RestaurantStatus.PENDING_APPROVAL)
                .monthlyUploadQuota(20)
                .monthlyUploadsUsed(0)
                .createdBy(companyAdmin)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build()));
        when(restaurantRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        RestaurantResponse response = restaurantService.approveRestaurant(100L, 1L);

        assertNotNull(response);
        assertEquals("ACTIVE", response.getStatus());
    }

    @Test
    void shouldUploadLogoWhenCompanyAdminIsAuthorized() throws Exception {
        Path tempFile = Files.createTempFile("logo", ".png");
        Files.writeString(tempFile, "fake-png", StandardCharsets.UTF_8);

        com.qrmenu.auth.entity.Restaurant restaurant = com.qrmenu.auth.entity.Restaurant.builder()
                .id(100L)
                .company(company)
                .name("Pending Restaurant")
                .slug("pending-restaurant")
                .status(com.qrmenu.auth.entity.Restaurant.RestaurantStatus.ACTIVE)
                .monthlyUploadQuota(20)
                .monthlyUploadsUsed(0)
                .createdBy(companyAdmin)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(restaurantRepository.findById(100L)).thenReturn(Optional.of(restaurant));
        when(companyUserRepository.findActiveByCompanyIdAndUserId(10L, 1L)).thenReturn(Optional.of(CompanyUser.builder()
                .company(company)
                .user(companyAdmin)
                .role(UserRole.COMPANY_ADMIN)
                .isActive(true)
                .build()));
        when(restaurantRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        RestaurantResponse response = restaurantService.uploadLogo(100L, tempFile.toString(), 1L);

        assertNotNull(response);
        assertEquals(tempFile.toString(), response.getLogoPath());
    }

    @Test
    void shouldRejectLogoUploadWhenUserIsNotAdmin() {
        com.qrmenu.auth.entity.Restaurant restaurant = com.qrmenu.auth.entity.Restaurant.builder()
                .id(100L)
                .company(company)
                .name("Pending Restaurant")
                .slug("pending-restaurant")
                .status(com.qrmenu.auth.entity.Restaurant.RestaurantStatus.ACTIVE)
                .monthlyUploadQuota(20)
                .monthlyUploadsUsed(0)
                .createdBy(companyAdmin)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(restaurantRepository.findById(100L)).thenReturn(Optional.of(restaurant));
        when(companyUserRepository.findActiveByCompanyIdAndUserId(10L, 1L)).thenReturn(Optional.of(CompanyUser.builder()
                .company(company)
                .user(companyAdmin)
                .role(UserRole.COMPANY_USER)
                .isActive(true)
                .build()));

        assertThrows(ForbiddenException.class, () -> restaurantService.uploadLogo(100L, "/tmp/logo.png", 1L));
    }

    @Test
    void shouldThrowWhenRestaurantNotFound() {
        assertThrows(ResourceNotFoundException.class, () -> restaurantService.getRestaurantById(999L));
    }
}
