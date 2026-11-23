package com.qrmenu.restaurant.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantResponse {
    private Long id;
    private Long companyId;
    private String name;
    private String slug;
    private String description;
    private String address;
    private String phone;
    private String logoPath;
    private String status;
    private Integer monthlyUploadQuota;
    private Integer monthlyUploadsUsed;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
