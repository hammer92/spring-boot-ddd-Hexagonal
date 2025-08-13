package com.pragma.tutorings.infrastructure.adapter.input.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pragma.config.TestConfig;
import com.pragma.shared.context.TestUserContextHelper;
import com.pragma.tutorings.domain.model.Tutoring;
import com.pragma.tutorings.domain.model.enums.TutoringStatus;
import com.pragma.tutorings.domain.port.input.CreateTutoringUseCase;
import com.pragma.tutorings.infrastructure.adapter.input.rest.dto.CreateTutoringDto;
import com.pragma.tutorings.infrastructure.adapter.input.rest.dto.TutoringDto;
import com.pragma.tutorings.infrastructure.adapter.input.rest.mapper.TutoringDtoMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.Date;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
@ActiveProfiles("test")
@Import(TestConfig.class)
class TutoringControllerTest {

    @Mock
    private CreateTutoringUseCase createTutoringUseCase;

    @Mock
    private TutoringDtoMapper tutoringDtoMapper;

    @InjectMocks
    private TutoringController tutoringController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private Tutoring tutoring;
    private TutoringDto tutoringDto;
    private CreateTutoringDto createTutoringDto;

    @BeforeEach
    void setUp() {
        TestUserContextHelper.setTestUserContext();
        mockMvc = MockMvcBuilders.standaloneSetup(tutoringController).build();
        objectMapper = new ObjectMapper();

        // Configurar datos de prueba
        tutoring = new Tutoring();
        tutoring.setId("tutoring-id");
        tutoring.setStartDate(new Date());
        tutoring.setExpectedEndDate(new Date());
        tutoring.setStatus(TutoringStatus.Activa);
        tutoring.setObjectives("Objetivos de prueba");
        tutoring.setSkills(new ArrayList<>());

        tutoringDto = new TutoringDto();
        tutoringDto.setId("tutoring-id");
        tutoringDto.setStartDate(new Date());
        tutoringDto.setExpectedEndDate(new Date());
        tutoringDto.setStatus(TutoringStatus.Activa);
        tutoringDto.setObjectives("Objetivos de prueba");
        tutoringDto.setSkills(new ArrayList<>());

        createTutoringDto = new CreateTutoringDto();
        createTutoringDto.setTutoringRequestId("request-id");
        createTutoringDto.setTutorId("tutor-id");
        createTutoringDto.setObjectives("Objetivos de prueba");
    }

    @AfterEach
    void tearDown() {
        TestUserContextHelper.clearUserContext();
    }

    @Test
    void createTutoring_Success() throws Exception {
        // Arrange
        when(createTutoringUseCase.createTutoring(anyString(), anyString(), anyString())).thenReturn(tutoring);
        when(tutoringDtoMapper.toDto(any(Tutoring.class))).thenReturn(tutoringDto);

        // Act & Assert
        mockMvc.perform(post("/api/v1/tutorings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createTutoringDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Tutoría creada exitosamente"))
                .andExpect(jsonPath("$.data.id").value("tutoring-id"))
                .andExpect(jsonPath("$.data.objectives").value("Objetivos de prueba"))
                .andExpect(jsonPath("$.data.status").value("Activa"));
    }

    @Test
    void createTutoring_ValidationError() throws Exception {
        // Arrange
        createTutoringDto.setTutoringRequestId("");
        createTutoringDto.setTutorId(null);

        // Act & Assert
        mockMvc.perform(post("/api/v1/tutorings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createTutoringDto)))
                .andExpect(status().isBadRequest());
    }
}