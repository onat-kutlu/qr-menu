package com.qrmenu.restaurant.controller;

import com.qrmenu.common.response.ApiResponse;
import com.qrmenu.restaurant.dto.CreateRestaurantRequest;
import com.qrmenu.restaurant.dto.QrCodeResponse;
import com.qrmenu.restaurant.dto.RequestSlugChangeRequest;
import com.qrmenu.restaurant.dto.RestaurantResponse;
import com.qrmenu.restaurant.dto.SlugChangeRequestResponse;
import com.qrmenu.restaurant.service.RestaurantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantService restaurantService;

    @PostMapping
    public ResponseEntity<ApiResponse<RestaurantResponse>> createRestaurant(
            @Valid @RequestBody CreateRestaurantRequest request,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        RestaurantResponse response = restaurantService.createRestaurant(request, userId);
        return ResponseEntity.ok(ApiResponse.success("Restaurant created successfully", response));
    }

    @GetMapping("/company/{companyId}")
    public ResponseEntity<ApiResponse<List<RestaurantResponse>>> getRestaurantsByCompany(
            @PathVariable Long companyId,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Restaurants retrieved", restaurantService.getRestaurantsByCompany(companyId, userId)));
    }

    @GetMapping("/{restaurantId}")
    public ResponseEntity<ApiResponse<RestaurantResponse>> getRestaurantById(
            @PathVariable Long restaurantId,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Restaurant retrieved", restaurantService.getRestaurantById(restaurantId, userId)));
    }

    @PostMapping("/{restaurantId}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<RestaurantResponse>> approveRestaurant(@PathVariable Long restaurantId, Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Restaurant approved", restaurantService.approveRestaurant(restaurantId, userId)));
    }

    @PostMapping("/{restaurantId}/slug-change")
    public ResponseEntity<ApiResponse<SlugChangeRequestResponse>> requestSlugChange(
            @PathVariable Long restaurantId,
            @Valid @RequestBody RequestSlugChangeRequest request,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Slug change requested", restaurantService.requestSlugChange(restaurantId, request, userId)));
    }

    @PostMapping("/{restaurantId}/logo")
    public ResponseEntity<ApiResponse<RestaurantResponse>> uploadLogo(
            @PathVariable Long restaurantId,
            @RequestParam String logoPath,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Logo uploaded", restaurantService.uploadLogo(restaurantId, logoPath, userId)));
    }

    @PostMapping(value = "/{restaurantId}/logo/upload", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<RestaurantResponse>> uploadLogoFile(
            @PathVariable Long restaurantId,
            @RequestPart MultipartFile logo,
            Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success("Logo uploaded", restaurantService.uploadLogo(
                restaurantId, logo, Long.parseLong(authentication.getName()))));
    }

    @GetMapping("/{restaurantId}/qr")
    public ResponseEntity<ApiResponse<QrCodeResponse>> getQrCode(
            @PathVariable Long restaurantId,
            @RequestParam(defaultValue = "png") String format,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("QR code generated", restaurantService.generateQrCode(restaurantId, format, userId)));
    }
}
