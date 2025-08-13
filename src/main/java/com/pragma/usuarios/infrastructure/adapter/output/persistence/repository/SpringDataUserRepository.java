package com.pragma.usuarios.infrastructure.adapter.output.persistence.repository;

import com.pragma.usuarios.infrastructure.adapter.output.persistence.entity.UsersEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpringDataUserRepository extends JpaRepository<UsersEntity, String> {
    Optional<UsersEntity> findByEmail(String email);
    Optional<UsersEntity> findByGoogleUserId(String googleUserId);
}