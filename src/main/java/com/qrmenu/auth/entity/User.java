package com.qrmenu.auth.entity;

import com.qrmenu.common.enums.UserRole;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * User entity representing a user account in the QR Menu system.
 *
 * A user can be associated with multiple companies through the company_users join table.
 * Each association has a specific role (SUPER_ADMIN, ADMIN, COMPANY_ADMIN, COMPANY_USER).
 */
@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_users_email", columnList = "email"),
        @Index(name = "idx_users_is_active", columnList = "is_active")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "first_name", length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    @Column(length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "global_role", length = 30)
    private UserRole globalRole;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "email_verified", nullable = false)
    @Builder.Default
    private Boolean emailVerified = false;

    @Column(name = "email_verified_at")
    private LocalDateTime emailVerifiedAt;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    /**
     * Companies this user is associated with (via company_users join table).
     */
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private Set<CompanyUser> companies = new HashSet<>();

    /**
     * Companies created by this user (superadmin feature).
     */
    @OneToMany(mappedBy = "createdBy", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Company> createdCompanies = new HashSet<>();

    public String getFullName() {
        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        }
        return firstName != null ? firstName : lastName;
    }

    /**
     * Get the highest role for this user across all companies.
     * Used for quick permission checks (admin roles override company roles).
     */
    public UserRole getHighestRole() {
        if (globalRole != null && globalRole.isAdmin()) {
            return globalRole;
        }
        return companies.stream()
                .map(CompanyUser::getRole)
                .min((r1, r2) -> {
                    if (r1.isAdmin()) return -1;
                    if (r2.isAdmin()) return 1;
                    return 0;
                })
                .orElse(UserRole.COMPANY_USER);
    }

    /**
     * Soft delete the user.
     */
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
        this.isActive = false;
    }

    /**
     * Check if user is soft deleted.
     */
    public boolean isDeleted() {
        return deletedAt != null;
    }
}
