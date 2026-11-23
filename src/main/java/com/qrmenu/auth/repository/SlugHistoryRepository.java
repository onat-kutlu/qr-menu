package com.qrmenu.auth.repository;

import com.qrmenu.auth.entity.SlugHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SlugHistoryRepository extends JpaRepository<SlugHistory, Long> {

    Optional<SlugHistory> findFirstByOldSlugIgnoreCaseOrderByCreatedAtDesc(String oldSlug);
}
