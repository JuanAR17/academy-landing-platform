package com.academia.backend.config;

import com.academia.backend.repo.SessionRepo;
import com.academia.backend.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
public class JwtFilter extends OncePerRequestFilter {
  private final JwtService jwt;
  private final SessionRepo sessions;

  public JwtFilter(JwtService jwt, SessionRepo sessions) {
    this.jwt = jwt;
    this.sessions = sessions;
  }

  @Override
  protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
    String path = request.getRequestURI();
    // Solo excluir rutas específicamente públicas
    return path.equals("/auth/login") ||
        path.equals("/auth/register") ||
        path.equals("/auth/refresh") ||
        path.equals("/auth/check") ||
        path.equals("/auth/logout") ||
        path.startsWith("/health") ||
        path.startsWith("/ingest") ||
        path.startsWith("/v3/api-docs") ||
        path.startsWith("/swagger-ui");
  }

  @Override
  protected void doFilterInternal(@NonNull HttpServletRequest req, @NonNull HttpServletResponse res,
      @NonNull FilterChain chain)
      throws ServletException, IOException {
    String h = req.getHeader("Authorization");
    if (h != null && h.startsWith("Bearer ")) {
      String token = h.substring(7);
      try {
        Jws<Claims> jws = jwt.verify(token);
        Claims c = jws.getBody();
        String sid = c.get("sid", String.class);
        var rowOpt = sessions.findById(UUID.fromString(sid));
        if (rowOpt.isPresent() &&
            !rowOpt.get().isRevoked() &&
            rowOpt.get().getExpiresAt().isAfter(Instant.now())) {
          Authentication auth = new UsernamePasswordAuthenticationToken(
              c.get("sub", String.class), null, List.of(new SimpleGrantedAuthority("USER")));
          SecurityContextHolder.getContext().setAuthentication(auth);

          // Auto-refresh: generate new access token with extended expiration
          String newToken = jwt.refreshAccess(token);
          res.setHeader("X-New-Access-Token", newToken);
        }
      } catch (Exception ignored) {
        // Ignored: if JWT parsing or session validation fails, continue without
        // authentication
      }
    }
    chain.doFilter(req, res);
  }
}
