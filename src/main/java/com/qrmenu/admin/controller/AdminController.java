package com.qrmenu.admin.controller;

import com.qrmenu.admin.dto.DashboardSummaryResponse;
import com.qrmenu.admin.service.AdminService;
import com.qrmenu.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<DashboardSummaryResponse>> getDashboard(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        adminService.assertAdminAccess(userId);
        return ResponseEntity.ok(ApiResponse.success("Admin dashboard summary retrieved", adminService.getDashboardSummary()));
    }
}
