package com.horseriding.ecommerce.exception;

import com.horseriding.ecommerce.common.dtos.responses.ApiErrorResponse;
import com.horseriding.ecommerce.common.dtos.responses.ValidationErrorResponse;
import com.horseriding.ecommerce.common.mapping.ResponseMapper;
import jakarta.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

/**
 * Global exception handler for consistent error responses across the application.
 */
@ControllerAdvice
@RequiredArgsConstructor
public final class GlobalExceptionHandler {

    /** Response mapper for creating error responses. */
    private final ResponseMapper responseMapper;

    /**
     * Handle validation errors from @Valid annotations.
     *
     * @param ex the exception
     * @param request the web request
     * @return validation error response
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationExceptions(
            final MethodArgumentNotValidException ex, final WebRequest request) {

        ValidationErrorResponse errorResponse =
                responseMapper.toValidationErrorResponse(
                        ex.getBindingResult(), request.getDescription(false));

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle validation errors from @Validated annotations.
     *
     * @param ex the exception
     * @param request the web request
     * @return validation error response
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ValidationErrorResponse> handleBindExceptions(
            final BindException ex, final WebRequest request) {

        ValidationErrorResponse errorResponse =
                responseMapper.toValidationErrorResponse(
                        ex.getBindingResult(), request.getDescription(false));

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle constraint violation exceptions.
     *
     * @param ex the exception
     * @param request the web request
     * @return validation error response
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ValidationErrorResponse> handleConstraintViolationException(
            final ConstraintViolationException ex, final WebRequest request) {

        Map<String, String> fieldErrors = new HashMap<>();
        ex.getConstraintViolations()
                .forEach(
                        violation -> {
                            String fieldName = violation.getPropertyPath().toString();
                            String errorMessage = violation.getMessage();
                            fieldErrors.put(fieldName, errorMessage);
                        });

        ValidationErrorResponse errorResponse =
                responseMapper.toValidationErrorResponse(
                        fieldErrors, request.getDescription(false));

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle resource not found exceptions.
     *
     * @param ex the exception
     * @param request the web request
     * @return API error response
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceNotFoundException(
            final ResourceNotFoundException ex, final WebRequest request) {

        ApiErrorResponse errorResponse =
                responseMapper.toErrorResponse(
                        "Not Found",
                        ex.getMessage(),
                        HttpStatus.NOT_FOUND,
                        request.getDescription(false));

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * Handle illegal argument exceptions.
     *
     * @param ex the exception
     * @param request the web request
     * @return API error response
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgumentException(
            final IllegalArgumentException ex, final WebRequest request) {

        ApiErrorResponse errorResponse =
                responseMapper.toErrorResponse(
                        "Bad Request",
                        ex.getMessage(),
                        HttpStatus.BAD_REQUEST,
                        request.getDescription(false));

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle runtime exceptions.
     *
     * @param ex the exception
     * @param request the web request
     * @return API error response
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiErrorResponse> handleRuntimeException(
            final RuntimeException ex, final WebRequest request) {

        ApiErrorResponse errorResponse =
                responseMapper.toErrorResponse(
                        "Internal Server Error",
                        "An unexpected error occurred",
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        request.getDescription(false));

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handle all other exceptions.
     *
     * @param ex the exception
     * @param request the web request
     * @return API error response
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(
            final Exception ex, final WebRequest request) {

        ApiErrorResponse errorResponse =
                responseMapper.toErrorResponse(
                        "Internal Server Error",
                        "An unexpected error occurred",
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        request.getDescription(false));

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
