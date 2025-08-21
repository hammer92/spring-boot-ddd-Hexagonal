package com.pragma.tutorings.domain.port.output;

import com.pragma.tutorings.domain.model.Tutoring;

import java.util.List;
import java.util.Optional;

public interface TutoringRepository {
    Tutoring save(Tutoring tutoring);
    Optional<Tutoring> findById(String id);
    List<Tutoring> findByTutorId(String tutorId);
    List<Tutoring> findByTuteeId(String tuteeId);
    Long countActiveTutoringByTutorId(String tutorId);
}