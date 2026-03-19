package com.github.jaimejean.catapi.adapters.inbound.rest.controllers;

import com.github.jaimejean.catapi.adapters.inbound.rest.dtos.AsyncBreedRequestDto;
import com.github.jaimejean.catapi.adapters.inbound.rest.dtos.AsyncBreedResponseDto;
import com.github.jaimejean.catapi.domain.model.AsyncBreedRequest;
import com.github.jaimejean.catapi.domain.ports.in.AsyncRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/async/breeds")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Async Breeds", description = "Asynchronous breed query — results sent by email")
public class AsyncBreedController {

  private final AsyncRequestService asyncRequestService;

  @PostMapping
  @Operation(
      summary = "Request breeds asynchronously",
      description =
          "Filters are optional and combinable. Without filters, returns all breeds. "
              + "Results will be sent to the provided email address.")
  public ResponseEntity<AsyncBreedResponseDto> requestBreeds(
      @Valid @RequestBody AsyncBreedRequestDto request) {

    log.info(
        "POST /api/v1/async/breeds - email: {}, temperament: {}, origin: {}",
        maskEmail(request.email()),
        request.temperament(),
        request.origin());

    String requestId =
        asyncRequestService.submit(
            new AsyncBreedRequest(request.email(), request.temperament(), request.origin()));

    return ResponseEntity.status(HttpStatus.ACCEPTED).body(AsyncBreedResponseDto.of(requestId));
  }

  private String maskEmail(String email) {
    int atIndex = email.indexOf('@');
    if (atIndex <= 1) return "***";
    return email.charAt(0) + "***" + email.substring(atIndex);
  }
}
