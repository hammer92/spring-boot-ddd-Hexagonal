package com.pragma.shared.security.exception;

/**
 * Exception thrown when a user with the provided Google ID is not found in the internal database.
 * This exception indicates that the Google ID is valid but the user is not registered in the system.
 */
public class UserNotFoundException extends AuthenticationException {

    private static final String DEFAULT_MESSAGE = "User not registered in the system";

    /**
     * Constructs a new UserNotFoundException with the default message.
     */
    public UserNotFoundException() {
        super(DEFAULT_MESSAGE);
    }

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

    /**
     * Constructs a new UserNotFoundException with a message that includes the Google ID.
     *
     * @param googleUserId the Google user ID that was not found
     */
    public UserNotFoundException(String googleUserId, boolean includeId) {
        super(includeId ? 
            String.format("User with Google ID '%s' not registered in the system", googleUserId) : 
            DEFAULT_MESSAGE);
    }
}