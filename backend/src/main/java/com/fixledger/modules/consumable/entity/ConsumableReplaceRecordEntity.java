package com.fixledger.modules.consumable.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fixledger.common.entity.BaseEntity;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * <p>
 * 文件功能说明：耗材持久化实体，对应数据库中的业务表结构。
 * </p>
 *
 * @Author FixLedger
 */
@TableName("fl_consumable_replace_record")
public class ConsumableReplaceRecordEntity extends BaseEntity {

  @TableId(type = IdType.AUTO)
  private Long id;

  private Long familyId;

  private Long consumableId;

  private Long deviceId;

  private LocalDate replacedDate;

  private BigDecimal cost;

  private String note;

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

  public Long getConsumableId() {
    return consumableId;
  }

  public void setConsumableId(Long consumableId) {
    this.consumableId = consumableId;
  }

  public Long getDeviceId() {
    return deviceId;
  }

  public void setDeviceId(Long deviceId) {
    this.deviceId = deviceId;
  }

  public LocalDate getReplacedDate() {
    return replacedDate;
  }

  public void setReplacedDate(LocalDate replacedDate) {
    this.replacedDate = replacedDate;
  }

  public BigDecimal getCost() {
    return cost;
  }

  public void setCost(BigDecimal cost) {
    this.cost = cost;
  }

  public String getNote() {
    return note;
  }

  public void setNote(String note) {
    this.note = note;
  }
}
