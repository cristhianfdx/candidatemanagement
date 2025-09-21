package com.cristhianfdx.candidatemanagement.application.service;

import com.cristhianfdx.candidatemanagement.application.event.CandidateRecalculateEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

import java.util.concurrent.CompletableFuture;

/**
 * Service responsible for publishing messages to an SQS queue and processing
 * received messages asynchronously. Each received message triggers a Spring event,
 * allowing other components (e.g., CandidatePort) to react without creating a direct dependency.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SqsManagementService {

    /** AWS SQS Async client for sending and receiving messages. */
    private final SqsAsyncClient sqsAsyncClient;

    /** Spring event publisher for emitting events when a message is processed. */
    private final ApplicationEventPublisher applicationEventPublisher;

    /** CompletableFuture containing the URL of the SQS queue this service will publish to and poll from. */
    private final CompletableFuture<String> queueUrlFuture;

    /**
     * Publishes a message to the SQS queue indicating that a new candidate has been created.
     * After sending, it can optionally trigger a one-time polling of the queue to process any new messages.
     *
     * @param candidateId the ID of the candidate to publish
     */
    public void publishCandidateCreated(String candidateId) {
        queueUrlFuture.thenAccept(queueUrl -> {
            sqsAsyncClient.sendMessage(builder -> builder
                            .queueUrl(queueUrl)
                            .messageBody(candidateId))
                    .thenAccept(resp -> {
                        log.info("Message sent, ID: {}", resp.messageId());
                        pollQueueOnce();
                    })
                    .exceptionally(e -> {
                        log.error("Error sending message", e);
                        return null;
                    });
        });
    }

    /**
     * Performs a one-time poll of the SQS queue using long polling.
     * Each received message is processed individually by {@link #processMessage(Message, String)}.
     */
    private void pollQueueOnce() {
        queueUrlFuture.thenAccept(url -> {
            ReceiveMessageRequest request = ReceiveMessageRequest.builder()
                    .queueUrl(url)
                    .maxNumberOfMessages(10)
                    .waitTimeSeconds(5) // Long polling: waits up to 5 seconds if no messages
                    .build();

            sqsAsyncClient.receiveMessage(request)
                    .thenAccept(response -> response.messages()
                            .forEach(msg -> processMessage(msg, url)))
                    .exceptionally(e -> {
                        log.error("Error receiving message from SQS", e);
                        return null;
                    });
        });
    }

    /**
     * Processes a single SQS message:
     * <ul>
     *     <li>Logs the message.</li>
     *     <li>Publishes a {@link CandidateRecalculateEvent} using the message body.</li>
     *     <li>Deletes the message from the SQS queue to prevent reprocessing.</li>
     * </ul>
     *
     * @param msg the SQS message to process
     * @param queueUrl the URL of the queue to delete the message from
     */
    private void processMessage(Message msg, String queueUrl) {
        try {
            log.info("Processing message: {}", msg.body());

            // Publish event so other services can react without a direct dependency
            applicationEventPublisher.publishEvent(new CandidateRecalculateEvent(msg.body()));

            // Delete the message to acknowledge successful processing
            DeleteMessageRequest deleteRequest = DeleteMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .receiptHandle(msg.receiptHandle())
                    .build();

            sqsAsyncClient.deleteMessage(deleteRequest)
                    .thenAccept(r -> log.info("Message deleted successfully, ID: {}", msg.messageId()))
                    .exceptionally(e -> {
                        log.error("Error deleting message from SQS", e);
                        return null;
                    });

        } catch (Exception e) {
            log.error("Error processing message", e);
        }
    }
}
