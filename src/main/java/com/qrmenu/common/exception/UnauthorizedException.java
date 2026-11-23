package com.qrmenu.common.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends QrMenuException {
    
    public UnauthorizedException(String message) {
        super(message, "UNAUTHORIZED", HttpStatus.UNAUTHORIZED.value());
    }
    
    public UnauthorizedException(String message, Object details) {
        super(message, "UNAUTHORIZED", HttpStatus.UNAUTHORIZED.value(), details);
    }
}
