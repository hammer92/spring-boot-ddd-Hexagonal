package com.pragma.tutorings.infrastructure.adapter.input.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pragma.shared.dto.OkResponseDto;
import com.pragma.tutorings.domain.model.Tutoring;
import com.pragma.tutorings.domain.model.enums.TutoringStatus;
import com.pragma.tutorings.domain.port.input.CancelTutoringUseCase;
import com.pragma.tutorings.domain.port.input.CompleteTutoringUseCase;
import com.pragma.tutorings.infrastructure.adapter.input.rest.dto.TutoringDto;
import com.pragma.tutorings.infrastructure.adapter.input.rest.dto.UpdateTutoringStatusDto;
import com.pragma.tutorings.infrastructure.adapter.input.rest.mapper.TutoringDtoMapper;
import com.pragma.usuarios.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TutoringController.class)
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

    // Necesario para que el controlador se inyecte correctamente
    @MockBean
    private com.pragma.tutorings.domain.port.input.CreateTutoringUseCase createTutoringUseCase;

    private Tutoring tutoring;
    private TutoringDto tutoringDto;
    private UpdateTutoringStatusDto updateDto;

    @BeforeEach
    void setUp() {
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

        updateDto = new UpdateTutoringStatusDto();
        updateDto.setUserId("user-id");
    }

    @Test
    void completeTutoring_Success() throws Exception {
        // Arrange
        when(completeTutoringUseCase.completeTutoring(anyString(), anyString(), anyString())).thenReturn(tutoring);
        when(tutoringDtoMapper.toDto(tutoring)).thenReturn(tutoringDto);

        // Act & Assert
        mockMvc.perform(patch("/api/v1/tutorings/tutoring-id/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Tutoría marcada como completada exitosamente"))
                .andExpect(jsonPath("$.data.id").value("tutoring-id"))
                .andExpect(jsonPath("$.data.status").value("Completada"));
    }

    @Test
    void cancelTutoring_Success() throws Exception {
        // Arrange
        tutoring.setStatus(TutoringStatus.Cancelada);
        tutoringDto.setStatus(TutoringStatus.Cancelada);
        updateDto.setComments("Comentario de cancelación");
        
        when(cancelTutoringUseCase.cancelTutoring(anyString(), anyString(), anyString())).thenReturn(tutoring);
        when(tutoringDtoMapper.toDto(tutoring)).thenReturn(tutoringDto);

        // Act & Assert
        mockMvc.perform(patch("/api/v1/tutorings/tutoring-id/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Tutoría cancelada exitosamente"))
                .andExpect(jsonPath("$.data.id").value("tutoring-id"))
                .andExpect(jsonPath("$.data.status").value("Cancelada"));
    }
}