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

/**
 * CompanyUser entity representing the relationship between users and companies.
 *
 * Each company can have multiple users with different roles.
 * This is a join table that tracks:
 * - Which user belongs to which company
 * - What role the user has within that company
 * - Whether the user is active in that company
 */
@Entity
@Table(name = "company_users", 
        indexes = {
                @Index(name = "idx_company_users_company_id", columnList = "company_id"),
                @Index(name = "idx_company_users_user_id", columnList = "user_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_company_user", columnNames = {"company_id", "user_id"})
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private UserRole role;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    /**
     * Check if this association is active.
     */
    public boolean isActive() {
        return isActive && user.getIsActive() && company.isActive();
    }

    /**
     * Check if user has admin role in this company.
     */
    public boolean isAdmin() {
        return role.isAdmin() || role.isCompanyAdmin();
    }

    /**
     * Deactivate this company-user association (soft delete).
     */
    public void deactivate() {
        this.isActive = false;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Reactivate this company-user association.
     */
    public void reactivate() {
        this.isActive = true;
        this.updatedAt = LocalDateTime.now();
    }
}
