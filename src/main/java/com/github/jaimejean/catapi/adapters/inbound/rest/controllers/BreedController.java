package com.github.jaimejean.catapi.adapters.inbound.rest.controllers;

import com.github.jaimejean.catapi.adapters.inbound.rest.dtos.BreedResponse;
import com.github.jaimejean.catapi.domain.ports.in.BreedService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/breeds")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Breeds", description = "Endpoints for querying cat breeds")
public class BreedController {

  private final BreedService breedService;

  @GetMapping
  @Operation(
      summary = "Search breeds",
      description =
          "Returns a paginated list of breeds. "
              + "Filters are optional and combinable. "
              + "Without filters, returns all breeds.")
  public ResponseEntity<Page<BreedResponse>> search(
      @Parameter(description = "Temperament keyword, case-insensitive (ex: 'playful', 'active')")
          @RequestParam(required = false)
          String temperament,
      @Parameter(description = "Country of origin, case-insensitive (ex: 'Egypt', 'United States')")
          @RequestParam(required = false)
          String origin,
      @PageableDefault(size = 20, sort = "name") Pageable pageable) {

    log.info(
        "GET /api/v1/breeds - temperament: {}, origin: {}, page: {}, size: {}",
        temperament,
        origin,
        pageable.getPageNumber(),
        pageable.getPageSize());

    Page<BreedResponse> response =
        breedService.search(temperament, origin, pageable).map(BreedResponse::from);

    return ResponseEntity.ok(response);
  }

  @GetMapping("/{id}")
  @Operation(
      summary = "Get breed by ID",
      description = "Returns a single breed with its associated images")
  public ResponseEntity<BreedResponse> getById(
      @Parameter(description = "Internal breed ID") @PathVariable Long id) {

    log.info("GET /api/v1/breeds/{}", id);

    BreedResponse response = BreedResponse.from(breedService.getById(id));

    return ResponseEntity.ok(response);
  }
}
