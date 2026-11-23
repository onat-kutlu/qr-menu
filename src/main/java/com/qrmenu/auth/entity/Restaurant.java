package com.qrmenu.auth.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "restaurants", indexes = {
        @Index(name = "idx_restaurants_company_id", columnList = "company_id"),
        @Index(name = "idx_restaurants_slug", columnList = "slug"),
        @Index(name = "idx_restaurants_status", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, length = 100)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 500)
    private String address;

    @Column(length = 20)
    private String phone;

    @Column(name = "logo_path", length = 500)
    private String logoPath;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Builder.Default
    private RestaurantStatus status = RestaurantStatus.PENDING_APPROVAL;

    @Column(name = "monthly_upload_quota", nullable = false)
    @Builder.Default
    private Integer monthlyUploadQuota = 20;

    @Column(name = "monthly_uploads_used", nullable = false)
    @Builder.Default
    private Integer monthlyUploadsUsed = 0;

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

    public enum RestaurantStatus {
        ACTIVE,
        INACTIVE,
        PENDING_APPROVAL
    }
}
