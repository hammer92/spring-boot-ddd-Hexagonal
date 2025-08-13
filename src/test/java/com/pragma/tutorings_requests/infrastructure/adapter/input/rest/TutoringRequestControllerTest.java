package com.pragma.tutorings_requests.infrastructure.adapter.input.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pragma.shared.context.TestUserContextHelper;
import com.pragma.shared.dto.OkResponseDto;
import com.pragma.tutorings_requests.domain.model.TutoringRequest;
import com.pragma.tutorings_requests.domain.model.enums.RequestStatus;
import com.pragma.tutorings_requests.domain.port.input.CreateTutoringRequestUseCase;
import com.pragma.tutorings_requests.domain.port.input.UpdateTutoringRequestStatusUseCase;
import com.pragma.tutorings_requests.infrastructure.adapter.input.rest.dto.CreateTutoringRequestDto;
import com.pragma.tutorings_requests.infrastructure.adapter.input.rest.dto.TutoringRequestDto;
import com.pragma.tutorings_requests.infrastructure.adapter.input.rest.dto.UpdateTutoringRequestStatusDto;
import com.pragma.tutorings_requests.infrastructure.adapter.input.rest.mapper.TutoringRequestDtoMapper;
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

import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TutoringRequestControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CreateTutoringRequestUseCase createTutoringRequestUseCase;

    @Mock
    private UpdateTutoringRequestStatusUseCase updateTutoringRequestStatusUseCase;

    @Mock
    private TutoringRequestDtoMapper tutoringRequestDtoMapper;

    @InjectMocks
    private TutoringRequestController tutoringRequestController;

    private ObjectMapper objectMapper;
    private TutoringRequest tutoringRequest;
    private TutoringRequestDto tutoringRequestDto;
    private String requestId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(tutoringRequestController).build();
        objectMapper = new ObjectMapper();
        requestId = UUID.randomUUID().toString();

        tutoringRequest = new TutoringRequest();
        tutoringRequest.setId(requestId);
        tutoringRequest.setRequestStatus(RequestStatus.Enviada);
        tutoringRequest.setRequestDate(new Date());

        tutoringRequestDto = new TutoringRequestDto();
        tutoringRequestDto.setId(requestId);
        tutoringRequestDto.setRequestStatus(RequestStatus.Enviada);
        
        // Set up user context for authentication
        TestUserContextHelper.setTestUserContext();
    }
    
    @AfterEach
    void tearDown() {
        // Clean up user context after each test
        TestUserContextHelper.clearUserContext();
    }

    @Test
    void updateTutoringRequestStatus_Success() throws Exception {
        // Arrange
        UpdateTutoringRequestStatusDto updateStatusDto = new UpdateTutoringRequestStatusDto();
        updateStatusDto.setStatus(RequestStatus.Aprobada);

        TutoringRequest updatedRequest = new TutoringRequest();
        updatedRequest.setId(requestId);
        updatedRequest.setRequestStatus(RequestStatus.Aprobada);

        TutoringRequestDto updatedDto = new TutoringRequestDto();
        updatedDto.setId(requestId);
        updatedDto.setRequestStatus(RequestStatus.Aprobada);

        when(updateTutoringRequestStatusUseCase.updateStatus(eq(requestId), eq(RequestStatus.Aprobada)))
                .thenReturn(updatedRequest);
        when(tutoringRequestDtoMapper.toDto(updatedRequest)).thenReturn(updatedDto);

        // Act & Assert
        mockMvc.perform(patch("/api/v1/tutoring-requests/{requestId}/status", requestId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateStatusDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(requestId))
                .andExpect(jsonPath("$.data.requestStatus").value("Aprobada"));
    }

    @Test
    void updateTutoringRequestStatus_ToRejected_Success() throws Exception {
        // Arrange
        UpdateTutoringRequestStatusDto updateStatusDto = new UpdateTutoringRequestStatusDto();
        updateStatusDto.setStatus(RequestStatus.Rechazada);

        TutoringRequest updatedRequest = new TutoringRequest();
        updatedRequest.setId(requestId);
        updatedRequest.setRequestStatus(RequestStatus.Rechazada);

        TutoringRequestDto updatedDto = new TutoringRequestDto();
        updatedDto.setId(requestId);
        updatedDto.setRequestStatus(RequestStatus.Rechazada);

        when(updateTutoringRequestStatusUseCase.updateStatus(eq(requestId), eq(RequestStatus.Rechazada)))
                .thenReturn(updatedRequest);
        when(tutoringRequestDtoMapper.toDto(updatedRequest)).thenReturn(updatedDto);

        // Act & Assert
        mockMvc.perform(patch("/api/v1/tutoring-requests/{requestId}/status", requestId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateStatusDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(requestId))
                .andExpect(jsonPath("$.data.requestStatus").value("Rechazada"));
    }
}