package com.github.jaimejean.catapi.domain.ports.in;

import com.github.jaimejean.catapi.domain.entities.Breed;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BreedService {

  Page<Breed> search(String temperament, String origin, Pageable pageable);

  Breed getById(Long id);
}
