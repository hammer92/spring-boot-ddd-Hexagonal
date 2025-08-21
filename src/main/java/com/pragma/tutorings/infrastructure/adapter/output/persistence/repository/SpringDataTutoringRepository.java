package com.pragma.tutorings.infrastructure.adapter.output.persistence.repository;

import com.pragma.tutorings.domain.model.enums.TutoringStatus;
import com.pragma.tutorings.infrastructure.adapter.output.persistence.entity.TutoringEntity;
import com.pragma.usuarios.infrastructure.adapter.output.persistence.entity.UsersEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpringDataTutoringRepository extends JpaRepository<TutoringEntity, String> {
    List<TutoringEntity> findByTutorId(UsersEntity tutorId);
    List<TutoringEntity> findByTuteeId(UsersEntity tuteeId);

    @Query("SELECT COUNT(t) FROM TutoringEntity t WHERE t.tutorId.id = :tutorId AND t.status = :status")
    Long countByTutorIdAndStatus(@Param("tutorId") String tutorId, @Param("status") TutoringStatus status);
}