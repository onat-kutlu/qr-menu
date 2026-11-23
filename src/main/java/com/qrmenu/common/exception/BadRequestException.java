package com.qrmenu.common.exception;

import org.springframework.http.HttpStatus;

public class BadRequestException extends QrMenuException {
    
    public BadRequestException(String message) {
        super(message, "BAD_REQUEST", HttpStatus.BAD_REQUEST.value());
    }
    
    public BadRequestException(String message, Object details) {
        super(message, "BAD_REQUEST", HttpStatus.BAD_REQUEST.value(), details);
    }
}
