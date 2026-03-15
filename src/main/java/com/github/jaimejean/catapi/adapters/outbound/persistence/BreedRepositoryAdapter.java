package com.github.jaimejean.catapi.adapters.outbound.persistence;

import com.github.jaimejean.catapi.domain.entities.Breed;
import com.github.jaimejean.catapi.domain.ports.out.BreedRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class BreedRepositoryAdapter implements BreedRepository {

  private final BreedJpaRepository jpa;

  @Override
  @Transactional
  public Breed save(Breed breed) {
    jpa.upsert(breed);
    return jpa.findByExternalId(breed.getExternalId()).orElseThrow();
  }

  @Override
  @Transactional
  public List<Breed> saveAll(List<Breed> breeds) {
    return breeds.stream().map(this::save).toList();
  }

  @Override
  public Optional<Breed> findByExternalId(String externalId) {
    return jpa.findByExternalId(externalId);
  }

  @Override
  public Optional<Breed> findById(Long id) {
    return jpa.findById(id);
  }

  @Override
  public Page<Breed> findAll(Pageable pageable) {
    return jpa.findAll(pageable);
  }

  @Override
  public Page<Breed> findByTemperamentContainingIgnoreCase(String temperament, Pageable pageable) {
    return jpa.findByTemperamentContainingIgnoreCase(temperament, pageable);
  }

  @Override
  public Page<Breed> findByOriginIgnoreCase(String origin, Pageable pageable) {
    return jpa.findByOriginIgnoreCase(origin, pageable);
  }
}
