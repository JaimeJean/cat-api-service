package com.github.jaimejean.catapi.adapters.outbound.catapi.dtos;

public record BreedApiResponse(
    String id, String name, String origin, String temperament, String description) {}
