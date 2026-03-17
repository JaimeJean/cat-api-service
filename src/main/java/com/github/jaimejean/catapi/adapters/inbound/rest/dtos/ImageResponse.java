package com.github.jaimejean.catapi.adapters.inbound.rest.dtos;

import com.github.jaimejean.catapi.domain.entities.Image;

public record ImageResponse(Long id, String url, String category) {
  public static ImageResponse from(Image image) {
    return new ImageResponse(image.getId(), image.getUrl(), image.getCategory().name());
  }
}
