package com.academia.backend.dto.in;

import java.math.BigDecimal;
import java.util.List;

public record UpdateCourseIn(
  String title,
  String description,
  String shortDescription,
  String thumbnailUrl,
  String videoPreviewUrl,
  BigDecimal price,
  Integer durationHours,
  String difficultyLevel,
  Integer maxStudents,
  String category,
  List<String> tags,
  List<String> requirements,
  List<String> learningOutcomes
) {}
