package com.pragma.shared.security.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InvalidAuthorizationExceptionTest {

    @Test
    void shouldCreateExceptionWithDefaultMessage() {
        // When
        InvalidAuthorizationException exception = new InvalidAuthorizationException("Invalid authorization header format");

        // Then
        assertEquals("Invalid authorization header format", exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void shouldCreateExceptionWithCustomMessage() {
        // Given
        String customMessage = "Custom invalid authorization message";

        // When
        InvalidAuthorizationException exception = new InvalidAuthorizationException(customMessage);

        // Then
        assertEquals(customMessage, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void shouldCreateExceptionWithMessageAndCause() {
        // Given
        String message = "Invalid authorization format";
        Throwable cause = new IllegalArgumentException("Invalid format");

        // When
        InvalidAuthorizationException exception = new InvalidAuthorizationException(message, cause);

        // Then
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void shouldBeInstanceOfAuthenticationException() {
        // Given
        InvalidAuthorizationException exception = new InvalidAuthorizationException("Invalid authorization header format");

        // Then
        assertInstanceOf(AuthenticationException.class, exception);
        assertInstanceOf(RuntimeException.class, exception);
    }
}