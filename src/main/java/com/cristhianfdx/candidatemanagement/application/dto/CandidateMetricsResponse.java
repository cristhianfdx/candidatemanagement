package com.cristhianfdx.candidatemanagement.application.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidateMetricsResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private double averageAge;
    private double ageStandardDeviation;
}
