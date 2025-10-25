package com.academia.backend.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Service
public class JwtService {
  @Value("${jwt.alg:HS256}") private String alg;
  @Value("${jwt.secret}")   private String secret;
  @Value("${jwt.accessTtlMinutes:15}") private long accessTtlMinutes;

  private SecretKey key;

  @PostConstruct
  void init() {
    if (secret == null || secret.isBlank()) {
      throw new IllegalStateException("JWT_SECRET no configurado");
    }
    if (secret.startsWith("base64:")) {
      key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret.substring(7)));
    } else {
      key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
    if (key.getEncoded().length < 32) {
      throw new IllegalStateException("JWT_SECRET debe tener >= 32 bytes (256 bits).");
    }
  }

  public String mintAccess(String sub, String sid) {
    Instant now = Instant.now();
    return Jwts.builder()
        .setClaims(Map.of("sub", sub, "sid", sid))
        .setIssuedAt(Date.from(now))
        .setExpiration(Date.from(now.plusSeconds(accessTtlMinutes * 60)))
        .signWith(key, SignatureAlgorithm.HS256)
        .compact();
  }

  public Jws<Claims> verify(String token) {
    return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
  }

  public String refreshAccess(String token) {
    var claims = verify(token);
    String sub = claims.getBody().get("sub", String.class);
    String sid = claims.getBody().get("sid", String.class);
    return mintAccess(sub, sid);
  }

  public UUID extractUserIdFromHeader(String authHeader) {
    // 1) header válido
    if (authHeader == null || !authHeader.startsWith("Bearer ")) return null;

    // 2) token
    String token = authHeader.substring(7).trim();
    if (token.isEmpty()) return null;

    try {
      var jws = verify(token);      // tu método existente que valida firma y devuelve Jws<Claims>
      var claims = jws.getBody();

      // 3) intentos: uid -> userId -> sub (subject)
      String raw = claims.get("uid", String.class);
      if (raw == null) raw = claims.get("userId", String.class);
      if (raw == null) raw = claims.getSubject(); // 'sub'

      // 4) convertir a UUID de forma segura
      if (raw == null || raw.isBlank()) return null;
      return UUID.fromString(raw);
    } catch (Exception e) {
      // token inválido, firma incorrecta, claim no-UUID, etc.
      return null;
    }
  }


  public UUID extractUserId(String token) {
    var claims = verify(token);
    return UUID.fromString(claims.getBody().get("sub", String.class));
  }
}
