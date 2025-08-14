package com.pragma.shared.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pragma.shared.context.UserContext;
import com.pragma.shared.context.UserContextHelper;
import com.pragma.shared.service.MessageService;
import com.pragma.usuarios.domain.model.User;
import com.pragma.usuarios.domain.model.enums.RolUsuario;
import com.pragma.chapter.domain.model.Chapter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@WebMvcTest(ProfileController.class)
class ProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MessageService messageService;

    private User testUser;
    private User adminUser;
    private Chapter testChapter;

    @BeforeEach
    void setUp() {
        testChapter = new Chapter();
        testChapter.setId("chapter-1");
        testChapter.setName("Test Chapter");

        testUser = new User();
        testUser.setId("user-1");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setEmail("john.doe@example.com");
        testUser.setGoogleUserId("google-123");
        testUser.setRol(RolUsuario.Tutorado);
        testUser.setChapter(testChapter);
        testUser.setActiveTutoringLimit(3);

        adminUser = new User();
        adminUser.setId("admin-1");
        adminUser.setFirstName("Admin");
        adminUser.setLastName("User");
        adminUser.setEmail("admin@example.com");
        adminUser.setGoogleUserId("google-admin");
        adminUser.setRol(RolUsuario.Administrador);
        adminUser.setChapter(testChapter);
        adminUser.setActiveTutoringLimit(10);
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void getCurrentUserProfile_WithAuthenticatedUser_ShouldReturnProfile() throws Exception {
        // Given
        UserContext.setCurrentUser(testUser);

        // When & Then
        mockMvc.perform(get("/api/v1/profile/me")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Perfil obtenido exitosamente")))
                .andExpect(jsonPath("$.data.id", is("user-1")))
                .andExpect(jsonPath("$.data.firstName", is("John")))
                .andExpect(jsonPath("$.data.lastName", is("Doe")))
                .andExpect(jsonPath("$.data.email", is("john.doe@example.com")))
                .andExpect(jsonPath("$.data.role", is("Tutorado")))
                .andExpect(jsonPath("$.data.chapterId", is("chapter-1")))
                .andExpect(jsonPath("$.data.activeTutoringLimit", is(3)));
    }

    @Test
    void getCurrentUserProfile_WithoutAuthenticatedUser_ShouldReturnUnauthorized() throws Exception {
        // Given - no user in context

        // When & Then
        mockMvc.perform(get("/api/v1/profile/me")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", is("Usuario no autenticado")))
                .andExpect(jsonPath("$.data", is(nullValue())));
    }

    @Test
    void getUserPermissions_WithStudentUser_ShouldReturnCorrectPermissions() throws Exception {
        // Given
        UserContext.setCurrentUser(testUser);

        // When & Then
        mockMvc.perform(get("/api/v1/profile/permissions")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Permisos obtenidos exitosamente")))
                .andExpect(jsonPath("$.data.isAdmin", is(false)))
                .andExpect(jsonPath("$.data.canActAsTutor", is(false)))
                .andExpect(jsonPath("$.data.canRequestTutoring", is(true)))
                .andExpect(jsonPath("$.data.hasStudentRole", is(true)))
                .andExpect(jsonPath("$.data.hasTutorRole", is(false)))
                .andExpect(jsonPath("$.data.hasAdminRole", is(false)));
    }

    @Test
    void getUserPermissions_WithAdminUser_ShouldReturnCorrectPermissions() throws Exception {
        // Given
        UserContext.setCurrentUser(adminUser);

        // When & Then
        mockMvc.perform(get("/api/v1/profile/permissions")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Permisos obtenidos exitosamente")))
                .andExpect(jsonPath("$.data.isAdmin", is(true)))
                .andExpect(jsonPath("$.data.canActAsTutor", is(true)))
                .andExpect(jsonPath("$.data.canRequestTutoring", is(true)))
                .andExpect(jsonPath("$.data.hasStudentRole", is(false)))
                .andExpect(jsonPath("$.data.hasTutorRole", is(false)))
                .andExpect(jsonPath("$.data.hasAdminRole", is(true)));
    }

    @Test
    void getContextStatus_WithAuthenticatedUser_ShouldReturnContextInfo() throws Exception {
        // Given
        UserContext.setCurrentUser(testUser);

        // When & Then
        mockMvc.perform(get("/api/v1/profile/context-status")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Estado del contexto obtenido")))
                .andExpect(jsonPath("$.data.hasAuthenticatedUser", is(true)))
                .andExpect(jsonPath("$.data.userId", is("user-1")))
                .andExpect(jsonPath("$.data.userEmail", is("john.doe@example.com")))
                .andExpect(jsonPath("$.data.userRole", is("Tutorado")))
                .andExpect(jsonPath("$.data.contextAvailable", is(true)));
    }

    @Test
    void getContextStatus_WithoutAuthenticatedUser_ShouldReturnNoContextInfo() throws Exception {
        // Given - no user in context

        // When & Then
        mockMvc.perform(get("/api/v1/profile/context-status")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Estado del contexto obtenido")))
                .andExpect(jsonPath("$.data.hasAuthenticatedUser", is(false)))
                .andExpect(jsonPath("$.data.contextAvailable", is(false)))
                .andExpect(jsonPath("$.data.message", is("No authenticated user in context")));
    }

    @Test
    void getAdminInfo_WithAdminUser_ShouldReturnAdminInfo() throws Exception {
        // Given
        UserContext.setCurrentUser(adminUser);

        // When & Then
        mockMvc.perform(get("/api/v1/profile/admin-info")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Información de administrador obtenida")))
                .andExpect(jsonPath("$.data.adminUserId", is("admin-1")))
                .andExpect(jsonPath("$.data.adminEmail", is("admin@example.com")))
                .andExpect(jsonPath("$.data.logInfo", containsString("admin@example.com")))
                .andExpect(jsonPath("$.data.accessTime", notNullValue()));
    }

    @Test
    void getAdminInfo_WithNonAdminUser_ShouldReturnForbidden() throws Exception {
        // Given
        UserContext.setCurrentUser(testUser);

        // When & Then
        mockMvc.perform(get("/api/v1/profile/admin-info")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", is("Acceso denegado: se requieren privilegios de administrador")))
                .andExpect(jsonPath("$.data", is(nullValue())));
    }

    @Test
    void getAdminInfo_WithoutAuthenticatedUser_ShouldReturnUnauthorized() throws Exception {
        // Given - no user in context

        // When & Then
        mockMvc.perform(get("/api/v1/profile/admin-info")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", is("Usuario no autenticado")))
                .andExpect(jsonPath("$.data", is(nullValue())));
    }

    @Test
    void checkUserResourceAccess_WithOwnResource_ShouldAllowAccess() throws Exception {
        // Given
        UserContext.setCurrentUser(testUser);

        // When & Then
        mockMvc.perform(get("/api/v1/profile/user/user-1/access-check")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Verificación de acceso completada")))
                .andExpect(jsonPath("$.data.requestedUserId", is("user-1")))
                .andExpect(jsonPath("$.data.currentUserId", is("user-1")))
                .andExpect(jsonPath("$.data.canAccess", is(true)))
                .andExpect(jsonPath("$.data.isOwnResource", is(true)))
                .andExpect(jsonPath("$.data.isAdmin", is(false)))
                .andExpect(jsonPath("$.data.accessValidation", is("GRANTED")));
    }

    @Test
    void checkUserResourceAccess_WithOtherUserResource_ShouldDenyAccess() throws Exception {
        // Given
        UserContext.setCurrentUser(testUser);

        // When & Then
        mockMvc.perform(get("/api/v1/profile/user/other-user/access-check")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Verificación de acceso completada")))
                .andExpect(jsonPath("$.data.requestedUserId", is("other-user")))
                .andExpect(jsonPath("$.data.currentUserId", is("user-1")))
                .andExpect(jsonPath("$.data.canAccess", is(false)))
                .andExpect(jsonPath("$.data.isOwnResource", is(false)))
                .andExpect(jsonPath("$.data.isAdmin", is(false)))
                .andExpect(jsonPath("$.data.accessValidation", is("DENIED")));
    }

    @Test
    void checkUserResourceAccess_WithAdminUser_ShouldAllowAccessToAnyResource() throws Exception {
        // Given
        UserContext.setCurrentUser(adminUser);

        // When & Then
        mockMvc.perform(get("/api/v1/profile/user/any-user/access-check")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Verificación de acceso completada")))
                .andExpect(jsonPath("$.data.requestedUserId", is("any-user")))
                .andExpect(jsonPath("$.data.currentUserId", is("admin-1")))
                .andExpect(jsonPath("$.data.canAccess", is(true)))
                .andExpect(jsonPath("$.data.isOwnResource", is(false)))
                .andExpect(jsonPath("$.data.isAdmin", is(true)))
                .andExpect(jsonPath("$.data.accessValidation", is("GRANTED")));
    }

    @Test
    void getPublicInfo_WithAuthenticatedUser_ShouldIncludeUserInfo() throws Exception {
        // Given
        UserContext.setCurrentUser(testUser);

        // When & Then
        mockMvc.perform(get("/api/v1/profile/public-info")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Información pública obtenida")))
                .andExpect(jsonPath("$.data.endpoint", is("public-info")))
                .andExpect(jsonPath("$.data.timestamp", notNullValue()))
                .andExpect(jsonPath("$.data.authenticatedUser", is("john.doe@example.com")))
                .andExpect(jsonPath("$.data.userRole", is("Tutorado")));
    }

    @Test
    void getPublicInfo_WithoutAuthenticatedUser_ShouldWorkWithoutUserInfo() throws Exception {
        // Given - no user in context

        // When & Then
        mockMvc.perform(get("/api/v1/profile/public-info")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Información pública obtenida")))
                .andExpect(jsonPath("$.data.endpoint", is("public-info")))
                .andExpect(jsonPath("$.data.timestamp", notNullValue()))
                .andExpect(jsonPath("$.data.authenticatedUser", is(nullValue())))
                .andExpect(jsonPath("$.data.message", is("Accessed without authentication")));
    }
}