package com.pragma.usuarios.domain.port.input;

import com.pragma.usuarios.domain.model.User;
import java.util.Optional;

/**
 * Use case for finding a user by their Google user ID.
 * This is used for authentication purposes to map Google IDs to internal users.
 */
public interface FindUserByGoogleIdUseCase {
    
    /**
     * Finds a user by their Google user ID.
     * 
     * @param googleUserId the Google user ID to search for
     * @return an Optional containing the user if found, empty otherwise
     */
    Optional<User> findUserByGoogleId(String googleUserId);
}