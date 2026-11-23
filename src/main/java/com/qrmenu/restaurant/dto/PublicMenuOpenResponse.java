package com.qrmenu.restaurant.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicMenuOpenResponse {
    private String restaurantSlug;
    private String language;
    private String pdfUrl;
    private String redirectUrl;
}
