package com.pragma.tutorings.infrastructure.adapter.input.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pragma.shared.config.AuthenticationProperties;
import com.pragma.shared.context.UserContext;
import com.pragma.shared.context.UserContextHelper;
import com.pragma.shared.dto.OkResponseDto;
import com.pragma.shared.security.GoogleAuthInterceptor;
import com.pragma.shared.service.MessageService;
import com.pragma.tutorings.domain.model.Tutoring;
import com.pragma.tutorings.domain.model.enums.TutoringStatus;
import com.pragma.tutorings.domain.port.input.CancelTutoringUseCase;
import com.pragma.tutorings.domain.port.input.CompleteTutoringUseCase;
import com.pragma.tutorings.infrastructure.adapter.input.rest.dto.CompleteTutoringDto;
import com.pragma.tutorings.infrastructure.adapter.input.rest.dto.TutoringDto;
import com.pragma.tutorings.infrastructure.adapter.input.rest.dto.UpdateTutoringStatusDto;
import com.pragma.tutorings.infrastructure.adapter.input.rest.mapper.TutoringDtoMapper;
import com.pragma.usuarios.application.service.UserService;
import com.pragma.usuarios.domain.model.User;
import com.pragma.usuarios.domain.model.enums.RolUsuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TutoringController.class)
class TutoringStatusControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CompleteTutoringUseCase completeTutoringUseCase;

    @MockBean
    private CancelTutoringUseCase cancelTutoringUseCase;

    @MockBean
    private TutoringDtoMapper tutoringDtoMapper;

    @MockBean
    private com.pragma.tutorings.domain.port.input.CreateTutoringUseCase createTutoringUseCase;

    @MockBean
    private UserService userService;

    @MockBean
    private MessageService messageService;

    @MockBean
    private AuthenticationProperties authenticationProperties;

    @MockBean
    private GoogleAuthInterceptor googleAuthInterceptor;

    private Tutoring tutoring;
    private TutoringDto tutoringDto;
    private CompleteTutoringDto completeDto;
    private UpdateTutoringStatusDto updateDto;

    @BeforeEach
    void setUp() {
        // Configurar MessageService mock
        when(messageService.getMessage(anyString())).thenReturn("Test message");
        UserContextHelper.setMessageServiceForTesting(messageService);
        
        // Configurar datos de prueba
        User tutor = new User();
        tutor.setId("tutor-id");
        tutor.setFirstName("Tutor");
        tutor.setLastName("Test");

        User tutee = new User();
        tutee.setId("tutee-id");
        tutee.setFirstName("Tutee");
        tutee.setLastName("Test");

        tutoring = new Tutoring();
        tutoring.setId("tutoring-id");
        tutoring.setTutor(tutor);
        tutoring.setTutee(tutee);
        tutoring.setStartDate(new Date());
        tutoring.setExpectedEndDate(new Date());
        tutoring.setStatus(TutoringStatus.Completada);
        tutoring.setObjectives("Objetivos de prueba");

        tutoringDto = new TutoringDto();
        tutoringDto.setId("tutoring-id");
        tutoringDto.setStatus(TutoringStatus.Completada);
        tutoringDto.setObjectives("Objetivos de prueba");

        completeDto = new CompleteTutoringDto();
        completeDto.setUserId("user-id");
        completeDto.setFinalActUrl("http://example.com/final-act.pdf");

        updateDto = new UpdateTutoringStatusDto();
        updateDto.setUserId("user-id");
    }

    @Test
    void completeTutoring_Success() throws Exception {
        // Arrange - Configurar usuario con rol de tutor
        User testUser = new User();
        testUser.setId("test-user-id");
        testUser.setEmail("test@example.com");
        testUser.setRol(RolUsuario.Tutor);
        UserContext.setCurrentUser(testUser);
        
        when(googleAuthInterceptor.preHandle(any(), any(), any())).thenReturn(true);
        when(completeTutoringUseCase.completeTutoring(anyString(), anyString(), anyString())).thenReturn(tutoring);
        when(tutoringDtoMapper.toDto(tutoring)).thenReturn(tutoringDto);

        try {
            // Act & Assert
            mockMvc.perform(patch("/api/v1/tutorings/tutoring-id/complete")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(completeDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Tutoría marcada como completada exitosamente"))
                    .andExpect(jsonPath("$.data.id").value("tutoring-id"))
                    .andExpect(jsonPath("$.data.status").value("Completada"));
        } finally {
            // Cleanup
            UserContext.clear();
        }
    }

    @Test
    void cancelTutoring_Success() throws Exception {
        // Arrange - Configurar usuario con rol de tutor
        User testUser = new User();
        testUser.setId("test-user-id");
        testUser.setEmail("test@example.com");
        testUser.setRol(RolUsuario.Tutor);
        UserContext.setCurrentUser(testUser);
        
        when(googleAuthInterceptor.preHandle(any(), any(), any())).thenReturn(true);
        tutoring.setStatus(TutoringStatus.Cancelada);
        tutoringDto.setStatus(TutoringStatus.Cancelada);
        updateDto.setComments("Comentario de cancelación");
        
        when(cancelTutoringUseCase.cancelTutoring(anyString(), anyString(), anyString())).thenReturn(tutoring);
        when(tutoringDtoMapper.toDto(tutoring)).thenReturn(tutoringDto);

        try {
            // Act & Assert
            mockMvc.perform(patch("/api/v1/tutorings/tutoring-id/cancel")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Tutoría cancelada exitosamente"))
                    .andExpect(jsonPath("$.data.id").value("tutoring-id"))
                    .andExpect(jsonPath("$.data.status").value("Cancelada"));
        } finally {
            // Cleanup
            UserContext.clear();
        }
    }
}