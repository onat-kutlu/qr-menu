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
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

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

    @GetMapping("/{menuVersionId}/preview")
    public ResponseEntity<Resource> preview(@PathVariable Long menuVersionId, Authentication authentication) throws IOException {
        Path previewPath = menuService.getPreviewPath(menuVersionId, Long.parseLong(authentication.getName()));
        InputStreamResource resource = new InputStreamResource(Files.newInputStream(previewPath));
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(Files.size(previewPath))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"menu-" + menuVersionId + ".pdf\"")
                .body(resource);
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
