package com.academia.backend.web;

import com.academia.backend.dto.SystemLogDto;
import com.academia.backend.service.LogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/logs")
@Tag(name = "System Logs", description = "Gestión de logs del sistema")
@SecurityRequirement(name = "bearerAuth")
public class LogController {
  
  private final LogService logService;
  
  public LogController(LogService logService) {
    this.logService = logService;
  }
  
  @GetMapping
  @Operation(summary = "Obtener todos los logs", description = "Solo admin")
  public ResponseEntity<Page<SystemLogDto>> getLogs(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "50") int size) {
    
    Pageable pageable = PageRequest.of(page, size);
    Page<SystemLogDto> logs = logService.getLogs(pageable);
    return ResponseEntity.ok(logs);
  }
  
  @GetMapping("/level/{level}")
  @Operation(summary = "Obtener logs por nivel", description = "Filtrar por INFO, ERROR, WARN, DEBUG")
  public ResponseEntity<Page<SystemLogDto>> getLogsByLevel(
      @PathVariable String level,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "50") int size) {
    
    Pageable pageable = PageRequest.of(page, size);
    Page<SystemLogDto> logs = logService.getLogsByLevel(level, pageable);
    return ResponseEntity.ok(logs);
  }
  
  @GetMapping("/module/{module}")
  @Operation(summary = "Obtener logs por módulo", description = "Filtrar por módulo (Auth, Course, Payment, etc.)")
  public ResponseEntity<Page<SystemLogDto>> getLogsByModule(
      @PathVariable String module,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "50") int size) {
    
    Pageable pageable = PageRequest.of(page, size);
    Page<SystemLogDto> logs = logService.getLogsByModule(module, pageable);
    return ResponseEntity.ok(logs);
  }
  
  @GetMapping("/between")
  @Operation(summary = "Obtener logs en rango de fechas")
  public ResponseEntity<Page<SystemLogDto>> getLogsBetween(
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "50") int size) {
    
    Pageable pageable = PageRequest.of(page, size);
    Page<SystemLogDto> logs = logService.getLogsBetween(startDate, endDate, pageable);
    return ResponseEntity.ok(logs);
  }
}
