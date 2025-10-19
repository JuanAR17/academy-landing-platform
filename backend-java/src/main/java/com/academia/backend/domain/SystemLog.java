package com.academia.backend.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "system_logs", indexes = {
    @Index(name = "idx_log_level", columnList = "log_level"),
    @Index(name = "idx_created_at", columnList = "created_at"),
    @Index(name = "idx_module", columnList = "module")
})
public class SystemLog {
  @Id
  @Column(columnDefinition = "uuid")
  private UUID id;

  @Column(name = "log_level", nullable = false, length = 20)
  private String logLevel; // INFO, ERROR, WARN, DEBUG

  @Column(nullable = false, length = 100)
  private String module; // Auth, Course, Payment, etc.

  @Column(nullable = false, length = 200)
  private String action;

  @Column(columnDefinition = "text")
  private String message;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private UserEntity user;

  @Column(name = "ip_address", columnDefinition = "text")
  private String ipAddress;

  @Column(name = "user_agent", columnDefinition = "text")
  private String userAgent;

  @Column(name = "request_path")
  private String requestPath;

  @Column(name = "request_method", length = 10)
  private String requestMethod;

  @Column(name = "status_code")
  private Integer statusCode;

  @Column(name = "duration_ms")
  private Long durationMs;

  @Column(name = "stack_trace", columnDefinition = "text")
  private String stackTrace;

  @Column(name = "additional_data", columnDefinition = "jsonb")
  @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
  private String additionalData;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @PrePersist
  void onCreate() {
    if (id == null)
      id = UUID.randomUUID();
    if (createdAt == null)
      createdAt = Instant.now();
  }

  // Getters y Setters
  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getLogLevel() {
    return logLevel;
  }

  public void setLogLevel(String logLevel) {
    this.logLevel = logLevel;
  }

  public String getModule() {
    return module;
  }

  public void setModule(String module) {
    this.module = module;
  }

  public String getAction() {
    return action;
  }

  public void setAction(String action) {
    this.action = action;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public UserEntity getUser() {
    return user;
  }

  public void setUser(UserEntity user) {
    this.user = user;
  }

  public String getIpAddress() {
    return ipAddress;
  }

  public void setIpAddress(String ipAddress) {
    this.ipAddress = ipAddress;
  }

  public String getUserAgent() {
    return userAgent;
  }

  public void setUserAgent(String userAgent) {
    this.userAgent = userAgent;
  }

  public String getRequestPath() {
    return requestPath;
  }

  public void setRequestPath(String requestPath) {
    this.requestPath = requestPath;
  }

  public String getRequestMethod() {
    return requestMethod;
  }

  public void setRequestMethod(String requestMethod) {
    this.requestMethod = requestMethod;
  }

  public Integer getStatusCode() {
    return statusCode;
  }

  public void setStatusCode(Integer statusCode) {
    this.statusCode = statusCode;
  }

  public Long getDurationMs() {
    return durationMs;
  }

  public void setDurationMs(Long durationMs) {
    this.durationMs = durationMs;
  }

  public String getStackTrace() {
    return stackTrace;
  }

  public void setStackTrace(String stackTrace) {
    this.stackTrace = stackTrace;
  }

  public String getAdditionalData() {
    return additionalData;
  }

  public void setAdditionalData(String additionalData) {
    this.additionalData = additionalData;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }
}
