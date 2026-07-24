package com.fixledger.modules.dashboard.config;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Validated
@Component
@ConfigurationProperties(prefix = "fixledger.dashboard")
public class DashboardProperties {

  private boolean summaryCacheEnabled = true;

  @NotNull
  private Duration summaryCacheTtl = Duration.ofMinutes(2);

  public boolean isSummaryCacheEnabled() {
    return summaryCacheEnabled;
  }

  public void setSummaryCacheEnabled(boolean summaryCacheEnabled) {
    this.summaryCacheEnabled = summaryCacheEnabled;
  }

  public Duration getSummaryCacheTtl() {
    return summaryCacheTtl;
  }

  public void setSummaryCacheTtl(Duration summaryCacheTtl) {
    this.summaryCacheTtl = summaryCacheTtl;
  }

  @AssertTrue(message = "Dashboard summary cache TTL must be positive")
  public boolean isSummaryCacheTtlValid() {
    return summaryCacheTtl != null
        && !summaryCacheTtl.isZero()
        && !summaryCacheTtl.isNegative();
  }
}
