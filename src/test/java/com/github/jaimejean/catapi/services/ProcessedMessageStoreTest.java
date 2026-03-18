package com.github.jaimejean.catapi.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.jaimejean.catapi.adapters.outbound.dynamodb.ProcessedMessageStore;
import com.github.jaimejean.catapi.config.AsyncPropertiesConfig;
import java.util.Collections;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

@ExtendWith(MockitoExtension.class)
class ProcessedMessageStoreTest {

  @Mock private DynamoDbClient dynamoDbClient;

  @Mock private AsyncPropertiesConfig asyncProperties;

  @InjectMocks private ProcessedMessageStore store;

  @Nested
  @DisplayName("exists — consulta de mensagem processada")
  class Exists {

    @Test
    @DisplayName("deve retornar true quando a mensagem existir na tabela")
    void shouldReturnTrueWhenMessageExists() {
      when(asyncProperties.getDynamodbTableName()).thenReturn("processed-messages");
      when(dynamoDbClient.getItem(any(GetItemRequest.class)))
          .thenReturn(
              GetItemResponse.builder()
                  .item(Map.of("messageId", AttributeValue.fromS("msg-1")))
                  .build());

      boolean exists = store.exists("msg-1");

      assertTrue(exists);

      ArgumentCaptor<GetItemRequest> captor = ArgumentCaptor.forClass(GetItemRequest.class);
      verify(dynamoDbClient).getItem(captor.capture());

      GetItemRequest request = captor.getValue();
      assertEquals("processed-messages", request.tableName());
      assertEquals("msg-1", request.key().get("messageId").s());
    }

    @Test
    @DisplayName("deve retornar false quando a mensagem não existir na tabela")
    void shouldReturnFalseWhenMessageDoesNotExist() {
      when(asyncProperties.getDynamodbTableName()).thenReturn("processed-messages");
      when(dynamoDbClient.getItem(any(GetItemRequest.class)))
          .thenReturn(GetItemResponse.builder().item(Collections.emptyMap()).build());

      boolean exists = store.exists("msg-2");

      assertFalse(exists);
    }
  }

  @Nested
  @DisplayName("tryMarkAsProcessed — marcação de mensagem processada")
  class TryMarkAsProcessed {

    @Test
    @DisplayName("deve retornar true quando conseguir marcar a mensagem como processada")
    void shouldReturnTrueWhenMessageIsMarkedAsProcessed() {
      when(asyncProperties.getDynamodbTableName()).thenReturn("processed-messages");
      when(asyncProperties.getTtlDays()).thenReturn(7L);

      boolean marked = store.tryMarkAsProcessed("msg-1");

      assertTrue(marked);

      ArgumentCaptor<PutItemRequest> captor = ArgumentCaptor.forClass(PutItemRequest.class);
      verify(dynamoDbClient).putItem(captor.capture());

      PutItemRequest request = captor.getValue();
      assertEquals("processed-messages", request.tableName());
      assertEquals("attribute_not_exists(messageId)", request.conditionExpression());
      assertEquals("msg-1", request.item().get("messageId").s());
      assertTrue(request.item().containsKey("processedAt"));
      assertTrue(request.item().containsKey("ttl"));
    }

    @Test
    @DisplayName("deve retornar false quando a mensagem já tiver sido processada")
    void shouldReturnFalseWhenMessageWasAlreadyProcessed() {
      when(asyncProperties.getDynamodbTableName()).thenReturn("processed-messages");
      when(asyncProperties.getTtlDays()).thenReturn(7L);
      when(dynamoDbClient.putItem(any(PutItemRequest.class)))
          .thenThrow(ConditionalCheckFailedException.builder().message("duplicate").build());

      boolean marked = store.tryMarkAsProcessed("msg-1");

      assertFalse(marked);
    }
  }
}
