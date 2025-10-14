package com.academia.backend.web;

import com.academia.backend.domain.LeadEntity;
import com.academia.backend.dto.LeadIn;
import com.academia.backend.repo.LeadRepo;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;

@RestController
public class LeadController {
  private final LeadRepo leads;
  public LeadController(LeadRepo leads) { this.leads = leads; }

  @PostMapping("/ingest")
  @Operation(summary = "Guarda un Lead en Postgres")
  public Map<String, Object> ingest(@Valid @RequestBody LeadIn in) {
    var e = new LeadEntity();
    e.setTsUtc(Instant.now());
    e.setEmail(safe(in.email));
    e.setName(safe(in.name));
    e.setCoursesJson(new com.fasterxml.jackson.databind.ObjectMapper().valueToTree(in.courses).toString());
    e = leads.save(e);
    return Map.of("ok", true, "id", e.getId());
  }

  private static String safe(String s) {
    if (s == null) return "";
    s = s.replace("\0", "").replace("\r", " ").replace("\n", " ");
    return s.startsWith("=") || s.startsWith("+") || s.startsWith("-") || s.startsWith("@") ? "'" + s : s;
  }
}

