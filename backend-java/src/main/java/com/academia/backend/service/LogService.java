package com.academia.backend.service;

import com.academia.backend.domain.SystemLog;
import com.academia.backend.dto.SystemLogDto;
import com.academia.backend.repo.SystemLogRepo;
import com.academia.backend.repo.UserRepo;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;
import java.util.UUID;

@Service
@Transactional
public class LogService {
  
  private static final Logger logger = LoggerFactory.getLogger(LogService.class);
  
  private final SystemLogRepo systemLogRepo;
  private final UserRepo userRepo;
  
  public LogService(SystemLogRepo systemLogRepo, UserRepo userRepo) {
    this.systemLogRepo = systemLogRepo;
    this.userRepo = userRepo;
  }
  
  // Log de información
  public void logInfo(String module, String action, String message, UUID userId) {
    logger.info("[{}] [{}] {} - User: {}", module, action, message, userId);
    saveLog("INFO", module, action, message, userId, null, null);
  }
  
  public void logInfo(String module, String action, String message) {
    logger.info("[{}] [{}] {}", module, action, message);
    saveLog("INFO", module, action, message, null, null, null);
  }
  
  // Log de error
  public void logError(String module, String action, String message, UUID userId) {
    logger.error("[{}] [{}] {} - User: {}", module, action, message, userId);
    saveLog("ERROR", module, action, message, userId, null, null);
  }
  
  public void logError(String module, String action, String message, UUID userId, Exception ex) {
    logger.error("[{}] [{}] {} - User: {}", module, action, message, userId, ex);
    saveLog("ERROR", module, action, message, userId, ex, null);
  }
  
  public void logError(String module, String action, String message, Exception ex) {
    logger.error("[{}] [{}] {}", module, action, message, ex);
    saveLog("ERROR", module, action, message, null, ex, null);
  }
  
  // Log de advertencia
  public void logWarn(String module, String action, String message, UUID userId) {
    logger.warn("[{}] [{}] {} - User: {}", module, action, message, userId);
    saveLog("WARN", module, action, message, userId, null, null);
  }
  
  public void logWarn(String module, String action, String message) {
    logger.warn("[{}] [{}] {}", module, action, message);
    saveLog("WARN", module, action, message, null, null, null);
  }
  
  // Log de debug
  public void logDebug(String module, String action, String message) {
    logger.debug("[{}] [{}] {}", module, action, message);
    // No guardamos debug en BD por defecto para no saturar
  }
  
  // Log de request HTTP
  public void logRequest(String module, String action, String message, UUID userId, Integer statusCode, Long durationMs) {
    logger.info("[{}] [{}] {} - Status: {} - Duration: {}ms", module, action, message, statusCode, durationMs);
    saveLog("INFO", module, action, message, userId, null, statusCode, durationMs);
  }
  
  // Guardar log en base de datos
  private void saveLog(String logLevel, String module, String action, String message, 
                      UUID userId, Exception exception, Integer statusCode) {
    saveLog(logLevel, module, action, message, userId, exception, statusCode, null);
  }
  
  private void saveLog(String logLevel, String module, String action, String message, 
                      UUID userId, Exception exception, Integer statusCode, Long durationMs) {
    try {
      SystemLog log = new SystemLog();
      log.setLogLevel(logLevel);
      log.setModule(module);
      log.setAction(action);
      log.setMessage(message);
      log.setStatusCode(statusCode);
      log.setDurationMs(durationMs);
      
      if (userId != null) {
        userRepo.findById(userId).ifPresent(log::setUser);
      }
      
      // Obtener información de la request si está disponible
      try {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
          HttpServletRequest request = attributes.getRequest();
          log.setIpAddress(getClientIpAddress(request));
          log.setUserAgent(request.getHeader("User-Agent"));
          log.setRequestPath(request.getRequestURI());
          log.setRequestMethod(request.getMethod());
        }
      } catch (Exception e) {
        // Ignorar si no hay contexto de request
      }
      
      // Stack trace si hay excepción
      if (exception != null) {
        log.setStackTrace(getStackTraceAsString(exception));
      }
      
      systemLogRepo.save(log);
    } catch (Exception e) {
      // Si falla el guardado del log, solo lo registramos en consola
      logger.error("Error al guardar log en BD: {}", e.getMessage());
    }
  }
  
  // Obtener IP del cliente
  private String getClientIpAddress(HttpServletRequest request) {
    String ip = request.getHeader("X-Forwarded-For");
    if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getHeader("X-Real-IP");
    }
    if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getRemoteAddr();
    }
    return ip;
  }
  
  // Convertir stack trace a string
  private String getStackTraceAsString(Exception exception) {
    StringBuilder sb = new StringBuilder();
    sb.append(exception.getClass().getName()).append(": ").append(exception.getMessage()).append("\n");
    for (StackTraceElement element : exception.getStackTrace()) {
      sb.append("\tat ").append(element.toString()).append("\n");
    }
    return sb.toString();
  }
  
  // Consultar logs
  @Transactional(readOnly = true)
  public Page<SystemLogDto> getLogs(Pageable pageable) {
    return systemLogRepo.findAll(pageable).map(this::toDto);
  }
  
  @Transactional(readOnly = true)
  public Page<SystemLogDto> getLogsByLevel(String level, Pageable pageable) {
    return systemLogRepo.findByLogLevelOrderByCreatedAtDesc(level, pageable).map(this::toDto);
  }
  
  @Transactional(readOnly = true)
  public Page<SystemLogDto> getLogsByModule(String module, Pageable pageable) {
    return systemLogRepo.findByModuleOrderByCreatedAtDesc(module, pageable).map(this::toDto);
  }
  
  @Transactional(readOnly = true)
  public Page<SystemLogDto> getLogsBetween(Instant start, Instant end, Pageable pageable) {
    return systemLogRepo.findLogsBetween(start, end, pageable).map(this::toDto);
  }
  
  // Convertir a DTO
  private SystemLogDto toDto(SystemLog log) {
    return new SystemLogDto(
      log.getId(),
      log.getLogLevel(),
      log.getModule(),
      log.getAction(),
      log.getMessage(),
      log.getUser() != null ? log.getUser().getId() : null,
      log.getUser() != null ? log.getUser().getUsername() : null,
      log.getIpAddress(),
      log.getUserAgent(),
      log.getRequestPath(),
      log.getRequestMethod(),
      log.getStatusCode(),
      log.getDurationMs(),
      log.getStackTrace(),
      log.getCreatedAt() != null ? log.getCreatedAt().toString() : null
    );
  }
}
