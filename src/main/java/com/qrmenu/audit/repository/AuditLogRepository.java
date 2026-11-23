package com.qrmenu.audit.repository;

import com.qrmenu.audit.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for AuditLog entity.
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long>, JpaSpecificationExecutor<AuditLog> {

    /**
     * Find all audit logs for a specific user.
     */
    @Query("SELECT a FROM AuditLog a WHERE a.userId = :userId ORDER BY a.createdAt DESC")
    List<AuditLog> findByUserId(@Param("userId") Long userId);

    /**
     * Find audit logs by action type.
     */
    @Query("SELECT a FROM AuditLog a WHERE a.action = :action ORDER BY a.createdAt DESC")
    List<AuditLog> findByAction(@Param("action") String action);

    /**
     * Find audit logs within a date range.
     */
    @Query("SELECT a FROM AuditLog a WHERE a.createdAt BETWEEN :startDate AND :endDate ORDER BY a.createdAt DESC")
    List<AuditLog> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * Find audit logs for a specific entity.
     */
    @Query("SELECT a FROM AuditLog a WHERE a.entityType = :entityType AND a.entityId = :entityId ORDER BY a.createdAt DESC")
    List<AuditLog> findByEntity(@Param("entityType") String entityType, @Param("entityId") Long entityId);
}
