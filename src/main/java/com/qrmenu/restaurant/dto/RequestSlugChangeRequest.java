package com.qrmenu.restaurant.dto;

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
public class RequestSlugChangeRequest {

    @NotBlank(message = "Requested slug is required")
    @Size(max = 100, message = "Slug must be at most 100 characters")
    @Pattern(regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$", message = "Slug must contain lowercase letters, numbers and hyphens only")
    private String requestedSlug;
}
