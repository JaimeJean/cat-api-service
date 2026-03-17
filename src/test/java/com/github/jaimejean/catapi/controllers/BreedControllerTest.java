package com.github.jaimejean.catapi.controllers;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.jaimejean.catapi.adapters.inbound.rest.controllers.BreedController;
import com.github.jaimejean.catapi.domain.entities.Breed;
import com.github.jaimejean.catapi.domain.entities.Image;
import com.github.jaimejean.catapi.domain.enums.ImageCategory;
import com.github.jaimejean.catapi.domain.exceptions.BreedNotFoundException;
import com.github.jaimejean.catapi.domain.ports.in.BreedService;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(BreedController.class)
class BreedControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private BreedService breedService;

  @Nested
  @DisplayName("GET /api/v1/breeds")
  class SearchBreeds {

    @Test
    @DisplayName("deve retornar 200 com página de raças")
    void shouldReturn200WithBreeds() throws Exception {
      Breed breed = buildBreedWithImages(1L, "abys", "Abyssinian", "Ethiopia", "Active");

      when(breedService.search(eq(null), eq(null), any(Pageable.class)))
          .thenReturn(new PageImpl<>(List.of(breed)));

      mockMvc
          .perform(get("/api/v1/breeds"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.content", hasSize(1)))
          .andExpect(jsonPath("$.content[0].id", is(1)))
          .andExpect(jsonPath("$.content[0].name", is("Abyssinian")))
          .andExpect(jsonPath("$.content[0].origin", is("Ethiopia")))
          .andExpect(jsonPath("$.content[0].temperament", is("Active")))
          .andExpect(jsonPath("$.content[0].images", hasSize(1)))
          .andExpect(
              jsonPath(
                  "$.content[0].images[0].url", is("https://cdn2.thecatapi.com/images/img-1.jpg")))
          .andExpect(jsonPath("$.content[0].images[0].category", is("BREED")));
    }

    @Test
    @DisplayName("deve retornar 200 com página vazia quando não há resultados")
    void shouldReturn200WithEmptyPage() throws Exception {
      when(breedService.search(eq(null), eq(null), any(Pageable.class)))
          .thenReturn(new PageImpl<>(Collections.emptyList()));

      mockMvc
          .perform(get("/api/v1/breeds"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.content", hasSize(0)))
          .andExpect(jsonPath("$.totalElements", is(0)));
    }

    @Test
    @DisplayName("deve passar filtros de temperament e origin para o service")
    void shouldPassFiltersToService() throws Exception {
      Breed breed = buildBreed(1L, "abys", "Abyssinian", "Ethiopia", "Active");

      when(breedService.search(eq("Active"), eq("Ethiopia"), any(Pageable.class)))
          .thenReturn(new PageImpl<>(List.of(breed)));

      mockMvc
          .perform(get("/api/v1/breeds").param("temperament", "Active").param("origin", "Ethiopia"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.content", hasSize(1)))
          .andExpect(jsonPath("$.content[0].name", is("Abyssinian")));
    }

    @Test
    @DisplayName("deve respeitar parâmetros de paginação")
    void shouldRespectPaginationParams() throws Exception {
      when(breedService.search(eq(null), eq(null), any(Pageable.class)))
          .thenReturn(new PageImpl<>(Collections.emptyList()));

      mockMvc
          .perform(
              get("/api/v1/breeds")
                  .param("page", "2")
                  .param("size", "5")
                  .param("sort", "origin,desc"))
          .andExpect(status().isOk());
    }
  }

  @Nested
  @DisplayName("GET /api/v1/breeds/{id}")
  class GetById {

    @Test
    @DisplayName("deve retornar 200 com breed e imagens quando encontrada")
    void shouldReturn200WithBreedAndImages() throws Exception {
      Breed breed = buildBreedWithImages(1L, "abys", "Abyssinian", "Ethiopia", "Active");

      when(breedService.getById(1L)).thenReturn(breed);

      mockMvc
          .perform(get("/api/v1/breeds/1"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id", is(1)))
          .andExpect(jsonPath("$.name", is("Abyssinian")))
          .andExpect(jsonPath("$.description", is("A cat breed")))
          .andExpect(jsonPath("$.images", hasSize(1)))
          .andExpect(jsonPath("$.images[0].id", is(1)))
          .andExpect(jsonPath("$.images[0].category", is("BREED")));
    }

    @Test
    @DisplayName("deve retornar 404 com ProblemDetail quando breed não encontrada")
    void shouldReturn404WhenNotFound() throws Exception {
      when(breedService.getById(99L)).thenThrow(new BreedNotFoundException(99L));

      mockMvc
          .perform(get("/api/v1/breeds/99"))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.title", is("Breed Not Found")))
          .andExpect(jsonPath("$.status", is(404)))
          .andExpect(jsonPath("$.type", is("https://api.catapi.com/errors/breed-not-found")));
    }

    @Test
    @DisplayName("deve retornar 400 quando id não é numérico")
    void shouldReturn400WhenIdIsNotNumeric() throws Exception {
      mockMvc
          .perform(get("/api/v1/breeds/abc"))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.title", is("Invalid Parameter")))
          .andExpect(jsonPath("$.status", is(400)));
    }
  }

  // --- Builders ---

  private Breed buildBreed(
      Long id, String externalId, String name, String origin, String temperament) {
    Breed breed = new Breed(externalId, name, origin, temperament, "A cat breed");
    ReflectionTestUtils.setField(breed, "id", id);
    return breed;
  }

  private Breed buildBreedWithImages(
      Long id, String externalId, String name, String origin, String temperament) {
    Breed breed = buildBreed(id, externalId, name, origin, temperament);

    Image img =
        new Image(
            "img-1", "https://cdn2.thecatapi.com/images/img-1.jpg", breed, ImageCategory.BREED);
    ReflectionTestUtils.setField(img, "id", 1L);

    breed.getImages().add(img);
    return breed;
  }
}
