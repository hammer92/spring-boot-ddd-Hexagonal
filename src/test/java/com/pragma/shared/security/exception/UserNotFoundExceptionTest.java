package com.pragma.shared.security.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserNotFoundExceptionTest {

    @Test
    void shouldCreateExceptionWithDefaultMessage() {
        // When
        UserNotFoundException exception = new UserNotFoundException();

        // Then
        assertEquals("User not registered in the system", exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void shouldCreateExceptionWithCustomMessage() {
        // Given
        String customMessage = "Custom user not found message";

        // When
        UserNotFoundException exception = new UserNotFoundException(customMessage);

        // Then
        assertEquals(customMessage, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void shouldCreateExceptionWithMessageAndCause() {
        // Given
        String message = "User not found in database";
        Throwable cause = new RuntimeException("Database error");

        // When
        UserNotFoundException exception = new UserNotFoundException(message, cause);

        // Then
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void shouldCreateExceptionWithGoogleIdIncluded() {
        // Given
        String googleUserId = "google123456";

        // When
        UserNotFoundException exception = new UserNotFoundException(googleUserId, true);

        // Then
        assertEquals("User with Google ID 'google123456' not registered in the system", exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void shouldCreateExceptionWithGoogleIdExcluded() {
        // Given
        String googleUserId = "google123456";

        // When
        UserNotFoundException exception = new UserNotFoundException(googleUserId, false);

        // Then
        assertEquals("User not registered in the system", exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void shouldBeInstanceOfAuthenticationException() {
        // Given
        UserNotFoundException exception = new UserNotFoundException();

        // Then
        assertInstanceOf(AuthenticationException.class, exception);
        assertInstanceOf(RuntimeException.class, exception);
    }
}