package com.fixledger.modules.family.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fixledger.common.entity.BaseEntity;

/**
 * <p>
 * 文件功能说明：家庭空间持久化实体，对应数据库中的业务表结构。
 * </p>
 *
 * @Author FixLedger
 */
@TableName("fl_family_space")
public class FamilySpaceEntity extends BaseEntity {

  @TableId(type = IdType.AUTO)
  private Long id;

  private String name;

  private String description;

  private Long ownerUserId;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Long getOwnerUserId() {
    return ownerUserId;
  }

  public void setOwnerUserId(Long ownerUserId) {
    this.ownerUserId = ownerUserId;
  }
}
