package com.github.jaimejean.catapi.domain.ports.out;

import com.github.jaimejean.catapi.domain.entities.Breed;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BreedRepository {

  Breed save(Breed breed);

  List<Breed> saveAll(List<Breed> breeds);

  Optional<Breed> findByExternalId(String externalId);

  Optional<Breed> findById(Long id);

  Page<Breed> findAll(Pageable pageable);

  Page<Breed> findByTemperamentContainingIgnoreCase(String temperament, Pageable pageable);

  Page<Breed> findByOriginIgnoreCase(String origin, Pageable pageable);
}
