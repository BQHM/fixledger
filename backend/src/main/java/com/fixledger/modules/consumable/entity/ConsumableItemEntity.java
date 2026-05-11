package com.fixledger.modules.consumable.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fixledger.common.entity.BaseEntity;
import java.time.LocalDate;

@TableName("fl_consumable_item")
public class ConsumableItemEntity extends BaseEntity {

  @TableId(type = IdType.AUTO)
  private Long id;

  private Long familyId;

  private Long deviceId;

  private String name;

  private String brand;

  private String model;

  private Integer cycleDays;

  private LocalDate lastReplacedDate;

  private LocalDate nextRemindDate;

  private Integer remindDaysBefore;

  private String status;

  private Boolean enabled;

  private String remark;

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

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getBrand() {
    return brand;
  }

  public void setBrand(String brand) {
    this.brand = brand;
  }

  public String getModel() {
    return model;
  }

  public void setModel(String model) {
    this.model = model;
  }

  public Integer getCycleDays() {
    return cycleDays;
  }

  public void setCycleDays(Integer cycleDays) {
    this.cycleDays = cycleDays;
  }

  public LocalDate getLastReplacedDate() {
    return lastReplacedDate;
  }

  public void setLastReplacedDate(LocalDate lastReplacedDate) {
    this.lastReplacedDate = lastReplacedDate;
  }

  public LocalDate getNextRemindDate() {
    return nextRemindDate;
  }

  public void setNextRemindDate(LocalDate nextRemindDate) {
    this.nextRemindDate = nextRemindDate;
  }

  public Integer getRemindDaysBefore() {
    return remindDaysBefore;
  }

  public void setRemindDaysBefore(Integer remindDaysBefore) {
    this.remindDaysBefore = remindDaysBefore;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public Boolean getEnabled() {
    return enabled;
  }

  public void setEnabled(Boolean enabled) {
    this.enabled = enabled;
  }

  public String getRemark() {
    return remark;
  }

  public void setRemark(String remark) {
    this.remark = remark;
  }
}
