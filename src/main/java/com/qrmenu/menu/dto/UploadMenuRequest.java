package com.qrmenu.menu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadMenuRequest {

    @NotNull(message = "Restaurant ID is required")
    private Long restaurantId;

    @NotBlank(message = "Language is required")
    private String language;

    @NotBlank(message = "File path is required")
    private String filePath;

    private Long fileSizeBytes;

    private String fileHash;

    private Integer pageCount;
}
