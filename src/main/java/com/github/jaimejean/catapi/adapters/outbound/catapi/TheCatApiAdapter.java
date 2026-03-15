package com.github.jaimejean.catapi.adapters.outbound.catapi;

import com.github.jaimejean.catapi.config.TheCatApiConfig;
import com.github.jaimejean.catapi.domain.dtos.BreedApiResponse;
import com.github.jaimejean.catapi.domain.dtos.ImageApiResponse;
import com.github.jaimejean.catapi.domain.ports.out.CatApiClient;
import io.github.resilience4j.retry.annotation.Retry;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
@Slf4j
public class TheCatApiAdapter implements CatApiClient {

  private final RestTemplate catApiRestTemplate;
  private final TheCatApiConfig catApiConfig;

  @Override
  @Retry(name = "catApi", fallbackMethod = "fetchAllBreedsFallback")
  public List<BreedApiResponse> fetchAllBreeds() {
    String url =
        UriComponentsBuilder.fromHttpUrl(catApiConfig.getBaseUrl()).path("/breeds").toUriString();

    log.debug("Buscando todas as raças: {}", url);

    return catApiRestTemplate
        .exchange(
            url, HttpMethod.GET, null, new ParameterizedTypeReference<List<BreedApiResponse>>() {})
        .getBody();
  }

  @Override
  @Retry(name = "catApi", fallbackMethod = "fetchImagesByBreedFallback")
  public List<ImageApiResponse> fetchImagesByBreed(String breedId, int limit) {
    String url =
        UriComponentsBuilder.fromHttpUrl(catApiConfig.getBaseUrl())
            .path("/images/search")
            .queryParam("breed_ids", breedId)
            .queryParam("limit", limit)
            .toUriString();

    log.debug("Buscando imagens da raça {}: {}", breedId, url);

    return catApiRestTemplate
        .exchange(
            url, HttpMethod.GET, null, new ParameterizedTypeReference<List<ImageApiResponse>>() {})
        .getBody();
  }

  @Override
  @Retry(name = "catApi", fallbackMethod = "fetchImagesByCategoryFallback")
  public List<ImageApiResponse> fetchImagesByCategory(int categoryId, int limit) {
    String url =
        UriComponentsBuilder.fromHttpUrl(catApiConfig.getBaseUrl())
            .path("/images/search")
            .queryParam("category_ids", categoryId)
            .queryParam("limit", limit)
            .toUriString();

    log.debug("Buscando imagens da categoria {}: {}", categoryId, url);

    return catApiRestTemplate
        .exchange(
            url, HttpMethod.GET, null, new ParameterizedTypeReference<List<ImageApiResponse>>() {})
        .getBody();
  }

  private List<BreedApiResponse> fetchAllBreedsFallback(Exception e) {
    log.error("Falha ao buscar raças após todas as tentativas: {}", e.getMessage());
    return Collections.emptyList();
  }

  private List<ImageApiResponse> fetchImagesByBreedFallback(
      String breedId, int limit, Exception e) {
    log.error(
        "Falha ao buscar imagens da raça {} após todas as tentativas: {}", breedId, e.getMessage());
    return Collections.emptyList();
  }

  private List<ImageApiResponse> fetchImagesByCategoryFallback(
      int categoryId, int limit, Exception e) {
    log.error(
        "Falha ao buscar imagens da categoria {} após todas as tentativas: {}",
        categoryId,
        e.getMessage());
    return Collections.emptyList();
  }
}
