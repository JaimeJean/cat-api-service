package com.github.jaimejean.catapi.domain.ports.out;

import com.github.jaimejean.catapi.domain.entities.Image;
import com.github.jaimejean.catapi.domain.enums.ImageCategory;
import java.util.List;

public interface ImageRepository {

  List<Image> findAll();

  Image save(Image image);

  List<Image> saveAll(List<Image> images);

  List<Image> findByCategory(ImageCategory category);

  boolean existsByBreedIdAndCategory(Long breedId, ImageCategory category);

  boolean existsByCategoryAndBreedIdIsNull(ImageCategory category);
}
