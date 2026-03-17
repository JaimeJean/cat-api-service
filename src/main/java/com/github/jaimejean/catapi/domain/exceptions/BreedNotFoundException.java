package com.github.jaimejean.catapi.domain.exceptions;

public class BreedNotFoundException extends RuntimeException {

  public BreedNotFoundException(Long id) {
    super("Breed not found with id: " + id);
  }
}
