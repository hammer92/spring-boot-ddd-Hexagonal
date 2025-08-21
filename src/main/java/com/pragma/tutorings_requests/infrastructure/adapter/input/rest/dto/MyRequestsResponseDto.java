package com.pragma.tutorings_requests.infrastructure.adapter.input.rest.dto;

import com.pragma.tutorings.infrastructure.adapter.input.rest.dto.TutoringDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MyRequestsResponseDto {
    private List<TutoringRequestDto> requests;
    private List<TutoringDto> tutoringsAsTutor;
    private List<TutoringDto> tutoringsAsTutee;
}