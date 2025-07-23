package com.horseriding.ecommerce.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when authentication fails.
 * Maps to HTTP 401 Unauthorized response.
 */
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class AuthenticationException extends RuntimeException {

    /**
     * Constructs a new authentication exception with the specified detail message.
     *
     * @param message the detail message
     */
    public AuthenticationException(final String message) {
        super(message);
    }

    /**
     * Constructs a new authentication exception with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public AuthenticationException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
