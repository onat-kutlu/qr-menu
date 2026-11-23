package com.qrmenu.company.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCompanyRequest {

    @NotBlank(message = "Company name is required")
    @Size(max = 255, message = "Company name must be at most 255 characters")
    private String name;

    @NotBlank(message = "Company slug is required")
    @Size(max = 100, message = "Company slug must be at most 100 characters")
    @Pattern(regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$", message = "Slug must contain lowercase letters, numbers and hyphens only")
    private String slug;

    private String description;
}
