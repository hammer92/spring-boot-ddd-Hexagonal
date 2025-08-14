package com.pragma.skills.infrastructure.adapter.input.rest;

import com.pragma.shared.context.UserContextHelper;
import com.pragma.shared.dto.OkResponseDto;
import com.pragma.skills.domain.model.Skill;
import com.pragma.skills.domain.port.input.*;
import com.pragma.skills.infrastructure.adapter.input.rest.dto.CreateSkillDto;
import com.pragma.skills.infrastructure.adapter.input.rest.dto.SkillDto;
import com.pragma.skills.infrastructure.adapter.input.rest.dto.UpdateSkillDto;
import com.pragma.skills.infrastructure.adapter.input.rest.mapper.SkillDtoMapper;
import com.pragma.usuarios.domain.model.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/skills")
@RequiredArgsConstructor
@Slf4j
public class SkillController {

    private final CreateSkillUseCase createSkillUseCase;
    private final FindSkillUseCase findSkillUseCase;
    private final GetAllSkillsUseCase getAllSkillsUseCase;
    private final UpdateSkillUseCase updateSkillUseCase;
    private final DeleteSkillUseCase deleteSkillUseCase;
    private final SkillDtoMapper skillDtoMapper;

    @PostMapping
    public ResponseEntity<OkResponseDto<SkillDto>> createSkill(@Valid @RequestBody CreateSkillDto createSkillDto) {
        User currentUser = UserContextHelper.getCurrentUserOrThrow();
        log.info("User {} creating skill: {}", currentUser.getEmail(), createSkillDto.getName());
        
        // Only admins can create skills
        UserContextHelper.requireAdminRole();
        
        Skill skill = skillDtoMapper.toModel(createSkillDto);
        Skill createdSkill = createSkillUseCase.createSkill(skill);
        SkillDto skillDto = skillDtoMapper.toDto(createdSkill);
        
        log.info("User {} successfully created skill with ID: {}", currentUser.getEmail(), createdSkill.getId());
        
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(OkResponseDto.of("Habilidad creada exitosamente", skillDto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OkResponseDto<SkillDto>> getSkillById(@PathVariable String id) {
        User currentUser = UserContextHelper.getCurrentUserOrThrow();
        log.debug("User {} requesting skill with ID: {}", currentUser.getEmail(), id);
        
        return findSkillUseCase.findSkillById(id)
                .map(skill -> {
                    SkillDto skillDto = skillDtoMapper.toDto(skill);
                    return ResponseEntity.ok(OkResponseDto.of("Habilidad encontrada", skillDto));
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(OkResponseDto.of("Habilidad no encontrada", null)));
    }

    @GetMapping
    public ResponseEntity<OkResponseDto<List<SkillDto>>> getAllSkills() {
        User currentUser = UserContextHelper.getCurrentUserOrThrow();
        log.debug("User {} requesting all skills", currentUser.getEmail());
        
        List<SkillDto> skills = getAllSkillsUseCase.getAllSkills().stream()
                .map(skillDtoMapper::toDto)
                .collect(Collectors.toList());
        
        log.debug("User {} retrieved {} skills", currentUser.getEmail(), skills.size());
        return ResponseEntity.ok(OkResponseDto.of("Habilidades obtenidas exitosamente", skills));
    }

    @PutMapping
    public ResponseEntity<OkResponseDto<SkillDto>> updateSkill(@Valid @RequestBody UpdateSkillDto updateSkillDto) {
        User currentUser = UserContextHelper.getCurrentUserOrThrow();
        log.info("User {} updating skill with ID: {}", currentUser.getEmail(), updateSkillDto.getId());
        
        // Only admins can update skills
        UserContextHelper.requireAdminRole();
        
        Skill skill = skillDtoMapper.toModel(updateSkillDto);
        return updateSkillUseCase.updateSkill(updateSkillDto.getId(), skill)
                .map(updatedSkill -> {
                    SkillDto skillDto = skillDtoMapper.toDto(updatedSkill);
                    log.info("User {} successfully updated skill with ID: {}", currentUser.getEmail(), updatedSkill.getId());
                    return ResponseEntity.ok(OkResponseDto.of("Habilidad actualizada exitosamente", skillDto));
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(OkResponseDto.of("Habilidad no encontrada", null)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<OkResponseDto<Void>> deleteSkill(@PathVariable String id) {
        User currentUser = UserContextHelper.getCurrentUserOrThrow();
        log.info("User {} deleting skill with ID: {}", currentUser.getEmail(), id);
        
        // Only admins can delete skills
        UserContextHelper.requireAdminRole();
        
        boolean deleted = deleteSkillUseCase.deleteSkill(id);
        
        if (deleted) {
            log.info("User {} successfully deleted skill with ID: {}", currentUser.getEmail(), id);
            return ResponseEntity.ok(OkResponseDto.of("Habilidad eliminada exitosamente", null));
        } else {
            log.warn("User {} attempted to delete non-existent skill with ID: {}", currentUser.getEmail(), id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(OkResponseDto.of("Habilidad no encontrada", null));
        }
    }
}