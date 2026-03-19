package com.github.jaimejean.catapi.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.jaimejean.catapi.application.services.BreedServiceImpl;
import com.github.jaimejean.catapi.domain.entities.Breed;
import com.github.jaimejean.catapi.domain.entities.Image;
import com.github.jaimejean.catapi.domain.enums.ImageCategory;
import com.github.jaimejean.catapi.domain.exceptions.BreedNotFoundException;
import com.github.jaimejean.catapi.domain.ports.out.BreedRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class BreedServiceImplTest {

  @Mock private BreedRepository breedRepository;

  @InjectMocks private BreedServiceImpl breedService;

  @Nested
  @DisplayName("search")
  class Search {

    private final Pageable defaultPageable = PageRequest.of(0, 20, Sort.by("name"));

    @Test
    @DisplayName("deve retornar página com raças quando existem resultados")
    void shouldReturnPageWithBreeds() {
      Breed breed = buildBreed(1L, "abys", "Abyssinian", "Ethiopia", "Active, Energetic");
      Page<Breed> page = new PageImpl<>(List.of(breed), defaultPageable, 1);

      when(breedRepository.search(null, null, defaultPageable)).thenReturn(page);

      Page<Breed> result = breedService.search(null, null, defaultPageable);

      assertThat(result.getContent()).hasSize(1);
      assertThat(result.getContent().get(0).getName()).isEqualTo("Abyssinian");
      verify(breedRepository).search(null, null, defaultPageable);
    }

    @Test
    @DisplayName("deve retornar página vazia quando não há resultados")
    void shouldReturnEmptyPage() {
      Page<Breed> emptyPage = Page.empty(defaultPageable);

      when(breedRepository.search(null, null, defaultPageable)).thenReturn(emptyPage);

      Page<Breed> result = breedService.search(null, null, defaultPageable);

      assertThat(result.getContent()).isEmpty();
      assertThat(result.getTotalElements()).isZero();
    }

    @Test
    @DisplayName("deve filtrar por temperamento")
    void shouldFilterByTemperament() {
      Breed breed = buildBreed(1L, "abys", "Abyssinian", "Ethiopia", "Active, Energetic");
      Page<Breed> page = new PageImpl<>(List.of(breed), defaultPageable, 1);

      when(breedRepository.search("Active", null, defaultPageable)).thenReturn(page);

      Page<Breed> result = breedService.search("Active", null, defaultPageable);

      assertThat(result.getContent()).hasSize(1);
      verify(breedRepository).search("Active", null, defaultPageable);
    }

    @Test
    @DisplayName("deve filtrar por origem")
    void shouldFilterByOrigin() {
      Breed breed = buildBreed(1L, "abys", "Abyssinian", "Ethiopia", "Active, Energetic");
      Page<Breed> page = new PageImpl<>(List.of(breed), defaultPageable, 1);

      when(breedRepository.search(null, "Ethiopia", defaultPageable)).thenReturn(page);

      Page<Breed> result = breedService.search(null, "Ethiopia", defaultPageable);

      assertThat(result.getContent()).hasSize(1);
      verify(breedRepository).search(null, "Ethiopia", defaultPageable);
    }

    @Test
    @DisplayName("deve filtrar por temperamento e origem combinados")
    void shouldFilterByTemperamentAndOrigin() {
      Breed breed = buildBreed(1L, "abys", "Abyssinian", "Ethiopia", "Active, Energetic");
      Page<Breed> page = new PageImpl<>(List.of(breed), defaultPageable, 1);

      when(breedRepository.search("Active", "Ethiopia", defaultPageable)).thenReturn(page);

      Page<Breed> result = breedService.search("Active", "Ethiopia", defaultPageable);

      assertThat(result.getContent()).hasSize(1);
      verify(breedRepository).search("Active", "Ethiopia", defaultPageable);
    }

    @Test
    @DisplayName("deve respeitar paginação customizada")
    void shouldRespectCustomPagination() {
      Pageable customPageable = PageRequest.of(2, 5, Sort.by("name"));
      Page<Breed> page = new PageImpl<>(List.of(), customPageable, 50);

      when(breedRepository.search(null, null, customPageable)).thenReturn(page);

      Page<Breed> result = breedService.search(null, null, customPageable);

      assertThat(result.getPageable().getPageNumber()).isEqualTo(2);
      assertThat(result.getPageable().getPageSize()).isEqualTo(5);
    }
  }

  @Nested
  @DisplayName("getById")
  class GetById {

    @Test
    @DisplayName("deve retornar breed quando encontrada")
    void shouldReturnBreedWhenFound() {
      Breed breed = buildBreedWithImages(1L, "abys", "Abyssinian");

      when(breedRepository.findById(1L)).thenReturn(Optional.of(breed));

      Breed result = breedService.getById(1L);

      assertThat(result.getName()).isEqualTo("Abyssinian");
      assertThat(result.getImages()).hasSize(2);
      verify(breedRepository).findById(1L);
    }

    @Test
    @DisplayName("deve lançar BreedNotFoundException quando não encontrada")
    void shouldThrowWhenNotFound() {
      when(breedRepository.findById(99L)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> breedService.getById(99L))
          .isInstanceOf(BreedNotFoundException.class);

      verify(breedRepository).findById(99L);
    }
  }

  // --- Builders ---

  private Breed buildBreed(
      Long id, String externalId, String name, String origin, String temperament) {
    Breed breed = new Breed(externalId, name, origin, temperament, "A cat breed");
    ReflectionTestUtils.setField(breed, "id", id);
    return breed;
  }

  private Breed buildBreedWithImages(Long id, String externalId, String name) {
    Breed breed = buildBreed(id, externalId, name, "Ethiopia", "Active");

    Image img1 =
        new Image(
            "img-1", "https://cdn2.thecatapi.com/images/img-1.jpg", breed, ImageCategory.BREED);
    ReflectionTestUtils.setField(img1, "id", 1L);

    Image img2 =
        new Image(
            "img-2", "https://cdn2.thecatapi.com/images/img-2.jpg", breed, ImageCategory.BREED);
    ReflectionTestUtils.setField(img2, "id", 2L);

    breed.getImages().addAll(List.of(img1, img2));
    return breed;
  }
}
