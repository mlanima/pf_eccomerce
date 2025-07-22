package com.horseriding.ecommerce.common.mapping;

import com.horseriding.ecommerce.common.dtos.responses.ApiErrorResponse;
import com.horseriding.ecommerce.common.dtos.responses.SuccessResponse;
import com.horseriding.ecommerce.common.dtos.responses.ValidationErrorResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

/**
 * Utility class for creating standardized API response DTOs.
 */
@Component
public class ResponseMapper {

    /**
     * Create a success response with message only
     */
    public <T> SuccessResponse<T> toSuccessResponse(String message, HttpStatus status) {
        return new SuccessResponse<>(message, status.value());
    }

    /**
     * Create a success response with message and data
     */
    public <T> SuccessResponse<T> toSuccessResponse(String message, HttpStatus status, T data) {
        return new SuccessResponse<>(message, status.value(), data);
    }

    /**
     * Create an error response
     */
    public ApiErrorResponse toErrorResponse(
            String error, String message, HttpStatus status, String path) {
        return new ApiErrorResponse(error, message, status.value(), path);
    }

    /**
     * Create an error response with details
     */
    public ApiErrorResponse toErrorResponse(
            String error, String message, HttpStatus status, String path, List<String> details) {
        return new ApiErrorResponse(error, message, status.value(), path, details);
    }

    /**
     * Create a validation error response from BindingResult
     */
    public ValidationErrorResponse toValidationErrorResponse(
            BindingResult bindingResult, String path) {
        Map<String, String> fieldErrors = new HashMap<>();

        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        return new ValidationErrorResponse(
                "Validation Failed",
                "Request validation failed",
                HttpStatus.BAD_REQUEST.value(),
                path,
                fieldErrors);
    }

    /**
     * Create a validation error response from field errors map
     */
    public ValidationErrorResponse toValidationErrorResponse(
            Map<String, String> fieldErrors, String path) {
        return new ValidationErrorResponse(
                "Validation Failed",
                "Request validation failed",
                HttpStatus.BAD_REQUEST.value(),
                path,
                fieldErrors);
    }

    /**
     * Create a validation error response from constraint violations
     */
    public ValidationErrorResponse toValidationErrorResponse(
            String message, Map<String, String> fieldErrors, String path) {
        return new ValidationErrorResponse(
                "Validation Failed", message, HttpStatus.BAD_REQUEST.value(), path, fieldErrors);
    }
}
