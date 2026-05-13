package com.fixledger.modules.warranty.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fixledger.common.entity.BaseEntity;
import java.time.LocalDate;

/**
 * <p>
 * 文件功能说明：保修持久化实体，对应数据库中的业务表结构。
 * </p>
 *
 * @Author FixLedger
 */
@TableName("fl_warranty_record")
public class WarrantyRecordEntity extends BaseEntity {

  @TableId(type = IdType.AUTO)
  private Long id;

  private Long familyId;

  private Long deviceId;

  private String warrantyType;

  private LocalDate startDate;

  private LocalDate endDate;

  private Integer remindDaysBefore;

  private String servicePhone;

  private String serviceAddress;

  private String serviceNote;

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

  public String getWarrantyType() {
    return warrantyType;
  }

  public void setWarrantyType(String warrantyType) {
    this.warrantyType = warrantyType;
  }

  public LocalDate getStartDate() {
    return startDate;
  }

  public void setStartDate(LocalDate startDate) {
    this.startDate = startDate;
  }

  public LocalDate getEndDate() {
    return endDate;
  }

  public void setEndDate(LocalDate endDate) {
    this.endDate = endDate;
  }

  public Integer getRemindDaysBefore() {
    return remindDaysBefore;
  }

  public void setRemindDaysBefore(Integer remindDaysBefore) {
    this.remindDaysBefore = remindDaysBefore;
  }

  public String getServicePhone() {
    return servicePhone;
  }

  public void setServicePhone(String servicePhone) {
    this.servicePhone = servicePhone;
  }

  public String getServiceAddress() {
    return serviceAddress;
  }

  public void setServiceAddress(String serviceAddress) {
    this.serviceAddress = serviceAddress;
  }

  public String getServiceNote() {
    return serviceNote;
  }

  public void setServiceNote(String serviceNote) {
    this.serviceNote = serviceNote;
  }
}
