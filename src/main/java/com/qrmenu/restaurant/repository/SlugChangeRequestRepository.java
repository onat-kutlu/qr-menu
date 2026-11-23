package com.qrmenu.restaurant.repository;

import com.qrmenu.auth.entity.SlugChangeRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public interface SlugChangeRequestRepository extends JpaRepository<SlugChangeRequest, Long> {

	Page<SlugChangeRequest> findByStatus(SlugChangeRequest.ApprovalStatus status, Pageable pageable);
}
