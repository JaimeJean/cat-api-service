package com.github.jaimejean.catapi.services;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.jaimejean.catapi.adapters.outbound.sqs.SqsAsyncRequestPublisher;
import com.github.jaimejean.catapi.config.AsyncPropertiesConfig;
import com.github.jaimejean.catapi.domain.model.AsyncBreedRequest;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SqsAsyncRequestPublisherTest {

  @Mock private SqsTemplate sqsTemplate;

  @Mock private AsyncPropertiesConfig asyncProperties;

  @InjectMocks private SqsAsyncRequestPublisher publisher;

  @Nested
  @DisplayName("publish — publicação na fila SQS")
  class Publish {

    @Test
    @DisplayName("deve publicar mensagem na fila com sucesso")
    void shouldPublishMessageSuccessfully() {
      AsyncBreedRequest request = buildRequest();
      when(asyncProperties.getSqsQueueName()).thenReturn("cat-api-queue");

      assertDoesNotThrow(() -> publisher.publish(request));

      verify(asyncProperties).getSqsQueueName();
      verify(sqsTemplate).send("cat-api-queue", request);
    }

    @Test
    @DisplayName("deve usar o nome da fila configurado")
    void shouldUseConfiguredQueueName() {
      AsyncBreedRequest request = buildRequest();
      when(asyncProperties.getSqsQueueName()).thenReturn("custom-queue-name");

      publisher.publish(request);

      verify(sqsTemplate).send("custom-queue-name", request);
    }

    @Test
    @DisplayName("deve lançar RuntimeException quando falhar ao publicar na fila")
    void shouldThrowRuntimeExceptionWhenPublishFails() {
      AsyncBreedRequest request = buildRequest();
      when(asyncProperties.getSqsQueueName()).thenReturn("cat-api-queue");
      doThrow(new IllegalStateException("SQS unavailable"))
          .when(sqsTemplate)
          .send("cat-api-queue", request);

      RuntimeException exception =
          assertThrows(RuntimeException.class, () -> publisher.publish(request));

      assertEquals("Failed to publish async request to SQS", exception.getMessage());
      assertEquals(IllegalStateException.class, exception.getCause().getClass());
      verify(sqsTemplate).send("cat-api-queue", request);
    }
  }

  private AsyncBreedRequest buildRequest() {
    return new AsyncBreedRequest("test@example.com", "playful", "Egypt");
  }
}
