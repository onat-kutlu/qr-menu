package com.qrmenu.menu.repository;

import com.qrmenu.auth.entity.MenuVersion;
import com.qrmenu.auth.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MenuVersionRepository extends JpaRepository<MenuVersion, Long>, JpaSpecificationExecutor<MenuVersion> {

    @Query("SELECT mv FROM MenuVersion mv WHERE mv.restaurant.id = :restaurantId AND mv.deletedAt IS NULL ORDER BY mv.createdAt DESC")
    List<MenuVersion> findByRestaurantId(@Param("restaurantId") Long restaurantId);

    @Query("SELECT mv FROM MenuVersion mv WHERE mv.restaurant = :restaurant AND mv.language = :language AND mv.isLive = true AND mv.deletedAt IS NULL")
    Optional<MenuVersion> findLiveByRestaurantAndLanguage(@Param("restaurant") Restaurant restaurant, @Param("language") MenuVersion.Language language);

    @Query("SELECT mv FROM MenuVersion mv WHERE mv.restaurant = :restaurant AND mv.language = :language AND mv.isLive = true AND mv.deletedAt IS NULL")
    List<MenuVersion> findAllLiveByRestaurantAndLanguage(@Param("restaurant") Restaurant restaurant, @Param("language") MenuVersion.Language language);

    @Query(value = "SELECT DISTINCT lower(language::text) FROM menu_versions WHERE restaurant_id = :restaurantId AND is_live = true AND deleted_at IS NULL ORDER BY lower(language::text)", nativeQuery = true)
    List<String> findLiveLanguagesByRestaurantId(@Param("restaurantId") Long restaurantId);

    boolean existsByRestaurantIdAndFileHash(Long restaurantId, String fileHash);
}
