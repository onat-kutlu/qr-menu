package com.qrmenu.auth.repository;

import com.qrmenu.auth.entity.CompanyUser;
import com.qrmenu.common.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for CompanyUser entity.
 *
 * Provides database access methods for company-user relationship queries.
 */
@Repository
public interface CompanyUserRepository extends JpaRepository<CompanyUser, Long> {

    /**
     * Find the relationship between a company and user.
     */
    Optional<CompanyUser> findByCompanyIdAndUserId(Long companyId, Long userId);

    /**
     * Find an active relationship between a company and user.
     */
    @Query("SELECT cu FROM CompanyUser cu WHERE cu.company.id = :companyId AND cu.user.id = :userId AND cu.isActive = true")
    Optional<CompanyUser> findActiveByCompanyIdAndUserId(@Param("companyId") Long companyId, @Param("userId") Long userId);

    /**
     * Find all companies a user belongs to.
     */
    @Query("SELECT cu FROM CompanyUser cu WHERE cu.user.id = :userId AND cu.isActive = true ORDER BY cu.company.name ASC")
    List<CompanyUser> findActiveByUserId(@Param("userId") Long userId);

    /**
     * Find all users in a company.
     */
    @Query("SELECT cu FROM CompanyUser cu WHERE cu.company.id = :companyId AND cu.isActive = true ORDER BY cu.user.email ASC")
    List<CompanyUser> findActiveByCompanyId(@Param("companyId") Long companyId);

    /**
     * Find all users in a company with a specific role.
     */
    @Query("SELECT cu FROM CompanyUser cu WHERE cu.company.id = :companyId AND cu.role = :role AND cu.isActive = true")
    List<CompanyUser> findByCompanyIdAndRole(@Param("companyId") Long companyId, @Param("role") UserRole role);

    /**
     * Check if a user is a company admin in any company.
     */
    @Query("SELECT CASE WHEN COUNT(cu) > 0 THEN true ELSE false END FROM CompanyUser cu WHERE cu.user.id = :userId AND cu.role = 'COMPANY_ADMIN' AND cu.isActive = true")
    boolean isCompanyAdmin(@Param("userId") Long userId);

    /**
     * Check if a user has admin role in a specific company.
     */
    @Query("SELECT CASE WHEN COUNT(cu) > 0 THEN true ELSE false END FROM CompanyUser cu WHERE cu.user.id = :userId AND cu.company.id = :companyId AND (cu.role = 'COMPANY_ADMIN' OR cu.role = 'ADMIN') AND cu.isActive = true")
    boolean isAdminInCompany(@Param("userId") Long userId, @Param("companyId") Long companyId);

    /**
     * Find all users that are company admins.
     */
    @Query("SELECT DISTINCT cu.user FROM CompanyUser cu WHERE cu.role = 'COMPANY_ADMIN' AND cu.isActive = true")
    List<com.qrmenu.auth.entity.User> findAllCompanyAdmins();
}
