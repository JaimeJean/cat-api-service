package com.github.jaimejean.catapi.controllers;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.jaimejean.catapi.adapters.inbound.rest.controllers.ImageController;
import com.github.jaimejean.catapi.domain.entities.Image;
import com.github.jaimejean.catapi.domain.enums.ImageCategory;
import com.github.jaimejean.catapi.domain.ports.in.ImageService;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ImageController.class)
class ImageControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private ImageService imageService;

  @Test
  @DisplayName("deve retornar 200 com imagens da categoria HAT")
  void shouldReturn200WithHatImages() throws Exception {
    List<Image> images =
        List.of(
            buildImage(1L, "hat-1", ImageCategory.HAT), buildImage(2L, "hat-2", ImageCategory.HAT));

    when(imageService.findByCategory(ImageCategory.HAT)).thenReturn(images);

    mockMvc
        .perform(get("/api/v1/images").param("category", "HAT"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[0].id", is(1)))
        .andExpect(jsonPath("$[0].category", is("HAT")))
        .andExpect(jsonPath("$[1].id", is(2)));

    verify(imageService).findByCategory(ImageCategory.HAT);
  }

  @Test
  @DisplayName("deve retornar 200 com lista vazia quando não há imagens")
  void shouldReturn200WithEmptyList() throws Exception {
    when(imageService.findByCategory(ImageCategory.GLASSES)).thenReturn(Collections.emptyList());

    mockMvc
        .perform(get("/api/v1/images").param("category", "GLASSES"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(0)));
  }

  @Test
  @DisplayName("deve retornar 400 quando categoria é inválida")
  void shouldReturn400WhenCategoryIsInvalid() throws Exception {
    mockMvc
        .perform(get("/api/v1/images").param("category", "INVALID"))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("deve retornar 200 com todas as imagens quando categoria não é informada")
  void shouldReturn200WhenNoCategoryProvided() throws Exception {
    when(imageService.findByCategory(null)).thenReturn(Collections.emptyList());

    mockMvc
        .perform(get("/api/v1/images"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(0)));
  }

  // --- Builder ---

  private Image buildImage(Long id, String externalId, ImageCategory category) {
    Image image =
        new Image(
            externalId, "https://cdn2.thecatapi.com/images/" + externalId + ".jpg", null, category);
    ReflectionTestUtils.setField(image, "id", id);
    return image;
  }
}
