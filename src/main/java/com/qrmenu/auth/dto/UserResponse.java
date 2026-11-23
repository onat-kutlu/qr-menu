package com.qrmenu.auth.dto;

import com.qrmenu.common.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * User response DTO.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private String phone;
    private UserRole globalRole;
    private Boolean isActive;
    private Boolean emailVerified;
    private List<CompanyInfo> companies;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CompanyInfo {
        private Long id;
        private String name;
        private String role;
    }
}
