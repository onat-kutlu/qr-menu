package com.qrmenu.auth.repository;

import com.qrmenu.auth.entity.PdfRejectionReason;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PdfRejectionReasonRepository extends JpaRepository<PdfRejectionReason, Long> {

    List<PdfRejectionReason> findByMenuVersionIdOrderByCreatedAtDesc(Long menuVersionId);
}
