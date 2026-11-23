package com.qrmenu.common.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiErrorResponse {
    
    @Builder.Default
    private boolean success = false;
    private String message;
    private String code;
    private int status;
    private LocalDateTime timestamp;
    private String path;
    private Map<String, String> validationErrors;
    private List<FieldError> fieldErrors;
    private String traceId;
    
    public static ApiErrorResponse of(String message, String code, int status, String path) {
        return ApiErrorResponse.builder()
                .success(false)
                .message(message)
                .code(code)
                .status(status)
                .path(path)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    public static ApiErrorResponse validationError(String message, Map<String, String> validationErrors, String path) {
        return ApiErrorResponse.builder()
                .success(false)
                .message(message)
                .code("VALIDATION_ERROR")
                .status(400)
                .path(path)
                .validationErrors(validationErrors)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    public static ApiErrorResponse internalError(String message, String path) {
        return ApiErrorResponse.builder()
                .success(false)
                .message(message)
                .code("INTERNAL_SERVER_ERROR")
                .status(500)
                .path(path)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FieldError {
        private String field;
        private String message;
        private Object rejectedValue;
    }
}
