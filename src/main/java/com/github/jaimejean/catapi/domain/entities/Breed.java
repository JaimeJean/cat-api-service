package com.github.jaimejean.catapi.domain.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "breeds")
@Getter
@NoArgsConstructor
public class Breed {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "external_id", nullable = false, unique = true, length = 10)
  private String externalId;

  @Setter
  @Column(nullable = false, length = 100)
  private String name;

  @Setter
  @Column(length = 100)
  private String origin;

  @Setter
  @Column(length = 255)
  private String temperament;

  @Setter
  @Column(columnDefinition = "TEXT")
  private String description;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @OneToMany(mappedBy = "breed")
  private List<Image> images = new ArrayList<>();

  public Breed(
      String externalId, String name, String origin, String temperament, String description) {
    this.externalId = externalId;
    this.name = name;
    this.origin = origin;
    this.temperament = temperament;
    this.description = description;
  }
}
