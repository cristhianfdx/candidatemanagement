package com.cristhianfdx.candidatemanagement.application.port;

import com.cristhianfdx.candidatemanagement.application.dto.CandidateMetricsResponse;
import com.cristhianfdx.candidatemanagement.application.dto.CandidateResponse;
import com.cristhianfdx.candidatemanagement.application.dto.CreateCandidateRequest;

import java.util.List;

public interface CandidatePort {
    void createCandidate(CreateCandidateRequest createCandidateRequest);
    List<CandidateResponse> getCandidates();
    CandidateMetricsResponse getMetrics();
    void recalculateMetrics();

}
