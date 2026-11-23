package com.qrmenu.admin;

import com.qrmenu.admin.dto.DashboardSummaryResponse;
import com.qrmenu.admin.service.AdminService;
import com.qrmenu.analytics.entity.AnalyticsEvent;
import com.qrmenu.analytics.repository.AnalyticsEventRepository;
import com.qrmenu.audit.entity.AuditLog;
import com.qrmenu.audit.repository.AuditLogRepository;
import com.qrmenu.auth.entity.Company;
import com.qrmenu.auth.entity.MenuVersion;
import com.qrmenu.auth.entity.Restaurant;
import com.qrmenu.auth.entity.User;
import com.qrmenu.auth.repository.CompanyRepository;
import com.qrmenu.auth.repository.UserRepository;
import com.qrmenu.common.enums.UserRole;
import com.qrmenu.menu.repository.MenuVersionRepository;
import com.qrmenu.restaurant.repository.RestaurantRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MenuVersionRepository menuVersionRepository;

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private AnalyticsEventRepository analyticsEventRepository;

    @InjectMocks
    private AdminService adminService;

    @Test
    void shouldBuildDashboardSummaryWithCountsAndAuditLogs() {
        Company company = Company.builder()
                .id(1L)
                .name("Acme")
                .slug("acme")
                .status(Company.CompanyStatus.ACTIVE)
                .createdBy(User.builder().id(10L).email("owner@example.com").build())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Restaurant restaurant = Restaurant.builder()
                .id(5L)
                .company(company)
                .name("Test Restaurant")
                .slug("test-restaurant")
                .status(Restaurant.RestaurantStatus.ACTIVE)
                .createdBy(User.builder().id(10L).email("owner@example.com").build())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Restaurant pendingRestaurant = Restaurant.builder()
                .id(6L)
                .company(company)
                .name("Pending Restaurant")
                .slug("pending-restaurant")
                .status(Restaurant.RestaurantStatus.PENDING_APPROVAL)
                .createdBy(User.builder().id(10L).email("owner@example.com").build())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        MenuVersion pendingMenu = MenuVersion.builder()
                .id(12L)
                .restaurant(restaurant)
                .language(MenuVersion.Language.TR)
                .filePath("/tmp/menu.pdf")
                .fileHash("hash")
                .status(MenuVersion.PdfStatus.PENDING_APPROVAL)
                .versionNumber(1)
                .createdBy(User.builder().id(10L).email("owner@example.com").build())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(companyRepository.findAll()).thenReturn(List.of(company));
        when(restaurantRepository.findAll()).thenReturn(List.of(restaurant, pendingRestaurant));
        when(menuVersionRepository.findAll()).thenReturn(List.of(pendingMenu));
        when(auditLogRepository.findAll()).thenReturn(List.of(
                AuditLog.builder().id(1).userEmail("owner@example.com").action("LOGIN").status("SUCCESS").createdAt(LocalDateTime.now()).build()
        ));
        when(analyticsEventRepository.findAll()).thenReturn(List.of(
                AnalyticsEvent.builder().id(1L).eventType("LANDING_VIEW").restaurantId(5L).companyId(1L).createdAt(LocalDateTime.now()).build(),
                AnalyticsEvent.builder().id(2L).eventType("MENU_OPEN").restaurantId(5L).companyId(1L).createdAt(LocalDateTime.now()).build(),
                AnalyticsEvent.builder().id(3L).eventType("MENU_OPEN").restaurantId(5L).companyId(1L).createdAt(LocalDateTime.now()).build()
        ));

        DashboardSummaryResponse summary = adminService.getDashboardSummary();

        assertEquals(1, summary.getTotalCompanies());
        assertEquals(2, summary.getTotalRestaurants());
        assertEquals(1, summary.getActiveRestaurants());
        assertEquals(1, summary.getPendingRestaurants());
        assertEquals(1, summary.getPendingMenus());
        assertEquals(1, summary.getTodayLandingViews());
        assertEquals(2, summary.getTodayMenuOpens());
        assertEquals(1, summary.getAuditLogs().size());
    }
}
