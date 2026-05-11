package com.fixledger;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@ConfigurationPropertiesScan
@SpringBootApplication
public class FixLedgerApplication {

  public static void main(String[] args) {
    SpringApplication.run(FixLedgerApplication.class, args);
  }
}

