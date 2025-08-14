package com.pragma.shared.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pragma.chapter.domain.model.Chapter;
import com.pragma.shared.config.AuthenticationProperties;
import com.pragma.shared.context.UserContext;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests that verify user context availability in controllers
 * after successful authentication through GoogleAuthInterceptor.
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("integration-test")
class AuthenticationControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

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
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .build();

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
        adminUser.setGoogleUserId("google-admin");
        adminUser.setChapter(testChapter);
        adminUser.setRol(RolUsuario.Administrador);
        adminUser.setActiveTutoringLimit(10);

        // Setup message service responses
        when(messageService.getMessage("auth.header.missing"))
                .thenReturn("Authorization header is required");
        when(messageService.getMessage("auth.user.not.registered"))
                .thenReturn("User not registered in the system");
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
        reset(userService, messageService);
    }

    @Test
    void profileController_WithAuthenticatedUser_ShouldReturnUserProfile() throws Exception {
        // Given
        String googleUserId = "google-123";
        when(userService.findUserByGoogleId(googleUserId)).thenReturn(Optional.of(testUser));

        // When & Then
        MvcResult result = mockMvc.perform(get("/api/v1/profile")
                        .header(authProperties.getHeaderName(), googleUserId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.id").value("test-user-id"))
                .andExpect(jsonPath("$.data.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.data.firstName").value("John"))
                .andExpect(jsonPath("$.data.lastName").value("Doe"))
                .andReturn();

        // Verify user service was called
        verify(userService).findUserByGoogleId(googleUserId);
        
        // Verify the response contains expected user information
        String responseContent = result.getResponse().getContentAsString();
        assertTrue(responseContent.contains("test-user-id"));
        assertTrue(responseContent.contains("john.doe@example.com"));
    }

    @Test
    void profileController_WithAdminUser_ShouldReturnAdminProfile() throws Exception {
        // Given
        String googleUserId = "google-admin";
        when(userService.findUserByGoogleId(googleUserId)).thenReturn(Optional.of(adminUser));

        // When & Then
        mockMvc.perform(get("/api/v1/profile")
                        .header(authProperties.getHeaderName(), googleUserId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value("admin-user-id"))
                .andExpect(jsonPath("$.data.email").value("admin@example.com"))
                .andExpect(jsonPath("$.data.role").value("Administrador"));

        verify(userService).findUserByGoogleId(googleUserId);
    }

    @Test
    void profileController_WithoutAuthentication_ShouldReturn401() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/profile")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Authorization header is required"));

        verify(userService, never()).findUserByGoogleId(anyString());
    }

    @Test
    void skillsController_WithAuthenticatedUser_ShouldAllowAccess() throws Exception {
        // Given
        String googleUserId = "google-123";
        when(userService.findUserByGoogleId(googleUserId)).thenReturn(Optional.of(testUser));

        // When & Then
        mockMvc.perform(get("/api/v1/skills")
                        .header(authProperties.getHeaderName(), googleUserId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(userService).findUserByGoogleId(googleUserId);
    }

    @Test
    void skillsController_AdminOperations_ShouldRequireAdminRole() throws Exception {
        // Test with regular user - should be forbidden for admin operations
        String regularUserGoogleId = "google-123";
        when(userService.findUserByGoogleId(regularUserGoogleId)).thenReturn(Optional.of(testUser));

        mockMvc.perform(post("/api/v1/skills")
                        .header(authProperties.getHeaderName(), regularUserGoogleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Test Skill\"}"))
                .andExpect(status().isForbidden());

        // Test with admin user - should be allowed
        String adminGoogleId = "google-admin";
        when(userService.findUserByGoogleId(adminGoogleId)).thenReturn(Optional.of(adminUser));

        mockMvc.perform(post("/api/v1/skills")
                        .header(authProperties.getHeaderName(), adminGoogleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Test Skill\"}"))
                .andExpect(status().isCreated());

        verify(userService).findUserByGoogleId(regularUserGoogleId);
        verify(userService).findUserByGoogleId(adminGoogleId);
    }

    @Test
    void userController_GetCurrentUser_ShouldReturnAuthenticatedUser() throws Exception {
        // Given
        String googleUserId = "google-123";
        when(userService.findUserByGoogleId(googleUserId)).thenReturn(Optional.of(testUser));

        // When & Then
        mockMvc.perform(get("/api/v1/users/me")
                        .header(authProperties.getHeaderName(), googleUserId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value("test-user-id"))
                .andExpect(jsonPath("$.data.email").value("john.doe@example.com"));

        verify(userService).findUserByGoogleId(googleUserId);
    }

    @Test
    void userController_AdminOperations_ShouldEnforceRoleBasedAccess() throws Exception {
        // Test user creation with regular user - should be forbidden
        String regularUserGoogleId = "google-123";
        when(userService.findUserByGoogleId(regularUserGoogleId)).thenReturn(Optional.of(testUser));

        mockMvc.perform(post("/api/v1/users")
                        .header(authProperties.getHeaderName(), regularUserGoogleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"New\",\"lastName\":\"User\",\"email\":\"new@example.com\"}"))
                .andExpect(status().isForbidden());

        // Test user creation with admin user - should be allowed
        String adminGoogleId = "google-admin";
        when(userService.findUserByGoogleId(adminGoogleId)).thenReturn(Optional.of(adminUser));

        mockMvc.perform(post("/api/v1/users")
                        .header(authProperties.getHeaderName(), adminGoogleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"New\",\"lastName\":\"User\",\"email\":\"new@example.com\"}"))
                .andExpect(status().isCreated());

        verify(userService).findUserByGoogleId(regularUserGoogleId);
        verify(userService).findUserByGoogleId(adminGoogleId);
    }

    @Test
    void multipleControllers_WithSameUser_ShouldMaintainConsistentContext() throws Exception {
        // Given
        String googleUserId = "google-123";
        when(userService.findUserByGoogleId(googleUserId)).thenReturn(Optional.of(testUser));

        // Test multiple endpoints with the same user
        String[] endpoints = {
                "/api/v1/profile",
                "/api/v1/users/me",
                "/api/v1/skills"
        };

        for (String endpoint : endpoints) {
            mockMvc.perform(get(endpoint)
                            .header(authProperties.getHeaderName(), googleUserId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        // Verify user was looked up for each request
        verify(userService, times(endpoints.length)).findUserByGoogleId(googleUserId);
    }

    @Test
    void contextCleanup_AfterRequest_ShouldClearUserContext() throws Exception {
        // Given
        String googleUserId = "google-123";
        when(userService.findUserByGoogleId(googleUserId)).thenReturn(Optional.of(testUser));

        // When - Make a request
        mockMvc.perform(get("/api/v1/profile")
                        .header(authProperties.getHeaderName(), googleUserId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Then - Context should be cleared after request completion
        // Note: This is tested indirectly since MockMvc handles the complete request lifecycle
        assertFalse(UserContext.hasCurrentUser(), 
                "User context should be cleared after request completion");
    }

    @Test
    void errorHandling_WithInvalidUser_ShouldReturnProperErrorResponse() throws Exception {
        // Given
        String invalidGoogleUserId = "invalid-google-id";
        when(userService.findUserByGoogleId(invalidGoogleUserId)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/v1/profile")
                        .header(authProperties.getHeaderName(), invalidGoogleUserId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("User not registered in the system"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(userService).findUserByGoogleId(invalidGoogleUserId);
    }

    @Test
    void userContextHelper_WithAuthenticatedUser_ShouldProvideCorrectInformation() throws Exception {
        // This test verifies that UserContextHelper works correctly with the interceptor
        // by testing endpoints that use UserContextHelper methods
        
        // Given
        String googleUserId = "google-123";
        when(userService.findUserByGoogleId(googleUserId)).thenReturn(Optional.of(testUser));

        // When & Then - Test endpoint that uses UserContextHelper
        mockMvc.perform(get("/api/v1/profile")
                        .header(authProperties.getHeaderName(), googleUserId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(testUser.getId()))
                .andExpect(jsonPath("$.data.email").value(testUser.getEmail()))
                .andExpect(jsonPath("$.data.role").value(testUser.getRol().toString()));

        verify(userService).findUserByGoogleId(googleUserId);
    }

    @Test
    void roleBasedAccess_AcrossMultipleEndpoints_ShouldBeConsistent() throws Exception {
        // Test that role-based access control works consistently across different endpoints
        
        // Given
        String regularUserGoogleId = "google-123";
        String adminGoogleId = "google-admin";
        
        when(userService.findUserByGoogleId(regularUserGoogleId)).thenReturn(Optional.of(testUser));
        when(userService.findUserByGoogleId(adminGoogleId)).thenReturn(Optional.of(adminUser));

        // Test regular user access to read operations (should work)
        mockMvc.perform(get("/api/v1/skills")
                        .header(authProperties.getHeaderName(), regularUserGoogleId))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/users/me")
                        .header(authProperties.getHeaderName(), regularUserGoogleId))
                .andExpect(status().isOk());

        // Test regular user access to admin operations (should fail)
        mockMvc.perform(post("/api/v1/skills")
                        .header(authProperties.getHeaderName(), regularUserGoogleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Test\"}"))
                .andExpect(status().isForbidden());

        // Test admin user access to all operations (should work)
        mockMvc.perform(get("/api/v1/skills")
                        .header(authProperties.getHeaderName(), adminGoogleId))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/skills")
                        .header(authProperties.getHeaderName(), adminGoogleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Test\"}"))
                .andExpect(status().isCreated());

        // Verify all user lookups occurred
        verify(userService, times(3)).findUserByGoogleId(regularUserGoogleId);
        verify(userService, times(2)).findUserByGoogleId(adminGoogleId);
    }
}