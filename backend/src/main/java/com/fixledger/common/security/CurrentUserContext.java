package com.fixledger.common.security;

import com.fixledger.common.exception.BusinessException;
import com.fixledger.common.exception.ErrorCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class CurrentUserContext {

  private CurrentUserContext() {
  }

  public static Long getUserId() {
    return getCurrentUser().id();
  }

  public static CurrentUser getCurrentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null) {
      throw new BusinessException(ErrorCode.UNAUTHORIZED, "请先登录");
    }
    if (!(authentication.getPrincipal() instanceof CurrentUser currentUser)) {
      throw new BusinessException(ErrorCode.UNAUTHORIZED, "请先登录");
    }
    return currentUser;
  }
}
