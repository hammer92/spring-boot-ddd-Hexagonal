package com.pragma.skills.infrastructure.adapter.input.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pragma.shared.context.UserContext;
import com.pragma.shared.service.MessageService;
import com.pragma.skills.domain.model.Skill;
import com.pragma.skills.domain.port.input.*;
import com.pragma.skills.infrastructure.adapter.input.rest.dto.CreateSkillDto;
import com.pragma.skills.infrastructure.adapter.input.rest.dto.SkillDto;
import com.pragma.skills.infrastructure.adapter.input.rest.dto.UpdateSkillDto;
import com.pragma.skills.infrastructure.adapter.input.rest.mapper.SkillDtoMapper;
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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@WebMvcTest(SkillController.class)
class SkillControllerUserContextTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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

    @MockBean
    private SkillDtoMapper skillDtoMapper;

    @MockBean
    private MessageService messageService;

    private User adminUser;
    private User studentUser;
    private Skill testSkill;
    private SkillDto testSkillDto;
    private CreateSkillDto createSkillDto;
    private UpdateSkillDto updateSkillDto;

    @BeforeEach
    void setUp() {
        Chapter testChapter = new Chapter();
        testChapter.setId("chapter-1");
        testChapter.setName("Test Chapter");

        adminUser = new User();
        adminUser.setId("admin-1");
        adminUser.setFirstName("Admin");
        adminUser.setLastName("User");
        adminUser.setEmail("admin@example.com");
        adminUser.setGoogleUserId("google-admin");
        adminUser.setRol(RolUsuario.Administrador);
        adminUser.setChapter(testChapter);

        studentUser = new User();
        studentUser.setId("student-1");
        studentUser.setFirstName("Student");
        studentUser.setLastName("User");
        studentUser.setEmail("student@example.com");
        studentUser.setGoogleUserId("google-student");
        studentUser.setRol(RolUsuario.Tutorado);
        studentUser.setChapter(testChapter);

        testSkill = new Skill();
        testSkill.setId("skill-1");
        testSkill.setName("Java Programming");

        testSkillDto = new SkillDto();
        testSkillDto.setId("skill-1");
        testSkillDto.setName("Java Programming");

        createSkillDto = new CreateSkillDto("Java Programming");
        updateSkillDto = new UpdateSkillDto("skill-1", "Advanced Java Programming");
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void createSkill_WithAdminUser_ShouldCreateSkill() throws Exception {
        // Given
        UserContext.setCurrentUser(adminUser);
        when(skillDtoMapper.toModel(any(CreateSkillDto.class))).thenReturn(testSkill);
        when(createSkillUseCase.createSkill(any(Skill.class))).thenReturn(testSkill);
        when(skillDtoMapper.toDto(testSkill)).thenReturn(testSkillDto);

        // When & Then
        mockMvc.perform(post("/api/v1/skills")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createSkillDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message", is("Habilidad creada exitosamente")))
                .andExpect(jsonPath("$.data.id", is("skill-1")))
                .andExpect(jsonPath("$.data.name", is("Java Programming")));
    }

    @Test
    void createSkill_WithNonAdminUser_ShouldReturnForbidden() throws Exception {
        // Given
        UserContext.setCurrentUser(studentUser);

        // When & Then
        mockMvc.perform(post("/api/v1/skills")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createSkillDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createSkill_WithoutAuthenticatedUser_ShouldReturnUnauthorized() throws Exception {
        // Given - no user in context

        // When & Then
        mockMvc.perform(post("/api/v1/skills")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createSkillDto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getSkillById_WithAuthenticatedUser_ShouldReturnSkill() throws Exception {
        // Given
        UserContext.setCurrentUser(studentUser);
        when(findSkillUseCase.findSkillById("skill-1")).thenReturn(Optional.of(testSkill));
        when(skillDtoMapper.toDto(testSkill)).thenReturn(testSkillDto);

        // When & Then
        mockMvc.perform(get("/api/v1/skills/skill-1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Habilidad encontrada")))
                .andExpect(jsonPath("$.data.id", is("skill-1")))
                .andExpect(jsonPath("$.data.name", is("Java Programming")));
    }

    @Test
    void getSkillById_WithNonExistentSkill_ShouldReturnNotFound() throws Exception {
        // Given
        UserContext.setCurrentUser(studentUser);
        when(findSkillUseCase.findSkillById("nonexistent")).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/v1/skills/nonexistent")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("Habilidad no encontrada")))
                .andExpect(jsonPath("$.data", is(nullValue())));
    }

    @Test
    void getSkillById_WithoutAuthenticatedUser_ShouldReturnUnauthorized() throws Exception {
        // Given - no user in context

        // When & Then
        mockMvc.perform(get("/api/v1/skills/skill-1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getAllSkills_WithAuthenticatedUser_ShouldReturnAllSkills() throws Exception {
        // Given
        UserContext.setCurrentUser(studentUser);
        Skill skill2 = new Skill();
        skill2.setId("skill-2");
        skill2.setName("Python Programming");
        
        SkillDto skillDto2 = new SkillDto();
        skillDto2.setId("skill-2");
        skillDto2.setName("Python Programming");
        
        List<Skill> skills = Arrays.asList(testSkill, skill2);
        when(getAllSkillsUseCase.getAllSkills()).thenReturn(skills);
        when(skillDtoMapper.toDto(testSkill)).thenReturn(testSkillDto);
        when(skillDtoMapper.toDto(skill2)).thenReturn(skillDto2);

        // When & Then
        mockMvc.perform(get("/api/v1/skills")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Habilidades obtenidas exitosamente")))
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].name", is("Java Programming")))
                .andExpect(jsonPath("$.data[1].name", is("Python Programming")));
    }

    @Test
    void getAllSkills_WithoutAuthenticatedUser_ShouldReturnUnauthorized() throws Exception {
        // Given - no user in context

        // When & Then
        mockMvc.perform(get("/api/v1/skills")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateSkill_WithAdminUser_ShouldUpdateSkill() throws Exception {
        // Given
        UserContext.setCurrentUser(adminUser);
        Skill updatedSkill = new Skill();
        updatedSkill.setId("skill-1");
        updatedSkill.setName("Advanced Java Programming");
        
        SkillDto updatedSkillDto = new SkillDto();
        updatedSkillDto.setId("skill-1");
        updatedSkillDto.setName("Advanced Java Programming");
        
        when(skillDtoMapper.toModel(any(UpdateSkillDto.class))).thenReturn(updatedSkill);
        when(updateSkillUseCase.updateSkill(anyString(), any(Skill.class))).thenReturn(Optional.of(updatedSkill));
        when(skillDtoMapper.toDto(updatedSkill)).thenReturn(updatedSkillDto);

        // When & Then
        mockMvc.perform(put("/api/v1/skills")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateSkillDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Habilidad actualizada exitosamente")))
                .andExpect(jsonPath("$.data.id", is("skill-1")))
                .andExpect(jsonPath("$.data.name", is("Advanced Java Programming")));
    }

    @Test
    void updateSkill_WithNonAdminUser_ShouldReturnForbidden() throws Exception {
        // Given
        UserContext.setCurrentUser(studentUser);

        // When & Then
        mockMvc.perform(put("/api/v1/skills")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateSkillDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateSkill_WithNonExistentSkill_ShouldReturnNotFound() throws Exception {
        // Given
        UserContext.setCurrentUser(adminUser);
        when(skillDtoMapper.toModel(any(UpdateSkillDto.class))).thenReturn(testSkill);
        when(updateSkillUseCase.updateSkill(anyString(), any(Skill.class))).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(put("/api/v1/skills")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateSkillDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("Habilidad no encontrada")))
                .andExpect(jsonPath("$.data", is(nullValue())));
    }

    @Test
    void deleteSkill_WithAdminUser_ShouldDeleteSkill() throws Exception {
        // Given
        UserContext.setCurrentUser(adminUser);
        when(deleteSkillUseCase.deleteSkill("skill-1")).thenReturn(true);

        // When & Then
        mockMvc.perform(delete("/api/v1/skills/skill-1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Habilidad eliminada exitosamente")))
                .andExpect(jsonPath("$.data", is(nullValue())));
    }

    @Test
    void deleteSkill_WithNonAdminUser_ShouldReturnForbidden() throws Exception {
        // Given
        UserContext.setCurrentUser(studentUser);

        // When & Then
        mockMvc.perform(delete("/api/v1/skills/skill-1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteSkill_WithNonExistentSkill_ShouldReturnNotFound() throws Exception {
        // Given
        UserContext.setCurrentUser(adminUser);
        when(deleteSkillUseCase.deleteSkill("nonexistent")).thenReturn(false);

        // When & Then
        mockMvc.perform(delete("/api/v1/skills/nonexistent")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("Habilidad no encontrada")))
                .andExpect(jsonPath("$.data", is(nullValue())));
    }

    @Test
    void deleteSkill_WithoutAuthenticatedUser_ShouldReturnUnauthorized() throws Exception {
        // Given - no user in context

        // When & Then
        mockMvc.perform(delete("/api/v1/skills/skill-1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}