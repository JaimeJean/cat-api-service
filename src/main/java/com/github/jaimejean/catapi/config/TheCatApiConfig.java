package com.github.jaimejean.catapi.config;

import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
@ConfigurationProperties(prefix = "catapi.api")
@Getter
@Setter
public class TheCatApiConfig {

  private String baseUrl;
  private String key;
  private int connectTimeout = 5000;
  private int readTimeout = 10000;

  @Bean
  public RestTemplate catApiRestTemplate(RestTemplateBuilder builder) {
    return builder
        .connectTimeout(Duration.ofMillis(connectTimeout))
        .readTimeout(Duration.ofMillis(readTimeout))
        .defaultHeader("x-api-key", key)
        .build();
  }
}
