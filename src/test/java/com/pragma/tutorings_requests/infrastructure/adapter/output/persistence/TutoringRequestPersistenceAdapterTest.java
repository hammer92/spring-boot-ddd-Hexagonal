package com.pragma.tutorings_requests.infrastructure.adapter.output.persistence;

import com.pragma.tutorings_requests.domain.model.TutoringRequest;
import com.pragma.tutorings_requests.domain.model.enums.RequestStatus;
import com.pragma.tutorings_requests.infrastructure.adapter.output.persistence.entity.TutoringRequestsEntity;
import com.pragma.tutorings_requests.infrastructure.adapter.output.persistence.mapper.TutoringRequestMapper;
import com.pragma.tutorings_requests.infrastructure.adapter.output.persistence.repository.SpringDataTutoringRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TutoringRequestPersistenceAdapterTest {

    @Mock
    private SpringDataTutoringRequestRepository repository;

    @Mock
    private TutoringRequestMapper mapper;

    @InjectMocks
    private TutoringRequestPersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void save_ShouldMapAndSaveEntity() {
        // Arrange
        TutoringRequest domainModel = new TutoringRequest();
        domainModel.setId("request-id");
        domainModel.setNeedsDescription("Necesito ayuda con Spring Boot");
        domainModel.setRequestDate(new Date());
        domainModel.setRequestStatus(RequestStatus.Pendiente);

        TutoringRequestsEntity entity = new TutoringRequestsEntity();
        entity.setId("request-id");
        entity.setNeedsDescription("Necesito ayuda con Spring Boot");
        entity.setRequestDate(new Date());
        entity.setRequestStatus(RequestStatus.Pendiente);

        when(mapper.toEntity(any(TutoringRequest.class))).thenReturn(entity);
        when(repository.save(any(TutoringRequestsEntity.class))).thenReturn(entity);
        when(mapper.toDomain(any(TutoringRequestsEntity.class))).thenReturn(domainModel);

        // Act
        TutoringRequest result = adapter.save(domainModel);

        // Assert
        assertEquals("request-id", result.getId());
        assertEquals("Necesito ayuda con Spring Boot", result.getNeedsDescription());
        assertEquals(RequestStatus.Pendiente, result.getRequestStatus());

        verify(mapper, times(1)).toEntity(domainModel);
        verify(repository, times(1)).save(entity);
        verify(mapper, times(1)).toDomain(entity);
    }

    @Test
    void findById_ShouldReturnDomainModel_WhenEntityExists() {
        // Arrange
        String id = "request-id";
        TutoringRequestsEntity entity = new TutoringRequestsEntity();
        entity.setId(id);
        entity.setNeedsDescription("Necesito ayuda con Spring Boot");

        TutoringRequest domainModel = new TutoringRequest();
        domainModel.setId(id);
        domainModel.setNeedsDescription("Necesito ayuda con Spring Boot");

        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(domainModel);

        // Act
        Optional<TutoringRequest> result = adapter.findById(id);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId());
        assertEquals("Necesito ayuda con Spring Boot", result.get().getNeedsDescription());

        verify(repository, times(1)).findById(id);
        verify(mapper, times(1)).toDomain(entity);
    }

    @Test
    void findAll_ShouldReturnAllDomainModels() {
        // Arrange
        TutoringRequestsEntity entity1 = new TutoringRequestsEntity();
        entity1.setId("request-1");
        
        TutoringRequestsEntity entity2 = new TutoringRequestsEntity();
        entity2.setId("request-2");
        
        List<TutoringRequestsEntity> entities = List.of(entity1, entity2);

        TutoringRequest domainModel1 = new TutoringRequest();
        domainModel1.setId("request-1");
        
        TutoringRequest domainModel2 = new TutoringRequest();
        domainModel2.setId("request-2");

        when(repository.findAll()).thenReturn(entities);
        when(mapper.toDomain(entity1)).thenReturn(domainModel1);
        when(mapper.toDomain(entity2)).thenReturn(domainModel2);

        // Act
        List<TutoringRequest> results = adapter.findAll();

        // Assert
        assertEquals(2, results.size());
        assertEquals("request-1", results.get(0).getId());
        assertEquals("request-2", results.get(1).getId());

        verify(repository, times(1)).findAll();
        verify(mapper, times(1)).toDomain(entity1);
        verify(mapper, times(1)).toDomain(entity2);
    }
}