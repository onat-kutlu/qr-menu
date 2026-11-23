package com.qrmenu.auth.repository;

import com.qrmenu.auth.entity.RestaurantUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RestaurantUserRepository extends JpaRepository<RestaurantUser, Long> {

    @Query("SELECT ru FROM RestaurantUser ru WHERE ru.restaurant.id = :restaurantId AND ru.user.id = :userId AND ru.isActive = true")
    Optional<RestaurantUser> findActiveByRestaurantIdAndUserId(@Param("restaurantId") Long restaurantId, @Param("userId") Long userId);

    @Query("SELECT ru FROM RestaurantUser ru WHERE ru.restaurant.company.id = :companyId AND ru.user.id = :userId AND ru.isActive = true")
    java.util.List<RestaurantUser> findActiveByCompanyIdAndUserId(@Param("companyId") Long companyId, @Param("userId") Long userId);
}
