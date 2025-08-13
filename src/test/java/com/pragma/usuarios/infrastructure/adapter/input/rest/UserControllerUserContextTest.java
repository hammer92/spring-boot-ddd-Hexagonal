package com.pragma.usuarios.infrastructure.adapter.input.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pragma.chapter.domain.model.Chapter;
import com.pragma.shared.context.UserContext;
import com.pragma.usuarios.domain.model.User;
import com.pragma.usuarios.domain.model.enums.RolUsuario;
import com.pragma.usuarios.domain.port.input.CreateUserUseCase;
import com.pragma.usuarios.domain.port.input.FindUserByIdUseCase;
import com.pragma.usuarios.domain.port.input.UpdateTutoringLimitUseCase;
import com.pragma.usuarios.domain.port.input.UpdateUserRoleUseCase;
import com.pragma.usuarios.domain.port.input.UpdateUserUseCase;
import com.pragma.usuarios.infrastructure.adapter.input.rest.dto.CreateUserDto;
import com.pragma.usuarios.infrastructure.adapter.input.rest.dto.UpdateTutoringLimitDto;
import com.pragma.usuarios.infrastructure.adapter.input.rest.dto.UpdateUserRoleDto;
import com.pragma.usuarios.infrastructure.adapter.input.rest.mapper.UserDtoMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests for UserController UserContext integration.
 * Verifies that controllers properly use UserContext for authorization and user information.
 */
@WebMvcTest(UserController.class)
@ActiveProfiles("test")
class UserControllerUserContextTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CreateUserUseCase createUserUseCase;

    @MockBean
    private UpdateUserUseCase updateUserUseCase;

    @MockBean
    private FindUserByIdUseCase findUserByIdUseCase;

    @MockBean
    private UpdateUserRoleUseCase updateUserRoleUseCase;

    @MockBean
    private UpdateTutoringLimitUseCase updateTutoringLimitUseCase;

    @MockBean
    private UserDtoMapper userDtoMapper;

    private User regularUser;
    private User adminUser;
    private Chapter testChapter;

    @BeforeEach
    void setUp() {
        UserContext.clear();
        
        testChapter = new Chapter();
        testChapter.setId("chapter-1");
        testChapter.setName("Test Chapter");
        
        regularUser = new User();
        regularUser.setId("regular-user-id");
        regularUser.setFirstName("John");
        regularUser.setLastName("Doe");
        regularUser.setEmail("john.doe@example.com");
        regularUser.setGoogleUserId("google-123");
        regularUser.setChapter(testChapter);
        regularUser.setRol(RolUsuario.Tutorado);
        regularUser.setActiveTutoringLimit(5);
        
        adminUser = new User();
        adminUser.setId("admin-user-id");
        adminUser.setFirstName("Admin");
        adminUser.setLastName("User");
        adminUser.setEmail("admin@example.com");
        adminUser.setGoogleUserId("google-admin");
        adminUser.setChapter(testChapter);
        adminUser.setRol(RolUsuario.Administrador);
        adminUser.setActiveTutoringLimit(10);
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void getCurrentUser_ShouldReturnCurrentUserFromContext() throws Exception {
        // Given
        UserContext.setCurrentUser(regularUser);
        
        // When & Then
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isOk());
    }

    @Test
    void createUser_ShouldAllowAdminToCreateUser() throws Exception {
        // Given
        UserContext.setCurrentUser(adminUser);
        
        CreateUserDto createUserDto = new CreateUserDto();
        createUserDto.setFirstName("New");
        createUserDto.setLastName("User");
        createUserDto.setEmail("new.user@example.com");
        createUserDto.setGoogleUserId("google-new");
        createUserDto.setChapterId("chapter-1");
        // Role will be set by the service
        
        User newUser = new User();
        newUser.setId("new-user-id");
        newUser.setFirstName("New");
        newUser.setLastName("User");
        newUser.setEmail("new.user@example.com");
        
        when(userDtoMapper.toModel(any(CreateUserDto.class))).thenReturn(newUser);
        when(createUserUseCase.createUser(any(User.class))).thenReturn(newUser);
        
        // When & Then
        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createUserDto)))
                .andExpect(status().isCreated());
    }

    @Test
    void createUser_ShouldForbidRegularUserToCreateUser() throws Exception {
        // Given
        UserContext.setCurrentUser(regularUser);
        
        CreateUserDto createUserDto = new CreateUserDto();
        createUserDto.setFirstName("New");
        createUserDto.setLastName("User");
        createUserDto.setEmail("new.user@example.com");
        
        // When & Then
        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createUserDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateUserRole_ShouldAllowAdminToUpdateRole() throws Exception {
        // Given
        UserContext.setCurrentUser(adminUser);
        
        UpdateUserRoleDto updateRoleDto = new UpdateUserRoleDto();
        updateRoleDto.setId("target-user-id");
        updateRoleDto.setRole(RolUsuario.Tutor);
        
        User updatedUser = new User();
        updatedUser.setId("target-user-id");
        updatedUser.setRol(RolUsuario.Tutor);
        
        when(updateUserRoleUseCase.updateUserRole(eq("target-user-id"), eq(RolUsuario.Tutor)))
                .thenReturn(Optional.of(updatedUser));
        
        // When & Then
        mockMvc.perform(patch("/api/v1/users/role")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRoleDto)))
                .andExpect(status().isOk());
    }

    @Test
    void updateUserRole_ShouldForbidRegularUserToUpdateRole() throws Exception {
        // Given
        UserContext.setCurrentUser(regularUser);
        
        UpdateUserRoleDto updateRoleDto = new UpdateUserRoleDto();
        updateRoleDto.setId("target-user-id");
        updateRoleDto.setRole(RolUsuario.Tutor);
        
        // When & Then
        mockMvc.perform(patch("/api/v1/users/role")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRoleDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateTutoringLimit_ShouldAllowAdminToUpdateLimit() throws Exception {
        // Given
        UserContext.setCurrentUser(adminUser);
        
        UpdateTutoringLimitDto updateLimitDto = new UpdateTutoringLimitDto();
        updateLimitDto.setId("target-user-id");
        updateLimitDto.setActiveTutoringLimit(8);
        
        User updatedUser = new User();
        updatedUser.setId("target-user-id");
        updatedUser.setActiveTutoringLimit(8);
        
        when(updateTutoringLimitUseCase.updateTutoringLimit(eq("target-user-id"), eq(8)))
                .thenReturn(Optional.of(updatedUser));
        
        // When & Then
        mockMvc.perform(patch("/api/v1/users/tutoring-limit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateLimitDto)))
                .andExpect(status().isOk());
    }

    @Test
    void updateTutoringLimit_ShouldForbidRegularUserToUpdateLimit() throws Exception {
        // Given
        UserContext.setCurrentUser(regularUser);
        
        UpdateTutoringLimitDto updateLimitDto = new UpdateTutoringLimitDto();
        updateLimitDto.setId("target-user-id");
        updateLimitDto.setActiveTutoringLimit(8);
        
        // When & Then
        mockMvc.perform(patch("/api/v1/users/tutoring-limit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateLimitDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getUserById_ShouldAllowAnyAuthenticatedUser() throws Exception {
        // Given
        UserContext.setCurrentUser(regularUser);
        
        User targetUser = new User();
        targetUser.setId("target-user-id");
        targetUser.setEmail("target@example.com");
        
        when(findUserByIdUseCase.findUserById("target-user-id"))
                .thenReturn(Optional.of(targetUser));
        
        // When & Then
        mockMvc.perform(get("/api/v1/users/target-user-id"))
                .andExpect(status().isOk());
    }
}