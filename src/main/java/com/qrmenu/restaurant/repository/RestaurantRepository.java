package com.qrmenu.restaurant.repository;

import com.qrmenu.auth.entity.Company;
import com.qrmenu.auth.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long>, JpaSpecificationExecutor<Restaurant> {

    @Query("SELECT r FROM Restaurant r WHERE r.company.id = :companyId AND r.deletedAt IS NULL ORDER BY r.createdAt DESC")
    List<Restaurant> findByCompanyId(@Param("companyId") Long companyId);

    @Query("SELECT r FROM Restaurant r WHERE LOWER(r.slug) = LOWER(:slug) AND r.deletedAt IS NULL")
    Optional<Restaurant> findBySlugIgnoreCase(@Param("slug") String slug);

    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM Restaurant r WHERE LOWER(r.slug) = LOWER(:slug) AND r.deletedAt IS NULL")
    boolean existsBySlugIgnoreCase(@Param("slug") String slug);

    @Query("SELECT r FROM Restaurant r WHERE r.company = :company AND LOWER(r.slug) = LOWER(:slug) AND r.deletedAt IS NULL")
    Optional<Restaurant> findByCompanyAndSlugIgnoreCase(@Param("company") Company company, @Param("slug") String slug);
}
