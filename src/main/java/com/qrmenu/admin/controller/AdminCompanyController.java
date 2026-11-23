package com.qrmenu.admin.controller;

import com.qrmenu.admin.dto.PageResponse;
import com.qrmenu.admin.dto.UpdateCompanyStatusRequest;
import com.qrmenu.admin.service.AdminManagementService;
import com.qrmenu.common.response.ApiResponse;
import com.qrmenu.company.dto.CompanyResponse;
import com.qrmenu.company.dto.CreateCompanyRequest;
import com.qrmenu.company.service.CompanyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/companies")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
public class AdminCompanyController {

    private final AdminManagementService adminManagementService;
    private final CompanyService companyService;

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ApiResponse<CompanyResponse> create(@Valid @RequestBody CreateCompanyRequest request,
                                                Authentication authentication) {
        return ApiResponse.success("Company created", companyService.createCompany(request,
                Long.parseLong(authentication.getName())));
    }

    @GetMapping
    public ApiResponse<PageResponse<CompanyResponse>> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sort) {
        return ApiResponse.success("Companies retrieved", adminManagementService.listCompanies(status, search, page, size, sort));
    }

    @GetMapping("/{companyId}")
    public ApiResponse<CompanyResponse> get(@PathVariable Long companyId) {
        return ApiResponse.success("Company retrieved", adminManagementService.getCompany(companyId));
    }

    @PatchMapping("/{companyId}/status")
    public ApiResponse<CompanyResponse> updateStatus(@PathVariable Long companyId,
                                                       @Valid @RequestBody UpdateCompanyStatusRequest request,
                                                       Authentication authentication) {
        return ApiResponse.success("Company status updated", adminManagementService.updateCompanyStatus(
                companyId, request, Long.parseLong(authentication.getName())));
    }

    @DeleteMapping("/{companyId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ApiResponse<CompanyResponse> deactivate(@PathVariable Long companyId, Authentication authentication) {
        return ApiResponse.success("Company deactivated", adminManagementService.deactivateCompany(
                companyId, Long.parseLong(authentication.getName())));
    }
}
