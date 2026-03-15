package com.github.jaimejean.catapi.domain.ports.out;

import com.github.jaimejean.catapi.domain.entities.Image;
import com.github.jaimejean.catapi.domain.enums.ImageCategory;
import java.util.List;
import java.util.Optional;

public interface ImageRepository {

  Image save(Image image);

  List<Image> saveAll(List<Image> images);

  Optional<Image> findByExternalId(String externalId);

  List<Image> findByBreedId(Long breedId);

  List<Image> findByCategory(ImageCategory category);

  boolean existsByBreedIdAndCategory(Long breedId, ImageCategory category);

  boolean existsByCategoryAndBreedIdIsNull(ImageCategory category);
}
