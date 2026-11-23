package com.qrmenu.auth.entity;

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
 * Company entity representing a firm/organization in the QR Menu system.
 *
 * A company can have multiple users with different roles through the company_users join table.
 * Each company has restaurants under it, and users manage restaurants within their company.
 */
@Entity
@Table(name = "companies", indexes = {
        @Index(name = "idx_companies_slug", columnList = "slug"),
        @Index(name = "idx_companies_status", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, unique = true, length = 100)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Builder.Default
    private CompanyStatus status = CompanyStatus.ACTIVE;

    @Column(name = "subscription_tier", length = 50)
    @Builder.Default
    private String subscriptionTier = "free";

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    /**
     * Users associated with this company (via company_users join table).
     */
    @OneToMany(mappedBy = "company", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private Set<CompanyUser> users = new HashSet<>();

    /**
     * Soft delete the company.
     */
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
        this.status = CompanyStatus.INACTIVE;
    }

    /**
     * Check if company is soft deleted.
     */
    public boolean isDeleted() {
        return deletedAt != null;
    }

    /**
     * Check if company is active.
     */
    public boolean isActive() {
        return status == CompanyStatus.ACTIVE && !isDeleted();
    }

    /**
     * Company status enum.
     */
    public enum CompanyStatus {
        ACTIVE("Active"),
        INACTIVE("Inactive"),
        SUSPENDED("Suspended");

        private final String displayName;

        CompanyStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}
