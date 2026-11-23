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
@Table(name = "menu_versions", indexes = {
        @Index(name = "idx_menu_versions_restaurant_id", columnList = "restaurant_id"),
        @Index(name = "idx_menu_versions_language", columnList = "language"),
        @Index(name = "idx_menu_versions_status", columnList = "status"),
        @Index(name = "idx_menu_versions_is_live", columnList = "is_live")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false)
    private Language language;

    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    @Column(name = "file_hash", nullable = false, length = 64)
    private String fileHash;

    @Column(name = "page_count")
    private Integer pageCount;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false)
    @Builder.Default
    private PdfStatus status = PdfStatus.PENDING_APPROVAL;

    @Column(name = "version_number", nullable = false)
    private Integer versionNumber;

    @Column(name = "is_live", nullable = false)
    @Builder.Default
    private Boolean isLive = false;

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

    public enum Language {
        TR,
        EN,
        DE,
        FR
    }

    public enum PdfStatus {
        PENDING_APPROVAL,
        APPROVED,
        REJECTED,
        LIVE,
        ARCHIVED,
        DELETED
    }
}
