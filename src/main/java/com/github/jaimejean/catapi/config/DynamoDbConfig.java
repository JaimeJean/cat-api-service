package com.github.jaimejean.catapi.config;

import java.net.URI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@Configuration
public class DynamoDbConfig {

  @Bean
  public DynamoDbClient dynamoDbClient(
      @Value("${spring.cloud.aws.region.static:us-east-1}") String region,
      @Value("${spring.cloud.aws.endpoint:}") String endpoint,
      @Value("${spring.cloud.aws.credentials.access-key:}") String accessKey,
      @Value("${spring.cloud.aws.credentials.secret-key:}") String secretKey) {

    var builder = DynamoDbClient.builder().region(Region.of(region));

    if (!endpoint.isBlank()) {
      builder
          .endpointOverride(URI.create(endpoint))
          .credentialsProvider(
              StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)));
    } else {
      builder.credentialsProvider(DefaultCredentialsProvider.create());
    }

    return builder.build();
  }
}
