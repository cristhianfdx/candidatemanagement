package com.cristhianfdx.candidatemanagement.infraestructure.entrypoint.controllers;

import com.cristhianfdx.candidatemanagement.application.dto.CandidateMetricsResponse;
import com.cristhianfdx.candidatemanagement.application.dto.CandidateResponse;
import com.cristhianfdx.candidatemanagement.application.dto.CreateCandidateRequest;
import com.cristhianfdx.candidatemanagement.application.port.CandidatePort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/candidates")
@RequiredArgsConstructor
@Tag(name = "Candidates", description = "Candidate management for recruitment processes")
@SecurityRequirement(name = "basicAuth")
public class CandidateController {

    private final CandidatePort candidateService;

    @Operation(summary = "Create a new candidate", description = "Registers a new candidate in the system")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Candidate created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping
    public ResponseEntity<Void> createCandidate(@Valid @RequestBody CreateCandidateRequest request) {
        candidateService.createCandidate(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "Retrieve all candidates", description = "Returns a list of all registered candidates")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successful retrieval of candidates"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping
    public ResponseEntity<List<CandidateResponse>> getCandidates() {
        List<CandidateResponse> candidates = candidateService.getCandidates();
        return ResponseEntity.ok(candidates);
    }

    @Operation(summary = "Retrieve candidate metrics", description = "Calculates and returns metrics such as average age and standard deviation")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Metrics calculated successfully"),
            @ApiResponse(responseCode = "404", description = "No candidates found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/metrics")
    public ResponseEntity<CandidateMetricsResponse> getMetrics() {
        CandidateMetricsResponse metrics = candidateService.getMetrics();
        return ResponseEntity.ok(metrics);
    }
}
