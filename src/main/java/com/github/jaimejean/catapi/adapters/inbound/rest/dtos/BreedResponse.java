package com.github.jaimejean.catapi.adapters.inbound.rest.dtos;

import com.github.jaimejean.catapi.domain.entities.Breed;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Request for sync Breeds")
public record BreedResponse(
    Long id,
    String name,
    String origin,
    String temperament,
    String description,
    List<ImageResponse> images) {
  public static BreedResponse from(Breed breed) {
    List<ImageResponse> images = breed.getImages().stream().map(ImageResponse::from).toList();

    return new BreedResponse(
        breed.getId(),
        breed.getName(),
        breed.getOrigin(),
        breed.getTemperament(),
        breed.getDescription(),
        images);
  }
}
