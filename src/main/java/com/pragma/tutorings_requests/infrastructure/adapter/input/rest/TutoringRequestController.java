package com.pragma.tutorings_requests.infrastructure.adapter.input.rest;

import com.pragma.shared.context.UserContext;
import com.pragma.shared.context.UserContextHelper;
import com.pragma.shared.dto.OkResponseDto;
import com.pragma.shared.service.MessageService;
import com.pragma.tutorings_requests.domain.model.TutoringRequest;
import com.pragma.tutorings_requests.domain.port.input.CreateTutoringRequestUseCase;
import com.pragma.tutorings_requests.domain.port.input.GetTutoringRequestsUseCase;
import com.pragma.tutorings_requests.domain.port.input.UpdateTutoringRequestStatusUseCase;
import com.pragma.tutorings_requests.infrastructure.adapter.input.rest.dto.CreateTutoringRequestDto;
import com.pragma.tutorings_requests.infrastructure.adapter.input.rest.dto.MyRequestsResponseDto;
import com.pragma.tutorings_requests.infrastructure.adapter.input.rest.dto.TutoringRequestDto;
import com.pragma.tutorings_requests.infrastructure.adapter.input.rest.dto.TutoringRequestFilterDto;
import com.pragma.tutorings_requests.infrastructure.adapter.input.rest.dto.UpdateTutoringRequestStatusDto;
import com.pragma.tutorings_requests.domain.model.enums.RequestStatus;
import com.pragma.tutorings.domain.model.Tutoring;
import com.pragma.tutorings.domain.port.input.GetTutoringsUseCase;
import com.pragma.tutorings.infrastructure.adapter.input.rest.mapper.TutoringDtoMapper;
import com.pragma.tutorings_requests.infrastructure.adapter.input.rest.mapper.TutoringRequestDtoMapper;
import com.pragma.usuarios.domain.model.User;
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
    private final GetTutoringsUseCase getTutoringsUseCase;
    private final TutoringRequestDtoMapper tutoringRequestDtoMapper;
    private final TutoringDtoMapper tutoringDtoMapper;
    private final MessageService messageService;

    @PostMapping
    public ResponseEntity<OkResponseDto<TutoringRequestDto>> createTutoringRequest(
            @Valid @RequestBody CreateTutoringRequestDto createTutoringRequestDto) {
        
        try {
            User currentUser = UserContextHelper.getCurrentUserOrThrow();
            log.info("User {} creating tutoring request for skills: {}", 
                    currentUser.getEmail(), createTutoringRequestDto.getSkillIds());
            
            // Set the current user as the tutee
            // Non-admin users can only see their own requests
            if (!UserContextHelper.isCurrentUserAdmin()) {
                createTutoringRequestDto.setTuteeId(currentUser.getId());
            }

            TutoringRequest tutoringRequest = tutoringRequestDtoMapper.toModel(createTutoringRequestDto);
            TutoringRequest createdRequest = createTutoringRequestUseCase.createTutoringRequest(tutoringRequest);
            TutoringRequestDto responseDto = tutoringRequestDtoMapper.toDto(createdRequest);
            
            log.info("User {} successfully created tutoring request with ID: {}", 
                    currentUser.getEmail(), createdRequest.getId());
            
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(OkResponseDto.of(messageService.getMessage("tutoringRequest.created.success"), responseDto));
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
            log.info("User {} updating tutoring request {} status to: {}", 
                    UserContextHelper.getCurrentUserEmail(), requestId, updateStatusDto.getStatus());
            
            // Admins and tutors can update tutoring request status
            if (!UserContextHelper.isCurrentUserAdmin() && !UserContextHelper.canActAsTutor()) {
                throw new SecurityException("Solo administradores y tutores pueden actualizar el estado de solicitudes");
            }
            
            TutoringRequest updatedRequest = updateTutoringRequestStatusUseCase
                    .updateStatus(requestId, updateStatusDto.getStatus());
            
            TutoringRequestDto responseDto = tutoringRequestDtoMapper.toDto(updatedRequest);
            
            log.info("User {} successfully updated tutoring request {} status to: {}", 
                    UserContextHelper.getCurrentUserEmail(), requestId, updatedRequest.getRequestStatus());
            
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(OkResponseDto.of(messageService.getMessage("tutoringRequest.status.updated.success"), responseDto));
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
            User currentUser = UserContextHelper.getCurrentUserOrThrow();
            log.info("User {} retrieving tutoring requests with filters: {}", 
                    currentUser.getEmail(), filterDto);
            
            List<TutoringRequest> requests;
            
            // Non-admin users can only see their own requests
            if (!UserContextHelper.isCurrentUserAdmin()) {
                log.debug("Non-admin user {} filtering requests to own requests only", currentUser.getEmail());
                if (filterDto == null) {
                    filterDto = new TutoringRequestFilterDto();
                }
                filterDto.setTuteeId(currentUser.getId());
            }
            
            // Check if filterDto is null or empty (all fields are null)
            boolean isEmptyFilter = filterDto == null || 
                    (filterDto.getTuteeId() == null && 
                     filterDto.getSkillId() == null && 
                     filterDto.getStatus() == null && 
                     filterDto.getChapterId() == null);
            
            if (isEmptyFilter) {
                requests = getTutoringRequestsUseCase.getAllTutoringRequests();
            } else {
                requests = getTutoringRequestsUseCase.getTutoringRequestsWithFilters(
                        filterDto.getTuteeId(), 
                        filterDto.getSkillId(), 
                        filterDto.getStatus(),
                        filterDto.getChapterId());
            }
            
            List<TutoringRequestDto> responseDtos = requests.stream()
                    .filter(request -> request.getRequestStatus() != RequestStatus.Asignada)
                    .map(tutoringRequestDtoMapper::toDto)
                    .collect(Collectors.toList());
            
            log.info("User {} retrieved {} tutoring requests", currentUser.getEmail(), responseDtos.size());
            
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(OkResponseDto.of(messageService.getMessage("tutoringRequest.retrieved.success"), responseDtos));
        } catch (Exception e) {
            log.error("Error retrieving tutoring requests", e);
            throw e;
        }
    }
    
    @GetMapping("/my-requests")
    public ResponseEntity<OkResponseDto<MyRequestsResponseDto>> getMyTutoringRequests() {
        try {
            User currentUser = UserContextHelper.getCurrentUserOrThrow();
            log.info("User {} retrieving own tutoring requests and tutorings", currentUser.getEmail());
            
            // Get tutoring requests
            List<TutoringRequest> requests = getTutoringRequestsUseCase.getTutoringRequestsWithFilters(
                    currentUser.getId(), null, null, null);
            List<TutoringRequestDto> requestDtos = requests.stream()
                    .filter(request -> request.getRequestStatus() != RequestStatus.Asignada)
                    .map(tutoringRequestDtoMapper::toDto)
                    .collect(Collectors.toList());
            
            MyRequestsResponseDto response = new MyRequestsResponseDto();
            response.setRequests(requestDtos);
            
            // If user is tutor, get tutorings where they are tutor and tutee
            if (UserContextHelper.canActAsTutor()) {
                List<Tutoring> tutoringsAsTutor = getTutoringsUseCase.getTutoringsByTutorId(currentUser.getId());
                List<Tutoring> tutoringsAsTutee = getTutoringsUseCase.getTutoringsByTuteeId(currentUser.getId());
                
                response.setTutoringsAsTutor(tutoringDtoMapper.toDtoList(tutoringsAsTutor));
                response.setTutoringsAsTutee(tutoringDtoMapper.toDtoList(tutoringsAsTutee));
                
                log.info("User {} has {} requests, {} tutorings as tutor, {} tutorings as tutee", 
                        currentUser.getEmail(), requestDtos.size(), tutoringsAsTutor.size(), tutoringsAsTutee.size());
            } else {
                // Non-tutors only get tutorings where they are tutee
                List<Tutoring> tutoringsAsTutee = getTutoringsUseCase.getTutoringsByTuteeId(currentUser.getId());
                response.setTutoringsAsTutee(tutoringDtoMapper.toDtoList(tutoringsAsTutee));
                
                log.info("User {} has {} requests, {} tutorings as tutee", 
                        currentUser.getEmail(), requestDtos.size(), tutoringsAsTutee.size());
            }
            
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(OkResponseDto.of("Mis solicitudes y tutorías obtenidas exitosamente", response));
        } catch (Exception e) {
            log.error("Error retrieving user's own tutoring requests and tutorings", e);
            throw e;
        }
    }
}