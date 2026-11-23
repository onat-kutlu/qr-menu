package com.qrmenu.menu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.qrmenu.auth.entity.PdfRejectionReason;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RejectMenuRequest {

    @NotBlank(message = "Rejection reason is required")
    @Size(max = 2000, message = "Rejection reason must be at most 2000 characters")
    private String reason;

    @NotNull(message = "Rejection category is required")
    private PdfRejectionReason.Category category;
}
