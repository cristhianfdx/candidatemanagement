package com.cristhianfdx.candidatemanagement.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.net.URI;
import java.util.concurrent.CompletableFuture;

@Configuration
public class AwsConfig {

    @Value("${aws.region}")
    private String region;

    @Value("${aws.access-key}")
    private String accessKey;

    @Value("${aws.secret-key}")
    private String secretKey;

    @Value("${aws.sqs.queue.name}")
    private String queueName;

    @Value("${aws.sqs.endpoint}")
    private String sqsEndpoint;

    @Bean
    public SqsAsyncClient sqsAsyncClient() {
        return SqsAsyncClient.builder()
                .endpointOverride(URI.create(sqsEndpoint))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)
                ))
                .region(Region.of(region))
                .build();
    }

    @Bean
    public CompletableFuture<String> queueUrlFuture(SqsAsyncClient sqsAsyncClient) {
        return sqsAsyncClient.getQueueUrl(GetQueueUrlRequest.builder().queueName(queueName).build())
                .thenApply(GetQueueUrlResponse::queueUrl)
                .exceptionallyCompose(ex -> {
                    if (ex.getCause() instanceof QueueDoesNotExistException) {
                        return sqsAsyncClient.createQueue(CreateQueueRequest.builder()
                                        .queueName(queueName)
                                        .build())
                                .thenApply(CreateQueueResponse::queueUrl);
                    }
                    return CompletableFuture.failedFuture(new RuntimeException("Error getting queue URL", ex));
                });
    }
}
