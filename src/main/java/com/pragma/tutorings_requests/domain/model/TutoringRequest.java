package com.pragma.tutorings_requests.domain.model;

import com.pragma.skills.domain.model.Skill;
import com.pragma.tutorings_requests.domain.model.enums.RequestStatus;
import com.pragma.usuarios.domain.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TutoringRequest {
    private String id;
    private User tutee;
    private List<Skill> skills;
    private String needsDescription;
    private Date requestDate;
    private RequestStatus requestStatus;
    private String assignedTutoringId;
    private String tutorId;
    private User tutor;
    private Date createdAt;
    private Date updatedAt;
}