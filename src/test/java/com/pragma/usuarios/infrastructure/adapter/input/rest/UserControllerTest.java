package com.pragma.usuarios.infrastructure.adapter.input.rest;

import com.pragma.chapter.domain.model.Chapter;
import com.pragma.shared.context.UserContext;
import com.pragma.shared.context.UserContextHelper;
import com.pragma.shared.service.MessageService;
import com.pragma.usuarios.domain.model.User;
import com.pragma.usuarios.domain.model.enums.RolUsuario;
import com.pragma.usuarios.domain.port.input.CreateUserUseCase;
import com.pragma.usuarios.domain.port.input.FindUserByIdUseCase;
import com.pragma.usuarios.domain.port.input.UpdateTutoringLimitUseCase;
import com.pragma.usuarios.domain.port.input.UpdateUserRoleUseCase;
import com.pragma.usuarios.domain.port.input.UpdateUserUseCase;
import com.pragma.usuarios.infrastructure.adapter.input.rest.dto.CreateUserDto;
import com.pragma.usuarios.infrastructure.adapter.input.rest.dto.UpdateTutoringLimitDto;
import com.pragma.usuarios.infrastructure.adapter.input.rest.dto.UpdateUserRequestDto;
import com.pragma.usuarios.infrastructure.adapter.input.rest.dto.UpdateUserRoleDto;
import com.pragma.usuarios.infrastructure.adapter.input.rest.dto.UserDto;
import com.pragma.usuarios.infrastructure.adapter.input.rest.mapper.UserDtoMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class UserControllerTest {

    @Mock
    private CreateUserUseCase createUserUseCase;

    @Mock
    private UpdateUserUseCase updateUserUseCase;
    
    @Mock
    private FindUserByIdUseCase findUserByIdUseCase;
    
    @Mock
    private UpdateUserRoleUseCase updateUserRoleUseCase;
    
    @Mock
    private UpdateTutoringLimitUseCase updateTutoringLimitUseCase;

    @Mock
    private UserDtoMapper userDtoMapper;

    @Mock
    private MessageService messageService;

    @InjectMocks
    private UserController userController;

    private User testUser;
    private UserDto testUserDto;
    private User updatedRoleUser;
    private UserDto updatedRoleUserDto;
    private User updatedLimitUser;
    private UserDto updatedLimitUserDto;
    private User adminUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Setup MessageService mock
        UserContextHelper.setMessageServiceForTesting(messageService);
        when(messageService.getMessage(anyString())).thenReturn("Test message");

        Chapter chapter = new Chapter("1", "Java");
        
        // Create admin user for context
        adminUser = new User();
        adminUser.setId("admin-1");
        adminUser.setFirstName("Admin");
        adminUser.setLastName("User");
        adminUser.setEmail("admin@pragma.com");
        adminUser.setSlackId(null);
        adminUser.setChapter(chapter);
        adminUser.setRol(RolUsuario.Administrador);
        adminUser.setActiveTutoringLimit(0);
        
        // Set admin user in context for tests that require admin privileges
        UserContext.setCurrentUser(adminUser);
        testUser = new User();
        testUser.setId("1");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setEmail("john.doe@pragma.com");
        testUser.setSlackId(null);
        testUser.setChapter(chapter);
        testUser.setRol(RolUsuario.Tutorado);
        testUser.setActiveTutoringLimit(0);

        testUserDto = new UserDto();
        testUserDto.setId("1");
        testUserDto.setFirstName("John");
        testUserDto.setLastName("Doe");
        testUserDto.setEmail("john.doe@pragma.com");
        testUserDto.setSlackId(null);
        testUserDto.setRol(RolUsuario.Tutorado);
        testUserDto.setActiveTutoringLimit(0);
        
        updatedRoleUser = new User();
        updatedRoleUser.setId("1");
        updatedRoleUser.setFirstName("John");
        updatedRoleUser.setLastName("Doe");
        updatedRoleUser.setEmail("john.doe@pragma.com");
        updatedRoleUser.setSlackId(null);
        updatedRoleUser.setChapter(chapter);
        updatedRoleUser.setRol(RolUsuario.Tutor);
        updatedRoleUser.setActiveTutoringLimit(0);
        
        updatedRoleUserDto = new UserDto();
        updatedRoleUserDto.setId("1");
        updatedRoleUserDto.setFirstName("John");
        updatedRoleUserDto.setLastName("Doe");
        updatedRoleUserDto.setEmail("john.doe@pragma.com");
        updatedRoleUserDto.setSlackId(null);
        updatedRoleUserDto.setRol(RolUsuario.Tutor);
        updatedRoleUserDto.setActiveTutoringLimit(0);
        
        updatedLimitUser = new User();
        updatedLimitUser.setId("1");
        updatedLimitUser.setFirstName("John");
        updatedLimitUser.setLastName("Doe");
        updatedLimitUser.setEmail("john.doe@pragma.com");
        updatedLimitUser.setSlackId(null);
        updatedLimitUser.setChapter(chapter);
        updatedLimitUser.setRol(RolUsuario.Tutorado);
        updatedLimitUser.setActiveTutoringLimit(5);
        
        updatedLimitUserDto = new UserDto();
        updatedLimitUserDto.setId("1");
        updatedLimitUserDto.setFirstName("John");
        updatedLimitUserDto.setLastName("Doe");
        updatedLimitUserDto.setEmail("john.doe@pragma.com");
        updatedLimitUserDto.setSlackId(null);
        updatedLimitUserDto.setRol(RolUsuario.Tutorado);
        updatedLimitUserDto.setActiveTutoringLimit(5);
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void getUserById_WhenUserExists_ShouldReturnOk() {
        // Arrange
        when(findUserByIdUseCase.findUserById("1")).thenReturn(Optional.of(testUser));
        when(userDtoMapper.toDto(testUser)).thenReturn(testUserDto);

        // Act
        ResponseEntity<UserDto> response = userController.getUserById("1");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("1", response.getBody().getId());
        assertEquals("John", response.getBody().getFirstName());
    }

    @Test
    void getUserById_WhenUserDoesNotExist_ShouldReturnNotFound() {
        // Arrange
        when(findUserByIdUseCase.findUserById(anyString())).thenReturn(Optional.empty());

        // Act
        ResponseEntity<UserDto> response = userController.getUserById("nonexistent");

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
    
    @Test
    void updateUserRole_WhenUserExists_ShouldReturnOk() {
        // Arrange
        UpdateUserRoleDto requestDto = new UpdateUserRoleDto("1", RolUsuario.Tutor);
        when(updateUserRoleUseCase.updateUserRole("1", RolUsuario.Tutor)).thenReturn(Optional.of(updatedRoleUser));
        when(userDtoMapper.toDto(updatedRoleUser)).thenReturn(updatedRoleUserDto);

        // Act
        ResponseEntity<UserDto> response = userController.updateUserRole(requestDto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(RolUsuario.Tutor, response.getBody().getRol());
    }
    
    @Test
    void updateUserRole_WhenUserDoesNotExist_ShouldReturnNotFound() {
        // Arrange
        UpdateUserRoleDto requestDto = new UpdateUserRoleDto("nonexistent", RolUsuario.Tutor);
        when(updateUserRoleUseCase.updateUserRole(anyString(), any(RolUsuario.class))).thenReturn(Optional.empty());

        // Act
        ResponseEntity<UserDto> response = userController.updateUserRole(requestDto);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
    
    @Test
    void updateTutoringLimit_WhenUserExistsAndRequestingUserIsTutor_ShouldReturnOk() {
        // Arrange
        UpdateTutoringLimitDto requestDto = new UpdateTutoringLimitDto("1", 5);
        when(updateTutoringLimitUseCase.updateTutoringLimit("1", 5)).thenReturn(Optional.of(updatedLimitUser));
        when(userDtoMapper.toDto(updatedLimitUser)).thenReturn(updatedLimitUserDto);

        // Act
        ResponseEntity<UserDto> response = userController.updateTutoringLimit(requestDto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(5, response.getBody().getActiveTutoringLimit());
    }
    
    @Test
    void updateTutoringLimit_WhenRequestingUserIsNotTutor_ShouldReturnForbidden() {
        // Arrange
        UpdateTutoringLimitDto requestDto = new UpdateTutoringLimitDto("1", 5);
        when(updateTutoringLimitUseCase.updateTutoringLimit("1", 5)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<UserDto> response = userController.updateTutoringLimit(requestDto);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }
    
    @Test
    void updateTutoringLimit_WhenUserDoesNotExist_ShouldReturnForbidden() {
        // Arrange
        UpdateTutoringLimitDto requestDto = new UpdateTutoringLimitDto("nonexistent", 5);
        when(updateTutoringLimitUseCase.updateTutoringLimit(anyString(), anyInt())).thenReturn(Optional.empty());

        // Act
        ResponseEntity<UserDto> response = userController.updateTutoringLimit(requestDto);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }
}