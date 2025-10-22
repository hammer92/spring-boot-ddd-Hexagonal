package com.pragma.tutorings_requests.infrastructure.adapter.output.persistence.mapper;

import com.pragma.skills.domain.model.Skill;
import com.pragma.skills.infrastructure.adapter.output.persistence.entity.SkillEntity;
import com.pragma.skills.infrastructure.adapter.output.persistence.mapper.SkillMapper;
import com.pragma.skills.infrastructure.adapter.output.persistence.repository.SpringDataSkillRepository;
import com.pragma.tutorings_requests.domain.model.TutoringRequest;
import com.pragma.tutorings_requests.infrastructure.adapter.output.persistence.entity.TutoringRequestsEntity;
import com.pragma.usuarios.infrastructure.adapter.output.persistence.entity.UsersEntity;
import com.pragma.usuarios.infrastructure.adapter.output.persistence.mapper.UserMapper;
import com.pragma.usuarios.infrastructure.adapter.output.persistence.repository.SpringDataUserRepository;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(
    componentModel = "spring", 
    uses = {SkillMapper.class, UserMapper.class},
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public abstract class TutoringRequestMapper {

    @Mapping(target = "assignedTutoringId", ignore = true)
    @Mapping(target = "tutor", ignore = true)
    public abstract TutoringRequestsEntity toEntity(TutoringRequest tutoringRequest);

    @Mapping(target = "skills", source = "skills")
    @Mapping(target = "tutee", source = "tutee")
    @Mapping(target = "tutor", source = "tutor")
    @Mapping(target = "assignedTutoringId", expression = "java(mapAssignedTutoringId(entity))")
    public abstract TutoringRequest toDomain(TutoringRequestsEntity entity);
    
    protected String mapAssignedTutoringId(TutoringRequestsEntity entity) {
        return entity.getAssignedTutoringId() != null ? entity.getAssignedTutoringId().getId() : null;
    }

}