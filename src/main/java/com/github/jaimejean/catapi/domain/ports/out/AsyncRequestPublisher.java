package com.github.jaimejean.catapi.domain.ports.out;

import com.github.jaimejean.catapi.domain.model.AsyncBreedRequest;

public interface AsyncRequestPublisher {

  void publish(AsyncBreedRequest request);
}
