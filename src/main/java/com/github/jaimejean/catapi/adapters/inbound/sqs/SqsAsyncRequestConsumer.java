package com.github.jaimejean.catapi.adapters.inbound.sqs;

import com.github.jaimejean.catapi.adapters.outbound.persistence.ProcessedMessageJpaRepository;
import com.github.jaimejean.catapi.domain.entities.ProcessedMessage;
import com.github.jaimejean.catapi.domain.model.AsyncBreedRequest;
import com.github.jaimejean.catapi.domain.ports.in.AsyncRequestService;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SqsAsyncRequestConsumer {

  private final AsyncRequestService asyncRequestService;
  private final ProcessedMessageJpaRepository processedMessageRepository;

  @SqsListener("${catapi.async.sqs-queue-name}")
  public void onMessage(AsyncBreedRequest request, @Header("id") String messageId) {
    log.info(
        "Message received from SQS: messageId={}, requestId={}", messageId, request.getRequestId());

    if (isAlreadyProcessed(messageId)) {
      log.warn("Duplicate message detected, skipping: messageId={}", messageId);
      return;
    }

    try {
      asyncRequestService.process(request);
      markAsProcessed(messageId);
      log.info(
          "Message processed successfully: messageId={}, requestId={}",
          messageId,
          request.getRequestId());
    } catch (Exception ex) {
      log.error(
          "Failed to process message: messageId={}, requestId={}",
          messageId,
          request.getRequestId(),
          ex);
      throw ex;
    }
  }

  private boolean isAlreadyProcessed(String messageId) {
    return processedMessageRepository.existsById(messageId);
  }

  private void markAsProcessed(String messageId) {
    processedMessageRepository.save(new ProcessedMessage(messageId));
  }
}
