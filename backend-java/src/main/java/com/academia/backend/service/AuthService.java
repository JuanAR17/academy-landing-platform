package com.academia.backend.service;

import com.academia.backend.domain.SessionEntity;
import com.academia.backend.domain.UserEntity;
import com.academia.backend.dto.TokenOut;
import com.academia.backend.repo.SessionRepo;
import com.academia.backend.repo.UserRepo;
import jakarta.servlet.http.Cookie;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.UUID;

@Service
public class AuthService {
  private final UserRepo users;
  private final SessionRepo sessions;
  private final JwtService jwt;
  private final SecureRandom rnd = new SecureRandom();
  private final Argon2PasswordEncoder argon = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();

  @Value("${jwt.sessionTtlDays:60}") long sessionTtlDays;
  @Value("${refresh.hmacSecret}") String refreshSecret;
  @Value("${cookies.secure:false}") boolean cookieSecure;
  @Value("${cookies.sameSite:Strict}") String sameSite;
  @Value("${cookies.domain:}") String cookieDomain;
  @Value("${cookies.path:/}") String cookiePath;

  public AuthService(UserRepo users, SessionRepo sessions, JwtService jwt) {
    this.users = users; this.sessions = sessions; this.jwt = jwt;
  }

  public String hashPassword(String raw) { 
    return argon.encode(raw); 
  }

  public boolean verifyPassword(String hash, String raw) { return argon.matches(raw, hash); }

  public String genRandomUrlToken() {
    byte[] buf = new byte[32];
    rnd.nextBytes(buf);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(buf);
  }

  public byte[] hmacRefresh(String plain) throws Exception {
    Mac mac = Mac.getInstance("HmacSHA256");
    mac.init(new SecretKeySpec(refreshSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
    return mac.doFinal(plain.getBytes(StandardCharsets.UTF_8));
  }

  public Cookie buildHttpOnlyCookie(String name, String value, long maxAgeSeconds) {
    Cookie c = new Cookie(name, value);
    c.setHttpOnly(true);
    c.setSecure(cookieSecure);
    c.setPath(cookiePath);
    if (!cookieDomain.isBlank()) c.setDomain(cookieDomain);
    c.setMaxAge((int) maxAgeSeconds);
    return c;
  }

  public TokenOut mint(UserEntity user, SessionEntity row, String csrf) {
    String access = jwt.mintAccess(user.getId().toString(), row.getId().toString());
    return new TokenOut(access, csrf);
  }

  public SessionEntity newSession(UUID userId, byte[] refreshHash, String ua) {
    SessionEntity s = new SessionEntity();
    s.setUserId(userId);
    s.setRefreshTokenHash(refreshHash);
    s.setCreatedAt(Instant.now());
    s.setExpiresAt(Instant.now().plus(sessionTtlDays, ChronoUnit.DAYS));
    s.setUserAgent(ua);
    return sessions.save(s);
  }
}

