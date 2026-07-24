package com.fixledger.modules.exporter.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Validated
@Component
@ConfigurationProperties(prefix = "fixledger.export")
public class ExportProperties {

  @Min(1)
  @Max(100000)
  private int maxSyncRows = 5000;

  public int getMaxSyncRows() {
    return maxSyncRows;
  }

  public void setMaxSyncRows(int maxSyncRows) {
    this.maxSyncRows = maxSyncRows;
  }
}
