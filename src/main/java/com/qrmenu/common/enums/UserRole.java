package com.qrmenu.common.enums;

/**
 * User roles in the QR Menu system.
 *
 * - SUPER_ADMIN: Platform administrator with full system access.
 * - ADMIN: Platform admin for approving restaurants and PDFs.
 * - COMPANY_ADMIN: Admin of a specific company/firm.
 * - COMPANY_USER: Regular user of a company with limited permissions.
 */
public enum UserRole {
    SUPER_ADMIN("Super Administrator", "Platform-wide administrative access"),
    ADMIN("Administrator", "Restaurant and PDF approval authority"),
    COMPANY_ADMIN("Company Administrator", "Company account administrator"),
    COMPANY_USER("Company User", "Regular company user with limited permissions");

    private final String displayName;
    private final String description;

    UserRole(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Check if this role is an admin role (SUPER_ADMIN or ADMIN).
     */
    public boolean isAdmin() {
        return this == SUPER_ADMIN || this == ADMIN;
    }

    /**
     * Check if this role is a company admin role.
     */
    public boolean isCompanyAdmin() {
        return this == COMPANY_ADMIN;
    }
}
