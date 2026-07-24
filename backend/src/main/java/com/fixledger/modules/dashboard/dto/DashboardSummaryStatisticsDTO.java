package com.fixledger.modules.dashboard.dto;

import java.math.BigDecimal;

public class DashboardSummaryStatisticsDTO {

  private Long deviceTotal;
  private Long warrantyExpiringCount;
  private Long warrantyExpiredCount;
  private Long consumableDueSoonCount;
  private Long consumableOverdueCount;
  private Long repairingCount;
  private BigDecimal monthlyMaintenanceCost;

  public Long getDeviceTotal() {
    return deviceTotal;
  }

  public void setDeviceTotal(Long deviceTotal) {
    this.deviceTotal = deviceTotal;
  }

  public Long getWarrantyExpiringCount() {
    return warrantyExpiringCount;
  }

  public void setWarrantyExpiringCount(Long warrantyExpiringCount) {
    this.warrantyExpiringCount = warrantyExpiringCount;
  }

  public Long getWarrantyExpiredCount() {
    return warrantyExpiredCount;
  }

  public void setWarrantyExpiredCount(Long warrantyExpiredCount) {
    this.warrantyExpiredCount = warrantyExpiredCount;
  }

  public Long getConsumableDueSoonCount() {
    return consumableDueSoonCount;
  }

  public void setConsumableDueSoonCount(Long consumableDueSoonCount) {
    this.consumableDueSoonCount = consumableDueSoonCount;
  }

  public Long getConsumableOverdueCount() {
    return consumableOverdueCount;
  }

  public void setConsumableOverdueCount(Long consumableOverdueCount) {
    this.consumableOverdueCount = consumableOverdueCount;
  }

  public Long getRepairingCount() {
    return repairingCount;
  }

  public void setRepairingCount(Long repairingCount) {
    this.repairingCount = repairingCount;
  }

  public BigDecimal getMonthlyMaintenanceCost() {
    return monthlyMaintenanceCost;
  }

  public void setMonthlyMaintenanceCost(BigDecimal monthlyMaintenanceCost) {
    this.monthlyMaintenanceCost = monthlyMaintenanceCost;
  }
}
