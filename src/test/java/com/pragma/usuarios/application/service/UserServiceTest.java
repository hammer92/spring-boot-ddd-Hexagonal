package com.pragma.usuarios.application.service;

import com.pragma.chapter.domain.model.Chapter;
import com.pragma.usuarios.domain.model.User;
import com.pragma.usuarios.domain.model.enums.RolUsuario;
import com.pragma.usuarios.domain.port.output.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private User tutorUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        Chapter chapter = new Chapter("1", "Java");
        testUser = new User();
        testUser.setId("1");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setEmail("john.doe@pragma.com");
        testUser.setChapter(chapter);
        testUser.setRol(RolUsuario.Tutorado);
        testUser.setActiveTutoringLimit(0);
        
        tutorUser = new User();
        tutorUser.setId("2");
        tutorUser.setFirstName("Jane");
        tutorUser.setLastName("Smith");
        tutorUser.setEmail("jane.smith@pragma.com");
        tutorUser.setChapter(chapter);
        tutorUser.setRol(RolUsuario.Tutor);
        tutorUser.setActiveTutoringLimit(5);
    }

    @Test
    void createUser_ShouldSetDefaultValues() {
        // Arrange
        User inputUser = new User();
        inputUser.setFirstName("John");
        inputUser.setLastName("Doe");
        inputUser.setEmail("john.doe@pragma.com");
        inputUser.setChapter(testUser.getChapter());
        // No establecemos rol ni activeTutoringLimit para verificar que el servicio los establece
        
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = userService.createUser(inputUser);

        // Assert
        assertNotNull(result);
        assertEquals(RolUsuario.Tutorado, result.getRol());
        assertEquals(0, result.getActiveTutoringLimit());
        
        // Verify that the service set the default values before saving
        verify(userRepository).save(argThat(user ->
            user.getRol() == RolUsuario.Tutorado && 
            user.getActiveTutoringLimit() == 0
        ));
    }
    
    @Test
    void findUserById_WhenUserExists_ShouldReturnUser() {
        // Arrange
        when(userRepository.findById("1")).thenReturn(Optional.of(testUser));

        // Act
        Optional<User> result = userService.findUserById("1");

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testUser.getId(), result.get().getId());
        assertEquals(testUser.getFirstName(), result.get().getFirstName());
        assertEquals(testUser.getLastName(), result.get().getLastName());
        
        // Verify repository was called with correct ID
        verify(userRepository).findById("1");
    }
    
    @Test
    void findUserById_WhenUserDoesNotExist_ShouldReturnEmpty() {
        // Arrange
        when(userRepository.findById(anyString())).thenReturn(Optional.empty());

        // Act
        Optional<User> result = userService.findUserById("nonexistent");

        // Assert
        assertFalse(result.isPresent());
        
        // Verify repository was called
        verify(userRepository).findById("nonexistent");
    }
    
    @Test
    void updateUserRole_WhenUserExists_ShouldUpdateRole() {
        // Arrange
        User updatedUser = new User();
        updatedUser.setId("1");
        updatedUser.setFirstName("John");
        updatedUser.setLastName("Doe");
        updatedUser.setEmail("john.doe@pragma.com");
        updatedUser.setChapter(testUser.getChapter());
        updatedUser.setRol(RolUsuario.Tutor);
        updatedUser.setActiveTutoringLimit(0);
        
        when(userRepository.findById("1")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        // Act
        Optional<User> result = userService.updateUserRole("1", RolUsuario.Tutor);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(RolUsuario.Tutor, result.get().getRol());
        
        // Verify repository was called with correct parameters
        verify(userRepository).findById("1");
        verify(userRepository).save(argThat(user -> user.getRol() == RolUsuario.Tutor));
    }
    
    @Test
    void updateUserRole_WhenUserDoesNotExist_ShouldReturnEmpty() {
        // Arrange
        when(userRepository.findById(anyString())).thenReturn(Optional.empty());

        // Act
        Optional<User> result = userService.updateUserRole("nonexistent", RolUsuario.Tutor);

        // Assert
        assertFalse(result.isPresent());
        
        // Verify repository was called but save was not
        verify(userRepository).findById("nonexistent");
        verify(userRepository, never()).save(any());
    }
    
    @Test
    void updateTutoringLimit_WhenRequestingUserIsTutor_ShouldUpdateLimit() {
        // Arrange
        User updatedUser = new User();
        updatedUser.setId("1");
        updatedUser.setFirstName("John");
        updatedUser.setLastName("Doe");
        updatedUser.setEmail("john.doe@pragma.com");
        updatedUser.setChapter(testUser.getChapter());
        updatedUser.setRol(RolUsuario.Tutorado);
        updatedUser.setActiveTutoringLimit(5);
        
        when(userRepository.findById("1")).thenReturn(Optional.of(testUser));
        when(userRepository.findById("2")).thenReturn(Optional.of(tutorUser));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        // Act
        Optional<User> result = userService.updateTutoringLimit("1", 5);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(5, result.get().getActiveTutoringLimit());
        
        // Verify repository was called with correct parameters
        verify(userRepository).findById("1");
        verify(userRepository).findById("2");
        verify(userRepository).save(argThat(user -> user.getActiveTutoringLimit() == 5));
    }
    
    @Test
    void updateTutoringLimit_WhenRequestingUserIsNotTutor_ShouldReturnEmpty() {
        // Arrange
        when(userRepository.findById("1")).thenReturn(Optional.of(testUser));
        when(userRepository.findById("3")).thenReturn(Optional.of(testUser)); // Usuario no tutor

        // Act
        Optional<User> result = userService.updateTutoringLimit("1", 5);

        // Assert
        assertFalse(result.isPresent());
        
        // Verify repository was called but save was not
        verify(userRepository).findById("3");
        verify(userRepository, never()).findById("1");
        verify(userRepository, never()).save(any());
    }
    
    @Test
    void updateTutoringLimit_WhenRequestingUserDoesNotExist_ShouldReturnEmpty() {
        // Arrange
        when(userRepository.findById("1")).thenReturn(Optional.of(testUser));
        when(userRepository.findById("nonexistent")).thenReturn(Optional.empty());

        // Act
        Optional<User> result = userService.updateTutoringLimit("1", 5);

        // Assert
        assertFalse(result.isPresent());
        
        // Verify repository was called but save was not
        verify(userRepository).findById("nonexistent");
        verify(userRepository, never()).findById("1");
        verify(userRepository, never()).save(any());
    }
    
    @Test
    void updateTutoringLimit_WhenTargetUserDoesNotExist_ShouldReturnEmpty() {
        // Arrange
        when(userRepository.findById("2")).thenReturn(Optional.of(tutorUser));
        when(userRepository.findById("nonexistent")).thenReturn(Optional.empty());

        // Act
        Optional<User> result = userService.updateTutoringLimit("nonexistent", 5);

        // Assert
        assertFalse(result.isPresent());
        
        // Verify repository was called but save was not
        verify(userRepository).findById("2");
        verify(userRepository).findById("nonexistent");
        verify(userRepository, never()).save(any());
    }
}