package com.fixledger.modules.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fixledger.common.exception.BusinessException;
import com.fixledger.common.exception.ErrorCode;
import com.fixledger.modules.auth.request.LoginRequest;
import com.fixledger.modules.auth.request.RegisterRequest;
import com.fixledger.modules.auth.response.LoginResponse;
import com.fixledger.modules.auth.response.RegisterResponse;
import com.fixledger.modules.auth.service.AuthService;
import com.fixledger.modules.family.mapper.FamilyMemberMapper;
import com.fixledger.modules.family.mapper.FamilySpaceMapper;
import com.fixledger.modules.user.entity.UserEntity;
import com.fixledger.modules.user.mapper.UserMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class AuthServiceTest {

  @Autowired
  private AuthService authService;

  @Autowired
  private UserMapper userMapper;

  @Autowired
  private FamilySpaceMapper familySpaceMapper;

  @Autowired
  private FamilyMemberMapper familyMemberMapper;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Test
  @DisplayName("注册成功后加密密码并自动创建默认家庭空间")
  void registerCreatesDefaultFamily() {
    RegisterResponse response = authService.register(new RegisterRequest(
        "zhangsan",
        "zhangsan@example.com",
        "123456",
        "张三"
    ));

    UserEntity user = userMapper.selectById(response.userId());

    assertThat(user).isNotNull();
    assertThat(user.getPasswordHash()).isNotEqualTo("123456");
    assertThat(passwordEncoder.matches("123456", user.getPasswordHash())).isTrue();
    assertThat(familySpaceMapper.selectCount(null)).isEqualTo(1);
    assertThat(familyMemberMapper.selectCount(null)).isEqualTo(1);
  }

  @Test
  @DisplayName("登录成功返回访问令牌和当前家庭空间")
  void loginReturnsTokenAndCurrentFamily() {
    authService.register(new RegisterRequest("lisi", null, "123456", "李四"));

    LoginResponse response = authService.login(new LoginRequest("lisi", "123456"));

    assertThat(response.accessToken()).isNotBlank();
    assertThat(response.expiresIn()).isPositive();
    assertThat(response.user().username()).isEqualTo("lisi");
    assertThat(response.currentFamilyId()).isNotNull();
  }

  @Test
  @DisplayName("登录密码错误时抛出业务异常")
  void loginWithWrongPasswordThrowsBusinessException() {
    authService.register(new RegisterRequest("wangwu", null, "123456", "王五"));

    assertThatThrownBy(() -> authService.login(new LoginRequest("wangwu", "bad-password")))
        .isInstanceOfSatisfying(BusinessException.class, e ->
            assertThat(e.getErrorCode()).isEqualTo(ErrorCode.PASSWORD_ERROR));
  }
}
