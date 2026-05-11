package com.fixledger.modules.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fixledger.common.exception.BusinessException;
import com.fixledger.common.exception.ErrorCode;
import com.fixledger.common.security.CurrentUserContext;
import com.fixledger.common.security.JwtTokenProvider;
import com.fixledger.modules.auth.request.LoginRequest;
import com.fixledger.modules.auth.request.RegisterRequest;
import com.fixledger.modules.auth.response.LoginResponse;
import com.fixledger.modules.auth.response.RegisterResponse;
import com.fixledger.modules.auth.response.UserProfileResponse;
import com.fixledger.modules.family.service.FamilyService;
import com.fixledger.modules.user.entity.UserEntity;
import com.fixledger.modules.user.enums.UserStatus;
import com.fixledger.modules.user.mapper.UserMapper;
import java.time.LocalDateTime;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class AuthServiceImpl implements AuthService {

  private final UserMapper userMapper;
  private final FamilyService familyService;
  private final PasswordEncoder passwordEncoder;
  private final JwtTokenProvider jwtTokenProvider;

  public AuthServiceImpl(
      UserMapper userMapper,
      FamilyService familyService,
      PasswordEncoder passwordEncoder,
      JwtTokenProvider jwtTokenProvider
  ) {
    this.userMapper = userMapper;
    this.familyService = familyService;
    this.passwordEncoder = passwordEncoder;
    this.jwtTokenProvider = jwtTokenProvider;
  }

  @Override
  @Transactional
  public RegisterResponse register(RegisterRequest request) {
    ensureUsernameAvailable(request.username());
    ensureEmailAvailable(request.email());

    UserEntity user = new UserEntity();
    user.setUsername(request.username());
    user.setEmail(normalizeBlank(request.email()));
    user.setPasswordHash(passwordEncoder.encode(request.password()));
    user.setNickname(normalizeBlank(request.nickname()));
    user.setStatus(UserStatus.ENABLED.getCode());
    userMapper.insert(user);

    String displayName = StringUtils.hasText(user.getNickname())
        ? user.getNickname()
        : user.getUsername();
    // 注册和默认家庭空间放在同一事务内，避免用户首次登录缺少家庭上下文。
    familyService.createDefaultFamily(user.getId(), displayName);
    return new RegisterResponse(user.getId(), user.getUsername(), user.getNickname());
  }

  @Override
  @Transactional
  public LoginResponse login(LoginRequest request) {
    UserEntity user = findByAccount(request.account());
    if (user == null) {
      throw new BusinessException(ErrorCode.USER_NOT_FOUND, "用户不存在");
    }
    if (!UserStatus.ENABLED.getCode().equals(user.getStatus())) {
      throw new BusinessException(ErrorCode.FORBIDDEN, "用户已被停用");
    }
    if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
      throw new BusinessException(ErrorCode.PASSWORD_ERROR, "账号或密码错误");
    }

    // 登录成功后只更新审计时间，令牌本身不承载密码等敏感信息。
    user.setLastLoginAt(LocalDateTime.now());
    userMapper.updateById(user);

    String accessToken = jwtTokenProvider.generateToken(user.getId(), user.getUsername());
    Long currentFamilyId = familyService.getDefaultFamilyId(user.getId());
    return new LoginResponse(
        accessToken,
        jwtTokenProvider.getAccessTokenTtlSeconds(),
        toUserProfile(user),
        currentFamilyId
    );
  }

  @Override
  public boolean logout() {
    return true;
  }

  @Override
  public UserProfileResponse getCurrentUser() {
    Long userId = CurrentUserContext.getUserId();
    UserEntity user = userMapper.selectById(userId);
    if (user == null) {
      throw new BusinessException(ErrorCode.USER_NOT_FOUND, "用户不存在");
    }
    return toUserProfile(user);
  }

  private void ensureUsernameAvailable(String username) {
    Long count = userMapper.selectCount(new LambdaQueryWrapper<UserEntity>()
        .eq(UserEntity::getUsername, username));
    if (count > 0) {
      throw new BusinessException(ErrorCode.BAD_REQUEST, "用户名已存在");
    }
  }

  private void ensureEmailAvailable(String email) {
    if (!StringUtils.hasText(email)) {
      return;
    }
    Long count = userMapper.selectCount(new LambdaQueryWrapper<UserEntity>()
        .eq(UserEntity::getEmail, email));
    if (count > 0) {
      throw new BusinessException(ErrorCode.BAD_REQUEST, "邮箱已存在");
    }
  }

  private UserEntity findByAccount(String account) {
    return userMapper.selectOne(new LambdaQueryWrapper<UserEntity>()
        .eq(UserEntity::getUsername, account)
        .or()
        .eq(UserEntity::getEmail, account)
        .last("LIMIT 1"));
  }

  private UserProfileResponse toUserProfile(UserEntity user) {
    return new UserProfileResponse(
        user.getId(),
        user.getUsername(),
        user.getNickname(),
        user.getEmail()
    );
  }

  private String normalizeBlank(String value) {
    return StringUtils.hasText(value) ? value : null;
  }
}

