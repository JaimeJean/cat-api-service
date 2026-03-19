package com.github.jaimejean.catapi.adapters.outbound.dynamodb;

import com.github.jaimejean.catapi.config.AsyncPropertiesConfig;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProcessedMessageStore {

  private final DynamoDbClient dynamoDbClient;
  private final AsyncPropertiesConfig asyncProperties;

  public boolean exists(String messageId) {
    var response =
        dynamoDbClient.getItem(
            GetItemRequest.builder()
                .tableName(asyncProperties.getDynamodbTableName())
                .key(Map.of("messageId", AttributeValue.fromS(messageId)))
                .build());

    return response.hasItem() && !response.item().isEmpty();
  }

  public boolean tryMarkAsProcessed(String messageId) {
    long ttl = Instant.now().plus(asyncProperties.getTtlDays(), ChronoUnit.DAYS).getEpochSecond();

    try {
      dynamoDbClient.putItem(
          PutItemRequest.builder()
              .tableName(asyncProperties.getDynamodbTableName())
              .item(
                  Map.of(
                      "messageId", AttributeValue.fromS(messageId),
                      "processedAt", AttributeValue.fromS(Instant.now().toString()),
                      "ttl", AttributeValue.fromN(String.valueOf(ttl))))
              .conditionExpression("attribute_not_exists(messageId)")
              .build());

      return true;
    } catch (ConditionalCheckFailedException ex) {
      log.warn("Message already processed (concurrent): messageId={}", messageId);
      return false;
    }
  }
}
