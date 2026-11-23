package com.qrmenu.company.controller;

import com.qrmenu.common.enums.UserRole;
import com.qrmenu.common.response.ApiResponse;
import com.qrmenu.company.dto.CompanyResponse;
import com.qrmenu.company.dto.CreateCompanyRequest;
import com.qrmenu.company.dto.CreateCompanyUserRequest;
import com.qrmenu.auth.dto.UserResponse;
import com.qrmenu.company.service.CompanyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<CompanyResponse>> createCompany(
            @Valid @RequestBody CreateCompanyRequest request,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Company created successfully", companyService.createCompany(request, userId)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CompanyResponse>>> getCompaniesForUser(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Companies retrieved", companyService.getCompaniesForUser(userId)));
    }

    @GetMapping("/{companyId}")
    public ResponseEntity<ApiResponse<CompanyResponse>> getCompanyById(
            @PathVariable Long companyId,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Company retrieved", companyService.getCompanyById(companyId, userId)));
    }

    @PostMapping("/{companyId}/users/{userId}")
    public ResponseEntity<ApiResponse<Void>> addUserToCompany(
            @PathVariable Long companyId,
            @PathVariable Long userId,
            @RequestParam UserRole role,
            Authentication authentication) {
        Long currentUserId = Long.parseLong(authentication.getName());
        companyService.addUserToCompany(companyId, userId, role, currentUserId);
        return ResponseEntity.ok(ApiResponse.success("User added to company"));
    }

    @GetMapping("/{companyId}/users")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getCompanyUsers(
            @PathVariable Long companyId, Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success("Company users retrieved",
                companyService.getUsersByCompany(companyId, Long.parseLong(authentication.getName()))));
    }

    @PostMapping("/{companyId}/users")
    @PreAuthorize("hasRole('COMPANY_ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> createCompanyUser(
            @PathVariable Long companyId,
            @Valid @RequestBody CreateCompanyUserRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success("Company user created", companyService.createCompanyUser(
                companyId, request, Long.parseLong(authentication.getName()))));
    }
}
