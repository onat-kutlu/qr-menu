package com.qrmenu.admin.dto;

import com.qrmenu.common.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateAdminUserRequest {
    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 8)
    private String password;

    @Size(max = 100)
    private String firstName;
    @Size(max = 100)
    private String lastName;
    @Size(max = 20)
    private String phone;

    @NotNull
    private Long companyId;

    @NotNull
    private UserRole role;
}
