package com.qrmenu.common.exception;

import org.springframework.http.HttpStatus;

public class ForbiddenException extends QrMenuException {
    
    public ForbiddenException(String message) {
        super(message, "FORBIDDEN", HttpStatus.FORBIDDEN.value());
    }
    
    public ForbiddenException(String message, Object details) {
        super(message, "FORBIDDEN", HttpStatus.FORBIDDEN.value(), details);
    }
}
