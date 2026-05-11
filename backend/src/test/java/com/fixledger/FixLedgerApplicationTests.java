package com.fixledger;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
class FixLedgerApplicationTests {

  @Test
  @DisplayName("应用上下文可以正常加载")
  void contextLoads() {
  }
}
