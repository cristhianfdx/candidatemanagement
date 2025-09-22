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
        log.info("Receiving CandidateRecalculateEvent {}...", event.getCandidateId());
        try {
            candidatePort.recalculateMetrics();
            log.info("CandidateRecalculateEvent processed {} successfully.", event.getCandidateId());
        } catch (Exception e) {
            log.error(
                    "CandidateRecalculateEvent {} is not proceed for error: {}",
                    event.getCandidateId(), e.getMessage()
            );
        }

    }
}
