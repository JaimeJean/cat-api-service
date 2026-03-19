package com.github.jaimejean.catapi.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.jaimejean.catapi.application.services.AsyncRequestServiceImpl;
import com.github.jaimejean.catapi.domain.entities.Breed;
import com.github.jaimejean.catapi.domain.model.AsyncBreedRequest;
import com.github.jaimejean.catapi.domain.ports.out.AsyncRequestPublisher;
import com.github.jaimejean.catapi.domain.ports.out.BreedRepository;
import com.github.jaimejean.catapi.domain.ports.out.NotificationPublisher;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class AsyncRequestServiceImplTest {

  @Mock private AsyncRequestPublisher asyncRequestPublisher;

  @Mock private NotificationPublisher notificationPublisher;

  @Mock private BreedRepository breedRepository;

  @InjectMocks private AsyncRequestServiceImpl asyncRequestService;

  @Nested
  @DisplayName("submit — publicação na fila")
  class Submit {

    @Test
    @DisplayName("deve publicar request na fila e retornar requestId")
    void shouldPublishRequestAndReturnRequestId() {
      AsyncBreedRequest request = buildRequest("test@example.com", null, null);

      String requestId = asyncRequestService.submit(request);

      assertNotNull(requestId);
      assertEquals(request.getRequestId(), requestId);
      verify(asyncRequestPublisher).publish(request);
    }

    @Test
    @DisplayName("deve publicar request com filtros na fila")
    void shouldPublishRequestWithFilters() {
      AsyncBreedRequest request = buildRequest("test@example.com", "playful", "Egypt");

      asyncRequestService.submit(request);

      verify(asyncRequestPublisher).publish(request);
    }
  }

  @Nested
  @DisplayName("process — execução da consulta e notificação")
  class Process {

    @Test
    @DisplayName("deve executar consulta sem filtros e notificar com resultados")
    void shouldQueryAllBreedsAndNotify() {
      AsyncBreedRequest request = buildRequest("test@example.com", null, null);
      List<Breed> breeds = List.of(buildBreed("abys", "Abyssinian"), buildBreed("beng", "Bengal"));
      Page<Breed> page = new PageImpl<>(breeds);

      when(breedRepository.search(null, null, Pageable.unpaged())).thenReturn(page);

      asyncRequestService.process(request);

      verify(breedRepository).search(null, null, Pageable.unpaged());
      verify(notificationPublisher).notify(request, breeds);
    }

    @Test
    @DisplayName("deve executar consulta com filtro de temperamento")
    void shouldQueryByTemperamentAndNotify() {
      AsyncBreedRequest request = buildRequest("test@example.com", "playful", null);
      List<Breed> breeds = List.of(buildBreed("abys", "Abyssinian"));
      Page<Breed> page = new PageImpl<>(breeds);

      when(breedRepository.search("playful", null, Pageable.unpaged())).thenReturn(page);

      asyncRequestService.process(request);

      verify(breedRepository).search("playful", null, Pageable.unpaged());
      verify(notificationPublisher).notify(request, breeds);
    }

    @Test
    @DisplayName("deve executar consulta com filtro de origem")
    void shouldQueryByOriginAndNotify() {
      AsyncBreedRequest request = buildRequest("test@example.com", null, "Egypt");
      List<Breed> breeds = List.of(buildBreed("emau", "Egyptian Mau"));
      Page<Breed> page = new PageImpl<>(breeds);

      when(breedRepository.search(null, "Egypt", Pageable.unpaged())).thenReturn(page);

      asyncRequestService.process(request);

      verify(breedRepository).search(null, "Egypt", Pageable.unpaged());
      verify(notificationPublisher).notify(request, breeds);
    }

    @Test
    @DisplayName("deve executar consulta com filtros combinados")
    void shouldQueryWithCombinedFiltersAndNotify() {
      AsyncBreedRequest request = buildRequest("test@example.com", "active", "United States");
      List<Breed> breeds = List.of(buildBreed("amsh", "American Shorthair"));
      Page<Breed> page = new PageImpl<>(breeds);

      when(breedRepository.search("active", "United States", Pageable.unpaged())).thenReturn(page);

      asyncRequestService.process(request);

      verify(breedRepository).search("active", "United States", Pageable.unpaged());
      verify(notificationPublisher).notify(request, breeds);
    }

    @Test
    @DisplayName("deve notificar com lista vazia quando nenhuma raça encontrada")
    void shouldNotifyWithEmptyListWhenNoBreedsFound() {
      AsyncBreedRequest request = buildRequest("test@example.com", "xyznotfound", null);
      Page<Breed> emptyPage = new PageImpl<>(Collections.emptyList());

      when(breedRepository.search("xyznotfound", null, Pageable.unpaged())).thenReturn(emptyPage);

      asyncRequestService.process(request);

      verify(notificationPublisher).notify(eq(request), eq(Collections.emptyList()));
    }
  }

  private AsyncBreedRequest buildRequest(String email, String temperament, String origin) {
    return new AsyncBreedRequest(email, temperament, origin);
  }

  private Breed buildBreed(String externalId, String name) {
    return new Breed(externalId, name, "Unknown", "Active", "A cat breed");
  }
}
