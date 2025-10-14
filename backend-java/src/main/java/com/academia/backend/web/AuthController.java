package com.academia.backend.web;

import com.academia.backend.domain.SessionEntity;
import com.academia.backend.domain.UserEntity;
import com.academia.backend.dto.LoginIn;
import com.academia.backend.dto.TokenOut;
import com.academia.backend.repo.SessionRepo;
import com.academia.backend.repo.UserRepo;
import com.academia.backend.service.AuthService;
import com.academia.backend.service.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

@RestController
@RequestMapping("/auth")
public class AuthController {
  private final UserRepo users;
  private final SessionRepo sessions;
  private final AuthService auth;
  private final JwtService jwt;

  @Value("${cookies.secure:false}") boolean cookieSecure;
  @Value("${cookies.sameSite:Strict}") String sameSite;
  @Value("${cookies.domain:}") String cookieDomain;
  @Value("${cookies.path:/}") String cookiePath;

  private static final String RT_COOKIE = "rt";
  private static final String CSRF_COOKIE = "csrf";

  public AuthController(UserRepo users, SessionRepo sessions, AuthService auth, JwtService jwt) {
    this.users = users; this.sessions = sessions; this.auth = auth; this.jwt = jwt;
  }

  @PostMapping("/login")
  @Operation(summary = "Login: crea sesión y entrega access JWT + cookies (rt, csrf)")
  public ResponseEntity<TokenOut> login(@Valid @RequestBody LoginIn in,
                                        @RequestHeader(value="User-Agent", required=false) String ua) throws Exception {
    UserEntity user = users.findByEmail(in.email)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
    if (!auth.verifyPassword(user.getPasswordHash(), in.password))
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");

    String refreshPlain = auth.genRandomUrlToken();
    byte[] rth = auth.hmacRefresh(refreshPlain);

    SessionEntity row = auth.newSession(user.getId(), rth, ua);

    String csrf = auth.genRandomUrlToken();
    String access = jwt.mintAccess(user.getId().toString(), row.getId().toString());
    TokenOut out = new TokenOut(access, csrf);

    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.SET_COOKIE, buildCookie(RT_COOKIE, refreshPlain, true, 60L*60*24*60));
    headers.add(HttpHeaders.SET_COOKIE, buildCookie(CSRF_COOKIE, csrf, false, 60L*60*24*60));
    return ResponseEntity.ok().headers(headers).body(out);
  }

  @PostMapping("/refresh")
  @Operation(summary = "Rota refresh y entrega nuevo access")
  public ResponseEntity<TokenOut> refresh(HttpServletRequest req) throws Exception {
    String csrfCookie = getCookie(req, CSRF_COOKIE);
    String csrfHeader = req.getHeader("X-CSRF-Token");
    if (csrfCookie == null || csrfHeader == null || !csrfCookie.equals(csrfHeader))
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "CSRF validation failed");

    String rt = getCookie(req, RT_COOKIE);
    if (rt == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing refresh token");

    byte[] rth = auth.hmacRefresh(rt);
    SessionEntity row = sessions.findByRefreshTokenHash(rth)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired session"));

    if (row.isRevoked() || row.getExpiresAt().isBefore(Instant.now()))
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired session");

    String newPlain = auth.genRandomUrlToken();
    row.setRefreshTokenHash(auth.hmacRefresh(newPlain));
    row.setLastUsedAt(Instant.now());
    sessions.save(row);

    String access = jwt.mintAccess(row.getUserId().toString(), row.getId().toString());
    TokenOut out = new TokenOut(access, null);

    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.SET_COOKIE, buildCookie(RT_COOKIE, newPlain, true, 60L*60*24*60));
    return ResponseEntity.ok().headers(headers).body(out);
  }

  @PostMapping("/logout")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Revoca sesión y borra cookies")
  public void logout(HttpServletRequest req, HttpServletResponse res) throws Exception {
    String rt = getCookie(req, RT_COOKIE);
    if (rt != null) {
      byte[] rth = auth.hmacRefresh(rt);
      sessions.findByRefreshTokenHash(rth).ifPresent(s -> { s.setRevoked(true); sessions.save(s); });
    }
    res.addHeader(HttpHeaders.SET_COOKIE, buildCookie(RT_COOKIE, "", true, 0));
    res.addHeader(HttpHeaders.SET_COOKIE, buildCookie(CSRF_COOKIE, "", false, 0));
  }

  @GetMapping("/check")
  public java.util.Map<String, String> check() { return java.util.Map.of("status", "success"); }

  // ---- helpers cookies ----
  private String buildCookie(String name, String value, boolean httpOnly, long maxAgeSeconds) {
    StringBuilder sb = new StringBuilder();
    sb.append(name).append("=").append(value).append("; Path=").append(cookiePath)
      .append("; Max-Age=").append(maxAgeSeconds);
    if (httpOnly) sb.append("; HttpOnly");
    if (cookieSecure) sb.append("; Secure");
    if (!cookieDomain.isBlank()) sb.append("; Domain=").append(cookieDomain);
    sb.append("; SameSite=").append(sameSite);
    return sb.toString();
  }
  private static String getCookie(HttpServletRequest req, String name) {
    jakarta.servlet.http.Cookie[] arr = req.getCookies();
    if (arr == null) return null;
    for (jakarta.servlet.http.Cookie c : arr) {
      if (name.equals(c.getName())) return c.getValue();
    }
    return null;
  }
}

