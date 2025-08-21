package com.pragma.tutorings.infrastructure.adapter.output.persistence;

import com.pragma.tutorings.domain.model.Tutoring;
import com.pragma.tutorings.domain.model.enums.TutoringStatus;
import com.pragma.tutorings.domain.port.output.TutoringRepository;
import com.pragma.tutorings.infrastructure.adapter.output.persistence.entity.TutoringEntity;
import com.pragma.tutorings.infrastructure.adapter.output.persistence.mapper.TutoringMapper;
import com.pragma.tutorings.infrastructure.adapter.output.persistence.repository.SpringDataTutoringRepository;
import com.pragma.usuarios.infrastructure.adapter.output.persistence.entity.UsersEntity;
import com.pragma.usuarios.infrastructure.adapter.output.persistence.repository.SpringDataUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TutoringPersistenceAdapter implements TutoringRepository {

    private final SpringDataTutoringRepository tutoringRepository;
    private final SpringDataUserRepository userRepository;
    private final TutoringMapper tutoringMapper;

    @Override
    public Tutoring save(Tutoring tutoring) {
        TutoringEntity entity = tutoringMapper.toEntity(tutoring);
        TutoringEntity savedEntity = tutoringRepository.save(entity);
        return tutoringMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Tutoring> findById(String id) {
        return tutoringRepository.findById(id)
                .map(tutoringMapper::toDomain);
    }

    @Override
    public List<Tutoring> findByTutorId(String tutorId) {
        Optional<UsersEntity> tutorEntity = userRepository.findById(tutorId);
        if (tutorEntity.isEmpty()) {
            return List.of();
        }

        List<TutoringEntity> entities = tutoringRepository.findByTutorId(tutorEntity.get());
        return tutoringMapper.toDomainList(entities);
    }

    @Override
    public List<Tutoring> findByTuteeId(String tuteeId) {
        Optional<UsersEntity> tuteeEntity = userRepository.findById(tuteeId);
        if (tuteeEntity.isEmpty()) {
            return List.of();
        }

        List<TutoringEntity> entities = tutoringRepository.findByTuteeId(tuteeEntity.get());
        return tutoringMapper.toDomainList(entities);
    }

    @Override
    public Long countActiveTutoringByTutorId(String tutorId) {
        return tutoringRepository.countByTutorIdAndStatus(tutorId, TutoringStatus.Activa);
    }
}