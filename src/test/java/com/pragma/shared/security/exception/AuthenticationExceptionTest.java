package com.pragma.shared.security.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthenticationExceptionTest {

    // Concrete implementation for testing the abstract class
    private static class TestAuthenticationException extends AuthenticationException {
        public TestAuthenticationException(String message) {
            super(message);
        }

        public TestAuthenticationException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    @Test
    void shouldCreateExceptionWithMessage() {
        // Given
        String message = "Test authentication error";

        // When
        AuthenticationException exception = new TestAuthenticationException(message);

        // Then
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void shouldCreateExceptionWithMessageAndCause() {
        // Given
        String message = "Test authentication error";
        Throwable cause = new RuntimeException("Root cause");

        // When
        AuthenticationException exception = new TestAuthenticationException(message, cause);

        // Then
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void shouldBeInstanceOfRuntimeException() {
        // Given
        AuthenticationException exception = new TestAuthenticationException("Test message");

        // Then
        assertInstanceOf(RuntimeException.class, exception);
    }
}