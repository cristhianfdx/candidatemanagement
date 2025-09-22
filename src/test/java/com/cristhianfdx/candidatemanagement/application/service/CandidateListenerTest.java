package com.cristhianfdx.candidatemanagement.application.service;


import com.cristhianfdx.candidatemanagement.application.event.CandidateRecalculateEvent;
import com.cristhianfdx.candidatemanagement.application.port.CandidatePort;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CandidateListenerTest {

    @Mock
    private CandidatePort subject;

    @InjectMocks
    private CandidateListener candidateListener;

    private CandidateRecalculateEvent event;

    @Before
    public void setUp() {
        event = new CandidateRecalculateEvent("candidate-123");
    }

    @Test
    public void shouldCallRecalculateMetricsCorrectly() {
        candidateListener.handleCandidateRecalculateEvent(event);
        verify(subject, times(1)).recalculateMetrics();
    }

    @Test
    public void shouldCatchAndLogWhenHandleCandidateRecalculateEventFailed() {
        doThrow(new RuntimeException("Test Exception")).when(subject).recalculateMetrics();

        candidateListener.handleCandidateRecalculateEvent(event);

        verify(subject, times(1)).recalculateMetrics();
    }
}