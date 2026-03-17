package com.github.jaimejean.catapi.adapters.outbound.persistence;

import com.github.jaimejean.catapi.domain.entities.Breed;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BreedJpaRepository extends JpaRepository<Breed, Long> {

  Optional<Breed> findByExternalId(String externalId);

  @Query(
      "SELECT b FROM Breed b WHERE "
          + "(CAST(:temperament AS string) IS NULL OR LOWER(b.temperament) LIKE LOWER(CONCAT('%', CAST(:temperament AS string), '%'))) AND "
          + "(CAST(:origin AS string) IS NULL OR LOWER(b.origin) = LOWER(CAST(:origin AS string)))")
  Page<Breed> search(
      @Param("temperament") String temperament, @Param("origin") String origin, Pageable pageable);

  @Modifying
  @Query(
      value =
          "INSERT INTO breeds (external_id, name, origin, temperament, description, created_at, updated_at) "
              + "VALUES (:#{#b.externalId}, :#{#b.name}, :#{#b.origin}, :#{#b.temperament}, :#{#b.description}, now(), now()) "
              + "ON CONFLICT (external_id) DO UPDATE SET "
              + "name = EXCLUDED.name, origin = EXCLUDED.origin, temperament = EXCLUDED.temperament, "
              + "description = EXCLUDED.description, updated_at = now()",
      nativeQuery = true)
  void upsert(@Param("b") Breed breed);
}
