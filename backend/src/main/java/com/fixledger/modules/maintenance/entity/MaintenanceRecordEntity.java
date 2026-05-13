package com.fixledger.modules.maintenance.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fixledger.common.entity.BaseEntity;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 文件功能说明：维修持久化实体，对应数据库中的业务表结构。
 * </p>
 *
 * @Author FixLedger
 */
@TableName("fl_maintenance_record")
public class MaintenanceRecordEntity extends BaseEntity {

  @TableId(type = IdType.AUTO)
  private Long id;

  private Long familyId;

  private Long deviceId;

  private String title;

  private String faultDescription;

  private LocalDateTime occurredAt;

  private String status;

  private String repairChannel;

  private String repairContact;

  private BigDecimal repairCost;

  private String resultDescription;

  private LocalDateTime completedAt;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getFamilyId() {
    return familyId;
  }

  public void setFamilyId(Long familyId) {
    this.familyId = familyId;
  }

  public Long getDeviceId() {
    return deviceId;
  }

  public void setDeviceId(Long deviceId) {
    this.deviceId = deviceId;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getFaultDescription() {
    return faultDescription;
  }

  public void setFaultDescription(String faultDescription) {
    this.faultDescription = faultDescription;
  }

  public LocalDateTime getOccurredAt() {
    return occurredAt;
  }

  public void setOccurredAt(LocalDateTime occurredAt) {
    this.occurredAt = occurredAt;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getRepairChannel() {
    return repairChannel;
  }

  public void setRepairChannel(String repairChannel) {
    this.repairChannel = repairChannel;
  }

  public String getRepairContact() {
    return repairContact;
  }

  public void setRepairContact(String repairContact) {
    this.repairContact = repairContact;
  }

  public BigDecimal getRepairCost() {
    return repairCost;
  }

  public void setRepairCost(BigDecimal repairCost) {
    this.repairCost = repairCost;
  }

  public String getResultDescription() {
    return resultDescription;
  }

  public void setResultDescription(String resultDescription) {
    this.resultDescription = resultDescription;
  }

  public LocalDateTime getCompletedAt() {
    return completedAt;
  }

  public void setCompletedAt(LocalDateTime completedAt) {
    this.completedAt = completedAt;
  }
}
