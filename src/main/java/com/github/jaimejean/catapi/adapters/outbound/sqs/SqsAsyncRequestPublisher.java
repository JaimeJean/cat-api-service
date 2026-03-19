package com.github.jaimejean.catapi.adapters.outbound.sqs;

import com.github.jaimejean.catapi.config.AsyncPropertiesConfig;
import com.github.jaimejean.catapi.domain.model.AsyncBreedRequest;
import com.github.jaimejean.catapi.domain.ports.out.AsyncRequestPublisher;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SqsAsyncRequestPublisher implements AsyncRequestPublisher {

  private final SqsTemplate sqsTemplate;
  private final AsyncPropertiesConfig asyncProperties;

  @Override
  public void publish(AsyncBreedRequest request) {
    String queueName = asyncProperties.getSqsQueueName();
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
