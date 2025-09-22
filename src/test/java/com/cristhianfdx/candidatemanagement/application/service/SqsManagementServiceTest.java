package com.cristhianfdx.candidatemanagement.application.service;

import com.cristhianfdx.candidatemanagement.application.event.CandidateRecalculateEvent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.context.ApplicationEventPublisher;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(org.mockito.junit.MockitoJUnitRunner.class)
public class SqsManagementServiceTest {

    @Mock
    private SqsAsyncClient sqsAsyncClient;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private SqsManagementService subject;

    private CompletableFuture<String> queueUrlFuture;

    @Before
    public void setUp() {
        queueUrlFuture = CompletableFuture.completedFuture("https://fake-queue-url");
        subject = new SqsManagementService(sqsAsyncClient, applicationEventPublisher, queueUrlFuture);
    }

    @Test
    public void shouldSendMessageAndPollWhenPublishCandidateCreated() {
        SendMessageResponse sendMessageResponse = SendMessageResponse.builder()
                .messageId("msg-123")
                .build();
        when(sqsAsyncClient.sendMessage(any(java.util.function.Consumer.class)))
                .thenReturn(CompletableFuture.completedFuture(sendMessageResponse));

        ReceiveMessageResponse receiveMessageResponse = ReceiveMessageResponse.builder()
                .messages(Collections.emptyList())
                .build();
        when(sqsAsyncClient.receiveMessage((ReceiveMessageRequest) any()))
                .thenReturn(CompletableFuture.completedFuture(receiveMessageResponse));

        subject.publishCandidateCreated("candidate-1");

        verify(sqsAsyncClient, times(1)).sendMessage(any(java.util.function.Consumer.class));
        verify(sqsAsyncClient, times(1)).receiveMessage((ReceiveMessageRequest) any());
    }

    @Test
    public void shouldPublishEventAndDeleteMessage() {
        Message msg = Message.builder()
                .body("candidate-123")
                .receiptHandle("receipt-abc")
                .messageId("msg-123")
                .build();

        DeleteMessageResponse deleteResponse = DeleteMessageResponse.builder().build();
        when(sqsAsyncClient.deleteMessage((DeleteMessageRequest) any()))
                .thenReturn(CompletableFuture.completedFuture(deleteResponse));


        try {
            java.lang.reflect.Method method = SqsManagementService.class.getDeclaredMethod("processMessage", Message.class, String.class);
            method.setAccessible(true);
            method.invoke(subject, msg, "https://fake-queue-url");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        verify(applicationEventPublisher, times(1)).publishEvent(any(CandidateRecalculateEvent.class));
        verify(sqsAsyncClient, times(1)).deleteMessage(any(DeleteMessageRequest.class));
    }
}
