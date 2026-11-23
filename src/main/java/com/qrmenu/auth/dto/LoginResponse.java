package com.qrmenu.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Login response DTO.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {
    private String token;
    private Long userId;
    private String email;
    private String firstName;
    private String lastName;
    private String highestRole;
    private List<CompanyInfo> companies;
    private Long expiresIn;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CompanyInfo {
        private Long id;
        private String name;
        private String role;
    }
}
