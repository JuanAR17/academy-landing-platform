package com.academia.backend.dto;

import java.util.UUID;

public record SystemLogDto(
  UUID id,
  String logLevel,
  String module,
  String action,
  String message,
  UUID userId,
  String userName,
  String ipAddress,
  String userAgent,
  String requestPath,
  String requestMethod,
  Integer statusCode,
  Long durationMs,
  String stackTrace,
  String createdAt
) {}
