package com.github.jaimejean.catapi.adapters.outbound.dynamodb;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

@Component
@Slf4j
public class ProcessedMessageStore {

  private static final long TTL_DAYS = 7;

  private final DynamoDbClient dynamoDbClient;
  private final String tableName;

  public ProcessedMessageStore(
      DynamoDbClient dynamoDbClient,
      @Value("${catapi.async.dynamodb-table-name}") String tableName) {
    this.dynamoDbClient = dynamoDbClient;
    this.tableName = tableName;
  }

  public boolean exists(String messageId) {
    var response =
        dynamoDbClient.getItem(
            GetItemRequest.builder()
                .tableName(tableName)
                .key(Map.of("messageId", AttributeValue.fromS(messageId)))
                .build());

    return response.hasItem() && !response.item().isEmpty();
  }

  public boolean tryMarkAsProcessed(String messageId) {
    long ttl = Instant.now().plus(TTL_DAYS, ChronoUnit.DAYS).getEpochSecond();

    try {
      dynamoDbClient.putItem(
          PutItemRequest.builder()
              .tableName(tableName)
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
