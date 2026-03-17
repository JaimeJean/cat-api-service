package com.github.jaimejean.catapi.adapters.outbound.persistence;

import com.github.jaimejean.catapi.domain.entities.ProcessedMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedMessageJpaRepository extends JpaRepository<ProcessedMessage, String> {}
