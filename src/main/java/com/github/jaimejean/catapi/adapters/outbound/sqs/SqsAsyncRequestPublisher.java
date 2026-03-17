package com.github.jaimejean.catapi.adapters.outbound.sqs;

import com.github.jaimejean.catapi.domain.model.AsyncBreedRequest;
import com.github.jaimejean.catapi.domain.ports.out.AsyncRequestPublisher;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SqsAsyncRequestPublisher implements AsyncRequestPublisher {

  private static final Logger log = LoggerFactory.getLogger(SqsAsyncRequestPublisher.class);

  private final SqsTemplate sqsTemplate;
  private final String queueName;

  public SqsAsyncRequestPublisher(
      SqsTemplate sqsTemplate, @Value("${catapi.async.sqs-queue-name}") String queueName) {
    this.sqsTemplate = sqsTemplate;
    this.queueName = queueName;
  }

  @Override
  public void publish(AsyncBreedRequest request) {
    log.debug(
        "Publishing message to SQS: queue={}, requestId={}", queueName, request.getRequestId());

    try {
      sqsTemplate.send(queueName, request);
      log.info(
          "Message published to SQS: queue={}, requestId={}", queueName, request.getRequestId());
    } catch (Exception ex) {
      log.error(
          "Failed to publish message to SQS: queue={}, requestId={}",
          queueName,
          request.getRequestId(),
          ex);
      throw new RuntimeException("Failed to publish async request to SQS", ex);
    }
  }
}
