package com.qrmenu.common.exception;

import com.qrmenu.common.response.ApiErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import jakarta.validation.ConstraintViolationException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(QrMenuException.class)
    public ResponseEntity<ApiErrorResponse> handleQrMenuException(
            QrMenuException ex,
            WebRequest request) {
        
        log.warn("QrMenuException: {} - {}", ex.getCode(), ex.getMessage());
        
        ApiErrorResponse error = ApiErrorResponse.of(
                ex.getMessage(),
                ex.getCode(),
                ex.getHttpStatus(),
                request.getDescription(false).replace("uri=", "")
        );
        
        return new ResponseEntity<>(error, HttpStatus.valueOf(ex.getHttpStatus()));
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex,
            WebRequest request) {
        
        log.warn("Validation error");
        
        Map<String, String> validationErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            validationErrors.put(fieldName, errorMessage);
        });
        
        ApiErrorResponse error = ApiErrorResponse.validationError(
                "Validation failed",
                validationErrors,
                request.getDescription(false).replace("uri=", "")
        );
        
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

        @ExceptionHandler(ConstraintViolationException.class)
        public ResponseEntity<ApiErrorResponse> handleConstraintViolation(
                        ConstraintViolationException ex,
                        WebRequest request) {
                log.warn("Constraint validation error: {}", ex.getMessage());
                return new ResponseEntity<>(ApiErrorResponse.of(
                                "Validation failed",
                                "VALIDATION_ERROR",
                                400,
                                request.getDescription(false).replace("uri=", "")), HttpStatus.BAD_REQUEST);
        }
    
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(
            NoHandlerFoundException ex,
            WebRequest request) {
        
        log.warn("Endpoint not found: {}", ex.getRequestURL());
        
        ApiErrorResponse error = ApiErrorResponse.of(
                "Endpoint not found",
                "NOT_FOUND",
                404,
                request.getDescription(false).replace("uri=", "")
        );
        
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

        @ExceptionHandler({HttpMessageNotReadableException.class, MissingServletRequestParameterException.class,
            HttpMediaTypeNotSupportedException.class, HttpRequestMethodNotSupportedException.class,
            MaxUploadSizeExceededException.class})
        public ResponseEntity<ApiErrorResponse> handleRequestFormatException(Exception ex, WebRequest request) {
        return new ResponseEntity<>(ApiErrorResponse.of(
            "Request format or payload is invalid",
            "INVALID_REQUEST",
            400,
            request.getDescription(false).replace("uri=", "")), HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(DataIntegrityViolationException.class)
        public ResponseEntity<ApiErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex, WebRequest request) {
        log.warn("Data integrity violation", ex);
        return new ResponseEntity<>(ApiErrorResponse.of(
            "The request conflicts with existing data",
            "DATA_CONFLICT",
            409,
            request.getDescription(false).replace("uri=", "")), HttpStatus.CONFLICT);
        }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex,
            WebRequest request) {
        
        log.warn("Illegal argument: {}", ex.getMessage());
        
        ApiErrorResponse error = ApiErrorResponse.of(
                ex.getMessage(),
                "INVALID_ARGUMENT",
                400,
                request.getDescription(false).replace("uri=", "")
        );
        
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneralException(
            Exception ex,
            WebRequest request) {
        
        log.error("Unexpected error occurred", ex);
        
        ApiErrorResponse error = ApiErrorResponse.internalError(
                "An unexpected error occurred. Please contact support.",
                request.getDescription(false).replace("uri=", "")
        );
        
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
