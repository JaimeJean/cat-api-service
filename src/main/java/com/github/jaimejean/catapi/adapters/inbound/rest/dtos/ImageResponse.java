package com.github.jaimejean.catapi.adapters.inbound.rest.dtos;

import com.github.jaimejean.catapi.domain.entities.Image;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request for sync Images")
public record ImageResponse(Long id, String url, String category) {
  public static ImageResponse from(Image image) {
    return new ImageResponse(image.getId(), image.getUrl(), image.getCategory().name());
  }
}
