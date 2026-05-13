package com.fixledger.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

/**
 * <p>
 * 文件功能说明：认证安全组件，为各业务模块提供可复用能力。
 * </p>
 *
 * @Author FixLedger
 */
@Component
public class JwtTokenProvider {

  private static final String USERNAME_CLAIM = "username";

  private final JwtProperties jwtProperties;
  private final SecretKey secretKey;

  public JwtTokenProvider(JwtProperties jwtProperties) {
    this.jwtProperties = jwtProperties;
    this.secretKey = Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：完成生成 JWT 令牌安全处理。
   * </p>
   * @param userId 当前用户 ID
   * @param username 用户名
   * @return 令牌或解析结果
   */
  public String generateToken(Long userId, String username) {
    Instant now = Instant.now();
    Instant expiresAt = now.plusSeconds(jwtProperties.accessTokenTtlSeconds());
    return Jwts.builder()
        .subject(String.valueOf(userId))
        .claim(USERNAME_CLAIM, username)
        .issuedAt(Date.from(now))
        .expiration(Date.from(expiresAt))
        .signWith(secretKey)
        .compact();
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：完成解析数据安全处理。
   * </p>
   * @param token JWT 令牌
   * @return 令牌或解析结果
   */
  public CurrentUser parseToken(String token) {
    Claims claims = Jwts.parser()
        .verifyWith(secretKey)
        .build()
        .parseSignedClaims(token)
        .getPayload();
    Long userId = Long.valueOf(claims.getSubject());
    String username = claims.get(USERNAME_CLAIM, String.class);
    return new CurrentUser(userId, username);
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：完成查询安全处理。
   * </p>
   * @return 查询结果
   */
  public long getAccessTokenTtlSeconds() {
    return jwtProperties.accessTokenTtlSeconds();
  }
}
