package com.academia.backend.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {
  @Value("${jwt.alg:HS256}") private String alg;
  @Value("${jwt.secret:dev-secret-change-me}") private String secret;
  @Value("${jwt.accessTtlMinutes:15}") private long accessTtlMinutes;

  private Key signingKey() {
    // HS256 por defecto; (si deseas RS256, añade lectura de llaves PEM)
    return Keys.hmacShaKeyFor(secret.getBytes());
  }

  public String mintAccess(String sub, String sid) {
    Instant now = Instant.now();
    return Jwts.builder()
        .setClaims(Map.of("sub", sub, "sid", sid))
        .setIssuedAt(Date.from(now))
        .setExpiration(Date.from(now.plusSeconds(accessTtlMinutes*60)))
        .signWith(signingKey(), SignatureAlgorithm.HS256)
        .compact();
  }

  public Jws<Claims> verify(String token) {
    return Jwts.parserBuilder().setSigningKey(signingKey()).build().parseClaimsJws(token);
  }

  public String refreshAccess(String token) {
    Jws<Claims> claims = verify(token);
    String sub = claims.getBody().get("sub", String.class);
    String sid = claims.getBody().get("sid", String.class);
    return mintAccess(sub, sid);
  }
}

