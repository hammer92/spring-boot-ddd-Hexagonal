package com.pragma.usuarios.domain.port.output;

import com.pragma.usuarios.domain.model.User;
import java.util.Optional;
import java.util.List;

public interface UserRepository {
    User save(User account);
    Optional<User> findById(String id);
    Optional<User> findByEmail(String email);
    Optional<User> findByGoogleUserId(String googleUserId);
    List<User> findAll();
}