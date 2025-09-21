package com.cristhianfdx.candidatemanagement.application.event;

import lombok.Getter;

@Getter
public class CandidateRecalculateEvent {
    private final String candidateId;
    public CandidateRecalculateEvent(String candidateId) { this.candidateId = candidateId; }
}
