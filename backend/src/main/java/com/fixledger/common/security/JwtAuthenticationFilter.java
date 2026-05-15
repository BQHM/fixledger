package com.fixledger.common.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * <p>
 * 文件功能说明：JWT 认证过滤器，在请求进入业务层前解析令牌并拦截已退出令牌。
 * </p>
 *
 * @Author FixLedger
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  public static final String BEARER_PREFIX = "Bearer ";

  private final JwtTokenProvider jwtTokenProvider;
  private final JwtBlacklistService jwtBlacklistService;

  public JwtAuthenticationFilter(
      JwtTokenProvider jwtTokenProvider,
      JwtBlacklistService jwtBlacklistService
  ) {
    this.jwtTokenProvider = jwtTokenProvider;
    this.jwtBlacklistService = jwtBlacklistService;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain
  ) throws ServletException, IOException {
    String token = resolveToken(request);
    if (StringUtils.hasText(token)) {
      authenticate(token);
    }
    filterChain.doFilter(request, response);
  }

  private void authenticate(String token) {
    try {
      CurrentUser currentUser = jwtTokenProvider.parseToken(token);
      if (jwtBlacklistService.isBlacklisted(currentUser.tokenId())) {
        SecurityContextHolder.clearContext();
        return;
      }
      UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
          currentUser,
          null,
          List.of(new SimpleGrantedAuthority("ROLE_USER"))
      );
      SecurityContextHolder.getContext().setAuthentication(authentication);
    } catch (JwtException | IllegalArgumentException e) {
      SecurityContextHolder.clearContext();
    }
  }

  private String resolveToken(HttpServletRequest request) {
    String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (!StringUtils.hasText(authorization) || !authorization.startsWith(BEARER_PREFIX)) {
      return null;
    }
    return authorization.substring(BEARER_PREFIX.length());
  }
}
