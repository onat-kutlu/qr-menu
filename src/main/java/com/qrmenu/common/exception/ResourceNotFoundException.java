package com.qrmenu.common.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends QrMenuException {
    
    public ResourceNotFoundException(String resourceName, String identifier) {
        super(
                String.format("%s not found: %s", resourceName, identifier),
                "RESOURCE_NOT_FOUND",
                HttpStatus.NOT_FOUND.value()
        );
    }
    
    public ResourceNotFoundException(String message) {
        super(message, "RESOURCE_NOT_FOUND", HttpStatus.NOT_FOUND.value());
    }
}
