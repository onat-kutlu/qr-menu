package com.qrmenu.menu.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuVersionResponse {
    private Long id;
    private Long restaurantId;
    private String language;
    private String filePath;
    private Long fileSizeBytes;
    private String fileHash;
    private Integer pageCount;
    private String status;
    private Integer versionNumber;
    private Boolean isLive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
