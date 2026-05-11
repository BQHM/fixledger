package com.fixledger.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

  private static final String USERNAME_CLAIM = "username";

  private final JwtProperties jwtProperties;
  private final SecretKey secretKey;

  public JwtTokenProvider(JwtProperties jwtProperties) {
    this.jwtProperties = jwtProperties;
    this.secretKey = Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
  }

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

  public long getAccessTokenTtlSeconds() {
    return jwtProperties.accessTokenTtlSeconds();
  }
}
