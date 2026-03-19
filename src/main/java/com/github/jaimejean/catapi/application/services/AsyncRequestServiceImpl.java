package com.github.jaimejean.catapi.application.services;

import com.github.jaimejean.catapi.domain.entities.Breed;
import com.github.jaimejean.catapi.domain.model.AsyncBreedRequest;
import com.github.jaimejean.catapi.domain.ports.in.AsyncRequestService;
import com.github.jaimejean.catapi.domain.ports.out.AsyncRequestPublisher;
import com.github.jaimejean.catapi.domain.ports.out.BreedRepository;
import com.github.jaimejean.catapi.domain.ports.out.NotificationPublisher;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AsyncRequestServiceImpl implements AsyncRequestService {

  private final AsyncRequestPublisher asyncRequestPublisher;
  private final NotificationPublisher notificationPublisher;
  private final BreedRepository breedRepository;

  @Override
  public String submit(AsyncBreedRequest request) {
    log.info(
        "Async submit: requestId={}, temperament='{}' (len={}), origin='{}' (len={})",
        request.getRequestId(),
        request.getTemperament(),
        request.getTemperament() == null ? null : request.getTemperament().length(),
        request.getOrigin(),
        request.getOrigin() == null ? null : request.getOrigin().length());
    log.info("Submitting async request: requestId={}", request.getRequestId());
    asyncRequestPublisher.publish(request);
    log.info("Async request published successfully: requestId={}", request.getRequestId());
    return request.getRequestId();
  }

  @Override
  public void process(AsyncBreedRequest request) {
    log.info(
        "Async search params: requestId={}, temperament='{}' (len={}), origin='{}' (len={})",
        request.getRequestId(),
        request.getTemperament(),
        request.getTemperament() == null ? null : request.getTemperament().length(),
        request.getOrigin(),
        request.getOrigin() == null ? null : request.getOrigin().length());

    List<Breed> breeds =
        breedRepository
            .search(request.getTemperament(), request.getOrigin(), Pageable.unpaged())
            .getContent();

    log.info(
        "Query executed: requestId={}, resultsFound={}", request.getRequestId(), breeds.size());

    notificationPublisher.notify(request, breeds);
    log.info("Notification sent: requestId={}", request.getRequestId());
  }
}
