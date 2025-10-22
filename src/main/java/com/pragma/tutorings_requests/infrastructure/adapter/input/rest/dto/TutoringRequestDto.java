package com.pragma.tutorings_requests.infrastructure.adapter.input.rest.dto;

import com.pragma.skills.infrastructure.adapter.input.rest.dto.SkillDto;
import com.pragma.tutorings_requests.domain.model.enums.RequestStatus;
import com.pragma.usuarios.infrastructure.adapter.input.rest.dto.UserDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TutoringRequestDto {
    private String id;
    private UserDto tutee;
    private List<SkillDto> skills;
    private String needsDescription;
    private Date requestDate;
    private RequestStatus requestStatus;
    private String assignedTutoringId;
    private UserDto tutor;
    private Date createdAt;
    private Date updatedAt;
}