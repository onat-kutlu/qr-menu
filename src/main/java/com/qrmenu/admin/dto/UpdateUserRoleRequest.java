package com.qrmenu.admin.dto;

import com.qrmenu.common.enums.UserRole;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateUserRoleRequest {
    @NotNull
    private Long companyId;

    @NotNull
    private UserRole role;
}
