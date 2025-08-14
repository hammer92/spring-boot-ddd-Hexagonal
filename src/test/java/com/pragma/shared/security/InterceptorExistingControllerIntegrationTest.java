package com.pragma.shared.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pragma.chapter.domain.model.Chapter;
import com.pragma.shared.config.AuthenticationProperties;
import com.pragma.shared.context.UserContext;
import com.pragma.shared.service.MessageService;
import com.pragma.skills.domain.model.Skill;
import com.pragma.skills.domain.port.input.*;
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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests that verify GoogleAuthInterceptor works correctly
 * with existing controllers in the application.
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("integration-test")
class InterceptorExistingControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private AuthenticationProperties authProperties;

    @MockBean
    private UserService userService;

    @MockBean
    private MessageService messageService;

    // Mock the skill use cases to test skill controller integration
    @MockBean
    private CreateSkillUseCase createSkillUseCase;

    @MockBean
    private FindSkillUseCase findSkillUseCase;

    @MockBean
    private GetAllSkillsUseCase getAllSkillsUseCase;

    @MockBean
    private UpdateSkillUseCase updateSkillUseCase;

    @MockBean
    private DeleteSkillUseCase deleteSkillUseCase;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;
    private User regularUser;
    private User adminUser;
    private Chapter testChapter;
    private Skill testSkill;

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

        regularUser = new User();
        regularUser.setId("regular-user-id");
        regularUser.setFirstName("John");
        regularUser.setLastName("Doe");
        regularUser.setEmail("john.doe@example.com");
        regularUser.setGoogleUserId("google-regular");
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

        testSkill = new Skill();
        testSkill.setId("skill-1");
        testSkill.setName("Java Programming");

        // Setup message service responses
        when(messageService.getMessage("auth.header.missing"))
                .thenReturn("Authorization header is required");
        when(messageService.getMessage("auth.user.not.registered"))
                .thenReturn("User not registered in the system");
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
        reset(userService, messageService, createSkillUseCase, findSkillUseCase, 
              getAllSkillsUseCase, updateSkillUseCase, deleteSkillUseCase);
    }

    @Test
    void skillController_GetAllSkills_WithAuthenticatedUser_ShouldWork() throws Exception {
        // Given
        String googleUserId = "google-regular";
        List<Skill> skills = Arrays.asList(testSkill);
        
        when(userService.findUserByGoogleId(googleUserId)).thenReturn(Optional.of(regularUser));
        when(getAllSkillsUseCase.getAllSkills()).thenReturn(skills);

        // When & Then
        mockMvc.perform(get("/api/v1/skills")
                        .header(authProperties.getHeaderName(), googleUserId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.data").isArray());

        // Verify authentication and business logic were called
        verify(userService).findUserByGoogleId(googleUserId);
        verify(getAllSkillsUseCase).getAllSkills();
    }

    @Test
    void skillController_GetAllSkills_WithoutAuthentication_ShouldReturn401() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/skills")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Authorization header is required"));

        // Verify business logic was not called
        verify(userService, never()).findUserByGoogleId(anyString());
        verify(getAllSkillsUseCase, never()).getAllSkills();
    }

    @Test
    void skillController_GetSkillById_WithAuthenticatedUser_ShouldWork() throws Exception {
        // Given
        String googleUserId = "google-regular";
        String skillId = "skill-1";
        
        when(userService.findUserByGoogleId(googleUserId)).thenReturn(Optional.of(regularUser));
        when(findSkillUseCase.findSkillById(skillId)).thenReturn(Optional.of(testSkill));

        // When & Then
        mockMvc.perform(get("/api/v1/skills/" + skillId)
                        .header(authProperties.getHeaderName(), googleUserId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.data").exists());

        // Verify authentication and business logic were called
        verify(userService).findUserByGoogleId(googleUserId);
        verify(findSkillUseCase).findSkillById(skillId);
    }

    @Test
    void skillController_CreateSkill_WithRegularUser_ShouldReturn403() throws Exception {
        // Given
        String googleUserId = "google-regular";
        String skillJson = "{\"name\":\"New Skill\"}";
        
        when(userService.findUserByGoogleId(googleUserId)).thenReturn(Optional.of(regularUser));

        // When & Then
        mockMvc.perform(post("/api/v1/skills")
                        .header(authProperties.getHeaderName(), googleUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(skillJson))
                .andExpect(status().isForbidden());

        // Verify authentication was called but business logic was not
        verify(userService).findUserByGoogleId(googleUserId);
        verify(createSkillUseCase, never()).createSkill(any(Skill.class));
    }

    @Test
    void skillController_CreateSkill_WithAdminUser_ShouldWork() throws Exception {
        // Given
        String googleUserId = "google-admin";
        String skillJson = "{\"name\":\"New Skill\"}";
        
        when(userService.findUserByGoogleId(googleUserId)).thenReturn(Optional.of(adminUser));
        when(createSkillUseCase.createSkill(any(Skill.class))).thenReturn(testSkill);

        // When & Then
        mockMvc.perform(post("/api/v1/skills")
                        .header(authProperties.getHeaderName(), googleUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(skillJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.data").exists());

        // Verify both authentication and business logic were called
        verify(userService).findUserByGoogleId(googleUserId);
        verify(createSkillUseCase).createSkill(any(Skill.class));
    }

    @Test
    void skillController_UpdateSkill_WithAdminUser_ShouldWork() throws Exception {
        // Given
        String googleUserId = "google-admin";
        String skillJson = "{\"id\":\"skill-1\",\"name\":\"Updated Skill\"}";
        
        when(userService.findUserByGoogleId(googleUserId)).thenReturn(Optional.of(adminUser));
        when(updateSkillUseCase.updateSkill(anyString(), any(Skill.class))).thenReturn(Optional.of(testSkill));

        // When & Then
        mockMvc.perform(put("/api/v1/skills")
                        .header(authProperties.getHeaderName(), googleUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(skillJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.data").exists());

        // Verify both authentication and business logic were called
        verify(userService).findUserByGoogleId(googleUserId);
        verify(updateSkillUseCase).updateSkill(anyString(), any(Skill.class));
    }

    @Test
    void skillController_DeleteSkill_WithRegularUser_ShouldReturn403() throws Exception {
        // Given
        String googleUserId = "google-regular";
        String skillId = "skill-1";
        
        when(userService.findUserByGoogleId(googleUserId)).thenReturn(Optional.of(regularUser));

        // When & Then
        mockMvc.perform(delete("/api/v1/skills/" + skillId)
                        .header(authProperties.getHeaderName(), googleUserId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        // Verify authentication was called but business logic was not
        verify(userService).findUserByGoogleId(googleUserId);
        verify(deleteSkillUseCase, never()).deleteSkill(anyString());
    }

    @Test
    void skillController_DeleteSkill_WithAdminUser_ShouldWork() throws Exception {
        // Given
        String googleUserId = "google-admin";
        String skillId = "skill-1";
        
        when(userService.findUserByGoogleId(googleUserId)).thenReturn(Optional.of(adminUser));
        when(deleteSkillUseCase.deleteSkill(skillId)).thenReturn(true);

        // When & Then
        mockMvc.perform(delete("/api/v1/skills/" + skillId)
                        .header(authProperties.getHeaderName(), googleUserId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists());

        // Verify both authentication and business logic were called
        verify(userService).findUserByGoogleId(googleUserId);
        verify(deleteSkillUseCase).deleteSkill(skillId);
    }

    @Test
    void userController_GetCurrentUser_WithAuthenticatedUser_ShouldWork() throws Exception {
        // Given
        String googleUserId = "google-regular";
        
        when(userService.findUserByGoogleId(googleUserId)).thenReturn(Optional.of(regularUser));

        // When & Then
        mockMvc.perform(get("/api/v1/users/me")
                        .header(authProperties.getHeaderName(), googleUserId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.id").value("regular-user-id"))
                .andExpect(jsonPath("$.data.email").value("john.doe@example.com"));

        // Verify authentication was called
        verify(userService).findUserByGoogleId(googleUserId);
    }

    @Test
    void userController_CreateUser_WithRegularUser_ShouldReturn403() throws Exception {
        // Given
        String googleUserId = "google-regular";
        String userJson = "{\"firstName\":\"New\",\"lastName\":\"User\",\"email\":\"new@example.com\"}";
        
        when(userService.findUserByGoogleId(googleUserId)).thenReturn(Optional.of(regularUser));

        // When & Then
        mockMvc.perform(post("/api/v1/users")
                        .header(authProperties.getHeaderName(), googleUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isForbidden());

        // Verify authentication was called
        verify(userService).findUserByGoogleId(googleUserId);
    }

    @Test
    void userController_CreateUser_WithAdminUser_ShouldWork() throws Exception {
        // Given
        String googleUserId = "google-admin";
        String userJson = "{\"firstName\":\"New\",\"lastName\":\"User\",\"email\":\"new@example.com\",\"googleUserId\":\"google-new\",\"chapterId\":\"chapter-1\"}";
        
        when(userService.findUserByGoogleId(googleUserId)).thenReturn(Optional.of(adminUser));

        // When & Then
        mockMvc.perform(post("/api/v1/users")
                        .header(authProperties.getHeaderName(), googleUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isCreated());

        // Verify authentication was called
        verify(userService).findUserByGoogleId(googleUserId);
    }

    @Test
    void multipleControllers_WithSameAuthentication_ShouldWorkConsistently() throws Exception {
        // Given
        String googleUserId = "google-regular";
        
        when(userService.findUserByGoogleId(googleUserId)).thenReturn(Optional.of(regularUser));
        when(getAllSkillsUseCase.getAllSkills()).thenReturn(Arrays.asList(testSkill));

        // Test multiple controllers with the same authentication
        
        // Skills controller
        mockMvc.perform(get("/api/v1/skills")
                        .header(authProperties.getHeaderName(), googleUserId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // User controller
        mockMvc.perform(get("/api/v1/users/me")
                        .header(authProperties.getHeaderName(), googleUserId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Profile controller
        mockMvc.perform(get("/api/v1/profile")
                        .header(authProperties.getHeaderName(), googleUserId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Verify authentication was called for each request
        verify(userService, times(3)).findUserByGoogleId(googleUserId);
        verify(getAllSkillsUseCase).getAllSkills();
    }

    @Test
    void errorScenarios_WithExistingControllers_ShouldHandleGracefully() throws Exception {
        // Test missing authentication
        mockMvc.perform(get("/api/v1/skills")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Authorization header is required"));

        // Test invalid user
        String invalidGoogleUserId = "invalid-google-id";
        when(userService.findUserByGoogleId(invalidGoogleUserId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/skills")
                        .header(authProperties.getHeaderName(), invalidGoogleUserId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("User not registered in the system"));

        // Verify appropriate service calls
        verify(userService, never()).findUserByGoogleId(""); // No call for missing header
        verify(userService).findUserByGoogleId(invalidGoogleUserId);
        verify(getAllSkillsUseCase, never()).getAllSkills(); // Business logic not called on auth failure
    }

    @Test
    void contextIsolation_BetweenRequests_ShouldMaintainSeparation() throws Exception {
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

        // When & Then - Make requests with different users
        mockMvc.perform(get("/api/v1/users/me")
                        .header(authProperties.getHeaderName(), googleUserId1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value("user-1"));

        mockMvc.perform(get("/api/v1/users/me")
                        .header(authProperties.getHeaderName(), googleUserId2)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value("user-2"));

        // Verify both users were looked up correctly
        verify(userService).findUserByGoogleId(googleUserId1);
        verify(userService).findUserByGoogleId(googleUserId2);
    }
}