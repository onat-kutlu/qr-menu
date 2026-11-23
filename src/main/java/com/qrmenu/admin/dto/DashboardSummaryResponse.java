package com.qrmenu.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryResponse {
    private long totalCompanies;
    private long totalRestaurants;
    private long activeRestaurants;
    private long pendingRestaurants;
    private long pendingMenus;
    private long todayLandingViews;
    private long todayMenuOpens;
    private List<TopRestaurantStat> topRestaurants;
    private List<AuditLogSummary> auditLogs;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopRestaurantStat {
        private Long restaurantId;
        private String restaurantName;
        private long views;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuditLogSummary {
        private Long id;
        private String userEmail;
        private String action;
        private String status;
        private String description;
        private LocalDateTime createdAt;
    }
}
