package com.github.jaimejean.catapi.domain.ports.out;

import com.github.jaimejean.catapi.domain.entities.Breed;
import com.github.jaimejean.catapi.domain.model.AsyncBreedRequest;
import java.util.List;

public interface NotificationPublisher {

  void notify(AsyncBreedRequest request, List<Breed> breeds);
}
