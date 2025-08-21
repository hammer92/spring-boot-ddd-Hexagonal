package com.pragma.tutorings_requests.application.service;

import com.pragma.chapter.domain.model.Chapter;
import com.pragma.shared.context.UserContext;
import com.pragma.shared.context.UserContextHelper;
import com.pragma.shared.service.MessageService;
import com.pragma.skills.domain.model.Skill;
import com.pragma.tutorings_requests.domain.model.TutoringRequest;
import com.pragma.tutorings_requests.domain.model.enums.RequestStatus;
import com.pragma.tutorings_requests.domain.port.output.TutoringRequestRepository;
import com.pragma.usuarios.domain.model.User;
import com.pragma.usuarios.domain.model.enums.RolUsuario;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TutoringRequestServiceTest {

    @Mock
    private TutoringRequestRepository tutoringRequestRepository;

    @Mock
    private MessageService messageService;

    @InjectMocks
    private TutoringRequestService tutoringRequestService;

    private TutoringRequest tutoringRequest;
    private String requestId;
    private User adminUser;
    private User tutorUser;

    @BeforeEach
    void setUp() {
        requestId = UUID.randomUUID().toString();
        
        // Setup test users
        Chapter chapter = new Chapter("chapter-1", "Test Chapter");
        
        adminUser = new User();
        adminUser.setId("admin-id");
        adminUser.setEmail("admin@test.com");
        adminUser.setRol(RolUsuario.Administrador);
        adminUser.setChapter(chapter);
        
        tutorUser = new User();
        tutorUser.setId("tutor-id");
        tutorUser.setEmail("tutor@test.com");
        tutorUser.setRol(RolUsuario.Tutor);
        tutorUser.setChapter(chapter);
        
        User tutee = new User();
        tutee.setId("user-id");
        
        Skill skill = new Skill();
        skill.setId("skill-id");
        skill.setName("Java");
        
        tutoringRequest = new TutoringRequest();
        tutoringRequest.setId(requestId);
        tutoringRequest.setTutee(tutee);
        tutoringRequest.setSkills(Arrays.asList(skill));
        tutoringRequest.setNeedsDescription("Necesito ayuda con Java");
        tutoringRequest.setRequestDate(new Date());
        tutoringRequest.setRequestStatus(RequestStatus.Pendiente);
        
        // Setup MessageService mock
        UserContextHelper.setMessageServiceForTesting(messageService);
        lenient().when(messageService.getMessage(anyString())).thenReturn("Test message");
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void createTutoringRequest_Success() {
        // Arrange
        when(tutoringRequestRepository.save(any(TutoringRequest.class))).thenReturn(tutoringRequest);

        // Act
        TutoringRequest result = tutoringRequestService.createTutoringRequest(tutoringRequest);

        // Assert
        assertNotNull(result);
        assertEquals(requestId, result.getId());
        assertEquals(RequestStatus.Pendiente, result.getRequestStatus());
        verify(tutoringRequestRepository, times(1)).save(any(TutoringRequest.class));
    }

    @Test
    void updateStatus_ToApproved_Success() {
        // Arrange
        UserContext.setCurrentUser(adminUser);
        when(tutoringRequestRepository.findById(requestId)).thenReturn(Optional.of(tutoringRequest));
        when(tutoringRequestRepository.save(any(TutoringRequest.class))).thenAnswer(invocation -> {
            TutoringRequest savedRequest = invocation.getArgument(0);
            savedRequest.setRequestStatus(RequestStatus.Aprobada);
            return savedRequest;
        });

        // Act
        TutoringRequest result = tutoringRequestService.updateStatus(requestId, RequestStatus.Aprobada);

        // Assert
        assertNotNull(result);
        assertEquals(RequestStatus.Aprobada, result.getRequestStatus());
        verify(tutoringRequestRepository, times(1)).findById(requestId);
        verify(tutoringRequestRepository, times(1)).save(any(TutoringRequest.class));
    }

    @Test
    void updateStatus_ToCanceled_Success() {
        // Arrange
        UserContext.setCurrentUser(adminUser);
        when(tutoringRequestRepository.findById(requestId)).thenReturn(Optional.of(tutoringRequest));
        when(tutoringRequestRepository.save(any(TutoringRequest.class))).thenAnswer(invocation -> {
            TutoringRequest savedRequest = invocation.getArgument(0);
            savedRequest.setRequestStatus(RequestStatus.Cancelada);
            return savedRequest;
        });

        // Act
        TutoringRequest result = tutoringRequestService.updateStatus(requestId, RequestStatus.Cancelada);

        // Assert
        assertNotNull(result);
        assertEquals(RequestStatus.Cancelada, result.getRequestStatus());
        verify(tutoringRequestRepository, times(1)).findById(requestId);
        verify(tutoringRequestRepository, times(1)).save(any(TutoringRequest.class));
    }

    @Test
    void updateStatus_InvalidTransition_ThrowsException() {
        // Arrange
        UserContext.setCurrentUser(adminUser);
        tutoringRequest.setRequestStatus(RequestStatus.Finalizada);
        when(tutoringRequestRepository.findById(requestId)).thenReturn(Optional.of(tutoringRequest));
        
        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> 
            tutoringRequestService.updateStatus(requestId, RequestStatus.Aprobada)
        );
        
        assertTrue(exception.getMessage().contains("Transición de estado inválida"));
        verify(tutoringRequestRepository, never()).save(any(TutoringRequest.class));
    }

    @Test
    void updateStatus_RequestNotFound_ThrowsException() {
        // Arrange
        UserContext.setCurrentUser(adminUser);
        when(tutoringRequestRepository.findById(requestId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            tutoringRequestService.updateStatus(requestId, RequestStatus.Aprobada)
        );
        
        assertTrue(exception.getMessage().contains("Solicitud de tutoría no encontrada"));
        verify(tutoringRequestRepository, never()).save(any(TutoringRequest.class));
    }

    @Test
    void updateStatus_RequestNotInSentStatus_ThrowsException() {
        // Arrange
        UserContext.setCurrentUser(adminUser);
        tutoringRequest.setRequestStatus(RequestStatus.Aprobada);
        when(tutoringRequestRepository.findById(requestId)).thenReturn(Optional.of(tutoringRequest));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> 
            tutoringRequestService.updateStatus(requestId, RequestStatus.Pendiente)
        );
        
        assertTrue(exception.getMessage().contains("Transición de estado inválida"));
        verify(tutoringRequestRepository, never()).save(any(TutoringRequest.class));
    }

    @Test
    void getAllTutoringRequests_Success() {
        // Arrange
        List<TutoringRequest> requests = Arrays.asList(tutoringRequest);
        when(tutoringRequestRepository.findAll()).thenReturn(requests);

        // Act
        List<TutoringRequest> result = tutoringRequestService.getAllTutoringRequests();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(requestId, result.get(0).getId());
        verify(tutoringRequestRepository, times(1)).findAll();
    }

    @Test
    void getTutoringRequestsWithFilters_Success() {
        // Arrange
        List<TutoringRequest> requests = Arrays.asList(tutoringRequest);
        when(tutoringRequestRepository.findWithFilters(anyString(), anyString(), any(RequestStatus.class), anyString()))
                .thenReturn(requests);

        // Act
        List<TutoringRequest> result = tutoringRequestService.getTutoringRequestsWithFilters(
                "tutee-id", "skill-id", RequestStatus.Pendiente, "chapter-id");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(requestId, result.get(0).getId());
        verify(tutoringRequestRepository, times(1)).findWithFilters(
                "tutee-id", "skill-id", RequestStatus.Pendiente, "chapter-id");
    }
}