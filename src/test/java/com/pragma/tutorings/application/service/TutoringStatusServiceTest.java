package com.pragma.tutorings.application.service;

import com.pragma.feedbacks.domain.model.Feedback;
import com.pragma.feedbacks.domain.port.output.FeedbackRepository;
import com.pragma.tutorings.domain.model.Tutoring;
import com.pragma.tutorings.domain.model.enums.TutoringStatus;
import com.pragma.tutorings.domain.port.output.TutoringRepository;
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
class TutoringStatusServiceTest {

    @Mock
    private TutoringRepository tutoringRepository;

    @Mock
    private FindUserByIdUseCase findUserByIdUseCase;

    @Mock
    private FeedbackRepository feedbackRepository;

    @InjectMocks
    private TutoringStatusService tutoringStatusService;

    private User tutor;
    private User tutee;
    private User admin;
    private Tutoring activeTutoring;
    private Tutoring completedTutoring;
    private Feedback tutorFeedback;
    private Feedback tuteeFeedback;

    @BeforeEach
    void setUp() {
        // Configurar datos de prueba
        tutor = new User();
        tutor.setId("tutor-id");
        tutor.setFirstName("Tutor");
        tutor.setLastName("Test");
        tutor.setRol(RolUsuario.Tutor);

        tutee = new User();
        tutee.setId("tutee-id");
        tutee.setFirstName("Tutee");
        tutee.setLastName("Test");
        tutee.setRol(RolUsuario.Tutorado);

        admin = new User();
        admin.setId("admin-id");
        admin.setFirstName("Admin");
        admin.setLastName("Test");
        admin.setRol(RolUsuario.Administrador);

        activeTutoring = new Tutoring();
        activeTutoring.setId("tutoring-id");
        activeTutoring.setTutor(tutor);
        activeTutoring.setTutee(tutee);
        activeTutoring.setStartDate(new Date());
        activeTutoring.setExpectedEndDate(new Date());
        activeTutoring.setStatus(TutoringStatus.Activa);
        activeTutoring.setObjectives("Objetivos de prueba");

        completedTutoring = new Tutoring();
        completedTutoring.setId("completed-tutoring-id");
        completedTutoring.setTutor(tutor);
        completedTutoring.setTutee(tutee);
        completedTutoring.setStartDate(new Date());
        completedTutoring.setExpectedEndDate(new Date());
        completedTutoring.setStatus(TutoringStatus.Completada);
        completedTutoring.setObjectives("Objetivos de prueba");

        tutorFeedback = new Feedback();
        tutorFeedback.setId("tutor-feedback-id");
        tutorFeedback.setEvaluator(tutor);
        tutorFeedback.setTutoring(activeTutoring);
        tutorFeedback.setEvaluationDate(new Date());
        tutorFeedback.setScore("5");
        tutorFeedback.setComments("Excelente tutoría");

        tuteeFeedback = new Feedback();
        tuteeFeedback.setId("tutee-feedback-id");
        tuteeFeedback.setEvaluator(tutee);
        tuteeFeedback.setTutoring(activeTutoring);
        tuteeFeedback.setEvaluationDate(new Date());
        tuteeFeedback.setScore("5");
        tuteeFeedback.setComments("Aprendí mucho");
    }

    @Test
    void completeTutoring_ByTutor_Success() {
        // Arrange
        when(tutoringRepository.findById("tutoring-id")).thenReturn(Optional.of(activeTutoring));
        when(findUserByIdUseCase.findUserById("tutor-id")).thenReturn(Optional.of(tutor));
        when(feedbackRepository.findByTutoringIdAndEvaluatorId("tutoring-id", "tutor-id"))
                .thenReturn(List.of(tutorFeedback));
        when(feedbackRepository.findByTutoringIdAndEvaluatorId("tutoring-id", "tutee-id"))
                .thenReturn(List.of(tuteeFeedback));
        when(tutoringRepository.save(any(Tutoring.class))).thenAnswer(invocation -> {
            Tutoring savedTutoring = invocation.getArgument(0);
            savedTutoring.setStatus(TutoringStatus.Completada);
            return savedTutoring;
        });

        // Act
        Tutoring result = tutoringStatusService.completeTutoring("tutoring-id", "tutor-id", "http://example.com/final-act.pdf");

        // Assert
        assertNotNull(result);
        assertEquals(TutoringStatus.Completada, result.getStatus());
        verify(tutoringRepository).save(any(Tutoring.class));
    }

