package com.qrmenu.admin.dto;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class UpdateRestaurantRequest {
    private String status;

    @Min(1)
    private Integer monthlyUploadQuota;
}
