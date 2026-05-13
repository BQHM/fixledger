package com.fixledger;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * <p>
 * 文件功能说明：FixLedger 后端启动入口，负责启动 Spring Boot 应用并开启定时任务能力。
 * </p>
 *
 * @Author FixLedger
 */
@EnableScheduling
@ConfigurationPropertiesScan
@SpringBootApplication
public class FixLedgerApplication {
  /**
   * @Author FixLedger
   * <p>
   * 功能说明：启动应用。
   * </p>
   * @param args 启动参数
   */
  public static void main(String[] args) {
    SpringApplication.run(FixLedgerApplication.class, args);
  }
}
