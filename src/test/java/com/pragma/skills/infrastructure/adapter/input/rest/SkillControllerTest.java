package com.pragma.skills.infrastructure.adapter.input.rest;

import com.pragma.shared.context.UserContextHelper;
import com.pragma.shared.dto.OkResponseDto;
import com.pragma.skills.domain.model.Skill;
import com.pragma.skills.domain.port.input.*;
import com.pragma.skills.infrastructure.adapter.input.rest.dto.CreateSkillDto;
import com.pragma.skills.infrastructure.adapter.input.rest.dto.SkillDto;
import com.pragma.skills.infrastructure.adapter.input.rest.dto.UpdateSkillDto;
import com.pragma.skills.infrastructure.adapter.input.rest.mapper.SkillDtoMapper;
import com.pragma.usuarios.domain.model.User;
import com.pragma.usuarios.domain.model.enums.RolUsuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

class SkillControllerTest {

    @Mock
    private CreateSkillUseCase createSkillUseCase;

    @Mock
    private FindSkillUseCase findSkillUseCase;

    @Mock
    private GetAllSkillsUseCase getAllSkillsUseCase;

    @Mock
    private UpdateSkillUseCase updateSkillUseCase;

    @Mock
    private DeleteSkillUseCase deleteSkillUseCase;

    @Mock
    private SkillDtoMapper skillDtoMapper;

    @InjectMocks
    private SkillController skillController;

    private Skill testSkill;
    private SkillDto testSkillDto;
    private User testUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testSkill = new Skill();
        testSkill.setId("1");
        testSkill.setName("Java");

        testSkillDto = new SkillDto();
        testSkillDto.setId("1");
        testSkillDto.setName("Java");

