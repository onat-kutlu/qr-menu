package com.qrmenu.admin.service;

import com.qrmenu.admin.dto.DashboardSummaryResponse;
import com.qrmenu.analytics.entity.AnalyticsEvent;
import com.qrmenu.analytics.repository.AnalyticsEventRepository;
import com.qrmenu.audit.entity.AuditLog;
import com.qrmenu.audit.repository.AuditLogRepository;
import com.qrmenu.auth.entity.Company;
import com.qrmenu.auth.entity.MenuVersion;
import com.qrmenu.auth.entity.Restaurant;
import com.qrmenu.auth.repository.CompanyRepository;
import com.qrmenu.auth.repository.UserRepository;
import com.qrmenu.common.exception.ForbiddenException;
import com.qrmenu.menu.repository.MenuVersionRepository;
import com.qrmenu.restaurant.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminService {

    private final CompanyRepository companyRepository;
    private final RestaurantRepository restaurantRepository;
    private final MenuVersionRepository menuVersionRepository;
    private final AuditLogRepository auditLogRepository;
    private final AnalyticsEventRepository analyticsEventRepository;
        private final UserRepository userRepository;

    public DashboardSummaryResponse getDashboardSummary() {
        List<Company> companies = companyRepository.findAll();
        List<Restaurant> restaurants = restaurantRepository.findAll();
        List<MenuVersion> menuVersions = menuVersionRepository.findAll();
        List<AuditLog> auditLogs = auditLogRepository.findAll().stream()
                .sorted(Comparator.comparing(AuditLog::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
        List<AnalyticsEvent> analyticsEvents = analyticsEventRepository.findAll();

        long totalCompanies = companies.size();
        long totalRestaurants = restaurants.size();
        long activeRestaurants = restaurants.stream()
                .filter(r -> r.getStatus() == Restaurant.RestaurantStatus.ACTIVE)
                .count();
        long pendingRestaurants = restaurants.stream()
                .filter(r -> r.getStatus() == Restaurant.RestaurantStatus.PENDING_APPROVAL)
                .count();
        long pendingMenus = menuVersions.stream()
                .filter(m -> m.getStatus() == MenuVersion.PdfStatus.PENDING_APPROVAL)
                .count();

        LocalDate today = LocalDate.now();
        long todayLandingViews = analyticsEvents.stream()
                .filter(e -> "LANDING_VIEW".equals(e.getEventType()))
                .filter(e -> e.getCreatedAt() != null && e.getCreatedAt().toLocalDate().equals(today))
                .count();
        long todayMenuOpens = analyticsEvents.stream()
                .filter(e -> "MENU_OPEN".equals(e.getEventType()))
                .filter(e -> e.getCreatedAt() != null && e.getCreatedAt().toLocalDate().equals(today))
                .count();

        Map<Long, Long> restaurantEventCounts = analyticsEvents.stream()
                .filter(e -> e.getRestaurantId() != null)
                .collect(Collectors.groupingBy(AnalyticsEvent::getRestaurantId, Collectors.counting()));

        List<DashboardSummaryResponse.TopRestaurantStat> topRestaurants = restaurantEventCounts.entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue(Comparator.reverseOrder()))
                .limit(5)
                .map(entry -> {
                    Restaurant restaurant = restaurantRepository.findById(entry.getKey()).orElse(null);
                    return DashboardSummaryResponse.TopRestaurantStat.builder()
                            .restaurantId(entry.getKey())
                            .restaurantName(restaurant != null ? restaurant.getName() : "Unknown")
                            .views(entry.getValue())
                            .build();
                })
                .toList();

        List<DashboardSummaryResponse.AuditLogSummary> auditLogSummaries = auditLogs.stream()
                .limit(10)
                .map(log -> DashboardSummaryResponse.AuditLogSummary.builder()
                        .id(Long.valueOf(log.getId()))
                        .userEmail(log.getUserEmail())
                        .action(log.getAction())
                        .status(log.getStatus())
                        .description(log.getDescription())
                        .createdAt(log.getCreatedAt())
                        .build())
                .toList();

        return DashboardSummaryResponse.builder()
                .totalCompanies(totalCompanies)
                .totalRestaurants(totalRestaurants)
                .activeRestaurants(activeRestaurants)
                .pendingRestaurants(pendingRestaurants)
                .pendingMenus(pendingMenus)
                .todayLandingViews(todayLandingViews)
                .todayMenuOpens(todayMenuOpens)
                .topRestaurants(topRestaurants)
                .auditLogs(auditLogSummaries)
                .build();
    }

    public void assertAdminAccess(Long currentUserId) {
        boolean isAdmin = userRepository.findActiveById(currentUserId)
                .map(user -> user.getHighestRole().isAdmin())
                .orElse(false);

        if (!isAdmin) {
                        throw new ForbiddenException("Only platform admins can access the admin panel");
        }
    }
}
