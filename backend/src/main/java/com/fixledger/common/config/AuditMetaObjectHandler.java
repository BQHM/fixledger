package com.fixledger.common.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.fixledger.common.security.CurrentUser;
import java.time.LocalDateTime;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuditMetaObjectHandler implements MetaObjectHandler {

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

