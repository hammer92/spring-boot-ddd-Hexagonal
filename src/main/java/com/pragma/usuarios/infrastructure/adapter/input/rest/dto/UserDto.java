package com.pragma.usuarios.infrastructure.adapter.input.rest.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.pragma.chapter.infrastructure.adapter.input.rest.dto.ChapterDto;
import com.pragma.usuarios.domain.model.enums.RolUsuario;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String slackId;
    @JsonIgnore
    private String googleUserId;
    private ChapterDto chapter;
    private RolUsuario rol;
    private int activeTutoringLimit;
    private int seniority;
}