package com.github.jaimejean.catapi.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ImageCategory {
  BREED(null),
  HAT(1),
  GLASSES(4);

  private final Integer catApiCategoryId;
}
