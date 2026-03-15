package com.github.jaimejean.catapi.domain.ports.out;

import com.github.jaimejean.catapi.domain.dtos.BreedApiResponse;
import com.github.jaimejean.catapi.domain.dtos.ImageApiResponse;
import java.util.List;

public interface CatApiClient {

  List<BreedApiResponse> fetchAllBreeds();

  List<ImageApiResponse> fetchImagesByBreed(String breedId, int limit);

  List<ImageApiResponse> fetchImagesByCategory(int categoryId, int limit);
}
