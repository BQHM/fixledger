package com.fixledger.common.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * <p>
 * 文件功能说明：认证安全类，集中声明 Spring 容器中的基础 Bean。
 * </p>
 *
 * @Author FixLedger
 */
@Configuration
public class UserDetailsServiceDisabledConfig {
  /**
   * @Author FixLedger
   * <p>
   * 功能说明：完成执行业务处理安全处理。
   * </p>
   * @return 处理结果
   */
  @Bean
  public UserDetailsService userDetailsService() {
    return username -> {
      throw new UsernameNotFoundException("Form login is disabled");
    };
  }
}
