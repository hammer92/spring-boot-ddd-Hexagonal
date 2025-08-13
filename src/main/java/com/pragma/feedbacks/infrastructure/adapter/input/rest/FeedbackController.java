package com.pragma.feedbacks.infrastructure.adapter.input.rest;

import com.pragma.feedbacks.domain.model.Feedback;
import com.pragma.feedbacks.domain.port.input.CreateFeedbackUseCase;
import com.pragma.feedbacks.infrastructure.adapter.input.rest.dto.CreateFeedbackDto;
import com.pragma.feedbacks.infrastructure.adapter.input.rest.dto.FeedbackDto;
import com.pragma.feedbacks.infrastructure.adapter.input.rest.mapper.FeedbackDtoMapper;
import com.pragma.shared.context.UserContext;
import com.pragma.shared.dto.OkResponseDto;
import com.pragma.usuarios.domain.model.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/feedbacks")
@RequiredArgsConstructor
@Slf4j
public class FeedbackController {

    private final CreateFeedbackUseCase createFeedbackUseCase;
    private final FeedbackDtoMapper feedbackDtoMapper;

    @PostMapping
    public ResponseEntity<OkResponseDto<FeedbackDto>> createFeedback(@Valid @RequestBody CreateFeedbackDto createFeedbackDto) {
        User currentUser = getCurrentUserOrThrow();
        log.info("User {} creating feedback for tutoring: {}", 
                currentUser.getEmail(), createFeedbackDto.getTutoringId());
        
        // Set the current user as the evaluator
        createFeedbackDto.setEvaluatorId(currentUser.getId());
        
        Feedback feedback = feedbackDtoMapper.toDomain(createFeedbackDto);
        Feedback createdFeedback = createFeedbackUseCase.createFeedback(feedback);
        FeedbackDto feedbackDto = feedbackDtoMapper.toDto(createdFeedback);
        
        log.info("User {} successfully created feedback with ID: {} for tutoring: {}", 
                currentUser.getEmail(), createdFeedback.getId(), createdFeedback.getTutoring().getId());
        
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(OkResponseDto.of("Feedback creado exitosamente", feedbackDto));
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