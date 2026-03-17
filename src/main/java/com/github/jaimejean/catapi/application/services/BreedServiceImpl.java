package com.github.jaimejean.catapi.application.services;

import com.github.jaimejean.catapi.domain.entities.Breed;
import com.github.jaimejean.catapi.domain.exceptions.BreedNotFoundException;
import com.github.jaimejean.catapi.domain.ports.in.BreedService;
import com.github.jaimejean.catapi.domain.ports.out.BreedRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class BreedServiceImpl implements BreedService {

  private final BreedRepository breedRepository;

  @Override
  public Page<Breed> search(String temperament, String origin, Pageable pageable) {
    log.info(
        "Searching breeds - temperament: {}, origin: {}, page: {}, size: {}",
        temperament,
        origin,
        pageable.getPageNumber(),
        pageable.getPageSize());

    Page<Breed> page = breedRepository.search(temperament, origin, pageable);

    page.getContent().forEach(breed -> Hibernate.initialize(breed.getImages()));

    return page;
  }

  @Override
  public Breed getById(Long id) {
    log.info("Fetching breed by id: {}", id);
    Breed breed =
        breedRepository
            .findById(id)
            .orElseThrow(
                () -> {
                  log.warn("Breed not found with id: {}", id);
                  return new BreedNotFoundException(id);
                });

    Hibernate.initialize(breed.getImages());

    return breed;
  }
}
