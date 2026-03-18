package com.github.jaimejean.catapi.services;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.jaimejean.catapi.adapters.inbound.sqs.SqsAsyncRequestConsumer;
import com.github.jaimejean.catapi.adapters.outbound.dynamodb.ProcessedMessageStore;
import com.github.jaimejean.catapi.domain.model.AsyncBreedRequest;
import com.github.jaimejean.catapi.domain.ports.in.AsyncRequestService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SqsAsyncRequestConsumerTest {

  @Mock private AsyncRequestService asyncRequestService;

  @Mock private ProcessedMessageStore processedMessageStore;

  @InjectMocks private SqsAsyncRequestConsumer consumer;

  @Nested
  @DisplayName("onMessage — consumo da fila SQS")
  class OnMessage {

    @Test
    @DisplayName("deve ignorar mensagem duplicada quando ela já tiver sido processada")
    void shouldIgnoreDuplicateMessage() {
      AsyncBreedRequest request = buildRequest();
      when(processedMessageStore.exists("msg-1")).thenReturn(true);

      consumer.onMessage(request, "msg-1");

      verify(processedMessageStore).exists("msg-1");
      verify(asyncRequestService, never()).process(request);
      verify(processedMessageStore, never()).tryMarkAsProcessed("msg-1");
    }

    @Test
    @DisplayName("deve processar mensagem e marcá-la como processada quando for nova")
    void shouldProcessAndMarkMessageWhenItIsNew() {
      AsyncBreedRequest request = buildRequest();
      when(processedMessageStore.exists("msg-2")).thenReturn(false);

      consumer.onMessage(request, "msg-2");

      verify(processedMessageStore).exists("msg-2");
      verify(asyncRequestService).process(request);
      verify(processedMessageStore).tryMarkAsProcessed("msg-2");
    }

    @Test
    @DisplayName("deve propagar exceção quando falhar ao processar a mensagem")
    void shouldPropagateExceptionWhenProcessingFails() {
      AsyncBreedRequest request = buildRequest();
      when(processedMessageStore.exists("msg-3")).thenReturn(false);
      RuntimeException expected = new RuntimeException("processing failed");

      org.mockito.Mockito.doThrow(expected).when(asyncRequestService).process(request);

      RuntimeException exception =
          assertThrows(RuntimeException.class, () -> consumer.onMessage(request, "msg-3"));

      org.junit.jupiter.api.Assertions.assertEquals("processing failed", exception.getMessage());
      verify(asyncRequestService).process(request);
      verify(processedMessageStore, never()).tryMarkAsProcessed("msg-3");
    }
  }

  private AsyncBreedRequest buildRequest() {
    return new AsyncBreedRequest("test@example.com", "playful", "Egypt");
  }
}
