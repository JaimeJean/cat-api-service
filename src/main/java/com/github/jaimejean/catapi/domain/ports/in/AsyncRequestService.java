package com.github.jaimejean.catapi.domain.ports.in;

import com.github.jaimejean.catapi.domain.model.AsyncBreedRequest;

public interface AsyncRequestService {

  String submit(AsyncBreedRequest request);

  void process(AsyncBreedRequest request);
}
