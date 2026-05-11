package com.fixledger.modules.auth.service;

import com.fixledger.modules.auth.request.LoginRequest;
import com.fixledger.modules.auth.request.RegisterRequest;
import com.fixledger.modules.auth.response.LoginResponse;
import com.fixledger.modules.auth.response.RegisterResponse;
import com.fixledger.modules.auth.response.UserProfileResponse;

/**
 * 认证门面，负责账号注册、登录、退出和当前用户资料读取。
 */
public interface AuthService {

  /**
   * 注册新用户，并同步创建默认家庭空间。
   *
   * @param request 注册请求
   * @return 新用户基础信息
   */
  RegisterResponse register(RegisterRequest request);

  /**
   * 校验账号密码并签发访问令牌。
   *
   * @param request 登录请求，账号可以是用户名或邮箱
   * @return 访问令牌、用户资料和当前家庭空间
   */
  LoginResponse login(LoginRequest request);

  /**
   * 退出当前登录会话，后续接入 JWT 黑名单时在此扩展。
   *
   * @return 是否处理成功
   */
  boolean logout();

  /**
   * 根据安全上下文读取当前登录用户资料。
   *
   * @return 当前用户资料
   */
  UserProfileResponse getCurrentUser();
}