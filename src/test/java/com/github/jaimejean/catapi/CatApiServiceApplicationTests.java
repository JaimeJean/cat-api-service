package com.github.jaimejean.catapi;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Disabled("Requer banco de dados — coberto pelos testes E2E")
class CatApiServiceApplicationTests {

  @Test
  void contextLoads() {}
}
