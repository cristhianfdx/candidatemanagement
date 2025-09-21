package com.cristhianfdx.candidatemanagement.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidateResponse {
    private String firstname;

    private String lastname;

    private String email;

    private int age;

    private LocalDate birthDate;

    private LocalDate lifeExpectancyDate;
}
