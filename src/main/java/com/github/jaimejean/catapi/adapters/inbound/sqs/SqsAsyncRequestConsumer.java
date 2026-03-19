package com.github.jaimejean.catapi.adapters.inbound.sqs;

import com.github.jaimejean.catapi.adapters.outbound.dynamodb.ProcessedMessageStore;
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
  private final ProcessedMessageStore processedMessageStore;

  @SqsListener("${catapi.async.sqs-queue-name}")
  public void onMessage(AsyncBreedRequest request, @Header("id") String messageId) {
    log.info(
        "Consumer received: requestId={}, email='{}', temperament='{}' (len={}), origin='{}' (len={})",
        request.getRequestId(),
        request.getEmail(),
        request.getTemperament(),
        request.getTemperament() == null ? null : request.getTemperament().length(),
        request.getOrigin(),
        request.getOrigin() == null ? null : request.getOrigin().length());

    if (processedMessageStore.exists(messageId)) {
      log.warn("Duplicate message detected, skipping: messageId={}", messageId);
      return;
    }

    try {
      asyncRequestService.process(request);
      processedMessageStore.tryMarkAsProcessed(messageId);
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
}
