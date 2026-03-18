package com.github.jaimejean.catapi.adapters.outbound.ses;

import com.github.jaimejean.catapi.config.AsyncPropertiesConfig;
import com.github.jaimejean.catapi.domain.entities.Breed;
import com.github.jaimejean.catapi.domain.model.AsyncBreedRequest;
import com.github.jaimejean.catapi.domain.ports.out.NotificationPublisher;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.Body;
import software.amazon.awssdk.services.ses.model.Content;
import software.amazon.awssdk.services.ses.model.Destination;
import software.amazon.awssdk.services.ses.model.Message;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;

@Component
@RequiredArgsConstructor
@Slf4j
public class SesNotificationPublisher implements NotificationPublisher {

  private final SesClient sesClient;
  private final AsyncPropertiesConfig asyncProperties;

  @Override
  public void notify(AsyncBreedRequest request, List<Breed> breeds) {
    log.info(
        "Sending notification via SES: requestId={}, to={}",
        request.getRequestId(),
        maskEmail(request.getEmail()));

    try {
      SendEmailRequest emailRequest =
          SendEmailRequest.builder()
              .source(asyncProperties.getSesSenderEmail())
              .destination(Destination.builder().toAddresses(request.getEmail()).build())
              .message(
                  Message.builder()
                      .subject(buildSubject(request))
                      .body(
                          Body.builder().text(buildTextContent(buildBody(request, breeds))).build())
                      .build())
              .build();

      sesClient.sendEmail(emailRequest);
      log.info("Notification sent successfully via SES: requestId={}", request.getRequestId());
    } catch (Exception ex) {
      log.error("Failed to send notification via SES: requestId={}", request.getRequestId(), ex);
      throw new RuntimeException("Failed to send notification email", ex);
    }
  }

  private Content buildSubject(AsyncBreedRequest request) {
    String filters = describeFilters(request);
    String subject = filters.isEmpty() ? "Cat API — All Breeds" : "Cat API — Breeds: " + filters;
    return buildTextContent(subject);
  }

  private String buildBody(AsyncBreedRequest request, List<Breed> breeds) {
    String filters = describeFilters(request);
    String query = filters.isEmpty() ? "All breeds" : "Filters: " + filters;

    if (breeds.isEmpty()) {
      return "No breeds found for your query (%s).".formatted(query);
    }

    String header = "%s\nResults: %d breed(s)\n%s".formatted(query, breeds.size(), "─".repeat(40));

    String details = breeds.stream().map(this::formatBreed).collect(Collectors.joining("\n\n"));

    return header + "\n\n" + details;
  }

  private String describeFilters(AsyncBreedRequest request) {
    return Stream.of(
            Optional.ofNullable(request.getTemperament()).map("temperament=%s"::formatted),
            Optional.ofNullable(request.getOrigin()).map("origin=%s"::formatted))
        .flatMap(Optional::stream)
        .collect(Collectors.joining(", "));
  }

  private String formatBreed(Breed breed) {
    return Stream.of(
            Optional.of(breed.getName()),
            Optional.ofNullable(breed.getOrigin()).map("Origin: %s"::formatted),
            Optional.ofNullable(breed.getTemperament()).map("Temperament: %s"::formatted),
            Optional.ofNullable(breed.getDescription()))
        .flatMap(Optional::stream)
        .collect(Collectors.joining("\n"));
  }

  private Content buildTextContent(String text) {
    return Content.builder().charset("UTF-8").data(text).build();
  }

  private String maskEmail(String email) {
    int atIndex = email.indexOf('@');
    if (atIndex <= 1) return "***";
    return email.charAt(0) + "***" + email.substring(atIndex);
  }
}
