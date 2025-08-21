package com.pragma.tutorings_requests.infrastructure.adapter.input.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pragma.shared.context.TestUserContextHelper;
import com.pragma.shared.service.MessageService;
import com.pragma.tutorings_requests.domain.model.TutoringRequest;
import com.pragma.tutorings_requests.domain.model.enums.RequestStatus;
import com.pragma.tutorings_requests.domain.port.input.GetTutoringRequestsUseCase;
import com.pragma.tutorings_requests.infrastructure.adapter.input.rest.dto.TutoringRequestDto;
import com.pragma.tutorings_requests.infrastructure.adapter.input.rest.mapper.TutoringRequestDtoMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TutoringRequestControllerGetTest {

    private MockMvc mockMvc;

    @Mock
    private GetTutoringRequestsUseCase getTutoringRequestsUseCase;

    @Mock
    private TutoringRequestDtoMapper tutoringRequestDtoMapper;

    @Mock
    private MessageService messageService;

    @InjectMocks
    private TutoringRequestController tutoringRequestController;

    private ObjectMapper objectMapper;
    private TutoringRequest tutoringRequest1;
    private TutoringRequest tutoringRequest2;
    private TutoringRequestDto tutoringRequestDto1;
    private TutoringRequestDto tutoringRequestDto2;
    private String tuteeId;
    private String skillId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(tutoringRequestController).build();
        objectMapper = new ObjectMapper();
        
        tuteeId = UUID.randomUUID().toString();
        skillId = UUID.randomUUID().toString();
        
        tutoringRequest1 = new TutoringRequest();
        tutoringRequest1.setId(UUID.randomUUID().toString());
        tutoringRequest1.setRequestStatus(RequestStatus.Pendiente);
        
        tutoringRequest2 = new TutoringRequest();
        tutoringRequest2.setId(UUID.randomUUID().toString());
        tutoringRequest2.setRequestStatus(RequestStatus.Aprobada);
        
        tutoringRequestDto1 = new TutoringRequestDto();
        tutoringRequestDto1.setId(tutoringRequest1.getId());
        tutoringRequestDto1.setRequestStatus(RequestStatus.Pendiente);
        tutoringRequestDto1.setSkills(Arrays.asList());
        
        tutoringRequestDto2 = new TutoringRequestDto();
        tutoringRequestDto2.setId(tutoringRequest2.getId());
        tutoringRequestDto2.setRequestStatus(RequestStatus.Aprobada);
        tutoringRequestDto2.setSkills(Arrays.asList());
        
        // Set up user context for authentication
        TestUserContextHelper.setTestUserContext();
        
        // Mock MessageService
        when(messageService.getMessage("tutoringRequest.retrieved.success"))
                .thenReturn("Tutoring requests retrieved successfully");
    }
    
    @AfterEach
    void tearDown() {
        // Clean up user context after each test
        TestUserContextHelper.clearUserContext();
    }

    @Test
    void getAllTutoringRequests_Success() throws Exception {
        // Arrange
        List<TutoringRequest> requests = Arrays.asList(tutoringRequest1, tutoringRequest2);
        List<TutoringRequestDto> requestDtos = Arrays.asList(tutoringRequestDto1, tutoringRequestDto2);
        
        // Since the test user is an admin, it should call getAllTutoringRequests
        when(getTutoringRequestsUseCase.getAllTutoringRequests()).thenReturn(requests);
        when(tutoringRequestDtoMapper.toDto(tutoringRequest1)).thenReturn(tutoringRequestDto1);
        when(tutoringRequestDtoMapper.toDto(tutoringRequest2)).thenReturn(tutoringRequestDto2);

        // Act & Assert
        mockMvc.perform(get("/api/v1/tutoring-requests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].id").value(tutoringRequestDto1.getId()))
                .andExpect(jsonPath("$.data[1].id").value(tutoringRequestDto2.getId()))
                .andExpect(jsonPath("$.data[0].skills").isArray())
                .andExpect(jsonPath("$.data[1].skills").isArray());
    }

    @Test
    void getTutoringRequestsWithFilters_OnlyTuteeId_Success() throws Exception {
        // Arrange
        List<TutoringRequest> requests = Arrays.asList(tutoringRequest1, tutoringRequest2);
        List<TutoringRequestDto> requestDtos = Arrays.asList(tutoringRequestDto1, tutoringRequestDto2);
        
        when(getTutoringRequestsUseCase.getTutoringRequestsWithFilters(eq(tuteeId), isNull(), isNull(), isNull())).thenReturn(requests);
        when(tutoringRequestDtoMapper.toDto(tutoringRequest1)).thenReturn(tutoringRequestDto1);
        when(tutoringRequestDtoMapper.toDto(tutoringRequest2)).thenReturn(tutoringRequestDto2);

        // Act & Assert
        mockMvc.perform(get("/api/v1/tutoring-requests")
                .param("tuteeId", tuteeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].id").value(tutoringRequestDto1.getId()))
                .andExpect(jsonPath("$.data[1].id").value(tutoringRequestDto2.getId()))
                .andExpect(jsonPath("$.data[0].skills").isArray())
                .andExpect(jsonPath("$.data[1].skills").isArray());
    }

    @Test
    void getTutoringRequestsWithFilters_OnlySkillId_Success() throws Exception {
        // Arrange
        List<TutoringRequest> requests = Arrays.asList(tutoringRequest1, tutoringRequest2);
        List<TutoringRequestDto> requestDtos = Arrays.asList(tutoringRequestDto1, tutoringRequestDto2);
        
        when(getTutoringRequestsUseCase.getTutoringRequestsWithFilters(isNull(), eq(skillId), isNull(), isNull())).thenReturn(requests);
        when(tutoringRequestDtoMapper.toDto(tutoringRequest1)).thenReturn(tutoringRequestDto1);
        when(tutoringRequestDtoMapper.toDto(tutoringRequest2)).thenReturn(tutoringRequestDto2);

        // Act & Assert
        mockMvc.perform(get("/api/v1/tutoring-requests")
                .param("skillId", skillId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].id").value(tutoringRequestDto1.getId()))
                .andExpect(jsonPath("$.data[1].id").value(tutoringRequestDto2.getId()))
                .andExpect(jsonPath("$.data[0].skills").isArray())
                .andExpect(jsonPath("$.data[1].skills").isArray());
    }

    @Test
    void getTutoringRequestsWithFilters_OnlyStatus_Success() throws Exception {
        // Arrange
        List<TutoringRequest> requests = Arrays.asList(tutoringRequest1);
        List<TutoringRequestDto> requestDtos = Arrays.asList(tutoringRequestDto1);
        
        when(getTutoringRequestsUseCase.getTutoringRequestsWithFilters(isNull(), isNull(), eq(RequestStatus.Pendiente), isNull())).thenReturn(requests);
        when(tutoringRequestDtoMapper.toDto(tutoringRequest1)).thenReturn(tutoringRequestDto1);

        // Act & Assert
        mockMvc.perform(get("/api/v1/tutoring-requests")
                .param("status", "Pendiente"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value(tutoringRequestDto1.getId()))
                .andExpect(jsonPath("$.data[0].requestStatus").value("Pendiente"))
                .andExpect(jsonPath("$.data[0].skills").isArray());
    }
    
    @Test
    void getTutoringRequestsWithFilters_AllFilters_Success() throws Exception {
        // Arrange
        List<TutoringRequest> requests = Arrays.asList(tutoringRequest1);
        List<TutoringRequestDto> requestDtos = Arrays.asList(tutoringRequestDto1);
        
        when(getTutoringRequestsUseCase.getTutoringRequestsWithFilters(eq(tuteeId), eq(skillId), eq(RequestStatus.Pendiente), isNull())).thenReturn(requests);
        when(tutoringRequestDtoMapper.toDto(tutoringRequest1)).thenReturn(tutoringRequestDto1);

        // Act & Assert
        mockMvc.perform(get("/api/v1/tutoring-requests")
                .param("tuteeId", tuteeId)
                .param("skillId", skillId)
                .param("status", "Pendiente"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value(tutoringRequestDto1.getId()))
                .andExpect(jsonPath("$.data[0].requestStatus").value("Pendiente"))
                .andExpect(jsonPath("$.data[0].skills").isArray());
    }
}