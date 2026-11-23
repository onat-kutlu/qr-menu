package com.qrmenu.menu.controller;

import com.qrmenu.common.response.ApiResponse;
import com.qrmenu.menu.dto.MenuVersionResponse;
import com.qrmenu.menu.dto.RejectMenuRequest;
import com.qrmenu.menu.dto.UploadMenuRequest;
import com.qrmenu.menu.service.MenuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/menus")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @PostMapping
    public ResponseEntity<ApiResponse<MenuVersionResponse>> uploadMenu(
            @Valid @RequestBody UploadMenuRequest request,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Menu uploaded successfully", menuService.uploadMenu(request, userId)));
    }

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<MenuVersionResponse>> uploadMenuFile(
            @RequestParam Long restaurantId,
            @RequestParam String language,
            @RequestPart MultipartFile file,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Menu uploaded successfully",
                menuService.uploadMenu(file, restaurantId, language, userId)));
    }

    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<ApiResponse<List<MenuVersionResponse>>> getMenusForRestaurant(
            @PathVariable Long restaurantId,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Menus retrieved", menuService.getMenusForRestaurant(restaurantId, userId)));
    }

    @PostMapping("/{menuVersionId}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<MenuVersionResponse>> approveMenu(@PathVariable Long menuVersionId, Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Menu approved", menuService.approveMenu(menuVersionId, userId)));
    }

    @PostMapping("/{menuVersionId}/publish")
    public ResponseEntity<ApiResponse<MenuVersionResponse>> publishMenu(@PathVariable Long menuVersionId, Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Menu published", menuService.publishMenu(menuVersionId, userId)));
    }

    @PostMapping("/{menuVersionId}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<MenuVersionResponse>> rejectMenu(
            @PathVariable Long menuVersionId,
            @Valid @RequestBody RejectMenuRequest request,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Menu rejected", menuService.rejectMenu(menuVersionId, request, userId)));
    }
}
