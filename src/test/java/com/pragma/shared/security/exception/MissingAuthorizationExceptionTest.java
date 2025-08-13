package com.pragma.shared.security.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MissingAuthorizationExceptionTest {

    @Test
    void shouldCreateExceptionWithDefaultMessage() {
        // When
        MissingAuthorizationException exception = new MissingAuthorizationException();

        // Then
        assertEquals("Authorization header is required", exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void shouldCreateExceptionWithCustomMessage() {
        // Given
        String customMessage = "Custom authorization header missing message";

        // When
        MissingAuthorizationException exception = new MissingAuthorizationException(customMessage);

        // Then
        assertEquals(customMessage, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void shouldCreateExceptionWithMessageAndCause() {
        // Given
        String message = "Authorization header missing";
        Throwable cause = new RuntimeException("Root cause");

        // When
        MissingAuthorizationException exception = new MissingAuthorizationException(message, cause);

        // Then
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void shouldBeInstanceOfAuthenticationException() {
        // Given
        MissingAuthorizationException exception = new MissingAuthorizationException();

        // Then
        assertInstanceOf(AuthenticationException.class, exception);
        assertInstanceOf(RuntimeException.class, exception);
    }
}