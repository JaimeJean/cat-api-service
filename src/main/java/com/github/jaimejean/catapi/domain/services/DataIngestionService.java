package com.github.jaimejean.catapi.domain.services;

import com.github.jaimejean.catapi.config.IngestionPropertiesConfig;
import com.github.jaimejean.catapi.domain.dtos.BreedApiResponse;
import com.github.jaimejean.catapi.domain.dtos.ImageApiResponse;
import com.github.jaimejean.catapi.domain.entities.Breed;
import com.github.jaimejean.catapi.domain.entities.Image;
import com.github.jaimejean.catapi.domain.enums.ImageCategory;
import com.github.jaimejean.catapi.domain.ports.out.BreedRepository;
import com.github.jaimejean.catapi.domain.ports.out.CatApiClient;
import com.github.jaimejean.catapi.domain.ports.out.ImageRepository;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataIngestionService {

  private final CatApiClient catApiClient;
  private final BreedRepository breedRepository;
  private final ImageRepository imageRepository;
  private final IngestionPropertiesConfig properties;

  public void ingest() {
    log.info("Iniciando ingestão de dados da TheCatAPI");

    List<Breed> breeds = this.ingestBreeds();
    this.ingestBreedImages(breeds);
    this.ingestCategoryImages(ImageCategory.HAT);
    this.ingestCategoryImages(ImageCategory.GLASSES);

    log.info("Ingestão de dados finalizada");
  }

  private List<Breed> ingestBreeds() {
    List<BreedApiResponse> breedsFromApi = catApiClient.fetchAllBreeds();
    log.info("Raças encontradas na API: {}", breedsFromApi.size());

    List<Breed> breeds = breedsFromApi.stream().map(this::toBreedEntity).toList();

    return breedRepository.saveAll(breeds);
  }

  private void ingestBreedImages(List<Breed> breeds) {
    Semaphore semaphore = new Semaphore(properties.getMaxConcurrentRequests());

    try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {

      List<? extends Future<?>> futures =
          breeds.stream()
              .map(breed -> executor.submit(() -> this.fetchAndSaveWithThrottle(breed, semaphore)))
              .toList();

      for (Future<?> future : futures) {
        try {
          future.get();
        } catch (Exception e) {
          log.error("Erro ao buscar imagens de raça: {}", e.getMessage(), e);
        }
      }
    }

    log.info("Ingestão de imagens de raças finalizada");
  }

  private void fetchAndSaveWithThrottle(Breed breed, Semaphore semaphore) {
    try {
      semaphore.acquire();
      this.fetchAndSaveBreedImages(breed);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      log.error(
          "Thread interrompida ao buscar imagens da raça {}: {}",
          breed.getExternalId(),
          e.getMessage());
    } finally {
      semaphore.release();
    }
  }

  private void fetchAndSaveBreedImages(Breed breed) {
    if (imageRepository.existsByBreedIdAndCategory(breed.getId(), ImageCategory.BREED)) {
      log.debug("Imagens da raça {} já existem, pulando", breed.getExternalId());
      return;
    }

    List<ImageApiResponse> imagesFromApi =
        catApiClient.fetchImagesByBreed(breed.getExternalId(), properties.getImagesPerBreed());

    List<Image> images =
        imagesFromApi.stream()
            .map(response -> this.toImageEntity(response, breed, ImageCategory.BREED))
            .toList();

    imageRepository.saveAll(images);
  }

  private void ingestCategoryImages(ImageCategory category) {
    if (imageRepository.existsByCategoryAndBreedIdIsNull(category)) {
      log.info("Imagens de {} já existem, pulando", category);
      return;
    }

    List<ImageApiResponse> imagesFromApi =
        catApiClient.fetchImagesByCategory(
            category.getCatApiCategoryId(), properties.getCategoryImagesLimit());

    log.info("Imagens de {} encontradas: {}", category, imagesFromApi.size());

    List<Image> images =
        imagesFromApi.stream()
            .map(response -> this.toImageEntity(response, null, category))
            .toList();

    imageRepository.saveAll(images);
  }

  private Breed toBreedEntity(BreedApiResponse response) {
    return new Breed(
        response.id(),
        response.name(),
        response.origin(),
        response.temperament(),
        response.description());
  }

  private Image toImageEntity(ImageApiResponse response, Breed breed, ImageCategory category) {
    return new Image(response.id(), response.url(), breed, category);
  }
}
