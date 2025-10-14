package com.academia.backend.web;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class HealthController {
  @GetMapping("/health")
  @Operation(summary = "Healthcheck")
  public Map<String, String> health() { return Map.of("status", "ok"); }
}

