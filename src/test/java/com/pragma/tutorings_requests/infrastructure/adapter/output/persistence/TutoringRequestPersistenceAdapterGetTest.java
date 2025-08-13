package com.pragma.tutorings_requests.infrastructure.adapter.output.persistence;

import com.pragma.tutorings_requests.domain.model.TutoringRequest;
import com.pragma.tutorings_requests.domain.model.enums.RequestStatus;
import com.pragma.tutorings_requests.infrastructure.adapter.output.persistence.entity.TutoringRequestsEntity;
import com.pragma.tutorings_requests.infrastructure.adapter.output.persistence.mapper.TutoringRequestMapper;
import com.pragma.tutorings_requests.infrastructure.adapter.output.persistence.repository.SpringDataTutoringRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TutoringRequestPersistenceAdapterGetTest {

    @Mock
    private SpringDataTutoringRequestRepository repository;

    @Mock
    private TutoringRequestMapper mapper;

    @InjectMocks
    private TutoringRequestPersistenceAdapter adapter;

    private TutoringRequestsEntity entity1;
    private TutoringRequestsEntity entity2;
    private TutoringRequest domain1;
    private TutoringRequest domain2;
    private String tuteeId;
    private String skillId;

    @BeforeEach
    void setUp() {
        tuteeId = UUID.randomUUID().toString();
        skillId = UUID.randomUUID().toString();
        
        entity1 = new TutoringRequestsEntity();
        entity1.setId(UUID.randomUUID().toString());
        entity1.setRequestStatus(RequestStatus.Enviada);
        
        entity2 = new TutoringRequestsEntity();
        entity2.setId(UUID.randomUUID().toString());
        entity2.setRequestStatus(RequestStatus.Aprobada);
        
        domain1 = new TutoringRequest();
        domain1.setId(entity1.getId());
        domain1.setRequestStatus(RequestStatus.Enviada);
        
        domain2 = new TutoringRequest();
        domain2.setId(entity2.getId());
        domain2.setRequestStatus(RequestStatus.Aprobada);
    }

    @Test
    void findAll_Success() {
        // Arrange
        List<TutoringRequestsEntity> entities = Arrays.asList(entity1, entity2);
        List<TutoringRequest> expected = Arrays.asList(domain1, domain2);
        
        when(repository.findAll()).thenReturn(entities);
        when(mapper.toDomain(entity1)).thenReturn(domain1);
        when(mapper.toDomain(entity2)).thenReturn(domain2);

        // Act
        List<TutoringRequest> result = adapter.findAll();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(expected.get(0).getId(), result.get(0).getId());
        assertEquals(expected.get(1).getId(), result.get(1).getId());
        verify(repository, times(1)).findAll();
        verify(mapper, times(1)).toDomain(entity1);
        verify(mapper, times(1)).toDomain(entity2);
    }

    @Test
    void findWithFilters_OnlyTuteeId_Success() {
        // Arrange
        List<TutoringRequestsEntity> entities = Arrays.asList(entity1, entity2);
        List<TutoringRequest> expected = Arrays.asList(domain1, domain2);
        
        when(repository.findWithFilters(tuteeId, null, null, null)).thenReturn(entities);
        when(mapper.toDomain(entity1)).thenReturn(domain1);
        when(mapper.toDomain(entity2)).thenReturn(domain2);

        // Act
        List<TutoringRequest> result = adapter.findWithFilters(tuteeId, null, null, null);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(expected.get(0).getId(), result.get(0).getId());
        assertEquals(expected.get(1).getId(), result.get(1).getId());
        verify(repository, times(1)).findWithFilters(tuteeId, null, null, null);
        verify(mapper, times(1)).toDomain(entity1);
        verify(mapper, times(1)).toDomain(entity2);
    }

    @Test
    void findWithFilters_OnlySkillId_Success() {
        // Arrange
        List<TutoringRequestsEntity> entities = Arrays.asList(entity1, entity2);
        List<TutoringRequest> expected = Arrays.asList(domain1, domain2);
        
        when(repository.findWithFilters(null, skillId, null, null)).thenReturn(entities);
        when(mapper.toDomain(entity1)).thenReturn(domain1);
        when(mapper.toDomain(entity2)).thenReturn(domain2);

        // Act
        List<TutoringRequest> result = adapter.findWithFilters(null, skillId, null, null);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(expected.get(0).getId(), result.get(0).getId());
        assertEquals(expected.get(1).getId(), result.get(1).getId());
        verify(repository, times(1)).findWithFilters(null, skillId, null, null);
        verify(mapper, times(1)).toDomain(entity1);
        verify(mapper, times(1)).toDomain(entity2);
    }

    @Test
    void findWithFilters_OnlyStatus_Success() {
        // Arrange
        List<TutoringRequestsEntity> entities = Arrays.asList(entity1);
        List<TutoringRequest> expected = Arrays.asList(domain1);
        
        when(repository.findWithFilters(null, null, RequestStatus.Enviada, null)).thenReturn(entities);
        when(mapper.toDomain(entity1)).thenReturn(domain1);

        // Act
        List<TutoringRequest> result = adapter.findWithFilters(null, null, RequestStatus.Enviada, null);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(expected.get(0).getId(), result.get(0).getId());
        verify(repository, times(1)).findWithFilters(null, null, RequestStatus.Enviada, null);
        verify(mapper, times(1)).toDomain(entity1);
    }
    
    @Test
    void findWithFilters_AllFilters_Success() {
        // Arrange
        List<TutoringRequestsEntity> entities = Arrays.asList(entity1);
        List<TutoringRequest> expected = Arrays.asList(domain1);
        
        when(repository.findWithFilters(tuteeId, skillId, RequestStatus.Enviada, null)).thenReturn(entities);
        when(mapper.toDomain(entity1)).thenReturn(domain1);

        // Act
        List<TutoringRequest> result = adapter.findWithFilters(tuteeId, skillId, RequestStatus.Enviada, null);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(expected.get(0).getId(), result.get(0).getId());
        verify(repository, times(1)).findWithFilters(tuteeId, skillId, RequestStatus.Enviada, null);
        verify(mapper, times(1)).toDomain(entity1);
    }
}