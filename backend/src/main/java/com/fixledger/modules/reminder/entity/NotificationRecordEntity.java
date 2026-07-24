package com.fixledger.modules.reminder.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fixledger.common.entity.BaseEntity;
import java.time.LocalDateTime;

/**
 * <p>
 * 文件功能说明：提醒通知持久化实体，对应数据库中的业务表结构。
 * </p>
 *
 * @Author FixLedger
 */
@TableName("fl_notification_record")
public class NotificationRecordEntity extends BaseEntity {

  @TableId(type = IdType.AUTO)
  private Long id;

  private Long familyId;

  private Long userId;

  private Long reminderId;

  private String channel;

  private String title;

  private String content;

  private String recipient;

  private String status;

  private String errorMessage;

  private Integer attemptCount;

  private LocalDateTime nextRetryAt;

  private LocalDateTime lastAttemptAt;

  private LocalDateTime sentAt;

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

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public Long getReminderId() {
    return reminderId;
  }

  public void setReminderId(Long reminderId) {
    this.reminderId = reminderId;
  }

  public String getChannel() {
    return channel;
  }

  public void setChannel(String channel) {
    this.channel = channel;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public String getRecipient() {
    return recipient;
  }

  public void setRecipient(String recipient) {
    this.recipient = recipient;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }

  public Integer getAttemptCount() {
    return attemptCount;
  }

  public void setAttemptCount(Integer attemptCount) {
    this.attemptCount = attemptCount;
  }

  public LocalDateTime getNextRetryAt() {
    return nextRetryAt;
  }

  public void setNextRetryAt(LocalDateTime nextRetryAt) {
    this.nextRetryAt = nextRetryAt;
  }

  public LocalDateTime getLastAttemptAt() {
    return lastAttemptAt;
  }

  public void setLastAttemptAt(LocalDateTime lastAttemptAt) {
    this.lastAttemptAt = lastAttemptAt;
  }

  public LocalDateTime getSentAt() {
    return sentAt;
  }

  public void setSentAt(LocalDateTime sentAt) {
    this.sentAt = sentAt;
  }
}
