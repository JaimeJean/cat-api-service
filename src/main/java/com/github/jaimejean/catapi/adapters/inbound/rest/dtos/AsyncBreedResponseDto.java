package com.github.jaimejean.catapi.adapters.inbound.rest.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Confirmation that the async request was accepted for processing")
public record AsyncBreedResponseDto(String requestId, String message) {

  public static AsyncBreedResponseDto of(String requestId) {
    return new AsyncBreedResponseDto(
        requestId, "Request accepted. Results will be sent to your email.");
  }
}
