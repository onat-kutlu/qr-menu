package com.qrmenu.auth.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "pdf_rejection_reasons", indexes = {
        @Index(name = "idx_pdf_rejection_reasons_menu_version_id", columnList = "menu_version_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PdfRejectionReason {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "menu_version_id", nullable = false)
    private MenuVersion menuVersion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 100)
    private Category category;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "rejected_by", nullable = false)
    private User rejectedBy;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum Category {
        UNREADABLE_FILE,
        WRONG_RESTAURANT,
        INAPPROPRIATE_CONTENT,
        FILE_CORRUPTED,
        POLICY_VIOLATION,
        OTHER
    }
}
