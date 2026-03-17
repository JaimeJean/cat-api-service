package com.github.jaimejean.catapi.config;

import java.net.URI;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.BillingMode;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.ResourceInUseException;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;
import software.amazon.awssdk.services.dynamodb.model.TimeToLiveSpecification;
import software.amazon.awssdk.services.dynamodb.model.UpdateTimeToLiveRequest;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.GetQueueAttributesRequest;
import software.amazon.awssdk.services.sqs.model.QueueAttributeName;
import software.amazon.awssdk.services.sqs.model.SetQueueAttributesRequest;

@Configuration
@Profile("local")
@Slf4j
public class LocalStackInitConfig {

  @Bean
  CommandLineRunner initLocalStackResources(
      @Value("${spring.cloud.aws.endpoint}") String endpoint,
      @Value("${spring.cloud.aws.region.static}") String region,
      @Value("${catapi.async.sqs-queue-name}") String queueName,
      @Value("${catapi.async.sns-topic-name}") String topicName,
      @Value("${catapi.async.ses-sender-email}") String senderEmail,
      @Value("${catapi.async.dynamodb-table-name}") String dynamoTableName) {

    return args -> {
      log.info("Initializing LocalStack resources...");

      var credentials =
          StaticCredentialsProvider.create(AwsBasicCredentials.create("test", "test"));
      var endpointUri = URI.create(endpoint);
      var awsRegion = Region.of(region);

      try (var sqsClient =
              SqsClient.builder()
                  .endpointOverride(endpointUri)
                  .region(awsRegion)
                  .credentialsProvider(credentials)
                  .build();
          var snsClient =
              SnsClient.builder()
                  .endpointOverride(endpointUri)
                  .region(awsRegion)
                  .credentialsProvider(credentials)
                  .build();
          var sesClient =
              SesClient.builder()
                  .endpointOverride(endpointUri)
                  .region(awsRegion)
                  .credentialsProvider(credentials)
                  .build();
          var dynamoDbClient =
              DynamoDbClient.builder()
                  .endpointOverride(endpointUri)
                  .region(awsRegion)
                  .credentialsProvider(credentials)
                  .build()) {

        String dlqUrl = ensureQueue(sqsClient, queueName + "-dlq");
        String dlqArn = getQueueArn(sqsClient, dlqUrl);
        String mainQueueUrl = ensureQueue(sqsClient, queueName);
        attachDlq(sqsClient, mainQueueUrl, dlqArn);
        createTopic(snsClient, topicName);
        verifyEmail(sesClient, senderEmail);
        createDynamoTable(dynamoDbClient, dynamoTableName);
      }

      log.info("LocalStack resources initialized successfully");
    };
  }

  private String ensureQueue(SqsClient sqsClient, String name) {
    String queueUrl = sqsClient.createQueue(r -> r.queueName(name)).queueUrl();
    log.info("SQS queue ensured: {}", name);
    return queueUrl;
  }

  private String getQueueArn(SqsClient sqsClient, String queueUrl) {
    return sqsClient
        .getQueueAttributes(
            GetQueueAttributesRequest.builder()
                .queueUrl(queueUrl)
                .attributeNames(QueueAttributeName.QUEUE_ARN)
                .build())
        .attributes()
        .get(QueueAttributeName.QUEUE_ARN);
  }

  private void attachDlq(SqsClient sqsClient, String queueUrl, String dlqArn) {
    String redrivePolicy =
        "{\"deadLetterTargetArn\":\"%s\",\"maxReceiveCount\":\"3\"}".formatted(dlqArn);

    sqsClient.setQueueAttributes(
        SetQueueAttributesRequest.builder()
            .queueUrl(queueUrl)
            .attributes(Map.of(QueueAttributeName.REDRIVE_POLICY, redrivePolicy))
            .build());

    log.info("DLQ attached to queue: dlqArn={}", dlqArn);
  }

  private void createTopic(SnsClient snsClient, String name) {
    snsClient.createTopic(r -> r.name(name));
    log.info("SNS topic created: {}", name);
  }

  private void verifyEmail(SesClient sesClient, String email) {
    sesClient.verifyEmailIdentity(r -> r.emailAddress(email));
    log.info("SES email verified: {}", email);
  }

  private void createDynamoTable(DynamoDbClient dynamoDbClient, String tableName) {
    try {
      dynamoDbClient.createTable(
          r ->
              r.tableName(tableName)
                  .keySchema(
                      KeySchemaElement.builder()
                          .attributeName("messageId")
                          .keyType(KeyType.HASH)
                          .build())
                  .attributeDefinitions(
                      AttributeDefinition.builder()
                          .attributeName("messageId")
                          .attributeType(ScalarAttributeType.S)
                          .build())
                  .billingMode(BillingMode.PAY_PER_REQUEST));

      dynamoDbClient.updateTimeToLive(
          UpdateTimeToLiveRequest.builder()
              .tableName(tableName)
              .timeToLiveSpecification(
                  TimeToLiveSpecification.builder().attributeName("ttl").enabled(true).build())
              .build());

      log.info("DynamoDB table created with TTL: {}", tableName);
    } catch (ResourceInUseException ex) {
      log.info("DynamoDB table already exists: {}", tableName);
    }
  }
}
