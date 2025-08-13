package com.pragma.tutorings_requests.infrastructure.adapter.input.rest;

import com.pragma.shared.context.UserContext;
import com.pragma.shared.dto.OkResponseDto;
import com.pragma.tutorings_requests.domain.model.TutoringRequest;
import com.pragma.tutorings_requests.domain.port.input.CreateTutoringRequestUseCase;
import com.pragma.tutorings_requests.domain.port.input.GetTutoringRequestsUseCase;
import com.pragma.tutorings_requests.domain.port.input.UpdateTutoringRequestStatusUseCase;
import com.pragma.tutorings_requests.infrastructure.adapter.input.rest.dto.CreateTutoringRequestDto;
import com.pragma.tutorings_requests.infrastructure.adapter.input.rest.dto.TutoringRequestDto;
import com.pragma.tutorings_requests.infrastructure.adapter.input.rest.dto.TutoringRequestFilterDto;
import com.pragma.tutorings_requests.infrastructure.adapter.input.rest.dto.UpdateTutoringRequestStatusDto;
import com.pragma.tutorings_requests.infrastructure.adapter.input.rest.mapper.TutoringRequestDtoMapper;
import com.pragma.usuarios.domain.model.User;
import com.pragma.usuarios.domain.model.enums.RolUsuario;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/tutoring-requests")
@RequiredArgsConstructor
@Slf4j
public class TutoringRequestController {

    private final CreateTutoringRequestUseCase createTutoringRequestUseCase;
    private final UpdateTutoringRequestStatusUseCase updateTutoringRequestStatusUseCase;
    private final GetTutoringRequestsUseCase getTutoringRequestsUseCase;
    private final TutoringRequestDtoMapper tutoringRequestDtoMapper;

    @PostMapping
    public ResponseEntity<OkResponseDto<TutoringRequestDto>> createTutoringRequest(
            @Valid @RequestBody CreateTutoringRequestDto createTutoringRequestDto) {
        
        try {
            User currentUser = getCurrentUserOrThrow();
            log.info("User {} creating tutoring request for skills: {}", 
                    currentUser.getEmail(), createTutoringRequestDto.getSkillIds());
            
            // Set the current user as the tutee
            createTutoringRequestDto.setTuteeId(currentUser.getId());
            
            TutoringRequest tutoringRequest = tutoringRequestDtoMapper.toModel(createTutoringRequestDto);
            TutoringRequest createdRequest = createTutoringRequestUseCase.createTutoringRequest(tutoringRequest);
            TutoringRequestDto responseDto = tutoringRequestDtoMapper.toDto(createdRequest);
            
            log.info("User {} successfully created tutoring request with ID: {}", 
                    currentUser.getEmail(), createdRequest.getId());
            
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(OkResponseDto.of("Solicitud de tutoría creada exitosamente", responseDto));
        } catch (Exception e) {
            log.error("Error creating tutoring request", e);
            throw e;
        }
    }
    
    @PatchMapping("/{requestId}/status")
    public ResponseEntity<OkResponseDto<TutoringRequestDto>> updateTutoringRequestStatus(
            @PathVariable String requestId,
            @Valid @RequestBody UpdateTutoringRequestStatusDto updateStatusDto) {
        
        try {
            User currentUser = getCurrentUserOrThrow();
            log.info("User {} updating tutoring request {} status to: {}", 
                    currentUser.getEmail(), requestId, updateStatusDto.getStatus());
            
            // Only admins can update tutoring request status
            if (currentUser.getRol() != RolUsuario.Administrador) {
                log.warn("User {} attempted to update tutoring request status without admin privileges", 
                        currentUser.getEmail());
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(OkResponseDto.of("No tiene permisos para actualizar el estado de solicitudes", null));
            }
            
            TutoringRequest updatedRequest = updateTutoringRequestStatusUseCase
                    .updateStatus(requestId, updateStatusDto.getStatus());
            
            TutoringRequestDto responseDto = tutoringRequestDtoMapper.toDto(updatedRequest);
            
            log.info("User {} successfully updated tutoring request {} status to: {}", 
                    currentUser.getEmail(), requestId, updatedRequest.getRequestStatus());
            
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(OkResponseDto.of("Estado de solicitud de tutoría actualizado exitosamente", responseDto));
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.error("Validation error updating tutoring request status: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error updating tutoring request status", e);
            throw e;
        }
    }
    
    @GetMapping
    public ResponseEntity<OkResponseDto<List<TutoringRequestDto>>> getTutoringRequests(
            TutoringRequestFilterDto filterDto) {
        
        try {
            User currentUser = getCurrentUserOrThrow();
            log.info("User {} retrieving tutoring requests with filters: {}", 
                    currentUser.getEmail(), filterDto);
            
            List<TutoringRequest> requests;
            
            // Non-admin users can only see their own requests
            if (currentUser.getRol() != RolUsuario.Administrador) {
                log.debug("Non-admin user {} filtering requests to own requests only", currentUser.getEmail());
                if (filterDto == null) {
                    filterDto = new TutoringRequestFilterDto();
                }
                filterDto.setTuteeId(currentUser.getId());
            }
            
            if (filterDto == null) {
                requests = getTutoringRequestsUseCase.getAllTutoringRequests();
            } else {
                requests = getTutoringRequestsUseCase.getTutoringRequestsWithFilters(
                        filterDto.getTuteeId(), 
                        filterDto.getSkillId(), 
                        filterDto.getStatus(),
                        filterDto.getChapterId());
            }
            
            List<TutoringRequestDto> responseDtos = requests.stream()
                    .map(tutoringRequestDtoMapper::toDto)
                    .collect(Collectors.toList());
            
            log.info("User {} retrieved {} tutoring requests", currentUser.getEmail(), responseDtos.size());
            
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(OkResponseDto.of("Solicitudes de tutoría obtenidas exitosamente", responseDtos));
        } catch (Exception e) {
            log.error("Error retrieving tutoring requests", e);
            throw e;
        }
    }
    
    @GetMapping("/my-requests")
    public ResponseEntity<OkResponseDto<List<TutoringRequestDto>>> getMyTutoringRequests() {
        try {
            User currentUser = getCurrentUserOrThrow();
            log.info("User {} retrieving own tutoring requests", currentUser.getEmail());
            
            List<TutoringRequest> requests = getTutoringRequestsUseCase.getTutoringRequestsWithFilters(
                    currentUser.getId(), null, null, null);
            
            List<TutoringRequestDto> responseDtos = requests.stream()
                    .map(tutoringRequestDtoMapper::toDto)
                    .collect(Collectors.toList());
            
            log.info("User {} has {} tutoring requests", currentUser.getEmail(), responseDtos.size());
            
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(OkResponseDto.of("Mis solicitudes de tutoría obtenidas exitosamente", responseDtos));
        } catch (Exception e) {
            log.error("Error retrieving user's own tutoring requests", e);
            throw e;
        }
    }
    
    /**
     * Gets the current authenticated user from UserContext.
     * 
     * @return the current user
     * @throws IllegalStateException if no user is authenticated
     */
    private User getCurrentUserOrThrow() {
        if (!UserContext.hasCurrentUser()) {
            log.error("No authenticated user found in context");
            throw new IllegalStateException("No authenticated user found");
        }
        return UserContext.getCurrentUser();
    }
}