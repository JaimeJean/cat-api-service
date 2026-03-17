package com.github.jaimejean.catapi.adapters.outbound.catapi.mappers;

import com.github.jaimejean.catapi.adapters.outbound.catapi.dtos.ImageApiResponse;
import com.github.jaimejean.catapi.domain.entities.Breed;
import com.github.jaimejean.catapi.domain.entities.Image;
import com.github.jaimejean.catapi.domain.enums.ImageCategory;

public class ImageMapper {

  private ImageMapper() {}

  public static Image toEntity(ImageApiResponse response, Breed breed, ImageCategory category) {
    return new Image(response.id(), response.url(), breed, category);
  }
}
