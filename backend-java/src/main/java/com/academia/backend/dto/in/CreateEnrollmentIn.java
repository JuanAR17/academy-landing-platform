package com.academia.backend.dto.in;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateEnrollmentIn(
  @NotNull(message = "El ID del curso es requerido")
  UUID courseId,
  
  String notes
) {}
