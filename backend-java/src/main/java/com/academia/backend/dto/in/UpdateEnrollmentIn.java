package com.academia.backend.dto.in;

import com.academia.backend.domain.EnrollmentStatus;

public record UpdateEnrollmentIn(
        EnrollmentStatus status,
        Integer progressPercentage,
        String notes) {
}
