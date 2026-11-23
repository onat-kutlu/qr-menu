package com.qrmenu.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RejectRestaurantRequest {
    @NotBlank
    @Size(max = 2000)
    private String reason;
}
