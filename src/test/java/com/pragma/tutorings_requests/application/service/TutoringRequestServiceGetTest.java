package com.pragma.tutorings_requests.application.service;

import com.pragma.skills.domain.model.Skill;
import com.pragma.tutorings_requests.domain.model.TutoringRequest;
import com.pragma.tutorings_requests.domain.model.enums.RequestStatus;
import com.pragma.tutorings_requests.domain.port.output.TutoringRequestRepository;
import com.pragma.usuarios.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TutoringRequestServiceGetTest {

    @Mock
    private TutoringRequestRepository tutoringRequestRepository;

    @InjectMocks
    private TutoringRequestService tutoringRequestService;

    private TutoringRequest tutoringRequest1;
    private TutoringRequest tutoringRequest2;
    private String tuteeId;
    private String skillId;

    @BeforeEach
    void setUp() {
        tuteeId = UUID.randomUUID().toString();
        skillId = UUID.randomUUID().toString();
        
        User tutee = new User();
        tutee.setId(tuteeId);
        
        Skill skill = new Skill();
        skill.setId(skillId);
        skill.setName("Java");
        
        tutoringRequest1 = new TutoringRequest();
        tutoringRequest1.setId(UUID.randomUUID().toString());
        tutoringRequest1.setTutee(tutee);
        tutoringRequest1.setSkills(Arrays.asList(skill));
        tutoringRequest1.setNeedsDescription("Necesito ayuda con Java");
        tutoringRequest1.setRequestDate(new Date());
        tutoringRequest1.setRequestStatus(RequestStatus.Enviada);
        
        tutoringRequest2 = new TutoringRequest();
        tutoringRequest2.setId(UUID.randomUUID().toString());
        tutoringRequest2.setTutee(tutee);
        tutoringRequest2.setSkills(Arrays.asList(skill));
        tutoringRequest2.setNeedsDescription("Necesito ayuda con Spring");
        tutoringRequest2.setRequestDate(new Date());
        tutoringRequest2.setRequestStatus(RequestStatus.Aprobada);
    }

    @Test
    void getAllTutoringRequests_Success() {
        // Arrange
        List<TutoringRequest> expectedRequests = Arrays.asList(tutoringRequest1, tutoringRequest2);
        when(tutoringRequestRepository.findAll()).thenReturn(expectedRequests);

        // Act
        List<TutoringRequest> result = tutoringRequestService.getAllTutoringRequests();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(expectedRequests, result);
        verify(tutoringRequestRepository, times(1)).findAll();
    }

    @Test
    void getTutoringRequestsWithFilters_OnlyTuteeId_Success() {
        // Arrange
        List<TutoringRequest> expectedRequests = Arrays.asList(tutoringRequest1, tutoringRequest2);
        when(tutoringRequestRepository.findWithFilters(tuteeId, null, null, null)).thenReturn(expectedRequests);

        // Act
        List<TutoringRequest> result = tutoringRequestService.getTutoringRequestsWithFilters(tuteeId, null, null, null);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(expectedRequests, result);
        verify(tutoringRequestRepository, times(1)).findWithFilters(tuteeId, null, null, null);
    }

    @Test
    void getTutoringRequestsWithFilters_OnlySkillId_Success() {
        // Arrange
        List<TutoringRequest> expectedRequests = Arrays.asList(tutoringRequest1, tutoringRequest2);
        when(tutoringRequestRepository.findWithFilters(null, skillId, null, null)).thenReturn(expectedRequests);

        // Act
        List<TutoringRequest> result = tutoringRequestService.getTutoringRequestsWithFilters(null, skillId, null, null);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(expectedRequests, result);
        verify(tutoringRequestRepository, times(1)).findWithFilters(null, skillId, null, null);
    }

    @Test
    void getTutoringRequestsWithFilters_OnlyStatus_Success() {
        // Arrange
        List<TutoringRequest> expectedRequests = Arrays.asList(tutoringRequest1);
        when(tutoringRequestRepository.findWithFilters(null, null, RequestStatus.Enviada, null)).thenReturn(expectedRequests);

        // Act
        List<TutoringRequest> result = tutoringRequestService.getTutoringRequestsWithFilters(null, null, RequestStatus.Enviada, null);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(expectedRequests, result);
        verify(tutoringRequestRepository, times(1)).findWithFilters(null, null, RequestStatus.Enviada, null);
    }
    
    @Test
    void getTutoringRequestsWithFilters_AllFilters_Success() {
        // Arrange
        List<TutoringRequest> expectedRequests = Arrays.asList(tutoringRequest1);
        when(tutoringRequestRepository.findWithFilters(tuteeId, skillId, RequestStatus.Enviada, null)).thenReturn(expectedRequests);

        // Act
        List<TutoringRequest> result = tutoringRequestService.getTutoringRequestsWithFilters(tuteeId, skillId, RequestStatus.Enviada, null);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(expectedRequests, result);
        verify(tutoringRequestRepository, times(1)).findWithFilters(tuteeId, skillId, RequestStatus.Enviada, null);
    }
}