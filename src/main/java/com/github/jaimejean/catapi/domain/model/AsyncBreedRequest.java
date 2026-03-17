package com.github.jaimejean.catapi.domain.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;

@Getter
public class AsyncBreedRequest {

  private final String requestId;
  private final String email;
  private final String temperament;
  private final String origin;

  public AsyncBreedRequest(String email, String temperament, String origin) {
    this.requestId = UUID.randomUUID().toString();
    this.email = validateEmail(email);
    this.temperament = temperament;
    this.origin = origin;
  }

  @JsonCreator
  public static AsyncBreedRequest reconstitute(
      @JsonProperty("requestId") String requestId,
      @JsonProperty("email") String email,
      @JsonProperty("temperament") String temperament,
      @JsonProperty("origin") String origin) {
    return new AsyncBreedRequest(requestId, email, temperament, origin);
  }

  private AsyncBreedRequest(String requestId, String email, String temperament, String origin) {
    this.requestId = Objects.requireNonNull(requestId, "requestId must not be null");
    this.email = validateEmail(email);
    this.temperament = temperament;
    this.origin = origin;
  }

  private String validateEmail(String email) {
    if (email == null || email.isBlank()) {
      throw new IllegalArgumentException("email must not be blank");
    }
    return email;
  }

  @Override
  public String toString() {
    return "AsyncBreedRequest{requestId='%s', temperament='%s', origin='%s'}"
        .formatted(requestId, temperament, origin);
  }
}
