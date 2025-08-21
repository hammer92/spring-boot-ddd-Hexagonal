package com.pragma.usuarios.domain.model;

import com.pragma.chapter.domain.model.Chapter;
import com.pragma.usuarios.domain.model.enums.RolUsuario;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String googleUserId;
    private String slackId;
    private Chapter chapter;
    private RolUsuario rol;
    private int activeTutoringLimit;
}