package com.horseriding.ecommerce.exception;

import com.horseriding.ecommerce.common.dtos.responses.ApiErrorResponse;
import com.horseriding.ecommerce.common.dtos.responses.ValidationErrorResponse;
import com.horseriding.ecommerce.common.mapping.ResponseMapper;
import jakarta.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.util.MimeType;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Global exception handler for consistent error responses across the application.
 */
@ControllerAdvice
@RequiredArgsConstructor
@Slf4j
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
     * Handle invalid json structure exception.
     *
     * @param ex the exception
     * @param request the web request
     * @return API error response
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleHttpMessageNotReadableException(
            final HttpMessageNotReadableException ex, final WebRequest request) {

        String message = "Invalid JSON format or malformed request body";
        if (ex.getMessage() != null && ex.getMessage().contains("JSON")) {
            message = "Invalid JSON structure in request body";
        }

        ApiErrorResponse errorResponse =
                responseMapper.toErrorResponse(
                        "Bad Request",
                        message,
                        HttpStatus.BAD_REQUEST,
                        request.getDescription(false));

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle unsupported media type exception.
     *
     * @param ex the exception
     * @param request the web request
     * @return API error response
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleHttpMediaTypeNotSupportedException(
            final HttpMediaTypeNotSupportedException ex, final WebRequest request) {

        String supportedTypes =
                ex.getSupportedMediaTypes().stream()
                        .map(MimeType::toString)
                        .reduce((a, b) -> a + ", " + b)
                        .orElse("none");

        String message =
                String.format(
                        "Content type '%s' not supported. Supported types are: %s",
                        ex.getContentType(), supportedTypes);

        ApiErrorResponse errorResponse =
                responseMapper.toErrorResponse(
                        "Unsupported Media Type",
                        message,
                        HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                        request.getDescription(false));

        return new ResponseEntity<>(errorResponse, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    /**
     * Handle resource not found exceptions.
     *
     * @param ex the exception
     * @param request the web request
     * @return API error response
     */
    @ExceptionHandler({ResourceNotFoundException.class, NoResourceFoundException.class})
    public ResponseEntity<ApiErrorResponse> handleResourceNotFoundException(
            final ResourceNotFoundException ex, final WebRequest request) {

        ApiErrorResponse errorResponse =
                responseMapper.toErrorResponse(
                        "Not Found",
                        "Resource not found",
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

        log.error(
                "Unhandled exception at {}: {}",
                request.getDescription(false),
                ex.getMessage(),
                ex);

        ApiErrorResponse errorResponse =
                responseMapper.toErrorResponse(
                        "Internal Server Error",
                        "An unexpected error occurred: " + ex.getMessage(),
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        request.getDescription(false));

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handle authentication exceptions.
     *
     * @param ex the exception
     * @param request the web request
     * @return API error response
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthenticationException(
            final AuthenticationException ex, final WebRequest request) {

        ApiErrorResponse errorResponse =
                responseMapper.toErrorResponse(
                        "Authentication Failed",
                        ex.getMessage(),
                        HttpStatus.UNAUTHORIZED,
                        request.getDescription(false));

        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Handle Spring Security authentication exceptions.
     *
     * @param ex the exception
     * @param request the web request
     * @return API error response
     */
    @ExceptionHandler(org.springframework.security.core.AuthenticationException.class)
    public ResponseEntity<ApiErrorResponse> handleSpringAuthenticationException(
            final org.springframework.security.core.AuthenticationException ex,
            final WebRequest request) {

        ApiErrorResponse errorResponse =
                responseMapper.toErrorResponse(
                        "Authentication Failed",
                        "Invalid credentials or authentication token",
                        HttpStatus.UNAUTHORIZED,
                        request.getDescription(false));

        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Handle access denied exceptions.
     *
     * @param ex the exception
     * @param request the web request
     * @return API error response
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDeniedException(
            final AccessDeniedException ex, final WebRequest request) {

        ApiErrorResponse errorResponse =
                responseMapper.toErrorResponse(
                        "Access Denied",
                        ex.getMessage(),
                        HttpStatus.FORBIDDEN,
                        request.getDescription(false));

        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    /**
     * Handle Spring Security access denied exceptions.
     *
     * @param ex the exception
     * @param request the web request
     * @return API error response
     */
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleSpringAccessDeniedException(
            final org.springframework.security.access.AccessDeniedException ex,
            final WebRequest request) {

        ApiErrorResponse errorResponse =
                responseMapper.toErrorResponse(
                        "Access Denied",
                        "You don't have permission to access this resource: " + ex.getMessage(),
                        HttpStatus.FORBIDDEN,
                        request.getDescription(false));

        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    /**
     * Handle HTTP method not supported exceptions.
     *
     * @param ex the exception
     * @param request the web request
     * @return API error response
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleHttpRequestMethodNotSupported(
            final HttpRequestMethodNotSupportedException ex, final WebRequest request) {

        String supportedMethods = String.join(", ", ex.getSupportedMethods());
        String message =
                String.format(
                        "Request method '%s' not supported. Supported methods are: %s",
                        ex.getMethod(), supportedMethods);

        ApiErrorResponse errorResponse =
                responseMapper.toErrorResponse(
                        "Method Not Allowed",
                        message,
                        HttpStatus.METHOD_NOT_ALLOWED,
                        request.getDescription(false));

        return new ResponseEntity<>(errorResponse, HttpStatus.METHOD_NOT_ALLOWED);
    }

    /**
     * Handle illegal state exceptions (often thrown by SecurityUtils).
     *
     * @param ex the exception
     * @param request the web request
     * @return API error response
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalStateException(
            final IllegalStateException ex, final WebRequest request) {

        // Check if this is an authentication-related IllegalStateException
        if (ex.getMessage() != null && ex.getMessage().contains("authenticated")) {
            ApiErrorResponse errorResponse =
                    responseMapper.toErrorResponse(
                            "Authentication Required",
                            "You must be authenticated to access this resource",
                            HttpStatus.UNAUTHORIZED,
                            request.getDescription(false));

            return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
        }

        // Generic IllegalStateException handling
        ApiErrorResponse errorResponse =
                responseMapper.toErrorResponse(
                        "Bad Request",
                        ex.getMessage(),
                        HttpStatus.BAD_REQUEST,
                        request.getDescription(false));

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
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

        log.error(
                "Unhandled exception at {}: {}",
                request.getDescription(false),
                ex.getMessage(),
                ex);

        ApiErrorResponse errorResponse =
                responseMapper.toErrorResponse(
                        "Internal Server Error",
                        "An unexpected error occurred",
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        request.getDescription(false));

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
