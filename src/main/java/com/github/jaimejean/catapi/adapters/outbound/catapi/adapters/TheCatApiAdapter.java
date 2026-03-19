package com.github.jaimejean.catapi.adapters.outbound.catapi.adapters;

import com.github.jaimejean.catapi.adapters.outbound.catapi.dtos.BreedApiResponse;
import com.github.jaimejean.catapi.adapters.outbound.catapi.dtos.ImageApiResponse;
import com.github.jaimejean.catapi.adapters.outbound.catapi.mappers.BreedMapper;
import com.github.jaimejean.catapi.adapters.outbound.catapi.mappers.ImageMapper;
import com.github.jaimejean.catapi.config.TheCatApiConfig;
import com.github.jaimejean.catapi.domain.entities.Breed;
import com.github.jaimejean.catapi.domain.entities.Image;
import com.github.jaimejean.catapi.domain.enums.ImageCategory;
import com.github.jaimejean.catapi.domain.exceptions.CatApiIntegrationException;
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
  public List<Breed> fetchAllBreeds() {
    String url =
        UriComponentsBuilder.fromHttpUrl(catApiConfig.getBaseUrl()).path("/breeds").toUriString();

    log.debug("Buscando todas as raças: {}", url);

    // exchange para deserializar a lista evitando erros de run time
    List<BreedApiResponse> response =
        catApiRestTemplate
            .exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<BreedApiResponse>>() {})
            .getBody();

    if (response == null) {
      return Collections.emptyList();
    }

    return response.stream().map(BreedMapper::toEntity).toList();
  }

  @Override
  @Retry(name = "catApi", fallbackMethod = "fetchImagesByBreedFallback")
  public List<Image> fetchImagesByBreed(Breed breed, int limit) {
    String url =
        UriComponentsBuilder.fromHttpUrl(catApiConfig.getBaseUrl())
            .path("/images/search")
            .queryParam("breed_ids", breed.getExternalId())
            .queryParam("limit", limit)
            .toUriString();

    log.debug("Buscando imagens da raça {}: {}", breed.getExternalId(), url);

    List<ImageApiResponse> response =
        catApiRestTemplate
            .exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<ImageApiResponse>>() {})
            .getBody();

    if (response == null) {
      return Collections.emptyList();
    }

    return response.stream()
        .map(dto -> ImageMapper.toEntity(dto, breed, ImageCategory.BREED))
        .toList();
  }

  @Override
  @Retry(name = "catApi", fallbackMethod = "fetchImagesByCategoryFallback")
  public List<Image> fetchImagesByCategory(ImageCategory category, int limit) {
    String url =
        UriComponentsBuilder.fromHttpUrl(catApiConfig.getBaseUrl())
            .path("/images/search")
            .queryParam("category_ids", category.getCatApiCategoryId())
            .queryParam("limit", limit)
            .toUriString();

    log.debug("Buscando imagens da categoria {}: {}", category, url);

    List<ImageApiResponse> response =
        catApiRestTemplate
            .exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<ImageApiResponse>>() {})
            .getBody();

    if (response == null) {
      return Collections.emptyList();
    }

    return response.stream().map(dto -> ImageMapper.toEntity(dto, null, category)).toList();
  }

  // Falha crítica — sem raças, a ingestão inteira perde sentido
  private List<Breed> fetchAllBreedsFallback(Exception e) {
    log.error("Falha ao buscar raças após todas as tentativas: {}", e.getMessage());
    throw new CatApiIntegrationException("Falha ao buscar raças da TheCatAPI", e);
  }

  // Falha parcial — uma raça sem imagens não compromete as demais
  private List<Image> fetchImagesByBreedFallback(Breed breed, int limit, Exception e) {
    log.error(
        "Falha ao buscar imagens da raça {} após todas as tentativas: {}",
        breed.getExternalId(),
        e.getMessage());
    return Collections.emptyList();
  }

  // Falha parcial — chapéu/óculos são bônus
  private List<Image> fetchImagesByCategoryFallback(
      ImageCategory category, int limit, Exception e) {
    log.error(
        "Falha ao buscar imagens da categoria {} após todas as tentativas: {}",
        category,
        e.getMessage());
    return Collections.emptyList();
  }
}
