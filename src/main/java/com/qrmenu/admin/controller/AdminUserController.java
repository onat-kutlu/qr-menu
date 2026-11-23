package com.qrmenu.admin.controller;

import com.qrmenu.admin.dto.PageResponse;
import com.qrmenu.admin.dto.CreateAdminUserRequest;
import com.qrmenu.admin.dto.UpdateUserRoleRequest;
import com.qrmenu.admin.service.AdminManagementService;
import com.qrmenu.auth.dto.UserResponse;
import com.qrmenu.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
public class AdminUserController {

    private final AdminManagementService adminManagementService;

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ApiResponse<UserResponse> create(@jakarta.validation.Valid @RequestBody CreateAdminUserRequest request,
                                             org.springframework.security.core.Authentication authentication) {
        return ApiResponse.success("User created", adminManagementService.createUser(request,
                Long.parseLong(authentication.getName())));
    }

    @GetMapping
    public ApiResponse<PageResponse<UserResponse>> list(
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sort) {
        return ApiResponse.success("Users retrieved", adminManagementService.listUsers(active, search, page, size, sort));
    }

    @GetMapping("/{userId}")
    public ApiResponse<UserResponse> get(@PathVariable Long userId) {
        return ApiResponse.success("User retrieved", adminManagementService.getUser(userId));
    }

    @PatchMapping("/{userId}/deactivate")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ApiResponse<UserResponse> deactivate(@PathVariable Long userId,
                                                 org.springframework.security.core.Authentication authentication) {
        return ApiResponse.success("User deactivated", adminManagementService.deactivateUser(
                userId, Long.parseLong(authentication.getName())));
    }

    @PatchMapping("/{userId}/role")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ApiResponse<UserResponse> updateRole(@PathVariable Long userId,
                                                 @jakarta.validation.Valid @RequestBody UpdateUserRoleRequest request,
                                                 org.springframework.security.core.Authentication authentication) {
        return ApiResponse.success("User role updated", adminManagementService.updateUserRole(
                userId, request, Long.parseLong(authentication.getName())));
    }
}
