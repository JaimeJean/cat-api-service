package com.github.jaimejean.catapi.adapters.outbound.catapi.mappers;

import com.github.jaimejean.catapi.adapters.outbound.catapi.dtos.BreedApiResponse;
import com.github.jaimejean.catapi.domain.entities.Breed;

public class BreedMapper {

  private BreedMapper() {}

  public static Breed toEntity(BreedApiResponse response) {
    return new Breed(
        response.id(),
        response.name(),
        response.origin(),
        response.temperament(),
        response.description());
  }
}
