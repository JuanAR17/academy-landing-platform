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
    this.jwt = jwt; this.sessions = sessions;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
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
        }
      } catch (Exception ignored) {}
    }
    chain.doFilter(req, res);
  }
}

