package com.pragma.shared.security.exception;

/**
 * Exception thrown when a user with the provided Google ID is not found in the internal database.
 * This exception indicates that the Google ID is valid but the user is not registered in the system.
 */
public class UserNotFoundException extends AuthenticationException {

    /**
     * Constructs a new UserNotFoundException with the specified detail message.
     *
     * @param message the detail message
     */
    public UserNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructs a new UserNotFoundException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause   the cause
     */
    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}