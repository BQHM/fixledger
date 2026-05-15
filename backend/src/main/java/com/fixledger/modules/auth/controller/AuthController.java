package com.fixledger.modules.auth.controller;

import com.fixledger.common.result.Result;
import com.fixledger.common.security.JwtAuthenticationFilter;
import com.fixledger.modules.auth.request.LoginRequest;
import com.fixledger.modules.auth.request.RegisterRequest;
import com.fixledger.modules.auth.response.LoginResponse;
import com.fixledger.modules.auth.response.RegisterResponse;
import com.fixledger.modules.auth.response.UserProfileResponse;
import com.fixledger.modules.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 文件功能说明：认证接口控制器，负责参数校验、认证上下文读取和调用业务服务。
 * </p>
 *
 * @Author FixLedger
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：处理认证注册用户接口请求。
   * </p>
   * @param request 请求参数
   * @return 统一响应结果
   */
  @PostMapping("/register")
  public Result<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
    return Result.success(authService.register(request));
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：处理认证登录认证接口请求。
   * </p>
   * @param request 请求参数
   * @return 统一响应结果
   */
  @PostMapping("/login")
  public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
    return Result.success(authService.login(request));
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：处理认证退出登录接口请求。
   * </p>
   * @return 统一响应结果
   */
  @PostMapping("/logout")
  public Result<Boolean> logout(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
    return Result.success(authService.logout(resolveToken(authorization)));
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：处理认证执行业务处理接口请求。
   * </p>
   * @return 统一响应结果
   */
  @GetMapping("/me")
  public Result<UserProfileResponse> me() {
    return Result.success(authService.getCurrentUser());
  }

  private String resolveToken(String authorization) {
    if (!StringUtils.hasText(authorization)
        || !authorization.startsWith(JwtAuthenticationFilter.BEARER_PREFIX)) {
      return null;
    }
    return authorization.substring(JwtAuthenticationFilter.BEARER_PREFIX.length());
  }
}
