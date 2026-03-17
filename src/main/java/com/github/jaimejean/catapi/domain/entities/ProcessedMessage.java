package com.github.jaimejean.catapi.domain.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
@Entity
@Table(name = "processed_messages")
public class ProcessedMessage {

  @Id
  @Column(name = "message_id", nullable = false, updatable = false)
  private String messageId;

  @Column(name = "processed_at", nullable = false, updatable = false)
  private LocalDateTime processedAt;

  protected ProcessedMessage() {}

  public ProcessedMessage(String messageId) {
    this.messageId = messageId;
    this.processedAt = LocalDateTime.now();
  }
}
