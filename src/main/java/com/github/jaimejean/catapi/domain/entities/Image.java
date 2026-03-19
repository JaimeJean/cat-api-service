package com.github.jaimejean.catapi.domain.entities;

import com.github.jaimejean.catapi.domain.enums.ImageCategory;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "images")
@Getter
@NoArgsConstructor
public class Image {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "external_id", nullable = false, unique = true, length = 20)
  private String externalId;

  @Column(nullable = false, length = 500)
  private String url;

  @ManyToOne
  @JoinColumn(name = "breed_id")
  private Breed breed;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private ImageCategory category;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  public Image(String externalId, String url, Breed breed, ImageCategory category) {
    this.externalId = externalId;
    this.url = url;
    this.breed = breed;
    this.category = category;
  }
}
