package com.qrmenu.audit.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Audit log entity for tracking system events.
 *
 * Records:
 * - User actions (login, create, update, delete)
 * - Admin actions
 * - System events
 */
@Entity
@Table(name = "audit_logs", indexes = {
        @Index(name = "idx_audit_logs_user_id", columnList = "user_id"),
        @Index(name = "idx_audit_logs_action", columnList = "action"),
        @Index(name = "idx_audit_logs_created_at", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "user_email", length = 255)
    private String userEmail;

    @Column(nullable = false, length = 100)
    private String action;  // e.g., LOGIN, CREATE_COMPANY, APPROVE_PDF

    @Column(length = 500)
    private String description;

    @Column(name = "entity_type", length = 100)
    private String entityType;  // e.g., USER, COMPANY, RESTAURANT, PDF

    @Column(name = "entity_id")
    private Long entityId;

    @Column(columnDefinition = "TEXT")
    private String details;  // JSON payload

    @Column(length = 50)
    private String status;  // SUCCESS, FAILURE

    @Column(length = 500)
    private String errorMessage;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @JsonIgnore
    @Column(columnDefinition = "TEXT")
    private String rawRequest;  // For debugging (not stored in production)
}
