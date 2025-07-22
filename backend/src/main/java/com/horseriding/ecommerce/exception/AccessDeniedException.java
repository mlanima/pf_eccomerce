package com.horseriding.ecommerce.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a user attempts to access a resource they don't have permission for.
 * Maps to HTTP 403 Forbidden response.
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class AccessDeniedException extends RuntimeException {

    /**
     * Constructs a new access denied exception with the specified detail message.
     *
     * @param message the detail message
     */
    public AccessDeniedException(final String message) {
        super(message);
    }

    /**
     * Constructs a new access denied exception with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public AccessDeniedException(final String message, final Throwable cause) {
        super(message, cause);
    }
}