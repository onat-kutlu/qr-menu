package com.qrmenu.admin.controller;

import com.qrmenu.admin.dto.PageResponse;
import com.qrmenu.admin.service.AdminManagementService;
import com.qrmenu.audit.entity.AuditLog;
import com.qrmenu.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/audit-logs")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
public class AdminAuditController {

    private final AdminManagementService adminManagementService;

    @GetMapping
    public ApiResponse<PageResponse<AuditLog>> list(
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sort) {
        return ApiResponse.success("Audit logs retrieved", adminManagementService.listAuditLogs(action, status, userId, page, size, sort));
    }
}
