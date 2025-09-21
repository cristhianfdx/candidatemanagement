package com.cristhianfdx.candidatemanagement.domain.exception;

public class CandidateAlreadyExistsException  extends DomainException {
    public CandidateAlreadyExistsException(String message) {
        super(message);
    }
}
