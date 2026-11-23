package com.qrmenu.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SlugChangeRequestResponse {
    private Long id;
    private Long restaurantId;
    private String currentSlug;
    private String requestedSlug;
    private String status;
    private LocalDateTime createdAt;
}