        testUser = new User();
        testUser.setId("user1");
        testUser.setEmail("admin@test.com");
        testUser.setRol(RolUsuario.Administrador);
    }

    @Test
    void createSkill_ShouldReturnCreatedSkill() {
        // Arrange
        CreateSkillDto createSkillDto = new CreateSkillDto("Java");
        
        try (MockedStatic<UserContextHelper> mockedUserContext = mockStatic(UserContextHelper.class)) {
            mockedUserContext.when(UserContextHelper::getCurrentUserOrThrow).thenReturn(testUser);
            mockedUserContext.when(UserContextHelper::requireAdminRole).thenAnswer(invocation -> null);
            
            when(skillDtoMapper.toModel(createSkillDto)).thenReturn(testSkill);
            when(createSkillUseCase.createSkill(any(Skill.class))).thenReturn(testSkill);
            when(skillDtoMapper.toDto(testSkill)).thenReturn(testSkillDto);

            // Act
            ResponseEntity<OkResponseDto<SkillDto>> response = skillController.createSkill(createSkillDto);

            // Assert
            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertNotNull(response.getBody());
            assertNotNull(response.getBody().getData());
            assertEquals("1", response.getBody().getData().getId());
            assertEquals("Java", response.getBody().getData().getName());
        }
    }

    @Test
    void getSkillById_WhenSkillExists_ShouldReturnSkill() {
        // Arrange
        try (MockedStatic<UserContextHelper> mockedUserContext = mockStatic(UserContextHelper.class)) {
            mockedUserContext.when(UserContextHelper::getCurrentUserOrThrow).thenReturn(testUser);
            
            when(findSkillUseCase.findSkillById("1")).thenReturn(Optional.of(testSkill));
            when(skillDtoMapper.toDto(testSkill)).thenReturn(testSkillDto);

            // Act
            ResponseEntity<OkResponseDto<SkillDto>> response = skillController.getSkillById("1");

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertNotNull(response.getBody().getData());
            assertEquals("1", response.getBody().getData().getId());
            assertEquals("Java", response.getBody().getData().getName());
        }
    }

    @Test
    void getSkillById_WhenSkillDoesNotExist_ShouldReturnNotFound() {
        // Arrange
        try (MockedStatic<UserContextHelper> mockedUserContext = mockStatic(UserContextHelper.class)) {
            mockedUserContext.when(UserContextHelper::getCurrentUserOrThrow).thenReturn(testUser);
            
            when(findSkillUseCase.findSkillById(anyString())).thenReturn(Optional.empty());

            // Act
            ResponseEntity<OkResponseDto<SkillDto>> response = skillController.getSkillById("nonexistent");

            // Assert
            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        }
    }

    @Test
    void getAllSkills_ShouldReturnAllSkills() {
        // Arrange
        try (MockedStatic<UserContextHelper> mockedUserContext = mockStatic(UserContextHelper.class)) {
            mockedUserContext.when(UserContextHelper::getCurrentUserOrThrow).thenReturn(testUser);
            
            Skill skill2 = new Skill();
            skill2.setId("2");
            skill2.setName("Python");
            
            SkillDto skillDto2 = new SkillDto();
            skillDto2.setId("2");
            skillDto2.setName("Python");
            
            List<Skill> skills = Arrays.asList(testSkill, skill2);
            
            when(getAllSkillsUseCase.getAllSkills()).thenReturn(skills);
            when(skillDtoMapper.toDto(testSkill)).thenReturn(testSkillDto);
            when(skillDtoMapper.toDto(skill2)).thenReturn(skillDto2);

            // Act
            ResponseEntity<OkResponseDto<List<SkillDto>>> response = skillController.getAllSkills();

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertNotNull(response.getBody().getData());
            assertEquals(2, response.getBody().getData().size());
            assertEquals("Java", response.getBody().getData().get(0).getName());
            assertEquals("Python", response.getBody().getData().get(1).getName());
        }
    }

    @Test
    void updateSkill_WhenSkillExists_ShouldReturnUpdatedSkill() {
        // Arrange
        try (MockedStatic<UserContextHelper> mockedUserContext = mockStatic(UserContextHelper.class)) {
            mockedUserContext.when(UserContextHelper::getCurrentUserOrThrow).thenReturn(testUser);
            mockedUserContext.when(UserContextHelper::requireAdminRole).thenAnswer(invocation -> null);
            
            UpdateSkillDto updateSkillDto = new UpdateSkillDto("1", "Java Updated");
            
            Skill updatedSkill = new Skill();
            updatedSkill.setId("1");
            updatedSkill.setName("Java Updated");
            
            SkillDto updatedSkillDto = new SkillDto();
            updatedSkillDto.setId("1");
            updatedSkillDto.setName("Java Updated");
            
            when(skillDtoMapper.toModel(updateSkillDto)).thenReturn(updatedSkill);
            when(updateSkillUseCase.updateSkill("1", updatedSkill)).thenReturn(Optional.of(updatedSkill));
            when(skillDtoMapper.toDto(updatedSkill)).thenReturn(updatedSkillDto);

            // Act
            ResponseEntity<OkResponseDto<SkillDto>> response = skillController.updateSkill(updateSkillDto);

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertNotNull(response.getBody().getData());
            assertEquals("1", response.getBody().getData().getId());
            assertEquals("Java Updated", response.getBody().getData().getName());
        }
    }

    @Test
    void updateSkill_WhenSkillDoesNotExist_ShouldReturnNotFound() {
        // Arrange
        try (MockedStatic<UserContextHelper> mockedUserContext = mockStatic(UserContextHelper.class)) {
            mockedUserContext.when(UserContextHelper::getCurrentUserOrThrow).thenReturn(testUser);
            mockedUserContext.when(UserContextHelper::requireAdminRole).thenAnswer(invocation -> null);
            
            UpdateSkillDto updateSkillDto = new UpdateSkillDto("nonexistent", "Java Updated");
            
            Skill updatedSkill = new Skill();
            updatedSkill.setId("nonexistent");
            updatedSkill.setName("Java Updated");
            
            when(skillDtoMapper.toModel(updateSkillDto)).thenReturn(updatedSkill);
            when(updateSkillUseCase.updateSkill(anyString(), any(Skill.class))).thenReturn(Optional.empty());

            // Act
            ResponseEntity<OkResponseDto<SkillDto>> response = skillController.updateSkill(updateSkillDto);

            // Assert
            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        }
    }

    @Test
    void deleteSkill_WhenSkillExists_ShouldReturnOk() {
        // Arrange
        try (MockedStatic<UserContextHelper> mockedUserContext = mockStatic(UserContextHelper.class)) {
            mockedUserContext.when(UserContextHelper::getCurrentUserOrThrow).thenReturn(testUser);
            mockedUserContext.when(UserContextHelper::requireAdminRole).thenAnswer(invocation -> null);
            
            when(deleteSkillUseCase.deleteSkill("1")).thenReturn(true);

            // Act
            ResponseEntity<OkResponseDto<Void>> response = skillController.deleteSkill("1");

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
        }
    }

    @Test
    void deleteSkill_WhenSkillDoesNotExist_ShouldReturnNotFound() {
        // Arrange
        try (MockedStatic<UserContextHelper> mockedUserContext = mockStatic(UserContextHelper.class)) {
            mockedUserContext.when(UserContextHelper::getCurrentUserOrThrow).thenReturn(testUser);
            mockedUserContext.when(UserContextHelper::requireAdminRole).thenAnswer(invocation -> null);
            
            when(deleteSkillUseCase.deleteSkill(anyString())).thenReturn(false);

            // Act
            ResponseEntity<OkResponseDto<Void>> response = skillController.deleteSkill("nonexistent");

            // Assert
            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        }
    }
}