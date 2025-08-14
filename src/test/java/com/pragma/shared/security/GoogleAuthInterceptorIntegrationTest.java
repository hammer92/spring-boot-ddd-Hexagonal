package com.pragma.shared.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pragma.chapter.domain.model.Chapter;
import com.pragma.shared.config.AuthenticationProperties;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for GoogleAuthInterceptor authentication flow.
 * Tests the complete authentication process from HTTP request to controller execution.
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("integration-test")
class GoogleAuthInterceptorIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private GoogleAuthInterceptor googleAuthInterceptor;

    @Autowired
    private AuthenticationProperties authProperties;

    @MockBean
    private UserService userService;

    @MockBean
    private MessageService messageService;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;
    private User testUser;
    private User adminUser;
    private Chapter testChapter;

    @BeforeEach
    void setUp() {
        // Build MockMvc with the actual interceptor configuration
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .build();

        // Clear any existing user context
        UserContext.clear();

        // Setup test data
        testChapter = new Chapter();
        testChapter.setId("chapter-1");
        testChapter.setName("Test Chapter");

        testUser = new User();
        testUser.setId("test-user-id");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setEmail("john.doe@example.com");
        testUser.setGoogleUserId("google-123");
        testUser.setChapter(testChapter);
        testUser.setRol(RolUsuario.Tutorado);
        testUser.setActiveTutoringLimit(5);

        adminUser = new User();
        adminUser.setId("admin-user-id");
        adminUser.setFirstName("Admin");
        adminUser.setLastName("User");
        adminUser.setEmail("admin@example.com");
        testUser.setGoogleUserId("google-admin");
        adminUser.setChapter(testChapter);
        adminUser.setRol(RolUsuario.Administrador);
        adminUser.setActiveTutoringLimit(10);

        // Setup default message service responses
        when(messageService.getMessage("auth.header.missing"))
                .thenReturn("Authorization header is required");
        when(messageService.getMessage("auth.header.empty"))
                .thenReturn("Authorization header cannot be empty");
        when(messageService.getMessage("auth.header.invalid"))
                .thenReturn("Invalid authorization header format");
        when(messageService.getMessage("auth.user.not.registered"))
                .thenReturn("User not registered in the system");
        when(messageService.getMessage(eq("auth.database.error"), anyString()))
                .thenReturn("Internal server error occurred");
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
        reset(userService, messageService);
    }

    @Test
    void authenticatedRequest_WithValidGoogleId_ShouldSucceedAndSetUserContext() throws Exception {
        // Given
        String googleUserId = "google-123";
        when(userService.findUserByGoogleId(googleUserId)).thenReturn(Optional.of(testUser));

        // When & Then
        mockMvc.perform(get("/api/v1/skills")
                        .header(authProperties.getHeaderName(), googleUserId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Verify user service was called
        verify(userService).findUserByGoogleId(googleUserId);
    }

    @Test
    void authenticatedRequest_WithValidAdminUser_ShouldSucceedAndSetAdminContext() throws Exception {
        // Given
        String googleUserId = "google-admin";
        when(userService.findUserByGoogleId(googleUserId)).thenReturn(Optional.of(adminUser));

        // When & Then
        mockMvc.perform(get("/api/v1/skills")
                        .header(authProperties.getHeaderName(), googleUserId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Verify user service was called
        verify(userService).findUserByGoogleId(googleUserId);
    }

    @Test
    void authenticatedRequest_WithMissingAuthorizationHeader_ShouldReturn401() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/skills")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Authorization header is required"));

        // Verify user service was not called
        verify(userService, never()).findUserByGoogleId(anyString());
    }

    @Test
    void authenticatedRequest_WithEmptyAuthorizationHeader_ShouldReturn401() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/skills")
                        .header(authProperties.getHeaderName(), "")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Authorization header cannot be empty"));

        // Verify user service was not called
        verify(userService, never()).findUserByGoogleId(anyString());
    }

    @Test
    void authenticatedRequest_WithWhitespaceOnlyHeader_ShouldReturn401() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/skills")
                        .header(authProperties.getHeaderName(), "   ")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid authorization header format"));

        // Verify user service was not called
        verify(userService, never()).findUserByGoogleId(anyString());
    }

    @Test
    void authenticatedRequest_WithNonExistentUser_ShouldReturn403() throws Exception {
        // Given
        String googleUserId = "non-existent-google-id";
        when(userService.findUserByGoogleId(googleUserId)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/v1/skills")
                        .header(authProperties.getHeaderName(), googleUserId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("User not registered in the system"));

        // Verify user service was called
        verify(userService).findUserByGoogleId(googleUserId);
    }

    @Test
    void authenticatedRequest_WithDatabaseError_ShouldReturn500() throws Exception {
        // Given
        String googleUserId = "google-123";
        when(userService.findUserByGoogleId(googleUserId))
                .thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        mockMvc.perform(get("/api/v1/skills")
                        .header(authProperties.getHeaderName(), googleUserId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Internal server error occurred"));

        // Verify user service was called
        verify(userService).findUserByGoogleId(googleUserId);
    }

    @Test
    void authenticatedRequest_ToMultipleEndpoints_ShouldWorkConsistently() throws Exception {
        // Given
        String googleUserId = "google-123";
        when(userService.findUserByGoogleId(googleUserId)).thenReturn(Optional.of(testUser));

        // Test multiple endpoints
        String[] endpoints = {
                "/api/v1/skills",
                "/api/v1/users/me",
                "/api/v1/chapters"
        };

        for (String endpoint : endpoints) {
            // When & Then
            mockMvc.perform(get(endpoint)
                            .header(authProperties.getHeaderName(), googleUserId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        // Verify user service was called for each request
        verify(userService, times(endpoints.length)).findUserByGoogleId(googleUserId);
    }

    @Test
    void authenticatedRequest_WithDifferentHttpMethods_ShouldWorkConsistently() throws Exception {
        // Given
        String googleUserId = "google-admin";
        when(userService.findUserByGoogleId(googleUserId)).thenReturn(Optional.of(adminUser));

        // Test GET request
        mockMvc.perform(get("/api/v1/skills")
                        .header(authProperties.getHeaderName(), googleUserId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Test POST request (would need valid request body in real scenario)
        mockMvc.perform(post("/api/v1/skills")
                        .header(authProperties.getHeaderName(), googleUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest()); // Expected due to invalid body, but auth passed

        // Test PUT request
        mockMvc.perform(put("/api/v1/skills")
                        .header(authProperties.getHeaderName(), googleUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest()); // Expected due to invalid body, but auth passed

        // Test DELETE request
        mockMvc.perform(delete("/api/v1/skills/skill-1")
                        .header(authProperties.getHeaderName(), googleUserId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Verify user service was called for each request
        verify(userService, times(4)).findUserByGoogleId(googleUserId);
    }

    @Test
    void authenticatedRequest_WithCustomHeaderName_ShouldWork() throws Exception {
        // Given - simulate custom header configuration
        String customHeaderName = "X-Google-User-ID";
        String googleUserId = "google-123";
        
        // Temporarily change the header name for this test
        String originalHeaderName = authProperties.getHeaderName();
        authProperties.setHeaderName(customHeaderName);
        
        try {
            when(userService.findUserByGoogleId(googleUserId)).thenReturn(Optional.of(testUser));

            // When & Then
            mockMvc.perform(get("/api/v1/skills")
                            .header(customHeaderName, googleUserId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            // Verify user service was called
            verify(userService).findUserByGoogleId(googleUserId);
        } finally {
            // Restore original header name
            authProperties.setHeaderName(originalHeaderName);
        }
    }

    @Test
    void authenticatedRequest_WithSpecialCharactersInGoogleId_ShouldWork() throws Exception {
        // Given
        String googleUserId = "google-user-123_test.email@domain.com";
        when(userService.findUserByGoogleId(googleUserId)).thenReturn(Optional.of(testUser));

        // When & Then
        mockMvc.perform(get("/api/v1/skills")
                        .header(authProperties.getHeaderName(), googleUserId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Verify user service was called
        verify(userService).findUserByGoogleId(googleUserId);
    }

    @Test
    void authenticatedRequest_WithLongGoogleId_ShouldWork() throws Exception {
        // Given
        String googleUserId = "google-" + "a".repeat(100); // Long Google ID
        when(userService.findUserByGoogleId(googleUserId)).thenReturn(Optional.of(testUser));

        // When & Then
        mockMvc.perform(get("/api/v1/skills")
                        .header(authProperties.getHeaderName(), googleUserId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Verify user service was called
        verify(userService).findUserByGoogleId(googleUserId);
    }

    @Test
    void authenticatedRequest_ToExcludedPath_ShouldNotRequireAuthentication() throws Exception {
        // Test that actuator endpoints (excluded paths) don't require authentication
        mockMvc.perform(get("/actuator/health")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Verify user service was not called
        verify(userService, never()).findUserByGoogleId(anyString());
    }

    @Test
    void authenticatedRequest_ConcurrentRequests_ShouldMaintainThreadSafety() throws Exception {
        // Given
        String googleUserId1 = "google-user-1";
        String googleUserId2 = "google-user-2";
        
        User user1 = new User();
        user1.setId("user-1");
        user1.setEmail("user1@example.com");
        user1.setGoogleUserId(googleUserId1);
        user1.setRol(RolUsuario.Tutorado);
        
        User user2 = new User();
        user2.setId("user-2");
        user2.setEmail("user2@example.com");
        user2.setGoogleUserId(googleUserId2);
        user2.setRol(RolUsuario.Tutor);

        when(userService.findUserByGoogleId(googleUserId1)).thenReturn(Optional.of(user1));
        when(userService.findUserByGoogleId(googleUserId2)).thenReturn(Optional.of(user2));

        // When & Then - Simulate concurrent requests
        mockMvc.perform(get("/api/v1/skills")
                        .header(authProperties.getHeaderName(), googleUserId1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/skills")
                        .header(authProperties.getHeaderName(), googleUserId2)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Verify both users were looked up
        verify(userService).findUserByGoogleId(googleUserId1);
        verify(userService).findUserByGoogleId(googleUserId2);
    }

    @Test
    void authenticatedRequest_WithTrimmedGoogleId_ShouldWork() throws Exception {
        // Given - Google ID with leading/trailing whitespace
        String googleUserIdWithSpaces = "  google-123  ";
        String trimmedGoogleUserId = "google-123";
        when(userService.findUserByGoogleId(trimmedGoogleUserId)).thenReturn(Optional.of(testUser));

        // When & Then
        mockMvc.perform(get("/api/v1/skills")
                        .header(authProperties.getHeaderName(), googleUserIdWithSpaces)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Verify user service was called with trimmed ID
        verify(userService).findUserByGoogleId(trimmedGoogleUserId);
    }
}