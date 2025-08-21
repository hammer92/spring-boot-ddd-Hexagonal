package com.pragma.tutorings.application.service;

import com.pragma.skills.domain.model.Skill;
import com.pragma.tutorings.domain.model.Tutoring;
import com.pragma.tutorings.domain.model.enums.TutoringStatus;
import com.pragma.tutorings.domain.port.output.TutoringRepository;
import com.pragma.tutorings_requests.domain.model.TutoringRequest;
import com.pragma.tutorings_requests.domain.model.enums.RequestStatus;
import com.pragma.tutorings_requests.domain.port.output.TutoringRequestRepository;
import com.pragma.usuarios.domain.model.User;
import com.pragma.usuarios.domain.model.enums.RolUsuario;
import com.pragma.usuarios.domain.port.input.FindUserByIdUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TutoringServiceTest {

    @Mock
    private TutoringRepository tutoringRepository;

    @Mock
    private TutoringRequestRepository tutoringRequestRepository;

    @Mock
    private FindUserByIdUseCase findUserByIdUseCase;

    @InjectMocks
    private TutoringService tutoringService;

    private User tutor;
    private User tutee;
    private TutoringRequest tutoringRequest;
    private List<Skill> skills;
    private Tutoring tutoring;

    @BeforeEach
    void setUp() {
        // Configurar datos de prueba
        tutor = new User();
        tutor.setId("tutor-id");
        tutor.setFirstName("Tutor");
        tutor.setLastName("Test");
        tutor.setRol(RolUsuario.Tutor);
        tutor.setActiveTutoringLimit(5);

        tutee = new User();
        tutee.setId("tutee-id");
        tutee.setFirstName("Tutee");
        tutee.setLastName("Test");
        tutee.setRol(RolUsuario.Tutorado);

        skills = new ArrayList<>();
        Skill skill1 = new Skill("skill-1", "Java");
        Skill skill2 = new Skill("skill-2", "Spring");
        skills.add(skill1);
        skills.add(skill2);

        tutoringRequest = new TutoringRequest();
        tutoringRequest.setId("request-id");
        tutoringRequest.setTutee(tutee);
        tutoringRequest.setSkills(skills);
        tutoringRequest.setNeedsDescription("Necesito ayuda con Java y Spring");
        tutoringRequest.setRequestDate(new Date());
        tutoringRequest.setRequestStatus(RequestStatus.Aprobada);

        tutoring = new Tutoring();
        tutoring.setId("tutoring-id");
        tutoring.setTutor(tutor);
        tutoring.setTutee(tutee);
        tutoring.setSkills(skills);
        tutoring.setStartDate(new Date());
        tutoring.setExpectedEndDate(new Date());
        tutoring.setStatus(TutoringStatus.Activa);
        tutoring.setObjectives("Objetivos de prueba");
    }

    @Test
    void createTutoring_Success() {
        // Arrange
        when(findUserByIdUseCase.findUserById("tutor-id")).thenReturn(Optional.of(tutor));
        when(tutoringRequestRepository.findById("request-id")).thenReturn(Optional.of(tutoringRequest));
        when(tutoringRepository.countActiveTutoringByTutorId("tutor-id")).thenReturn(2L);
        when(tutoringRepository.save(any(Tutoring.class))).thenReturn(tutoring);
        when(tutoringRequestRepository.save(any(TutoringRequest.class))).thenReturn(tutoringRequest);

        // Act
        Tutoring result = tutoringService.createTutoring("request-id", "tutor-id", "Objetivos de prueba");

        // Assert
        assertNotNull(result);
        assertEquals("tutoring-id", result.getId());
        assertEquals(tutor, result.getTutor());
        assertEquals(tutee, result.getTutee());
        assertEquals(skills, result.getSkills());
        assertEquals(TutoringStatus.Activa, result.getStatus());
        assertEquals("Objetivos de prueba", result.getObjectives());

        verify(tutoringRepository).save(any(Tutoring.class));
        verify(tutoringRequestRepository).save(any(TutoringRequest.class));
        assertEquals(RequestStatus.Asignada, tutoringRequest.getRequestStatus());
        assertEquals("tutoring-id", tutoringRequest.getAssignedTutoringId());
    }

    @Test
    void createTutoring_TutorNotFound() {
        // Arrange
        when(findUserByIdUseCase.findUserById("tutor-id")).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            tutoringService.createTutoring("request-id", "tutor-id", "Objetivos de prueba");
        });

        assertEquals("El tutor no existe", exception.getMessage());
        verify(tutoringRepository, never()).save(any(Tutoring.class));
    }

    @Test
    void createTutoring_InvalidTutorRole() {
        // Arrange
        tutor.setRol(RolUsuario.Tutorado);
        when(findUserByIdUseCase.findUserById("tutor-id")).thenReturn(Optional.of(tutor));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            tutoringService.createTutoring("request-id", "tutor-id", "Objetivos de prueba");
        });

        assertEquals("Solo los usuarios con rol de Tutor o Administrador pueden crear tutorías", exception.getMessage());
        verify(tutoringRepository, never()).save(any(Tutoring.class));
    }

    @Test
    void createTutoring_RequestNotFound() {
        // Arrange
        when(findUserByIdUseCase.findUserById("tutor-id")).thenReturn(Optional.of(tutor));
        when(tutoringRequestRepository.findById("request-id")).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            tutoringService.createTutoring("request-id", "tutor-id", "Objetivos de prueba");
        });

        assertEquals("La solicitud de tutoría no existe", exception.getMessage());
        verify(tutoringRepository, never()).save(any(Tutoring.class));
    }

    @Test
    void createTutoring_InvalidRequestStatus() {
        // Arrange
        tutoringRequest.setRequestStatus(RequestStatus.Pendiente);
        when(findUserByIdUseCase.findUserById("tutor-id")).thenReturn(Optional.of(tutor));
        when(tutoringRequestRepository.findById("request-id")).thenReturn(Optional.of(tutoringRequest));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            tutoringService.createTutoring("request-id", "tutor-id", "Objetivos de prueba");
        });

        assertEquals("Solo se pueden crear tutorías a partir de solicitudes en estado Aprobada", exception.getMessage());
        verify(tutoringRepository, never()).save(any(Tutoring.class));
    }

    @Test
    void createTutoring_TutorLimitExceeded() {
        // Arrange
        when(findUserByIdUseCase.findUserById("tutor-id")).thenReturn(Optional.of(tutor));
        when(tutoringRequestRepository.findById("request-id")).thenReturn(Optional.of(tutoringRequest));
        when(tutoringRepository.countActiveTutoringByTutorId("tutor-id")).thenReturn(5L); // Igual al límite

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            tutoringService.createTutoring("request-id", "tutor-id", "Objetivos de prueba");
        });

        assertEquals("El tutor ha excedido su límite de tutorías activas", exception.getMessage());
        verify(tutoringRepository, never()).save(any(Tutoring.class));
    }

    @Test
    void createTutoring_AdminRoleSuccess() {
        // Arrange
        tutor.setRol(RolUsuario.Administrador);
        when(findUserByIdUseCase.findUserById("tutor-id")).thenReturn(Optional.of(tutor));
        when(tutoringRequestRepository.findById("request-id")).thenReturn(Optional.of(tutoringRequest));
        when(tutoringRepository.countActiveTutoringByTutorId("tutor-id")).thenReturn(2L);
        when(tutoringRepository.save(any(Tutoring.class))).thenReturn(tutoring);
        when(tutoringRequestRepository.save(any(TutoringRequest.class))).thenReturn(tutoringRequest);

        // Act
        Tutoring result = tutoringService.createTutoring("request-id", "tutor-id", "Objetivos de prueba");

        // Assert
        assertNotNull(result);
        assertEquals("tutoring-id", result.getId());
        verify(tutoringRepository).save(any(Tutoring.class));
    }
}