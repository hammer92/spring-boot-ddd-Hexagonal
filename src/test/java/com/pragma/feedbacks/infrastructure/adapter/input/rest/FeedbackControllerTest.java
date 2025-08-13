package com.pragma.feedbacks.infrastructure.adapter.input.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pragma.feedbacks.domain.model.Feedback;
import com.pragma.feedbacks.domain.port.input.CreateFeedbackUseCase;
import com.pragma.feedbacks.infrastructure.adapter.input.rest.dto.CreateFeedbackDto;
import com.pragma.feedbacks.infrastructure.adapter.input.rest.dto.FeedbackDto;
import com.pragma.feedbacks.infrastructure.adapter.input.rest.mapper.FeedbackDtoMapper;
import com.pragma.shared.context.TestUserContextHelper;
import com.pragma.tutorings.domain.model.Tutoring;
import com.pragma.usuarios.domain.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Date;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class FeedbackControllerTest {

    @Mock
    private CreateFeedbackUseCase createFeedbackUseCase;

    @Mock
    private FeedbackDtoMapper feedbackDtoMapper;

    @InjectMocks
    private FeedbackController feedbackController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private CreateFeedbackDto createFeedbackDto;
    private Feedback feedback;
    private FeedbackDto feedbackDto;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(feedbackController).build();
        objectMapper = new ObjectMapper();

        // Configurar datos de prueba
        createFeedbackDto = new CreateFeedbackDto();
        createFeedbackDto.setEvaluatorId("evaluator-id");
        createFeedbackDto.setTutoringId("tutoring-id");
        createFeedbackDto.setScore("5");
        createFeedbackDto.setComments("Excelente tutoría");

        User evaluator = new User();
        evaluator.setId("evaluator-id");
        evaluator.setFirstName("Evaluator");
        evaluator.setLastName("Test");

        Tutoring tutoring = new Tutoring();
        tutoring.setId("tutoring-id");

        feedback = new Feedback();
        feedback.setId("feedback-id");
        feedback.setEvaluator(evaluator);
        feedback.setTutoring(tutoring);
        feedback.setEvaluationDate(new Date());
        feedback.setScore("5");
        feedback.setComments("Excelente tutoría");

        feedbackDto = new FeedbackDto();
        feedbackDto.setId("feedback-id");
        feedbackDto.setScore("5");
        feedbackDto.setComments("Excelente tutoría");
        feedbackDto.setEvaluationDate(new Date());
        
        // Set up user context for authentication
        TestUserContextHelper.setTestUserContext();
    }
    
    @AfterEach
    void tearDown() {
        // Clean up user context after each test
        TestUserContextHelper.clearUserContext();
    }

    @Test
    void createFeedback_Success() throws Exception {
        // Arrange
        when(feedbackDtoMapper.toDomain(any(CreateFeedbackDto.class))).thenReturn(feedback);
        when(createFeedbackUseCase.createFeedback(any(Feedback.class))).thenReturn(feedback);
        when(feedbackDtoMapper.toDto(any(Feedback.class))).thenReturn(feedbackDto);

        // Act & Assert
        mockMvc.perform(post("/api/v1/feedbacks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createFeedbackDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Feedback creado exitosamente"))
                .andExpect(jsonPath("$.data.id").value("feedback-id"))
                .andExpect(jsonPath("$.data.score").value("5"))
                .andExpect(jsonPath("$.data.comments").value("Excelente tutoría"));
    }

    @Test
    void createFeedback_ValidationError() throws Exception {
        // Arrange
        createFeedbackDto.setEvaluatorId("");
        createFeedbackDto.setTutoringId(null);

        // Act & Assert
        mockMvc.perform(post("/api/v1/feedbacks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createFeedbackDto)))
                .andExpect(status().isBadRequest());
    }
}