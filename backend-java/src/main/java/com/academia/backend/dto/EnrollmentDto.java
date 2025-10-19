package com.academia.backend.dto;

import com.academia.backend.domain.EnrollmentStatus;
import java.math.BigDecimal;
import java.util.UUID;

public record EnrollmentDto(
        UUID id,
        UUID studentId,
        String studentName,
        String studentEmail,
        UUID courseId,
        String courseTitle,
        EnrollmentStatus status,
        String enrolledAt,
        String completedAt,
        Integer progressPercentage,
        BigDecimal amountPaid,
        String notes,
        boolean certificateIssued,
        String certificateUrl,
        String updatedAt) {
}
