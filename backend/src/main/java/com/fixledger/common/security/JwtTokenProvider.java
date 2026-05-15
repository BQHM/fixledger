package com.fixledger.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

/**
 * <p>
 * 文件功能说明：JWT 令牌组件，负责签发、解析令牌并计算令牌剩余有效期。
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
   * 生成带唯一 jti 的访问令牌，便于退出登录时将本次令牌加入黑名单。
   *
   * @param userId 当前用户 ID
   * @param username 用户名
   * @return JWT 访问令牌
   */
  public String generateToken(Long userId, String username) {
    Instant now = Instant.now();
    Instant expiresAt = now.plusSeconds(jwtProperties.accessTokenTtlSeconds());
    return Jwts.builder()
        .id(UUID.randomUUID().toString())
        .subject(String.valueOf(userId))
        .claim(USERNAME_CLAIM, username)
        .issuedAt(Date.from(now))
        .expiration(Date.from(expiresAt))
        .signWith(secretKey)
        .compact();
  }

  /**
   * 解析 JWT 并提取当前用户身份和令牌唯一标识。
   *
   * @param token JWT 令牌
   * @return 当前用户上下文
   */
  public CurrentUser parseToken(String token) {
    Claims claims = parseClaims(token);
    Long userId = Long.valueOf(claims.getSubject());
    String username = claims.get(USERNAME_CLAIM, String.class);
    return new CurrentUser(userId, username, claims.getId());
  }

  /**
   * 计算令牌剩余有效期，用于黑名单 TTL，避免 Redis 长期保存已过期令牌。
   *
   * @param token JWT 令牌
   * @return 剩余有效期；已过期时返回 0 秒
   */
  public Duration getRemainingTtl(String token) {
    Date expiration = parseClaims(token).getExpiration();
    long remainingMillis = expiration.getTime() - System.currentTimeMillis();
    return remainingMillis <= 0 ? Duration.ZERO : Duration.ofMillis(remainingMillis);
  }

  /**
   * 查询访问令牌默认有效期，供登录响应展示。
   *
   * @return 访问令牌默认有效秒数
   */
  public long getAccessTokenTtlSeconds() {
    return jwtProperties.accessTokenTtlSeconds();
  }

  private Claims parseClaims(String token) {
    return Jwts.parser()
        .verifyWith(secretKey)
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }
}
