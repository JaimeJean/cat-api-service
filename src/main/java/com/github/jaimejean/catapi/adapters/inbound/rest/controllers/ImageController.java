package com.github.jaimejean.catapi.adapters.inbound.rest.controllers;

import com.github.jaimejean.catapi.adapters.inbound.rest.dtos.ImageResponse;
import com.github.jaimejean.catapi.domain.enums.ImageCategory;
import com.github.jaimejean.catapi.domain.ports.in.ImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/images")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Images", description = "Endpoints for querying cat images by category")
public class ImageController {

  private final ImageService imageService;

  @GetMapping
  @Operation(
      summary = "List images",
      description = "Returns images filtered by category. " + "Without filter, returns all images.")
  public ResponseEntity<List<ImageResponse>> findByCategory(
      @Parameter(description = "Image category: BREED, HAT or GLASSES")
          @RequestParam(required = false)
          ImageCategory category) {

    log.info("GET /api/v1/images - category: {}", category);

    List<ImageResponse> response =
        imageService.findByCategory(category).stream().map(ImageResponse::from).toList();

    return ResponseEntity.ok(response);
  }
}
