package com.qrmenu.restaurant.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicRestaurantResponse {
    private Long id;
    private String name;
    private String slug;
    private String address;
    private String phone;
    private String logoPath;
    /** Language-specific menu URLs. */
    private Map<String, String> openUrls;
    /** Legacy alias for the first available language URL. */
    private String openUrl;
    /** Canonical public URL when the request used a historical slug. */
    private String redirectUrl;
    private List<String> languages;
    private String status;
}