    @Test
    void completeTutoring_ByAdmin_Success() {
        // Arrange
        when(tutoringRepository.findById("tutoring-id")).thenReturn(Optional.of(activeTutoring));
        when(findUserByIdUseCase.findUserById("admin-id")).thenReturn(Optional.of(admin));
        when(feedbackRepository.findByTutoringIdAndEvaluatorId("tutoring-id", "tutor-id"))
                .thenReturn(List.of(tutorFeedback));
        when(feedbackRepository.findByTutoringIdAndEvaluatorId("tutoring-id", "tutee-id"))
                .thenReturn(List.of(tuteeFeedback));
        when(tutoringRepository.save(any(Tutoring.class))).thenAnswer(invocation -> {
            Tutoring savedTutoring = invocation.getArgument(0);
            savedTutoring.setStatus(TutoringStatus.Completada);
            return savedTutoring;
        });

        // Act
        Tutoring result = tutoringStatusService.completeTutoring("tutoring-id", "admin-id", "http://example.com/final-act.pdf");

        // Assert
        assertNotNull(result);
        assertEquals(TutoringStatus.Completada, result.getStatus());
        verify(tutoringRepository).save(any(Tutoring.class));
    }

    @Test
    void completeTutoring_TutoringNotFound() {
        // Arrange
        when(tutoringRepository.findById("non-existent-id")).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            tutoringStatusService.completeTutoring("non-existent-id", "tutor-id", "http://example.com/final-act.pdf");
        });

        assertEquals("La tutoría no existe", exception.getMessage());
        verify(tutoringRepository, never()).save(any(Tutoring.class));
    }

    @Test
    void completeTutoring_TutoringNotActive() {
        // Arrange
        when(tutoringRepository.findById("completed-tutoring-id")).thenReturn(Optional.of(completedTutoring));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            tutoringStatusService.completeTutoring("completed-tutoring-id", "tutor-id", "http://example.com/final-act.pdf");
        });

        assertEquals("No se puede cambiar el estado de la tutoría porque no está en estado Activa", exception.getMessage());
        verify(tutoringRepository, never()).save(any(Tutoring.class));
    }

    @Test
    void completeTutoring_UserNotFound() {
        // Arrange
        when(tutoringRepository.findById("tutoring-id")).thenReturn(Optional.of(activeTutoring));
        when(findUserByIdUseCase.findUserById("non-existent-id")).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            tutoringStatusService.completeTutoring("tutoring-id", "non-existent-id", "http://example.com/final-act.pdf");
        });

        assertEquals("El usuario no existe", exception.getMessage());
        verify(tutoringRepository, never()).save(any(Tutoring.class));
    }

    @Test
    void completeTutoring_UnauthorizedUser() {
        // Arrange
        User otherTutor = new User();
        otherTutor.setId("other-tutor-id");
        otherTutor.setRol(RolUsuario.Tutor);

        when(tutoringRepository.findById("tutoring-id")).thenReturn(Optional.of(activeTutoring));
        when(findUserByIdUseCase.findUserById("other-tutor-id")).thenReturn(Optional.of(otherTutor));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            tutoringStatusService.completeTutoring("tutoring-id", "other-tutor-id", "http://example.com/final-act.pdf");
        });

        assertEquals("No tienes permisos para completar esta tutoría", exception.getMessage());
        verify(tutoringRepository, never()).save(any(Tutoring.class));
    }

    @Test
    void completeTutoring_MissingTutorFeedback() {
        // Arrange
        when(tutoringRepository.findById("tutoring-id")).thenReturn(Optional.of(activeTutoring));
        when(findUserByIdUseCase.findUserById("tutor-id")).thenReturn(Optional.of(tutor));
        when(feedbackRepository.findByTutoringIdAndEvaluatorId("tutoring-id", "tutor-id"))
                .thenReturn(new ArrayList<>());
        when(feedbackRepository.findByTutoringIdAndEvaluatorId("tutoring-id", "tutee-id"))
                .thenReturn(List.of(tuteeFeedback));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            tutoringStatusService.completeTutoring("tutoring-id", "tutor-id", "http://example.com/final-act.pdf");
        });

        assertEquals("No se puede completar la tutoría porque falta el feedback del tutor", exception.getMessage());
        verify(tutoringRepository, never()).save(any(Tutoring.class));
    }

    @Test
    void completeTutoring_MissingTuteeFeedback() {
        // Arrange
        when(tutoringRepository.findById("tutoring-id")).thenReturn(Optional.of(activeTutoring));
        when(findUserByIdUseCase.findUserById("tutor-id")).thenReturn(Optional.of(tutor));
        when(feedbackRepository.findByTutoringIdAndEvaluatorId("tutoring-id", "tutor-id"))
                .thenReturn(List.of(tutorFeedback));
        when(feedbackRepository.findByTutoringIdAndEvaluatorId("tutoring-id", "tutee-id"))
                .thenReturn(new ArrayList<>());

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            tutoringStatusService.completeTutoring("tutoring-id", "tutor-id", "http://example.com/final-act.pdf");
        });

        assertEquals("No se puede completar la tutoría porque falta el feedback del tutee", exception.getMessage());
        verify(tutoringRepository, never()).save(any(Tutoring.class));
    }

    @Test
    void cancelTutoring_ByAdmin_Success() {
        // Arrange
        String cancellationComment = "Cancelada por inactividad";
        when(tutoringRepository.findById("tutoring-id")).thenReturn(Optional.of(activeTutoring));
        when(findUserByIdUseCase.findUserById("admin-id")).thenReturn(Optional.of(admin));
        when(feedbackRepository.save(any(Feedback.class))).thenReturn(new Feedback());
        when(tutoringRepository.save(any(Tutoring.class))).thenAnswer(invocation -> {
            Tutoring savedTutoring = invocation.getArgument(0);
            savedTutoring.setStatus(TutoringStatus.Cancelada);
            return savedTutoring;
        });

        // Act
        Tutoring result = tutoringStatusService.cancelTutoring("tutoring-id", "admin-id", cancellationComment);

        // Assert
        assertNotNull(result);
        assertEquals(TutoringStatus.Cancelada, result.getStatus());
        verify(feedbackRepository).save(any(Feedback.class));
        verify(tutoringRepository).save(any(Tutoring.class));
    }

    @Test
    void cancelTutoring_NonAdminUser() {
        // Arrange
        when(tutoringRepository.findById("tutoring-id")).thenReturn(Optional.of(activeTutoring));
        when(findUserByIdUseCase.findUserById("tutor-id")).thenReturn(Optional.of(tutor));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            tutoringStatusService.cancelTutoring("tutoring-id", "tutor-id", "Comentario de cancelación");
        });

        assertEquals("Solo los administradores pueden cancelar tutorías", exception.getMessage());
        verify(tutoringRepository, never()).save(any(Tutoring.class));
    }

    @Test
    void cancelTutoring_TutoringNotActive() {
        // Arrange
        when(tutoringRepository.findById("completed-tutoring-id")).thenReturn(Optional.of(completedTutoring));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            tutoringStatusService.cancelTutoring("completed-tutoring-id", "admin-id", "Comentario de cancelación");
        });

        assertEquals("No se puede cambiar el estado de la tutoría porque no está en estado Activa", exception.getMessage());
        verify(tutoringRepository, never()).save(any(Tutoring.class));
    }

    @Test
    void completeTutoring_MissingFinalActUrl() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            tutoringStatusService.completeTutoring("tutoring-id", "tutor-id", null);
        });

        assertEquals("El acta final es requerida para completar la tutoría", exception.getMessage());
        verify(tutoringRepository, never()).save(any(Tutoring.class));
    }

    @Test
    void completeTutoring_EmptyFinalActUrl() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            tutoringStatusService.completeTutoring("tutoring-id", "tutor-id", "");
        });

        assertEquals("El acta final es requerida para completar la tutoría", exception.getMessage());
        verify(tutoringRepository, never()).save(any(Tutoring.class));
    }
}