package com.academia.backend.config;

import com.academia.backend.repo.SessionRepo;
import com.academia.backend.service.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.security.authentication.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.*;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@EnableWebSecurity
@Configuration
public class SecurityConfig {

  @Bean
  SecurityFilterChain chain(HttpSecurity http, JwtFilter jwtFilter) throws Exception {
    http.csrf(csrf -> csrf.disable()); // manejamos CSRF manual en /auth/refresh
    http.authorizeHttpRequests(auth -> auth
        .requestMatchers("/auth/**", "/health", "/ingest", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
        .anyRequest().authenticated()
    );
    http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }

  @Bean AuthenticationManager authManager() {
    return authentication -> authentication; // no usamos UserDetails aquí
  }
}

@Component
class JwtFilter extends OncePerRequestFilter {
  @Autowired JwtService jwt;
  @Autowired SessionRepo sessions;

  @Override
  protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
      throws IOException, jakarta.servlet.ServletException {
    String h = req.getHeader("Authorization");
    if (h != null && h.startsWith("Bearer ")) {
      String token = h.substring(7);
      try {
        var jws = jwt.verify(token);
        Claims c = jws.getBody();
        String sub = c.get("sub", String.class);
        String sid = c.get("sid", String.class);
        var row = sessions.findById(UUID.fromString(sid)).orElse(null);
        if (row != null && !row.isRevoked() && row.getExpiresAt().isAfter(Instant.now())) {
          Authentication auth = new UsernamePasswordAuthenticationToken(sub, null, List.of(new SimpleGrantedAuthority("USER")));
          SecurityContextHolder.getContext().setAuthentication(auth);
        }
      } catch (Exception ignored) {}
    }
    chain.doFilter(req, res);
  }
}

