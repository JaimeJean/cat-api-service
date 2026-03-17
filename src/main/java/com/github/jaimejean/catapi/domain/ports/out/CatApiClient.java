package com.github.jaimejean.catapi.domain.ports.out;

import com.github.jaimejean.catapi.domain.entities.Breed;
import com.github.jaimejean.catapi.domain.entities.Image;
import com.github.jaimejean.catapi.domain.enums.ImageCategory;
import java.util.List;

public interface CatApiClient {

  List<Breed> fetchAllBreeds();

  List<Image> fetchImagesByBreed(Breed breed, int limit);

  List<Image> fetchImagesByCategory(ImageCategory category, int limit);
}
