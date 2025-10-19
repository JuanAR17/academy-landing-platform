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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import java.time.Instant;
import java.util.UUID;

@Service
public class LogService {

    private static final Logger logger = LoggerFactory.getLogger(LogService.class);

    // Constants for log levels
    private static final String LOG_LEVEL_INFO = "INFO";
    private static final String LOG_LEVEL_ERROR = "ERROR";

    // Constants for log patterns
    private static final String LOG_PATTERN_WITH_USER = "[{}] [{}] {} - User: {}";
    private static final String LOG_PATTERN_WITHOUT_USER = "[{}] [{}] {}";

    private final SystemLogRepo systemLogRepo;
    private final UserRepo userRepo;

    // Clase interna para parámetros del log
    private static class LogParams {
        final String logLevel;
        final String module;
        final String action;
        final String message;
        final UUID userId;
        final Exception exception;
        final Integer statusCode;
        final Long durationMs;

        LogParams(String logLevel, String module, String action, String message, UUID userId,
                 Exception exception, Integer statusCode, Long durationMs) {
            this.logLevel = logLevel;
            this.module = module;
            this.action = action;
            this.message = message;
            this.userId = userId;
            this.exception = exception;
            this.statusCode = statusCode;
            this.durationMs = durationMs;
        }
    }

    public LogService(SystemLogRepo systemLogRepo, UserRepo userRepo) {
        this.systemLogRepo = systemLogRepo;
        this.userRepo = userRepo;
    }

    // Log de información
    public void logInfo(String module, String action, String message, UUID userId) {
        if (userId != null) {
            logger.info(LOG_PATTERN_WITH_USER, module, action, message, userId);
        } else {
            logger.info(LOG_PATTERN_WITHOUT_USER, module, action, message);
        }
        this.saveInfoLog(module, action, message, userId);
    }

    // Log de error
    public void logError(String module, String action, String message, UUID userId) {
        logger.error(LOG_PATTERN_WITH_USER, module, action, message, userId);
        this.saveErrorLog(module, action, message, userId, null);
    }

    public void logError(String module, String action, String message, UUID userId, Exception exception) {
        if (userId != null) {
            logger.error(LOG_PATTERN_WITH_USER, module, action, message, userId, exception);
        } else {
            logger.error(LOG_PATTERN_WITHOUT_USER, module, action, message, exception);
        }
        this.saveErrorLog(module, action, message, userId, exception);
    }

    public void logError(String module, String action, String message, Exception ex) {
        logger.error(LOG_PATTERN_WITHOUT_USER, module, action, message, ex);
        this.saveErrorLog(module, action, message, null, ex);
    }

    // Log de advertencia
    public void logWarn(String module, String action, String message, UUID userId) {
        logger.warn(LOG_PATTERN_WITH_USER, module, action, message, userId);
        this.saveInfoLog(module, action, message, userId); // WARN se guarda como INFO en BD
    }

    public void logWarn(String module, String action, String message) {
        logger.warn(LOG_PATTERN_WITHOUT_USER, module, action, message);
        this.saveInfoLog(module, action, message, null); // WARN se guarda como INFO en BD
    }

    // Log de debug
    public void logDebug(String module, String action, String message) {
        logger.debug(LOG_PATTERN_WITHOUT_USER, module, action, message);
        // No guardamos debug en BD por defecto para no saturar
    }

    // Log de request HTTP
    public void logRequest(String module, String action, String message, UUID userId, Integer statusCode,
            Long durationMs) {
        logger.info("[{}] [{}] {} - Status: {} - Duration: {}ms", module, action, message, statusCode, durationMs);
        this.saveRequestLog(module, action, message, userId, statusCode, durationMs);
    }

    // Guardar log en base de datos
    private void saveLog(LogParams params) {
        try {
            SystemLog log = new SystemLog();
            log.setLogLevel(params.logLevel);
            log.setModule(params.module);
            log.setAction(params.action);
            log.setMessage(params.message);
            log.setStatusCode(params.statusCode);
            log.setDurationMs(params.durationMs);

            if (params.userId != null) {
                userRepo.findById(params.userId).ifPresent(log::setUser);
            }

            // Obtener información de la request si está disponible
            populateRequestInfo(log);

            // Stack trace si hay excepción
            if (params.exception != null) {
                log.setStackTrace(getStackTraceAsString(params.exception));
            }

            systemLogRepo.save(log);
        } catch (Exception e) {
            // Si falla el guardado del log, solo lo registramos en consola
            logger.error("Error al guardar log en BD: {}", e.getMessage());
        }
    }

    // Métodos específicos para reducir parámetros
    private void saveInfoLog(String module, String action, String message, UUID userId) {
        this.saveLog(new LogParams(LOG_LEVEL_INFO, module, action, message, userId, null, null, null));
    }

    private void saveErrorLog(String module, String action, String message, UUID userId, Exception exception) {
        this.saveLog(new LogParams(LOG_LEVEL_ERROR, module, action, message, userId, exception, null, null));
    }

    private void saveRequestLog(String module, String action, String message, UUID userId, Integer statusCode, Long durationMs) {
        this.saveLog(new LogParams(LOG_LEVEL_INFO, module, action, message, userId, null, statusCode, durationMs));
    }

    private void populateRequestInfo(SystemLog log) {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
                    .getRequestAttributes();
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
                log.getCreatedAt() != null ? log.getCreatedAt().toString() : null);
    }
}
