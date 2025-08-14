package com.pragma.tutorings.infrastructure.adapter.input.rest;

import com.pragma.shared.context.UserContextHelper;
import com.pragma.shared.dto.OkResponseDto;
import com.pragma.tutorings.domain.model.Tutoring;
import com.pragma.tutorings.domain.port.input.CancelTutoringUseCase;
import com.pragma.tutorings.domain.port.input.CompleteTutoringUseCase;
import com.pragma.tutorings.domain.port.input.CreateTutoringUseCase;
import com.pragma.tutorings.infrastructure.adapter.input.rest.dto.CompleteTutoringDto;
import com.pragma.tutorings.infrastructure.adapter.input.rest.dto.CreateTutoringDto;
import com.pragma.tutorings.infrastructure.adapter.input.rest.dto.TutoringDto;
import com.pragma.tutorings.infrastructure.adapter.input.rest.dto.UpdateTutoringStatusDto;
import com.pragma.tutorings.infrastructure.adapter.input.rest.mapper.TutoringDtoMapper;
import com.pragma.usuarios.domain.model.User;
import com.pragma.usuarios.domain.model.enums.RolUsuario;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tutorings")
@RequiredArgsConstructor
@Slf4j
public class TutoringController {

    private final CreateTutoringUseCase createTutoringUseCase;
    private final CompleteTutoringUseCase completeTutoringUseCase;
    private final CancelTutoringUseCase cancelTutoringUseCase;
    private final TutoringDtoMapper tutoringDtoMapper;

    @PostMapping
    public ResponseEntity<OkResponseDto<TutoringDto>> createTutoring(@Valid @RequestBody CreateTutoringDto createTutoringDto) {
        User currentUser = UserContextHelper.getCurrentUserOrThrow();
        log.info("User {} creating tutoring for request: {} with tutor: {}", 
                currentUser.getEmail(), createTutoringDto.getTutoringRequestId(), createTutoringDto.getTutorId());
        
        // Only admins can create tutorings
        UserContextHelper.requireAdminRole();
        
        Tutoring tutoring = createTutoringUseCase.createTutoring(
                createTutoringDto.getTutoringRequestId(),
                createTutoringDto.getTutorId(),
                createTutoringDto.getObjectives()
        );
        
        TutoringDto tutoringDto = tutoringDtoMapper.toDto(tutoring);
        
        log.info("User {} successfully created tutoring with ID: {}", currentUser.getEmail(), tutoring.getId());
        
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(OkResponseDto.of("Tutoría creada exitosamente", tutoringDto));
    }
    
    @PatchMapping("/{tutoringId}/complete")
    public ResponseEntity<OkResponseDto<TutoringDto>> completeTutoring(
            @PathVariable String tutoringId,
            @Valid @RequestBody CompleteTutoringDto completeDto) {
        
        User currentUser = UserContextHelper.getCurrentUserOrThrow();
        log.info("User {} completing tutoring: {}", currentUser.getEmail(), tutoringId);
        
        // Validate that user can complete tutorings (tutors and admins)
        if (!UserContextHelper.canActAsTutor()) {
            log.warn("User {} attempted to complete tutoring without tutor privileges", currentUser.getEmail());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(OkResponseDto.of("No tiene permisos para completar tutorías", null));
        }
        
        // Set the current user as the one completing the tutoring
        completeDto.setUserId(currentUser.getId());
        
        Tutoring tutoring = completeTutoringUseCase.completeTutoring(
                tutoringId, completeDto.getUserId(), completeDto.getFinalActUrl());
        TutoringDto tutoringDto = tutoringDtoMapper.toDto(tutoring);
        
        log.info("User {} successfully completed tutoring: {}", currentUser.getEmail(), tutoringId);
        
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(OkResponseDto.of("Tutoría marcada como completada exitosamente", tutoringDto));
    }
    
    @PatchMapping("/{tutoringId}/cancel")
    public ResponseEntity<OkResponseDto<TutoringDto>> cancelTutoring(
            @PathVariable String tutoringId,
            @Valid @RequestBody UpdateTutoringStatusDto updateDto) {
        
        User currentUser = UserContextHelper.getCurrentUserOrThrow();
        log.info("User {} canceling tutoring: {} with reason: {}", 
                currentUser.getEmail(), tutoringId, updateDto.getComments());
        
        // Validate that user can cancel tutorings (tutors and admins)
        if (!UserContextHelper.canActAsTutor()) {
            log.warn("User {} attempted to cancel tutoring without tutor privileges", currentUser.getEmail());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(OkResponseDto.of("No tiene permisos para cancelar tutorías", null));
        }
        
        // Set the current user as the one canceling the tutoring
        updateDto.setUserId(currentUser.getId());
        
        Tutoring tutoring = cancelTutoringUseCase.cancelTutoring(
                tutoringId, updateDto.getUserId(), updateDto.getComments());
        TutoringDto tutoringDto = tutoringDtoMapper.toDto(tutoring);
        
        log.info("User {} successfully canceled tutoring: {}", currentUser.getEmail(), tutoringId);
        
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(OkResponseDto.of("Tutoría cancelada exitosamente", tutoringDto));
    }
}