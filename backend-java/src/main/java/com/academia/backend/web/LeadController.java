package com.academia.backend.web;

import com.academia.backend.domain.LeadEntity;
import com.academia.backend.dto.LeadIn;
import com.academia.backend.repo.LeadRepo;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@RestController
public class LeadController {
  private final LeadRepo leads;
  private final com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();

  public LeadController(LeadRepo leads) { this.leads = leads; }

  @PostMapping("/ingest")
  @Operation(summary = "Guarda un Lead en Postgres")
  public Map<String, Object> ingest(@Valid @RequestBody LeadIn in) throws Exception {
    LeadEntity e = new LeadEntity();
    e.setTsUtc(Instant.now());
    e.setEmail(safe(in.email));
    e.setName(safe(in.name));
    e.setCoursesJson(mapper.writeValueAsString(in.courses));
    e = leads.save(e);
    return Map.of("ok", true, "id", e.getId());
  }

  private static String safe(String s) {
    if (s == null) return "";
    s = s.replace("\0", "").replace("\r", " ").replace("\n", " ");
    if (s.startsWith("=") || s.startsWith("+") || s.startsWith("-") || s.startsWith("@")) return "'" + s;
    return s;
  }
}

