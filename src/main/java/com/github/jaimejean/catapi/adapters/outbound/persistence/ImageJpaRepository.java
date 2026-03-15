package com.github.jaimejean.catapi.adapters.outbound.persistence;

import com.github.jaimejean.catapi.domain.entities.Image;
import com.github.jaimejean.catapi.domain.enums.ImageCategory;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ImageJpaRepository extends JpaRepository<Image, Long> {

  Optional<Image> findByExternalId(String externalId);

  List<Image> findByBreedId(Long breedId);

  List<Image> findByCategory(ImageCategory category);

  boolean existsByBreedIdAndCategory(Long breedId, ImageCategory category);

  boolean existsByCategoryAndBreedIdIsNull(ImageCategory category);

  @Modifying
  @Query(
      value =
          "INSERT INTO images (external_id, url, breed_id, category, created_at) "
              + "VALUES (:externalId, :url, :breedId, :category, now()) "
              + "ON CONFLICT (external_id) DO UPDATE SET "
              + "url = EXCLUDED.url, breed_id = EXCLUDED.breed_id, category = EXCLUDED.category",
      nativeQuery = true)
  void upsert(
      @Param("externalId") String externalId,
      @Param("url") String url,
      @Param("breedId") Long breedId,
      @Param("category") String category);
}
