package com.academia.backend.dto;

import com.academia.backend.domain.CourseStatus;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CourseDto(
        UUID id,
        String title,
        String description,
        String shortDescription,
        String thumbnailUrl,
        String videoPreviewUrl,
        UUID teacherId,
        String teacherName,
        BigDecimal price,
        Integer durationHours,
        String difficultyLevel,
        CourseStatus status,
        Integer maxStudents,
        Integer currentStudents,
        String category,
        List<String> tags,
        List<String> requirements,
        List<String> learningOutcomes,
        String createdAt,
        String updatedAt,
        String publishedAt) {
}
