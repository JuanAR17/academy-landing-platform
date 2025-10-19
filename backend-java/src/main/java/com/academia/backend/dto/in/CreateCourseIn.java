package com.academia.backend.dto.in;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

public record CreateCourseIn(
  @NotBlank(message = "El título es requerido")
  @Size(max = 200, message = "El título no puede exceder 200 caracteres")
  String title,
  
  String description,
  
  @Size(max = 500, message = "La descripción corta no puede exceder 500 caracteres")
  String shortDescription,
  
  String thumbnailUrl,
  
  String videoPreviewUrl,
  
  @NotNull(message = "El precio es requerido")
  @DecimalMin(value = "0.0", inclusive = true, message = "El precio debe ser mayor o igual a 0")
  BigDecimal price,
  
  @Min(value = 0, message = "La duración debe ser mayor o igual a 0")
  Integer durationHours,
  
  String difficultyLevel,
  
  Integer maxStudents,
  
  @NotBlank(message = "La categoría es requerida")
  String category,
  
  List<String> tags,
  
  List<String> requirements,
  
  List<String> learningOutcomes
) {}
