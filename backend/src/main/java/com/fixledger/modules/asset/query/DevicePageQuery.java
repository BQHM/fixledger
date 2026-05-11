package com.fixledger.modules.asset.query;

import com.fixledger.common.page.PageQuery;
import jakarta.validation.constraints.Size;

public class DevicePageQuery extends PageQuery {

  @Size(max = 128, message = "关键词最多 128 个字符")
  private String keyword;

  private Long categoryId;

  private String status;

  @Size(max = 128, message = "品牌最多 128 个字符")
  private String brand;

  public String getKeyword() {
    return keyword;
  }

  public void setKeyword(String keyword) {
    this.keyword = keyword;
  }

  public Long getCategoryId() {
    return categoryId;
  }

  public void setCategoryId(Long categoryId) {
    this.categoryId = categoryId;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getBrand() {
    return brand;
  }

  public void setBrand(String brand) {
    this.brand = brand;
  }
}
