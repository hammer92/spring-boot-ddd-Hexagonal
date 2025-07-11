package com.pragma.tutoring_sessions.infrastructure.adapter.output.persistence.entity;

import com.pragma.tutorings.infrastructure.adapter.output.persistence.entity.TutoringEntity;
import com.pragma.tutorings_requests.domain.model.enums.TutoringsSessionStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tutoring_sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TutoringSessionsEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    private TutoringEntity tutoringId; // "ID de la tutoría a la que pertenece esta sesión"

    @Column(name = "datetime")
    private String datetime; // "Fecha y hora de la sesión"

    @Column(name = "duration_minutes")
    private int durationMinutes; // "Duración de la sesión en minutos"

    @Column(name = "location_link")
    private String locationLink; // "Enlace grabación de reunión virtual"

    @Column(name = "topics_covered")
    private String topicsCovered; // "Temas tratados durante la sesión"

    @Column(name = "notes")
    private String notes; // "Notas o resumen de la sesión"

    @Column(name = "session_status")
    @Enumerated(EnumType.STRING) // O EnumType.ORDINAL si quieres guardar el índice numérico
    private TutoringsSessionStatus sessionStatus; // "Estado de la sesión (Programada, Realizada, Cancelada)"


}