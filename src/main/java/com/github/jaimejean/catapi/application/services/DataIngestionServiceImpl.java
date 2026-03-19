package com.github.jaimejean.catapi.application.services;

import com.github.jaimejean.catapi.config.IngestionPropertiesConfig;
import com.github.jaimejean.catapi.domain.entities.Breed;
import com.github.jaimejean.catapi.domain.entities.Image;
import com.github.jaimejean.catapi.domain.enums.ImageCategory;
import com.github.jaimejean.catapi.domain.exceptions.CatApiIntegrationException;
import com.github.jaimejean.catapi.domain.ports.in.DataIngestionService;
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
public class DataIngestionServiceImpl implements DataIngestionService {

  private final CatApiClient catApiClient;
  private final BreedRepository breedRepository;
  private final ImageRepository imageRepository;
  private final IngestionPropertiesConfig properties;

  @Override
  public void ingest() {
    log.info("Iniciando ingestão de dados da TheCatAPI");

    List<Breed> breeds = this.ingestBreeds();
    this.ingestBreedImages(breeds);
    this.ingestCategoryImages(ImageCategory.HAT);
    this.ingestCategoryImages(ImageCategory.GLASSES);

    log.info("Ingestão de dados finalizada");
  }

  private List<Breed> ingestBreeds() {
    List<Breed> breedsFromApi = catApiClient.fetchAllBreeds();
    if (breedsFromApi.isEmpty()) {

      throw new CatApiIntegrationException(
          "TheCatAPI retornou lista vazia de raças — ingestão abortada");
    }

    log.info("Raças encontradas na API: {}", breedsFromApi.size());

    return breedRepository.saveAll(breedsFromApi);
  }

  private void ingestBreedImages(List<Breed> breeds) {
    // controle de concorrência
    Semaphore semaphore = new Semaphore(properties.getMaxConcurrentRequests());

    try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {

      // Submete uma virtual thread por raça (todas são criadas de uma vez)
      // mas o semaphore controla quantas acessam a API ao mesmo tempo
      List<? extends Future<?>> futures =
          breeds.stream()
              .map(breed -> executor.submit(() -> this.fetchAndSaveWithThrottle(breed, semaphore)))
              .toList();

      // Aguarda todas terminarem; se alguma falhar, loga e segue com as demais
      for (Future<?> future : futures) {
        try {
          future.get();
        } catch (Exception e) {
          log.error("Erro ao buscar imagens de raça: {}", e.getMessage(), e);
        }
      }
    }
    // try-with-resources garante shutdown do executor ao sair do bloco

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

    List<Image> images = catApiClient.fetchImagesByBreed(breed, properties.getImagesPerBreed());
    imageRepository.saveAll(images);
  }

  private void ingestCategoryImages(ImageCategory category) {
    if (imageRepository.existsByCategoryAndBreedIdIsNull(category)) {
      log.info("Imagens de {} já existem, pulando", category);
      return;
    }

    List<Image> images =
        catApiClient.fetchImagesByCategory(category, properties.getCategoryImagesLimit());
    log.info("Imagens de {} encontradas: {}", category, images.size());

    imageRepository.saveAll(images);
  }
}
