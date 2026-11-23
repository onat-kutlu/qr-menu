package com.qrmenu.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateCompanyStatusRequest {
    @NotBlank
    private String status;
}
