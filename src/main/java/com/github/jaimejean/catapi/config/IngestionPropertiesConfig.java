package com.github.jaimejean.catapi.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "catapi.ingestion")
@Getter
@Setter
public class IngestionPropertiesConfig {

  private int imagesPerBreed = 3;
  private int categoryImagesLimit = 3;
  private int maxConcurrentRequests = 10;
}
