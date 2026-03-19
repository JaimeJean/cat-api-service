package com.github.jaimejean.catapi.domain.ports.in;

import com.github.jaimejean.catapi.domain.entities.Image;
import com.github.jaimejean.catapi.domain.enums.ImageCategory;
import java.util.List;

public interface ImageService {

  List<Image> findByCategory(ImageCategory category);
}
