package com.github.jaimejean.catapi.domain.dtos;

public record BreedApiResponse(
    String id, String name, String origin, String temperament, String description) {}
