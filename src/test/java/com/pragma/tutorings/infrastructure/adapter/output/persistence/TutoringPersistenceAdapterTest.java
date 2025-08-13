package com.pragma.tutorings.infrastructure.adapter.output.persistence;

import com.pragma.tutorings.domain.model.Tutoring;
import com.pragma.tutorings.domain.model.enums.TutoringStatus;
import com.pragma.tutorings.infrastructure.adapter.output.persistence.entity.TutoringEntity;
import com.pragma.tutorings.infrastructure.adapter.output.persistence.mapper.TutoringMapper;
import com.pragma.tutorings.infrastructure.adapter.output.persistence.repository.SpringDataTutoringRepository;
import com.pragma.usuarios.infrastructure.adapter.output.persistence.entity.UsersEntity;
import com.pragma.usuarios.infrastructure.adapter.output.persistence.repository.SpringDataUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TutoringPersistenceAdapterTest {

    @Mock
    private SpringDataTutoringRepository tutoringRepository;

    @Mock
    private SpringDataUserRepository userRepository;

    @Mock
    private TutoringMapper tutoringMapper;

    @InjectMocks
    private TutoringPersistenceAdapter tutoringPersistenceAdapter;

    private Tutoring tutoring;
    private TutoringEntity tutoringEntity;
    private UsersEntity tutorEntity;

    @BeforeEach
    void setUp() {
        // Configurar datos de prueba
        tutoring = new Tutoring();
        tutoring.setId("tutoring-id");
        tutoring.setObjectives("Objetivos de prueba");
        tutoring.setStatus(TutoringStatus.Activa);

        tutoringEntity = new TutoringEntity();
        tutoringEntity.setId("tutoring-id");
        tutoringEntity.setObjectives("Objetivos de prueba");
        tutoringEntity.setStatus(TutoringStatus.Activa);

        tutorEntity = new UsersEntity();
        tutorEntity.setId("tutor-id");
    }

    @Test
    void save_Success() {
        // Arrange
        when(tutoringMapper.toEntity(any(Tutoring.class))).thenReturn(tutoringEntity);
        when(tutoringRepository.save(any(TutoringEntity.class))).thenReturn(tutoringEntity);
        when(tutoringMapper.toDomain(any(TutoringEntity.class))).thenReturn(tutoring);

        // Act
        Tutoring result = tutoringPersistenceAdapter.save(tutoring);

        // Assert
        assertNotNull(result);
        assertEquals("tutoring-id", result.getId());
        verify(tutoringRepository).save(tutoringEntity);
    }

    @Test
    void findById_Success() {
        // Arrange
        when(tutoringRepository.findById("tutoring-id")).thenReturn(Optional.of(tutoringEntity));
        when(tutoringMapper.toDomain(any(TutoringEntity.class))).thenReturn(tutoring);

        // Act
        Optional<Tutoring> result = tutoringPersistenceAdapter.findById("tutoring-id");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("tutoring-id", result.get().getId());
    }

    @Test
    void findById_NotFound() {
        // Arrange
        when(tutoringRepository.findById("tutoring-id")).thenReturn(Optional.empty());

        // Act
        Optional<Tutoring> result = tutoringPersistenceAdapter.findById("tutoring-id");

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void findByTutorId_Success() {
        // Arrange
        List<TutoringEntity> entities = new ArrayList<>();
        entities.add(tutoringEntity);
        
        when(userRepository.findById("tutor-id")).thenReturn(Optional.of(tutorEntity));
        when(tutoringRepository.findByTutorId(tutorEntity)).thenReturn(entities);
        when(tutoringMapper.toDomainList(entities)).thenReturn(List.of(tutoring));

        // Act
        List<Tutoring> result = tutoringPersistenceAdapter.findByTutorId("tutor-id");

        // Assert
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals("tutoring-id", result.get(0).getId());
    }

    @Test
    void findByTutorId_TutorNotFound() {
        // Arrange
        when(userRepository.findById("tutor-id")).thenReturn(Optional.empty());

        // Act
        List<Tutoring> result = tutoringPersistenceAdapter.findByTutorId("tutor-id");

        // Assert
        assertTrue(result.isEmpty());
        verify(tutoringRepository, never()).findByTutorId(any(UsersEntity.class));
    }

    @Test
    void countActiveTutoringsByTutorId_Success() {
        // Arrange
        when(tutoringRepository.countByTutorIdAndStatus("tutor-id", TutoringStatus.Activa)).thenReturn(3L);

        // Act
        Long result = tutoringPersistenceAdapter.countActiveTutoringByTutorId("tutor-id");

        // Assert
        assertEquals(3, result);
    }

    @Test
    void countActiveTutoringsByTutorId_TutorNotFound() {
        // Arrange
        when(tutoringRepository.countByTutorIdAndStatus("tutor-id", TutoringStatus.Activa)).thenReturn(0L);

        // Act
        Long result = tutoringPersistenceAdapter.countActiveTutoringByTutorId("tutor-id");

        // Assert
        assertEquals(0, result);
    }
}