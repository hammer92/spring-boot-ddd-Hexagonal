package com.pragma.usuarios.infrastructure.adapter.output.persistence;

import com.pragma.chapter.domain.model.Chapter;
import com.pragma.usuarios.domain.model.User;
import com.pragma.usuarios.domain.model.enums.RolUsuario;
import com.pragma.usuarios.infrastructure.adapter.output.persistence.entity.UsersEntity;
import com.pragma.usuarios.infrastructure.adapter.output.persistence.mapper.UserMapper;
import com.pragma.usuarios.infrastructure.adapter.output.persistence.repository.SpringDataUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class UserPersistenceAdapterTest {

    @Mock
    private SpringDataUserRepository repository;

    @Mock
    private UserMapper mapper;

    @InjectMocks
    private UserPersistenceAdapter adapter;

    private User testUser;
    private UsersEntity testUserEntity;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        Chapter chapter = new Chapter("1", "Java");
        testUser = new User();
        testUser.setId("1");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setEmail("john.doe@pragma.com");
        testUser.setGoogleUserId("google123");
        testUser.setChapter(chapter);
        testUser.setRol(RolUsuario.Tutorado);
        testUser.setActiveTutoringLimit(0);

        testUserEntity = new UsersEntity();
        testUserEntity.setId("1");
        testUserEntity.setFirstName("John");
        testUserEntity.setLastName("Doe");
        testUserEntity.setEmail("john.doe@pragma.com");
        testUserEntity.setGoogleUserId("google123");
    }

    @Test
    void findById_WhenUserExists_ShouldReturnUser() {
        // Arrange
        when(repository.findById("1")).thenReturn(Optional.of(testUserEntity));
        when(mapper.toDomain(testUserEntity)).thenReturn(testUser);

        // Act
        Optional<User> result = adapter.findById("1");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("1", result.get().getId());
        assertEquals("John", result.get().getFirstName());
        assertEquals("Doe", result.get().getLastName());
    }

    @Test
    void findById_WhenUserDoesNotExist_ShouldReturnEmpty() {
        // Arrange
        when(repository.findById(anyString())).thenReturn(Optional.empty());

        // Act
        Optional<User> result = adapter.findById("nonexistent");

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void findByGoogleUserId_WhenUserExists_ShouldReturnUser() {
        // Arrange
        when(repository.findByGoogleUserId("google123")).thenReturn(Optional.of(testUserEntity));
        when(mapper.toDomain(testUserEntity)).thenReturn(testUser);

        // Act
        Optional<User> result = adapter.findByGoogleUserId("google123");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("1", result.get().getId());
        assertEquals("John", result.get().getFirstName());
        assertEquals("Doe", result.get().getLastName());
        assertEquals("google123", result.get().getGoogleUserId());
    }

    @Test
    void findByGoogleUserId_WhenUserDoesNotExist_ShouldReturnEmpty() {
        // Arrange
        when(repository.findByGoogleUserId(anyString())).thenReturn(Optional.empty());

        // Act
        Optional<User> result = adapter.findByGoogleUserId("nonexistent");

        // Assert
        assertFalse(result.isPresent());
    }
}