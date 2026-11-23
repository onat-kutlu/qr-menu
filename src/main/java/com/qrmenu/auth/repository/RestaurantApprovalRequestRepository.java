package com.qrmenu.auth.repository;

import com.qrmenu.auth.entity.RestaurantApprovalRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface RestaurantApprovalRequestRepository extends JpaRepository<RestaurantApprovalRequest, Long> {

    @Query("SELECT r FROM RestaurantApprovalRequest r WHERE r.status = 'PENDING' ORDER BY r.createdAt ASC")
    Page<RestaurantApprovalRequest> findPending(Pageable pageable);

    Optional<RestaurantApprovalRequest> findFirstByRestaurantIdAndStatusOrderByCreatedAtDesc(
            Long restaurantId, RestaurantApprovalRequest.ApprovalStatus status);
}
