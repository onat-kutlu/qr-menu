package com.qrmenu.common.exception;

import lombok.Getter;

@Getter
public class QrMenuException extends RuntimeException {
    
    private final String code;
    private final int httpStatus;
    private final Object details;
    
    public QrMenuException(String message, String code, int httpStatus) {
        super(message);
        this.code = code;
        this.httpStatus = httpStatus;
        this.details = null;
    }
    
    public QrMenuException(String message, String code, int httpStatus, Object details) {
        super(message);
        this.code = code;
        this.httpStatus = httpStatus;
        this.details = details;
    }
    
    public QrMenuException(String message, String code, int httpStatus, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.httpStatus = httpStatus;
        this.details = null;
    }
}
