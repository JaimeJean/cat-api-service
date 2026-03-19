package com.github.jaimejean.catapi.adapters.inbound.loader;

import com.github.jaimejean.catapi.domain.ports.in.DataIngestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataLoader implements ApplicationRunner {

  private final DataIngestionService dataIngestionService;

  @Override
  public void run(ApplicationArguments args) {
    log.info("Iniciando Data Loader");
    dataIngestionService.ingest();
    log.info("Data Loader finalizado");
  }
}
