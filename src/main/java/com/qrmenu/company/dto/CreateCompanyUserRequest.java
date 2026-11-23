package com.qrmenu.company.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateCompanyUserRequest {
    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 8, max = 128)
    private String password;

    @Size(max = 100)
    private String firstName;

    @Size(max = 100)
    private String lastName;

    @Size(max = 20)
    private String phone;
}
