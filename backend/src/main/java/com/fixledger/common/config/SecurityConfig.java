package com.fixledger.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fixledger.common.exception.ErrorCode;
import com.fixledger.common.result.Result;
import com.fixledger.common.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * <p>
 * 文件功能说明：基础配置类，集中声明 Spring 容器中的基础 Bean。
 * </p>
 *
 * @Author FixLedger
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

  private static final String[] PUBLIC_ENDPOINTS = {
      "/api/auth/register",
      "/api/auth/login",
      "/actuator/health",
      "/actuator/info",
      "/v3/api-docs/**",
      "/swagger-ui/**",
      "/swagger-ui.html"
  };

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：完成配置安全过滤链配置。
   * </p>
   * @param http http 参数
   * @param objectMapper JSON 处理器
   * @param jwtAuthenticationFilter jwtAuthenticationFilter 参数
   * @return 处理结果
   */
  @Bean
  public SecurityFilterChain securityFilterChain(
      HttpSecurity http,
      ObjectMapper objectMapper,
      JwtAuthenticationFilter jwtAuthenticationFilter
  ) throws Exception {
    return http
        .csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(session -> session
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(authorize -> authorize
            .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
            .anyRequest().authenticated())
        .exceptionHandling(exception -> exception
            .authenticationEntryPoint((request, response, authException) ->
                writeError(response, objectMapper, ErrorCode.UNAUTHORIZED))
            .accessDeniedHandler((request, response, accessDeniedException) ->
                writeError(response, objectMapper, ErrorCode.FORBIDDEN)))
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
        .build();
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：完成声明密码编码器配置。
   * </p>
   * @return 处理结果
   */
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  private void writeError(
      HttpServletResponse response,
      ObjectMapper objectMapper,
      ErrorCode errorCode
  ) throws IOException {
    response.setStatus(errorCode == ErrorCode.UNAUTHORIZED
        ? HttpServletResponse.SC_UNAUTHORIZED
        : HttpServletResponse.SC_FORBIDDEN);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");
    objectMapper.writeValue(response.getWriter(), Result.error(errorCode));
  }
}
