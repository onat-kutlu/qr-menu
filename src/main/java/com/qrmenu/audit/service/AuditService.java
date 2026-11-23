package com.qrmenu.audit.service;

import com.qrmenu.audit.entity.AuditLog;
import com.qrmenu.audit.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Audit service for recording system events.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    /**
     * Log an audit event.
     */
    public void log(Long userId, String userEmail, String action, String description) {
        log(userId, userEmail, action, description, null, null, null, null);
    }

    /**
     * Log an audit event with entity information.
     */
    public void log(Long userId, String userEmail, String action, String description,
                    String entityType, Long entityId, String ipAddress, String userAgent) {
        AuditLog auditLog = AuditLog.builder()
                .userId(userId)
                .userEmail(userEmail)
                .action(action)
                .description(description)
                .entityType(entityType)
                .entityId(entityId)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .status("SUCCESS")
                .createdAt(LocalDateTime.now())
                .build();

        auditLogRepository.save(auditLog);
        log.info("Audit log: {} - {} - {}", userEmail, action, description);
    }

    /**
     * Log a failed audit event.
     */
    public void logFailure(Long userId, String userEmail, String action, String errorMessage,
                           String entityType, Long entityId, String ipAddress, String userAgent) {
        AuditLog auditLog = AuditLog.builder()
                .userId(userId)
                .userEmail(userEmail)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .status("FAILURE")
                .errorMessage(errorMessage)
                .createdAt(LocalDateTime.now())
                .build();

        auditLogRepository.save(auditLog);
        log.warn("Audit log failure: {} - {} - {}", userEmail, action, errorMessage);
    }

    /**
     * Log a login event.
     */
    public void logLogin(Long userId, String email, String ipAddress, String userAgent, boolean success) {
        String action = "LOGIN";
        String status = success ? "SUCCESS" : "FAILURE";
        String errorMessage = success ? null : "Invalid credentials";

        AuditLog auditLog = AuditLog.builder()
                .userId(userId)
                .userEmail(email)
                .action(action)
                .status(status)
                .errorMessage(errorMessage)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .createdAt(LocalDateTime.now())
                .build();

        auditLogRepository.save(auditLog);
        log.info("Audit log: Login {} for {}", status, email);
    }

    /**
     * Log a logout event.
     */
    public void logLogout(Long userId, String email, String ipAddress, String userAgent) {
        AuditLog auditLog = AuditLog.builder()
                .userId(userId)
                .userEmail(email)
                .action("LOGOUT")
                .status("SUCCESS")
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .createdAt(LocalDateTime.now())
                .build();

        auditLogRepository.save(auditLog);
        log.info("Audit log: Logout for {}", email);
    }
}
