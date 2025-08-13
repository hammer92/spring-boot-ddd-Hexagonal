package com.pragma.usuarios.domain.port.input;

import com.pragma.usuarios.application.service.UserService;
import com.pragma.usuarios.domain.model.User;
import com.pragma.usuarios.domain.model.enums.RolUsuario;
import com.pragma.usuarios.domain.port.output.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FindUserByGoogleIdUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private static final String TEST_GOOGLE_USER_ID = "google123";

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId("user123");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setEmail("john.doe@example.com");
        testUser.setGoogleUserId(TEST_GOOGLE_USER_ID);
        testUser.setRol(RolUsuario.Tutorado);
        testUser.setActiveTutoringLimit(0);
    }

    @Test
    void findUserByGoogleId_WithExistingUser_ShouldReturnUser() {
        // Arrange
        when(userRepository.findByGoogleUserId(TEST_GOOGLE_USER_ID)).thenReturn(Optional.of(testUser));

        // Act
        Optional<User> result = userService.findUserByGoogleId(TEST_GOOGLE_USER_ID);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testUser, result.get());
        assertEquals(TEST_GOOGLE_USER_ID, result.get().getGoogleUserId());
        verify(userRepository).findByGoogleUserId(TEST_GOOGLE_USER_ID);
    }

    @Test
    void findUserByGoogleId_WithNonExistingUser_ShouldReturnEmpty() {
        // Arrange
        when(userRepository.findByGoogleUserId(TEST_GOOGLE_USER_ID)).thenReturn(Optional.empty());

        // Act
        Optional<User> result = userService.findUserByGoogleId(TEST_GOOGLE_USER_ID);

        // Assert
        assertFalse(result.isPresent());
        verify(userRepository).findByGoogleUserId(TEST_GOOGLE_USER_ID);
    }

    @Test
    void findUserByGoogleId_WithNullGoogleId_ShouldCallRepository() {
        // Arrange
        when(userRepository.findByGoogleUserId(null)).thenReturn(Optional.empty());

        // Act
        Optional<User> result = userService.findUserByGoogleId(null);

        // Assert
        assertFalse(result.isPresent());
        verify(userRepository).findByGoogleUserId(null);
    }

    @Test
    void findUserByGoogleId_WithEmptyGoogleId_ShouldCallRepository() {
        // Arrange
        String emptyGoogleId = "";
        when(userRepository.findByGoogleUserId(emptyGoogleId)).thenReturn(Optional.empty());

        // Act
        Optional<User> result = userService.findUserByGoogleId(emptyGoogleId);

        // Assert
        assertFalse(result.isPresent());
        verify(userRepository).findByGoogleUserId(emptyGoogleId);
    }
}