package com.pragma.usuarios.infrastructure.adapter.input.rest.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequestDto {
    @NotBlank(message = "User ID is required")
    private String id;
    
    private String firstName;
    private String lastName;
    private String chapterId;
    private String slackId;
}