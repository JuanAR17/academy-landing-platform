package com.academia.backend.dto.in;

import java.math.BigDecimal;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record UpdateCourseIn(
    String status,
    String title,
    String description,
    String shortDescription,
    String thumbnailUrl,
    String videoPreviewUrl,
    BigDecimal price,
    Integer duration,
    String level,
    Integer maxStudents,
    String category,
    List<String> tags,
    List<String> requirements,
    List<String> learningOutcomes,
    UUID instructorId) {
}
