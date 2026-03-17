package com.github.jaimejean.catapi.adapters.outbound.persistence;

import com.github.jaimejean.catapi.domain.entities.Breed;
import com.github.jaimejean.catapi.domain.ports.out.BreedRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class BreedRepositoryAdapter implements BreedRepository {

  private final BreedJpaRepository jpa;

  @Override
  @Transactional
  public Breed save(Breed breed) {
    jpa.upsert(breed);
    return jpa.findByExternalId(breed.getExternalId())
        .orElseThrow(
            () -> {
              log.error("Breed não encontrada após upsert: externalId={}", breed.getExternalId());
              return new IllegalStateException(
                  "Falha ao recuperar breed após upsert: externalId=" + breed.getExternalId());
            });
  }

  @Override
  @Transactional
  public List<Breed> saveAll(List<Breed> breeds) {
    return breeds.stream().map(this::save).toList();
  }

  @Override
  public Optional<Breed> findById(Long id) {
    return jpa.findById(id);
  }

  @Override
  public Page<Breed> search(String temperament, String origin, Pageable pageable) {
    return jpa.search(temperament, origin, pageable);
  }

  @Override
  public List<Breed> findAll() {
    return jpa.findAll();
  }

  @Override
  public List<Breed> findByTemperamentContainingIgnoreCase(String temperament) {
    return jpa.search(temperament, null, Pageable.unpaged()).getContent();
  }

  @Override
  public List<Breed> findByOriginIgnoreCase(String origin) {
    return jpa.search(null, origin, Pageable.unpaged()).getContent();
  }
}
