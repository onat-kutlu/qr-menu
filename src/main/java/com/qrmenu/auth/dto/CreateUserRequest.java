package com.qrmenu.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Create user request DTO.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateUserRequest {
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password should be at least 8 characters")
    private String password;

    @Size(max = 100, message = "First name should not exceed 100 characters")
    private String firstName;

    @Size(max = 100, message = "Last name should not exceed 100 characters")
    private String lastName;

    @Size(max = 20, message = "Phone should not exceed 20 characters")
    private String phone;
}
