package com.github.jaimejean.catapi.services;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.github.jaimejean.catapi.application.services.DataIngestionServiceImpl;
import com.github.jaimejean.catapi.config.IngestionPropertiesConfig;
import com.github.jaimejean.catapi.domain.entities.Breed;
import com.github.jaimejean.catapi.domain.entities.Image;
import com.github.jaimejean.catapi.domain.enums.ImageCategory;
import com.github.jaimejean.catapi.domain.exceptions.CatApiIntegrationException;
import com.github.jaimejean.catapi.domain.ports.out.BreedRepository;
import com.github.jaimejean.catapi.domain.ports.out.CatApiClient;
import com.github.jaimejean.catapi.domain.ports.out.ImageRepository;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class DataIngestionServiceImplTest {

  @Mock private CatApiClient catApiClient;

  @Mock private BreedRepository breedRepository;

  @Mock private ImageRepository imageRepository;

  @Mock private IngestionPropertiesConfig properties;

  @InjectMocks private DataIngestionServiceImpl dataIngestionService;

  @BeforeEach
  void setUpProperties() {
    lenient().when(properties.getMaxConcurrentRequests()).thenReturn(10);
    lenient().when(properties.getImagesPerBreed()).thenReturn(3);
    lenient().when(properties.getCategoryImagesLimit()).thenReturn(3);
  }

  @Nested
  @DisplayName("ingest — orquestração completa")
  class Ingest {

    @Test
    @DisplayName("deve executar fluxo completo: raças → imagens de raças → HAT → GLASSES")
    void shouldExecuteFullIngestionFlow() {
      Breed breed = buildBreed(1L, "abys", "Abyssinian");
      List<Breed> breeds = List.of(breed);
      List<Image> breedImages = List.of(buildImage("img-1", breed, ImageCategory.BREED));
      List<Image> hatImages = List.of(buildImage("hat-1", null, ImageCategory.HAT));
      List<Image> glassesImages = List.of(buildImage("glass-1", null, ImageCategory.GLASSES));

      when(catApiClient.fetchAllBreeds()).thenReturn(breeds);
      when(breedRepository.saveAll(breeds)).thenReturn(breeds);
      when(imageRepository.existsByBreedIdAndCategory(1L, ImageCategory.BREED)).thenReturn(false);
      when(catApiClient.fetchImagesByBreed(breed, 3)).thenReturn(breedImages);
      when(imageRepository.existsByCategoryAndBreedIdIsNull(ImageCategory.HAT)).thenReturn(false);
      when(catApiClient.fetchImagesByCategory(ImageCategory.HAT, 3)).thenReturn(hatImages);
      when(imageRepository.existsByCategoryAndBreedIdIsNull(ImageCategory.GLASSES))
          .thenReturn(false);
      when(catApiClient.fetchImagesByCategory(ImageCategory.GLASSES, 3)).thenReturn(glassesImages);

      dataIngestionService.ingest();

      verify(breedRepository).saveAll(breeds);
      verify(catApiClient).fetchImagesByBreed(breed, 3);
      verify(imageRepository, times(3)).saveAll(anyList());
    }

    @Test
    @DisplayName("deve lançar exceção quando API retorna lista vazia de raças")
    void shouldThrowExceptionWhenBreedsListIsEmpty() {
      when(catApiClient.fetchAllBreeds()).thenReturn(Collections.emptyList());

      assertThrows(CatApiIntegrationException.class, () -> dataIngestionService.ingest());

      verify(breedRepository, never()).saveAll(anyList());
      verify(catApiClient, never()).fetchImagesByBreed(any(), anyInt());
      verify(catApiClient, never()).fetchImagesByCategory(any(), anyInt());
    }
  }

  @Nested
  @DisplayName("ingestBreedImages — idempotência e resiliência")
  class IngestBreedImages {

    @Test
    @DisplayName("deve pular raça quando imagens já existem no banco")
    void shouldSkipBreedWhenImagesAlreadyExist() {
      Breed breed = buildBreed(1L, "abys", "Abyssinian");

      when(catApiClient.fetchAllBreeds()).thenReturn(List.of(breed));
      when(breedRepository.saveAll(anyList())).thenReturn(List.of(breed));
      when(imageRepository.existsByBreedIdAndCategory(1L, ImageCategory.BREED)).thenReturn(true);
      when(imageRepository.existsByCategoryAndBreedIdIsNull(any())).thenReturn(true);

      dataIngestionService.ingest();

      verify(catApiClient, never()).fetchImagesByBreed(any(), anyInt());
    }

    @Test
    @DisplayName("deve processar múltiplas raças em paralelo")
    void shouldProcessMultipleBreedsInParallel() {
      Breed breed1 = buildBreed(1L, "abys", "Abyssinian");
      Breed breed2 = buildBreed(2L, "beng", "Bengal");
      Breed breed3 = buildBreed(3L, "siam", "Siamese");
      List<Breed> breeds = List.of(breed1, breed2, breed3);

      when(catApiClient.fetchAllBreeds()).thenReturn(breeds);
      when(breedRepository.saveAll(breeds)).thenReturn(breeds);
      when(imageRepository.existsByBreedIdAndCategory(any(), eq(ImageCategory.BREED)))
          .thenReturn(false);
      when(catApiClient.fetchImagesByBreed(any(), eq(3))).thenReturn(Collections.emptyList());
      when(imageRepository.existsByCategoryAndBreedIdIsNull(any())).thenReturn(true);

      dataIngestionService.ingest();

      verify(catApiClient).fetchImagesByBreed(breed1, 3);
      verify(catApiClient).fetchImagesByBreed(breed2, 3);
      verify(catApiClient).fetchImagesByBreed(breed3, 3);
    }

    @Test
    @DisplayName("deve continuar ingestão quando uma raça falha")
    void shouldContinueWhenOneBreedFails() {
      Breed breed1 = buildBreed(1L, "abys", "Abyssinian");
      Breed breed2 = buildBreed(2L, "beng", "Bengal");
      List<Breed> breeds = List.of(breed1, breed2);

      when(catApiClient.fetchAllBreeds()).thenReturn(breeds);
      when(breedRepository.saveAll(breeds)).thenReturn(breeds);
      when(imageRepository.existsByBreedIdAndCategory(any(), eq(ImageCategory.BREED)))
          .thenReturn(false);
      when(catApiClient.fetchImagesByBreed(breed1, 3))
          .thenThrow(new RuntimeException("API timeout"));
      when(catApiClient.fetchImagesByBreed(breed2, 3))
          .thenReturn(List.of(buildImage("img-2", breed2, ImageCategory.BREED)));
      when(imageRepository.existsByCategoryAndBreedIdIsNull(any())).thenReturn(true);

      dataIngestionService.ingest();

      verify(imageRepository).saveAll(anyList());
    }
  }

  @Nested
  @DisplayName("ingestCategoryImages — idempotência")
  class IngestCategoryImages {

    @Test
    @DisplayName("deve pular categoria HAT quando imagens já existem")
    void shouldSkipHatWhenAlreadyExists() {
      Breed breed = buildBreed(1L, "abys", "Abyssinian");

      when(catApiClient.fetchAllBreeds()).thenReturn(List.of(breed));
      when(breedRepository.saveAll(anyList())).thenReturn(List.of(breed));
      when(imageRepository.existsByBreedIdAndCategory(1L, ImageCategory.BREED)).thenReturn(true);
      when(imageRepository.existsByCategoryAndBreedIdIsNull(ImageCategory.HAT)).thenReturn(true);
      when(imageRepository.existsByCategoryAndBreedIdIsNull(ImageCategory.GLASSES))
          .thenReturn(false);
      when(catApiClient.fetchImagesByCategory(ImageCategory.GLASSES, 3))
          .thenReturn(Collections.emptyList());

      dataIngestionService.ingest();

      verify(catApiClient, never()).fetchImagesByCategory(eq(ImageCategory.HAT), anyInt());
      verify(catApiClient).fetchImagesByCategory(ImageCategory.GLASSES, 3);
    }

    @Test
    @DisplayName("deve pular categoria GLASSES quando imagens já existem")
    void shouldSkipGlassesWhenAlreadyExists() {
      Breed breed = buildBreed(1L, "abys", "Abyssinian");

      when(catApiClient.fetchAllBreeds()).thenReturn(List.of(breed));
      when(breedRepository.saveAll(anyList())).thenReturn(List.of(breed));
      when(imageRepository.existsByBreedIdAndCategory(1L, ImageCategory.BREED)).thenReturn(true);
      when(imageRepository.existsByCategoryAndBreedIdIsNull(ImageCategory.HAT)).thenReturn(false);
      when(catApiClient.fetchImagesByCategory(ImageCategory.HAT, 3))
          .thenReturn(Collections.emptyList());
      when(imageRepository.existsByCategoryAndBreedIdIsNull(ImageCategory.GLASSES))
          .thenReturn(true);

      dataIngestionService.ingest();

      verify(catApiClient).fetchImagesByCategory(ImageCategory.HAT, 3);
      verify(catApiClient, never()).fetchImagesByCategory(eq(ImageCategory.GLASSES), anyInt());
    }

    @Test
    @DisplayName("deve buscar e salvar imagens quando categoria não existe")
    void shouldFetchAndSaveWhenCategoryNotExists() {
      Breed breed = buildBreed(1L, "abys", "Abyssinian");
      List<Image> hatImages =
          List.of(
              buildImage("hat-1", null, ImageCategory.HAT),
              buildImage("hat-2", null, ImageCategory.HAT),
              buildImage("hat-3", null, ImageCategory.HAT));

      when(catApiClient.fetchAllBreeds()).thenReturn(List.of(breed));
      when(breedRepository.saveAll(anyList())).thenReturn(List.of(breed));
      when(imageRepository.existsByBreedIdAndCategory(1L, ImageCategory.BREED)).thenReturn(true);
      when(imageRepository.existsByCategoryAndBreedIdIsNull(ImageCategory.HAT)).thenReturn(false);
      when(catApiClient.fetchImagesByCategory(ImageCategory.HAT, 3)).thenReturn(hatImages);
      when(imageRepository.existsByCategoryAndBreedIdIsNull(ImageCategory.GLASSES))
          .thenReturn(true);

      dataIngestionService.ingest();

      verify(imageRepository).saveAll(hatImages);
    }
  }

  private Breed buildBreed(Long id, String externalId, String name) {
    Breed breed = new Breed(externalId, name, "Unknown", "Active", "A cat breed");
    ReflectionTestUtils.setField(breed, "id", id);
    return breed;
  }

  private Image buildImage(String externalId, Breed breed, ImageCategory category) {
    return new Image(
        externalId, "https://cdn2.thecatapi.com/images/" + externalId + ".jpg", breed, category);
  }
}
