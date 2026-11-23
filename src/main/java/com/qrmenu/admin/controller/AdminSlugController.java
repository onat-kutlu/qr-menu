package com.qrmenu.admin.controller;

import com.qrmenu.admin.dto.PageResponse;
import com.qrmenu.admin.service.AdminManagementService;
import com.qrmenu.common.response.ApiResponse;
import com.qrmenu.restaurant.service.RestaurantService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/slug-change-requests")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
public class AdminSlugController {

    private final AdminManagementService adminManagementService;
    private final RestaurantService restaurantService;

    @GetMapping
    public ApiResponse<PageResponse<com.qrmenu.admin.dto.SlugChangeRequestResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success("Slug change requests retrieved", adminManagementService.listSlugChanges(page, size));
    }

    @PostMapping("/{requestId}/approve")
    public ApiResponse<com.qrmenu.restaurant.dto.SlugChangeRequestResponse> approve(
            @PathVariable Long requestId, Authentication authentication) {
        return ApiResponse.success("Slug change approved", restaurantService.approveSlugChange(
                requestId, Long.parseLong(authentication.getName())));
    }
}
