package com.pragma.usuarios.infrastructure.adapter.output.persistence;

import com.pragma.usuarios.domain.model.User;
import com.pragma.usuarios.domain.port.output.UserRepository;
import com.pragma.usuarios.infrastructure.adapter.output.persistence.entity.UsersEntity;
import com.pragma.usuarios.infrastructure.adapter.output.persistence.mapper.UserMapper;
import com.pragma.usuarios.infrastructure.adapter.output.persistence.repository.SpringDataUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserPersistenceAdapter implements UserRepository {

    private final SpringDataUserRepository repository;
    private final UserMapper mapper;

    @Override
    public User save(User User) {
        UsersEntity entity = mapper.toEntity(User);
        UsersEntity savedEntity = repository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<User> findById(String id) {
        return repository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return repository.findByEmail(email)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByGoogleUserId(String googleUserId) {
        return repository.findByGoogleUserId(googleUserId)
                .map(mapper::toDomain);
    }

    @Override
    public List<User> findAll() {
        return repository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}