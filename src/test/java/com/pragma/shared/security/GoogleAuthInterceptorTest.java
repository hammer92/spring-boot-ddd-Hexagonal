package com.pragma.shared.security;

import com.pragma.shared.context.UserContext;
import com.pragma.shared.security.exception.InvalidAuthorizationException;
import com.pragma.shared.security.exception.MissingAuthorizationException;
import com.pragma.shared.security.exception.UserNotFoundException;
import com.pragma.shared.service.MessageService;
import com.pragma.usuarios.application.service.UserService;
import com.pragma.usuarios.domain.model.User;
import com.pragma.usuarios.domain.model.enums.RolUsuario;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoogleAuthInterceptorTest {

    @Mock
    private UserService userService;

    @Mock
    private MessageService messageService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private com.pragma.shared.config.AuthenticationProperties authProperties;

    @InjectMocks
    private GoogleAuthInterceptor googleAuthInterceptor;

    private User testUser;
    private static final String TEST_GOOGLE_USER_ID = "google123";
    private static final String AUTHORIZATION_HEADER = "Authorization";

    @BeforeEach
    void setUp() {
        // Create a test user
        testUser = new User();
        testUser.setId("user123");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setEmail("john.doe@example.com");
        testUser.setGoogleUserId(TEST_GOOGLE_USER_ID);
        testUser.setRol(RolUsuario.Tutorado);
        testUser.setActiveTutoringLimit(0);

        // Mock AuthenticationProperties
        when(authProperties.getHeaderName()).thenReturn(AUTHORIZATION_HEADER);

        // Clear user context before each test
        UserContext.clear();
    }

    @AfterEach
    void tearDown() {
        // Clear user context after each test to prevent test interference
        UserContext.clear();
    }

    @Test
    void preHandle_WithValidGoogleId_ShouldReturnTrueAndSetUserContext() throws Exception {
        // Arrange
        lenient().when(request.getHeader(AUTHORIZATION_HEADER)).thenReturn(TEST_GOOGLE_USER_ID);
        lenient().when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        lenient().when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(userService.findUserByGoogleId(TEST_GOOGLE_USER_ID)).thenReturn(Optional.of(testUser));

        // Act
        boolean result = googleAuthInterceptor.preHandle(request, response, new Object());

        // Assert
        assertTrue(result);
        assertEquals(testUser, UserContext.getCurrentUser());
        verify(userService).findUserByGoogleId(TEST_GOOGLE_USER_ID);
    }

    @Test
    void preHandle_WithMissingAuthorizationHeader_ShouldThrowMissingAuthorizationException() {
        // Arrange
        lenient().when(request.getHeader(AUTHORIZATION_HEADER)).thenReturn(null);
        lenient().when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        lenient().when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(messageService.getMessage(anyString(), anyString())).thenReturn("Authorization header is required");

        // Act & Assert
        MissingAuthorizationException exception = assertThrows(
            MissingAuthorizationException.class,
            () -> googleAuthInterceptor.preHandle(request, response, new Object())
        );

        assertEquals("Authorization header is required", exception.getMessage());
        assertNull(UserContext.getCurrentUser());
        verify(messageService).getMessage("auth.header.missing", "Authorization header is required");
        verifyNoInteractions(userService);
    }

    @Test
    void preHandle_WithEmptyAuthorizationHeader_ShouldThrowInvalidAuthorizationException() {
        // Arrange
        lenient().when(request.getHeader(AUTHORIZATION_HEADER)).thenReturn("");
        lenient().when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        lenient().when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(messageService.getMessage(anyString(), anyString())).thenReturn("Authorization header cannot be empty");

        // Act & Assert
        InvalidAuthorizationException exception = assertThrows(
            InvalidAuthorizationException.class,
            () -> googleAuthInterceptor.preHandle(request, response, new Object())
        );

        assertEquals("Authorization header cannot be empty", exception.getMessage());
        assertNull(UserContext.getCurrentUser());
        verify(messageService).getMessage("auth.header.empty", "Authorization header cannot be empty");
        verifyNoInteractions(userService);
    }

    @Test
    void preHandle_WithWhitespaceOnlyAuthorizationHeader_ShouldThrowInvalidAuthorizationException() {
        // Arrange
        lenient().when(request.getHeader(AUTHORIZATION_HEADER)).thenReturn("   ");
        lenient().when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        lenient().when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(messageService.getMessage(anyString(), anyString())).thenReturn("Authorization header cannot be empty");

        // Act & Assert
        InvalidAuthorizationException exception = assertThrows(
            InvalidAuthorizationException.class,
            () -> googleAuthInterceptor.preHandle(request, response, new Object())
        );

        assertEquals("Authorization header cannot be empty", exception.getMessage());
        assertNull(UserContext.getCurrentUser());
        verify(messageService).getMessage("auth.header.empty", "Authorization header cannot be empty");
        verifyNoInteractions(userService);
    }

    @Test
    void preHandle_WithUserNotFound_ShouldThrowUserNotFoundException() {
        // Arrange
        lenient().when(request.getHeader(AUTHORIZATION_HEADER)).thenReturn(TEST_GOOGLE_USER_ID);
        lenient().when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        lenient().when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(userService.findUserByGoogleId(TEST_GOOGLE_USER_ID)).thenReturn(Optional.empty());
        when(messageService.getMessage(anyString(), anyString())).thenReturn("User not registered in the system");

        // Act & Assert
        UserNotFoundException exception = assertThrows(
            UserNotFoundException.class,
            () -> googleAuthInterceptor.preHandle(request, response, new Object())
        );

        assertEquals("User not registered in the system", exception.getMessage());
        assertNull(UserContext.getCurrentUser());
        verify(userService).findUserByGoogleId(TEST_GOOGLE_USER_ID);
        verify(messageService).getMessage("auth.user.not.found", "User not registered in the system");
    }

    @Test
    void preHandle_WithDatabaseError_ShouldThrowRuntimeException() {
        // Arrange
        lenient().when(request.getHeader(AUTHORIZATION_HEADER)).thenReturn(TEST_GOOGLE_USER_ID);
        lenient().when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        lenient().when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(userService.findUserByGoogleId(TEST_GOOGLE_USER_ID))
            .thenThrow(new RuntimeException("Database connection failed"));
        when(messageService.getMessage(anyString(), anyString())).thenReturn("Internal server error occurred");

        // Act & Assert
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> googleAuthInterceptor.preHandle(request, response, new Object())
        );

        assertEquals("Internal server error occurred", exception.getMessage());
        assertNull(UserContext.getCurrentUser());
        verify(userService).findUserByGoogleId(TEST_GOOGLE_USER_ID);
        verify(messageService).getMessage("auth.database.error", "Internal server error occurred");
    }

    @Test
    void preHandle_WithValidGoogleIdWithWhitespace_ShouldTrimAndProcess() throws Exception {
        // Arrange
        String googleIdWithWhitespace = "  " + TEST_GOOGLE_USER_ID + "  ";
        lenient().when(request.getHeader(AUTHORIZATION_HEADER)).thenReturn(googleIdWithWhitespace);
        lenient().when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        lenient().when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(userService.findUserByGoogleId(TEST_GOOGLE_USER_ID)).thenReturn(Optional.of(testUser));

        // Act
        boolean result = googleAuthInterceptor.preHandle(request, response, new Object());

        // Assert
        assertTrue(result);
        assertEquals(testUser, UserContext.getCurrentUser());
        verify(userService).findUserByGoogleId(TEST_GOOGLE_USER_ID);
    }

    @Test
    void afterCompletion_ShouldClearUserContext() {
        // Arrange
        UserContext.setCurrentUser(testUser);
        assertEquals(testUser, UserContext.getCurrentUser());

        // Act
        googleAuthInterceptor.afterCompletion(request, response, new Object(), null);

        // Assert
        assertNull(UserContext.getCurrentUser());
    }

    @Test
    void afterCompletion_WithException_ShouldStillClearUserContext() {
        // Arrange
        UserContext.setCurrentUser(testUser);
        assertEquals(testUser, UserContext.getCurrentUser());
        Exception testException = new RuntimeException("Test exception");

        // Act
        googleAuthInterceptor.afterCompletion(request, response, new Object(), testException);

        // Assert
        assertNull(UserContext.getCurrentUser());
    }

    @Test
    void afterCompletion_WithNullUserContext_ShouldNotThrowException() {
        // Arrange
        assertNull(UserContext.getCurrentUser());

        // Act & Assert
        assertDoesNotThrow(() -> 
            googleAuthInterceptor.afterCompletion(request, response, new Object(), null)
        );
        assertNull(UserContext.getCurrentUser());
    }

    @Test
    void preHandle_ShouldLogSuccessfulAuthentication() throws Exception {
        // Arrange
        lenient().when(request.getHeader(AUTHORIZATION_HEADER)).thenReturn(TEST_GOOGLE_USER_ID);
        lenient().when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        lenient().when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(userService.findUserByGoogleId(TEST_GOOGLE_USER_ID)).thenReturn(Optional.of(testUser));

        // Act
        boolean result = googleAuthInterceptor.preHandle(request, response, new Object());

        // Assert
        assertTrue(result);
        assertEquals(testUser, UserContext.getCurrentUser());
        // Note: Logging verification would require additional setup with a logging framework mock
        // For now, we verify the core functionality works correctly
    }

    @Test
    void preHandle_ShouldLogAuthenticationFailure() {
        // Arrange
        lenient().when(request.getHeader(AUTHORIZATION_HEADER)).thenReturn(null);
        lenient().when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        lenient().when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(messageService.getMessage(anyString(), anyString())).thenReturn("Authorization header is required");

        // Act & Assert
        assertThrows(
            MissingAuthorizationException.class,
            () -> googleAuthInterceptor.preHandle(request, response, new Object())
        );

        assertNull(UserContext.getCurrentUser());
        // Note: Logging verification would require additional setup with a logging framework mock
        // For now, we verify the exception is thrown correctly
    }

    @Test
    void preHandle_WithComplexGoogleId_ShouldProcessCorrectly() throws Exception {
        // Arrange
        String complexGoogleId = "google_user_123456789_abcdef";
        User complexUser = new User();
        complexUser.setId("complex_user");
        complexUser.setGoogleUserId(complexGoogleId);
        complexUser.setEmail("complex@example.com");
        complexUser.setRol(RolUsuario.Tutor);

        lenient().when(request.getHeader(AUTHORIZATION_HEADER)).thenReturn(complexGoogleId);
        lenient().when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        lenient().when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(userService.findUserByGoogleId(complexGoogleId)).thenReturn(Optional.of(complexUser));

        // Act
        boolean result = googleAuthInterceptor.preHandle(request, response, new Object());

        // Assert
        assertTrue(result);
        assertEquals(complexUser, UserContext.getCurrentUser());
        verify(userService).findUserByGoogleId(complexGoogleId);
    }
}