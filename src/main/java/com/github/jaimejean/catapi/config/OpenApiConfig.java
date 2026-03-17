package com.github.jaimejean.catapi.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI customOpenAPI() {
    return new OpenAPI()
        .info(
            new Info()
                .title("Cat API Service")
                .description(
                    "API for querying cat breeds, images, and related data. "
                        + "Consumes TheCatAPI and exposes curated endpoints with pagination and filtering.")
                .version("1.0.0")
                .contact(new Contact().name("Jaime Jean").url("https://github.com/jaimejean")));
  }
}
