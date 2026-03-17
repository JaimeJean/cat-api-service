package com.github.jaimejean.catapi.application.services;

import com.github.jaimejean.catapi.domain.entities.Image;
import com.github.jaimejean.catapi.domain.enums.ImageCategory;
import com.github.jaimejean.catapi.domain.ports.in.ImageService;
import com.github.jaimejean.catapi.domain.ports.out.ImageRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ImageServiceImpl implements ImageService {

  private final ImageRepository imageRepository;

  @Override
  public List<Image> findByCategory(ImageCategory category) {
    log.info("Fetching images by category: {}", category);
    if (category == null) {
      return imageRepository.findAll();
    }
    return imageRepository.findByCategory(category);
  }
}
