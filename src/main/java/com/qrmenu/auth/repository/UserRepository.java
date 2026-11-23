package com.qrmenu.auth.repository;

import com.qrmenu.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for User entity.
 *
 * Provides database access methods for user-related queries.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    /**
     * Find a user by email address (case-insensitive).
     */
    Optional<User> findByEmailIgnoreCase(String email);

    /**
     * Find an active user by email (case-insensitive).
     */
    @Query("SELECT u FROM User u WHERE LOWER(u.email) = LOWER(:email) AND u.isActive = true AND u.deletedAt IS NULL")
    Optional<User> findActiveByEmail(@Param("email") String email);

    /**
     * Find an active user by ID.
     */
    @Query("SELECT u FROM User u WHERE u.id = :id AND u.isActive = true AND u.deletedAt IS NULL")
    Optional<User> findActiveById(@Param("id") Long id);

    /**
     * Check if an email already exists (case-insensitive).
     */
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE LOWER(u.email) = LOWER(:email)")
    boolean emailExists(@Param("email") String email);
}
