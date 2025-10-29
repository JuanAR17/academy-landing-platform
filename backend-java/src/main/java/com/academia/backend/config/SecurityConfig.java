package com.academia.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableWebSecurity
@Configuration
public class SecurityConfig {

  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http, JwtFilter jwtFilter) throws Exception {
    http.csrf(csrf -> csrf.disable());
    http.sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

    http.authorizeHttpRequests(auth -> auth
        // --- públicos que ya tenías ---
        .requestMatchers(
            "/api/auth/**", "/health", "/ip", "/ingest",
            "/api/locations/**", "/api/courses/**",
            "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html"
        ).permitAll()

        // --- catálogos ePayco (GET) ---
        .requestMatchers(HttpMethod.GET,
            "/api/epayco/banks",
            "/api/epayco/document-types",
            "/api/epayco/payment-methods",
            "/api/epayco/payment/*/status",
            // si publicas consulta por token corto, déjalo habilitado:
            "/api/epayco/tx/**"
        ).permitAll()

        // --- pagos accesibles a invitados o logueados ---
        .requestMatchers(HttpMethod.POST,
            "/api/epayco/payment",   // tarjeta (ya lo tenías)
            "/api/epayco/pse",       // << NUEVO: PSE abierto (JWT opcional)
            "/webhook/epayco"        // << NUEVO: webhook server-to-server desde ePayco
        ).permitAll()

        // el resto requiere JWT
        .anyRequest().authenticated()
    );

    // 401 cuando falta/invalid JWT
    http.exceptionHandling(e ->
        e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
    );

    // Filtro JWT
    http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  AuthenticationManager authManager() {
    // No UserDetails tradicional por ahora
    return authentication -> authentication;
  }
}
