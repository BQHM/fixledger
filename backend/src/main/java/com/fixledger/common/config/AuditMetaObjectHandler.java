package com.fixledger.common.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.fixledger.common.security.CurrentUser;
import java.time.LocalDateTime;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * <p>
 * 文件功能说明：基础配置组件，为各业务模块提供可复用能力。
 * </p>
 *
 * @Author FixLedger
 */
@Component
public class AuditMetaObjectHandler implements MetaObjectHandler {
  /**
   * @Author FixLedger
   * <p>
   * 功能说明：完成填充新增审计字段配置。
   * </p>
   * @param metaObject MyBatis 元对象
   */
  @Override
  public void insertFill(MetaObject metaObject) {
    LocalDateTime now = LocalDateTime.now();
    strictInsertFill(metaObject, "createdAt", LocalDateTime.class, now);
    strictInsertFill(metaObject, "updatedAt", LocalDateTime.class, now);
    currentUserId().ifPresent(userId -> {
      strictInsertFill(metaObject, "createdBy", Long.class, userId);
      strictInsertFill(metaObject, "updatedBy", Long.class, userId);
    });
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：完成更新配置。
   * </p>
   * @param metaObject MyBatis 元对象
   */
  @Override
  public void updateFill(MetaObject metaObject) {
    strictUpdateFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
    currentUserId().ifPresent(userId ->
        strictUpdateFill(metaObject, "updatedBy", Long.class, userId));
  }

  private java.util.Optional<Long> currentUserId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null) {
      return java.util.Optional.empty();
    }
    if (!(authentication.getPrincipal() instanceof CurrentUser currentUser)) {
      return java.util.Optional.empty();
    }
    return java.util.Optional.of(currentUser.id());
  }
}
