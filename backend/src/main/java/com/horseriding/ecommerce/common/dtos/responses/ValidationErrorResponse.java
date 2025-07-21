package com.horseriding.ecommerce.common.dtos.responses;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO for field-specific validation error responses.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidationErrorResponse {

    private String error;
    private String message;
    private int status;
    private String path;
    private LocalDateTime timestamp;
    private Map<String, String> fieldErrors;

    public ValidationErrorResponse(String error, String message, int status, String path, Map<String, String> fieldErrors) {
        this.error = error;
        this.message = message;
        this.status = status;
        this.path = path;
        this.timestamp = LocalDateTime.now();
        this.fieldErrors = fieldErrors;
    }
}