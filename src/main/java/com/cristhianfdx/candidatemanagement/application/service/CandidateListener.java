package com.cristhianfdx.candidatemanagement.application.service;

import com.cristhianfdx.candidatemanagement.application.event.CandidateRecalculateEvent;
import com.cristhianfdx.candidatemanagement.application.port.CandidatePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CandidateListener {

    private final CandidatePort candidatePort;

    @EventListener
    public void handleCandidateRecalculateEvent(CandidateRecalculateEvent event) {
        candidatePort.recalculateMetrics();
        log.info("Recalculated event processed: {}", event.getCandidateId());
    }
}
