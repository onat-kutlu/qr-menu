package com.qrmenu.admin.controller;

import com.qrmenu.admin.dto.UpdateRestaurantRequest;
import com.qrmenu.admin.dto.PageResponse;
import com.qrmenu.admin.dto.RejectRestaurantRequest;
import com.qrmenu.admin.service.AdminManagementService;
import com.qrmenu.common.response.ApiResponse;
import com.qrmenu.restaurant.dto.RestaurantResponse;
import com.qrmenu.restaurant.service.RestaurantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/restaurants")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
public class AdminRestaurantController {

    private final AdminManagementService adminManagementService;
    private final RestaurantService restaurantService;

    @GetMapping("/approvals")
    public ApiResponse<PageResponse<RestaurantResponse>> approvals(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success("Restaurant approvals retrieved", adminManagementService.listRestaurantApprovals(page, size));
    }

    @GetMapping
    public ApiResponse<PageResponse<RestaurantResponse>> list(
            @RequestParam(required = false) Long companyId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sort) {
        return ApiResponse.success("Restaurants retrieved", adminManagementService.listRestaurants(companyId, status, search, page, size, sort));
    }

    @GetMapping("/{restaurantId}")
    public ApiResponse<RestaurantResponse> get(@PathVariable Long restaurantId) {
        return ApiResponse.success("Restaurant retrieved", adminManagementService.getRestaurant(restaurantId));
    }

    @PatchMapping("/{restaurantId}")
    public ApiResponse<RestaurantResponse> update(@PathVariable Long restaurantId,
                                                   @Valid @RequestBody UpdateRestaurantRequest request,
                                                   Authentication authentication) {
        return ApiResponse.success("Restaurant updated", adminManagementService.updateRestaurant(
                restaurantId, request, Long.parseLong(authentication.getName())));
    }

    @PostMapping("/approvals/{requestId}/approve")
    public ApiResponse<RestaurantResponse> approve(@PathVariable Long requestId, Authentication authentication) {
        return ApiResponse.success("Restaurant approved", restaurantService.approveRestaurantByRequest(
                requestId, Long.parseLong(authentication.getName())));
    }

    @PostMapping("/approvals/{requestId}/reject")
    public ApiResponse<RestaurantResponse> reject(@PathVariable Long requestId,
                                                   @Valid @RequestBody RejectRestaurantRequest request,
                                                   Authentication authentication) {
        return ApiResponse.success("Restaurant rejected", restaurantService.rejectRestaurantApproval(
                requestId, request.getReason(), Long.parseLong(authentication.getName())));
    }
}
