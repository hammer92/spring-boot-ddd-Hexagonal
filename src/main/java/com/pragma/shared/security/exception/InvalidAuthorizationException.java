package com.pragma.shared.security.exception;

/**
 * Exception thrown when the Authorization header is present but contains invalid or malformed data.
 * This exception indicates that the client provided an authentication header that cannot be processed.
 */
public class InvalidAuthorizationException extends AuthenticationException {

    /**
     * Constructs a new InvalidAuthorizationException with the specified detail message.
     *
     * @param message the detail message
     */
    public InvalidAuthorizationException(String message) {
        super(message);
    }

    /**
     * Constructs a new InvalidAuthorizationException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause   the cause
     */
    public InvalidAuthorizationException(String message, Throwable cause) {
        super(message, cause);
    }
}