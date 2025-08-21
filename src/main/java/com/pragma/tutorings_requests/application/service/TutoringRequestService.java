package com.pragma.tutorings_requests.application.service;

import com.pragma.shared.context.UserContextHelper;
import com.pragma.tutorings_requests.domain.model.TutoringRequest;
import com.pragma.tutorings_requests.domain.model.enums.RequestStatus;
import com.pragma.tutorings_requests.domain.port.input.CreateTutoringRequestUseCase;
import com.pragma.tutorings_requests.domain.port.input.GetTutoringRequestsUseCase;
import com.pragma.tutorings_requests.domain.port.input.UpdateTutoringRequestStatusUseCase;
import com.pragma.tutorings_requests.domain.port.output.TutoringRequestRepository;
import com.pragma.usuarios.domain.model.enums.RolUsuario;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TutoringRequestService implements 
        CreateTutoringRequestUseCase, 
        UpdateTutoringRequestStatusUseCase,
        GetTutoringRequestsUseCase {

    private final TutoringRequestRepository tutoringRequestRepository;

    @Override
    public TutoringRequest createTutoringRequest(TutoringRequest tutoringRequest) {
        try {
            log.info("Procesando solicitud de tutoría con ID: {}", tutoringRequest.getId());
            
            // Establecer la fecha actual
            tutoringRequest.setRequestDate(new Date());
            
            // Establecer el estado por defecto como Pendiente
            tutoringRequest.setRequestStatus(RequestStatus.Pendiente);
            
            TutoringRequest savedRequest = tutoringRequestRepository.save(tutoringRequest);
            log.info("Solicitud de tutoría guardada exitosamente con ID: {}", savedRequest.getId());
            
            return savedRequest;
        } catch (Exception e) {
            log.error("Error al crear la solicitud de tutoría: {}", e.getMessage(), e);
            throw new RuntimeException("Error al crear la solicitud de tutoría", e);
        }
    }
    
    @Override
    public TutoringRequest updateStatus(String requestId, RequestStatus newStatus) {
        try {
            log.info("Actualizando estado de solicitud de tutoría con ID: {} a estado: {}", requestId, newStatus);
            
            // Validar permisos según el rol del usuario
            validateStatusChangePermissions(newStatus);
            
            // Buscar la solicitud por ID
            TutoringRequest tutoringRequest = tutoringRequestRepository.findById(requestId)
                    .orElseThrow(() -> new RuntimeException("Solicitud de tutoría no encontrada con ID: " + requestId));
            
            // Validar transición de estado
            validateStatusTransition(tutoringRequest.getRequestStatus(), newStatus);
            
            // Actualizar el estado
            tutoringRequest.setRequestStatus(newStatus);
            
            // Guardar los cambios
            TutoringRequest updatedRequest = tutoringRequestRepository.save(tutoringRequest);
            log.info("Estado de solicitud de tutoría actualizado exitosamente a: {}", newStatus);
            
            return updatedRequest;
        } catch (Exception e) {
            log.error("Error al actualizar el estado de la solicitud de tutoría: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    private void validateStatusChangePermissions(RequestStatus newStatus) {
        RolUsuario userRole = UserContextHelper.getCurrentUserOrThrow().getRol();
        
        switch (newStatus) {
            case Aprobada, Cancelada -> {
                if (userRole != RolUsuario.Administrador) {
                    throw new SecurityException("Solo los administradores pueden cambiar el estado a " + newStatus);
                }
            }
            case Conversando, Asignada, Finalizada -> {
                if (userRole != RolUsuario.Tutor) {
                    throw new SecurityException("Solo los tutores pueden cambiar el estado a " + newStatus);
                }
            }
        }
    }
    
    private void validateStatusTransition(RequestStatus currentStatus, RequestStatus newStatus) {
        boolean isValidTransition = switch (currentStatus) {
            case Pendiente -> newStatus == RequestStatus.Aprobada || newStatus == RequestStatus.Cancelada;
            case Aprobada -> newStatus == RequestStatus.Conversando || newStatus == RequestStatus.Cancelada;
            case Conversando -> newStatus == RequestStatus.Asignada || newStatus == RequestStatus.Cancelada;
            case Asignada -> newStatus == RequestStatus.Finalizada || newStatus == RequestStatus.Cancelada;
            case Finalizada, Cancelada -> false;
        };
        
        if (!isValidTransition) {
            throw new IllegalStateException("Transición de estado inválida: " + currentStatus + " -> " + newStatus);
        }
    }
    
    @Override
    public List<TutoringRequest> getAllTutoringRequests() {
        try {
            log.info("Obteniendo todas las solicitudes de tutoría");
            return tutoringRequestRepository.findAll();
        } catch (Exception e) {
            log.error("Error al obtener todas las solicitudes de tutoría: {}", e.getMessage(), e);
            throw new RuntimeException("Error al obtener las solicitudes de tutoría", e);
        }
    }

    @Override
    public List<TutoringRequest> getTutoringRequestsWithFilters(String tuteeId, String skillId, RequestStatus status, String chapterId) {
        try {
            log.info("Obteniendo solicitudes de tutoría con filtros - tuteeId: {}, skillId: {}, status: {}, chapterId: {}", 
                    tuteeId, skillId, status, chapterId);
            return tutoringRequestRepository.findWithFilters(tuteeId, skillId, status, chapterId);
        } catch (Exception e) {
            log.error("Error al obtener solicitudes de tutoría con filtros: {}", e.getMessage(), e);
            throw new RuntimeException("Error al obtener solicitudes con filtros", e);
        }
    }
}