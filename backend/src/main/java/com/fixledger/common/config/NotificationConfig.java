package com.fixledger.common.config;

import com.fixledger.infrastructure.notification.NotificationProperties;
import java.net.http.HttpClient;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NotificationConfig {

  @Bean
  public Clock notificationClock() {
    return Clock.systemDefaultZone();
  }

  @Bean
  public HttpClient notificationHttpClient(NotificationProperties properties) {
    return HttpClient.newBuilder()
        .connectTimeout(properties.getConnectTimeout())
        .followRedirects(HttpClient.Redirect.NEVER)
        .build();
  }
}
