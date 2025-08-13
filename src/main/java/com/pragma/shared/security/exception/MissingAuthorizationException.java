package com.pragma.shared.security.exception;

import com.pragma.shared.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Exception thrown when the Authorization header is missing from the HTTP request.
 * This exception indicates that the client failed to provide the required authentication header.
 */
public class MissingAuthorizationException extends AuthenticationException {

    /**
     * Constructs a new MissingAuthorizationException with the specified detail message.
     *
     * @param message the detail message
     */
    public MissingAuthorizationException(String message) {
        super(message);
    }

    /**
     * Constructs a new MissingAuthorizationException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause   the cause
     */
    public MissingAuthorizationException(String message, Throwable cause) {
        super(message, cause);
    }
}