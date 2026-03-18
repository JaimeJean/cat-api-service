package com.github.jaimejean.catapi.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "catapi.async")
@Getter
@Setter
public class AsyncPropertiesConfig {

  private String sqsQueueName = "cat-api-async-requests";
  private String snsTopicName = "cat-api-notifications";
  private String sesSenderEmail = "noreply@catapi.local";
  private String dynamodbTableName = "processed-messages";
  private long ttlDays = 7;
}
