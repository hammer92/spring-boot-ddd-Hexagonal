package com.pragma.tutorings.application.service;

import com.pragma.feedbacks.domain.model.Feedback;
import com.pragma.feedbacks.domain.port.output.FeedbackRepository;
import com.pragma.tutorings.domain.model.Tutoring;
import com.pragma.tutorings.domain.model.enums.TutoringStatus;
import com.pragma.tutorings.domain.port.input.CancelTutoringUseCase;
import com.pragma.tutorings.domain.port.input.CompleteTutoringUseCase;
import com.pragma.tutorings.domain.port.output.TutoringRepository;
import com.pragma.usuarios.domain.model.User;
import com.pragma.usuarios.domain.model.enums.RolUsuario;
import com.pragma.usuarios.domain.port.input.FindUserByIdUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TutoringStatusService implements CompleteTutoringUseCase, CancelTutoringUseCase {

    private final TutoringRepository tutoringRepository;
    private final FindUserByIdUseCase findUserByIdUseCase;
    private final FeedbackRepository feedbackRepository;

    @Override
    public Tutoring completeTutoring(String tutoringId, String userId) {
        log.info("Iniciando proceso para marcar tutoría como completada. ID: {}, Usuario: {}", tutoringId, userId);
        
        // Validar que la tutoría existe
        Tutoring tutoring = validateTutoringExists(tutoringId);
        
        // Validar que la tutoría está activa
        validateTutoringStatus(tutoring);
        
        // Validar que el usuario existe y tiene permisos
        User user = validateUserExists(userId);
        validateUserPermission(user, tutoring);
        
        // Validar que existen feedbacks del tutor y del tutee
        validateFeedbacksExist(tutoringId, tutoring.getTutor().getId(), tutoring.getTutee().getId());
        
        // Actualizar el estado de la tutoría
        tutoring.setStatus(TutoringStatus.Completada);
        
        // Guardar y retornar la tutoría actualizada
        Tutoring updatedTutoring = tutoringRepository.save(tutoring);
        log.info("Tutoría marcada como completada exitosamente. ID: {}", tutoringId);
        
        return updatedTutoring;
    }

    @Override
    public Tutoring cancelTutoring(String tutoringId, String adminId, String cancellationComment) {
        log.info("Iniciando proceso para cancelar tutoría. ID: {}, Admin: {}", tutoringId, adminId);
        
        // Validar que la tutoría existe
        Tutoring tutoring = validateTutoringExists(tutoringId);
        
        // Validar que la tutoría está activa
        validateTutoringStatus(tutoring);
        
        // Validar que el usuario es administrador
        User admin = validateUserExists(adminId);
        validateAdminRole(admin);
        
        // Crear feedback de cancelación con el comentario proporcionado
        createCancellationFeedback(tutoring, admin, cancellationComment);
        
        // Actualizar el estado de la tutoría
        tutoring.setStatus(TutoringStatus.Cancelada);
        
        // Guardar y retornar la tutoría actualizada
        Tutoring updatedTutoring = tutoringRepository.save(tutoring);
        log.info("Tutoría cancelada exitosamente. ID: {}", tutoringId);
        
        return updatedTutoring;
    }
    
    private void createCancellationFeedback(Tutoring tutoring, User admin, String comments) {
        Feedback feedback = new Feedback();
        feedback.setEvaluator(admin);
        feedback.setTutoring(tutoring);
        feedback.setEvaluationDate(new Date());
        feedback.setScore("N/A");
        feedback.setComments(comments != null && !comments.isEmpty() ? comments : "Tutoría cancelada por administrador");
        
        feedbackRepository.save(feedback);
        log.info("Feedback de cancelación creado para la tutoría ID: {}", tutoring.getId());
    }
    
    private Tutoring validateTutoringExists(String tutoringId) {
        Optional<Tutoring> tutoringOpt = tutoringRepository.findById(tutoringId);
        
        if (tutoringOpt.isEmpty()) {
            log.error("La tutoría con ID: {} no existe", tutoringId);
            throw new IllegalArgumentException("La tutoría no existe");
        }
        
        return tutoringOpt.get();
    }
    
    private void validateTutoringStatus(Tutoring tutoring) {
        if (tutoring.getStatus() != TutoringStatus.Activa) {
            log.error("La tutoría con ID: {} no está en estado {}. Estado actual: {}", 
                    tutoring.getId(), TutoringStatus.Activa, tutoring.getStatus());
            throw new IllegalStateException("No se puede cambiar el estado de la tutoría porque no está en estado " + TutoringStatus.Activa);
        }
    }
    
    private User validateUserExists(String userId) {
        Optional<User> userOpt = findUserByIdUseCase.findUserById(userId);
        
        if (userOpt.isEmpty()) {
            log.error("El usuario con ID: {} no existe", userId);
            throw new IllegalArgumentException("El usuario no existe");
        }
        
        return userOpt.get();
    }
    
    private void validateUserPermission(User user, Tutoring tutoring) {
        // Administrador siempre tiene permiso
        if (user.getRol() == RolUsuario.Administrador) {
            return;
        }
        
        // Tutor solo puede completar sus propias tutorías
        if (user.getRol() == RolUsuario.Tutor && user.getId().equals(tutoring.getTutor().getId())) {
            return;
        }
        
        log.error("El usuario con ID: {} no tiene permisos para completar la tutoría", user.getId());
        throw new IllegalArgumentException("No tienes permisos para completar esta tutoría");
    }
    
    private void validateAdminRole(User user) {
        if (user.getRol() != RolUsuario.Administrador) {
            log.error("El usuario con ID: {} no es administrador", user.getId());
            throw new IllegalArgumentException("Solo los administradores pueden cancelar tutorías");
        }
    }
    
    private void validateFeedbacksExist(String tutoringId, String tutorId, String tuteeId) {
        List<Feedback> tutorFeedbacks = feedbackRepository.findByTutoringIdAndEvaluatorId(tutoringId, tutorId);
        List<Feedback> tuteeFeedbacks = feedbackRepository.findByTutoringIdAndEvaluatorId(tutoringId, tuteeId);
        
        if (tutorFeedbacks.isEmpty()) {
            log.error("No existe feedback del tutor para la tutoría con ID: {}", tutoringId);
            throw new IllegalStateException("No se puede completar la tutoría porque falta el feedback del tutor");
        }
        
        if (tuteeFeedbacks.isEmpty()) {
            log.error("No existe feedback del tutee para la tutoría con ID: {}", tutoringId);
            throw new IllegalStateException("No se puede completar la tutoría porque falta el feedback del tutee");
        }
    }
}