package com.qrmenu.auth.repository;

import com.qrmenu.auth.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Company entity.
 *
 * Provides database access methods for company-related queries.
 */
@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

    @Query("SELECT c FROM Company c WHERE c.deletedAt IS NULL")
    Page<Company> search(Pageable pageable);

    @Query("SELECT c FROM Company c WHERE c.deletedAt IS NULL AND (LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(c.slug) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Company> searchByTerm(Pageable pageable, @Param("search") String search);

    @Query("SELECT c FROM Company c WHERE c.deletedAt IS NULL AND c.status = :status")
    Page<Company> searchByStatus(Pageable pageable, @Param("status") Company.CompanyStatus status);

    @Query("SELECT c FROM Company c WHERE c.deletedAt IS NULL AND c.status = :status AND (LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(c.slug) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Company> searchByStatusAndTerm(Pageable pageable, @Param("status") Company.CompanyStatus status, @Param("search") String search);

    /**
     * Find an active company by slug (case-insensitive).
     */
    @Query("SELECT c FROM Company c WHERE LOWER(c.slug) = LOWER(:slug) AND c.status = 'ACTIVE' AND c.deletedAt IS NULL")
    Optional<Company> findActiveBySlug(@Param("slug") String slug);

    /**
     * Find a company by slug (case-insensitive).
     */
    @Query("SELECT c FROM Company c WHERE LOWER(c.slug) = LOWER(:slug)")
    Optional<Company> findBySlugIgnoreCase(@Param("slug") String slug);

    /**
     * Check if slug already exists (case-insensitive).
     */
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Company c WHERE LOWER(c.slug) = LOWER(:slug)")
    boolean slugExists(@Param("slug") String slug);

    /**
     * Find an active company by ID.
     */
    @Query("SELECT c FROM Company c WHERE c.id = :id AND c.status = 'ACTIVE' AND c.deletedAt IS NULL")
    Optional<Company> findActiveById(@Param("id") Long id);

    /**
     * Find all companies created by a specific user.
     */
    @Query("SELECT c FROM Company c WHERE c.createdBy.id = :userId AND c.deletedAt IS NULL ORDER BY c.createdAt DESC")
    List<Company> findByCreatedById(@Param("userId") Long userId);

    /**
     * Find all active companies.
     */
    @Query("SELECT c FROM Company c WHERE c.status = 'ACTIVE' AND c.deletedAt IS NULL ORDER BY c.name ASC")
    List<Company> findAllActive();
}
