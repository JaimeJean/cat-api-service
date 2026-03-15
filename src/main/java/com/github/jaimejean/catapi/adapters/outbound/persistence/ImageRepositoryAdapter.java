package com.github.jaimejean.catapi.adapters.outbound.persistence;

import com.github.jaimejean.catapi.domain.entities.Image;
import com.github.jaimejean.catapi.domain.enums.ImageCategory;
import com.github.jaimejean.catapi.domain.ports.out.ImageRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ImageRepositoryAdapter implements ImageRepository {

  private final ImageJpaRepository jpa;

  @Override
  @Transactional
  public Image save(Image image) {
    Long breedId = image.getBreed() != null ? image.getBreed().getId() : null;

    jpa.upsert(image.getExternalId(), image.getUrl(), breedId, image.getCategory().name());
    return jpa.findByExternalId(image.getExternalId()).orElseThrow();
  }

  @Override
  @Transactional
  public List<Image> saveAll(List<Image> images) {
    return images.stream().map(this::save).toList();
  }

  @Override
  public Optional<Image> findByExternalId(String externalId) {
    return jpa.findByExternalId(externalId);
  }

  @Override
  public List<Image> findByBreedId(Long breedId) {
    return jpa.findByBreedId(breedId);
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
