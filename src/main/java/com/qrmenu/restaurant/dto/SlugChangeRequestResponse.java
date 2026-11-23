package com.qrmenu.restaurant.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
}
