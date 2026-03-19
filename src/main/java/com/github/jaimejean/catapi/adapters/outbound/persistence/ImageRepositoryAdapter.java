package com.github.jaimejean.catapi.adapters.outbound.persistence;

import com.github.jaimejean.catapi.domain.entities.Image;
import com.github.jaimejean.catapi.domain.enums.ImageCategory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class ImageRepositoryAdapter
    implements com.github.jaimejean.catapi.domain.ports.out.ImageRepository {

  private final ImageJpaRepository jpa;

  @Override
  @Transactional
  public Image save(Image image) {
    Long breedId = image.getBreed() != null ? image.getBreed().getId() : null;

    jpa.upsert(image.getExternalId(), image.getUrl(), breedId, image.getCategory().name());
    return jpa.findByExternalId(image.getExternalId())
        .orElseThrow(
            () -> {
              log.error(
                  "Image não encontrada após upsert: externalId={}, breedId={}, category={}",
                  image.getExternalId(),
                  breedId,
                  image.getCategory());
              return new IllegalStateException(
                  "Falha ao recuperar image após upsert: externalId=" + image.getExternalId());
            });
  }

  @Override
  public List<Image> findAll() {
    return jpa.findAll();
  }

  @Override
  @Transactional
  public List<Image> saveAll(List<Image> images) {
    return images.stream().map(this::save).toList();
  }

  @Override
  public List<Image> findByCategory(ImageCategory category) {
    return jpa.findByCategory(category);
  }

  @Override
  public boolean existsByBreedIdAndCategory(Long breedId, ImageCategory category) {
    return jpa.existsByBreedIdAndCategory(breedId, category);
  }

  @Override
  public boolean existsByCategoryAndBreedIdIsNull(ImageCategory category) {
    return jpa.existsByCategoryAndBreedIdIsNull(category);
  }
}
