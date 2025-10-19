package com.academia.backend.config;

import com.academia.backend.service.LogService;
import com.academia.backend.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

@Component
public class RequestLoggingInterceptor implements HandlerInterceptor {
  
  private final LogService logService;
  private final JwtService jwtService;
  
  public RequestLoggingInterceptor(LogService logService, JwtService jwtService) {
    this.logService = logService;
    this.jwtService = jwtService;
  }
  
  @Override
  public boolean preHandle(@NonNull HttpServletRequest request, 
                          @NonNull HttpServletResponse response, 
                          @NonNull Object handler) {
    request.setAttribute("startTime", System.currentTimeMillis());
    return true;
  }
  
  @Override
  public void afterCompletion(@NonNull HttpServletRequest request, 
                             @NonNull HttpServletResponse response, 
                             @NonNull Object handler, 
                             @Nullable Exception ex) {
    
    // No loguear endpoints de health check o swagger
    String path = request.getRequestURI();
    if (path.contains("/health") || path.contains("/swagger") || path.contains("/v3/api-docs")) {
      return;
    }
    
    Long startTime = (Long) request.getAttribute("startTime");
    long duration = startTime != null ? System.currentTimeMillis() - startTime : 0;
    
    String method = request.getMethod();
    int status = response.getStatus();
    
    // Intentar extraer user ID del token si existe
    UUID userId = null;
    try {
      String authHeader = request.getHeader("Authorization");
      if (authHeader != null && authHeader.startsWith("Bearer ")) {
        userId = jwtService.extractUserIdFromHeader(authHeader);
      }
    } catch (Exception e) {
      // Ignorar si no hay token o es inválido
    }
    
    // Determinar el módulo basado en la ruta
    String module = determineModule(path);
    
    // Log según el código de estado
    if (status >= 500) {
      logService.logError(module, method.toLowerCase() + "_request", 
        "Error en request: " + path, userId, ex);
    } else if (status >= 400) {
      logService.logWarn(module, method.toLowerCase() + "_request", 
        "Request con error " + status + ": " + path, userId);
    } else {
      logService.logRequest(module, method.toLowerCase() + "_request", 
        "Request exitoso: " + path, userId, status, duration);
    }
  }
  
  private String determineModule(String path) {
    if (path.contains("/courses")) return "Course";
    if (path.contains("/enrollments")) return "Enrollment";
    if (path.contains("/transactions")) return "Transaction";
    if (path.contains("/auth")) return "Auth";
    if (path.contains("/users")) return "User";
    if (path.contains("/logs")) return "Log";
    return "API";
  }
}
