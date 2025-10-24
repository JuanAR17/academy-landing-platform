package com.academia.backend.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import com.academia.backend.service.IpResolver;

import io.swagger.v3.oas.annotations.Operation;

import com.academia.backend.service.IpResolver;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
public class HealthController {

  private final IpResolver ipResolver;

  @GetMapping("/health")
  public Map<String, String> health() {
    return Map.of("status", "ok");
  }

  public HealthController(IpResolver ipResolver) {
    this.ipResolver = ipResolver;
  }

  @GetMapping("/ip")
  @Operation(summary = "Obtener IP del cliente",
             description = "Resuelve la IP a partir de X-Forwarded-For / X-Real-IP / CF-Connecting-IP, con fallback a remoteAddr.")
  public Map<String, String> ip(HttpServletRequest request) {
    String ip = ipResolver.resolve(request);
    return Map.of("ip", ip);
  }


}
