package com.pragma.feedbacks.infrastructure.adapter.output.persistence.entity;

import com.pragma.tutorings.infrastructure.adapter.output.persistence.entity.TutoringEntity;
import com.pragma.usuarios.infrastructure.adapter.output.persistence.entity.UsersEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Table(name = "feedbacks")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.EAGER)
    private UsersEntity evaluatorId;

    @Column(name = "evaluation_date")
    private Date evaluationDate;

    @ManyToOne(fetch = FetchType.EAGER)
    private TutoringEntity tutoringId;

    @Column(name = "score")
    private String score;

    @Column(name = "comments", nullable = false)
    private String comments;
}