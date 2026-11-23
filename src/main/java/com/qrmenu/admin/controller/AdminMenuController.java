package com.qrmenu.admin.controller;

import com.qrmenu.admin.dto.PageResponse;
import com.qrmenu.admin.service.AdminManagementService;
import com.qrmenu.common.response.ApiResponse;
import com.qrmenu.menu.dto.MenuVersionResponse;
import com.qrmenu.menu.dto.RejectMenuRequest;
import com.qrmenu.menu.service.MenuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/pdf-approvals")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
public class AdminMenuController {

    private final AdminManagementService adminManagementService;
    private final MenuService menuService;

    @GetMapping
    public ApiResponse<PageResponse<MenuVersionResponse>> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long restaurantId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sort) {
        return ApiResponse.success("PDF approvals retrieved", adminManagementService.listMenus(status, restaurantId, page, size, sort));
    }

    @PostMapping("/{menuVersionId}/approve")
    public ApiResponse<MenuVersionResponse> approve(@PathVariable Long menuVersionId, Authentication authentication) {
        return ApiResponse.success("Menu approved", menuService.approveMenu(menuVersionId, Long.parseLong(authentication.getName())));
    }

    @PostMapping("/{menuVersionId}/reject")
    public ApiResponse<MenuVersionResponse> reject(@PathVariable Long menuVersionId,
                                                    @Valid @RequestBody RejectMenuRequest request,
                                                    Authentication authentication) {
        return ApiResponse.success("Menu rejected", menuService.rejectMenu(menuVersionId, request, Long.parseLong(authentication.getName())));
    }
}
