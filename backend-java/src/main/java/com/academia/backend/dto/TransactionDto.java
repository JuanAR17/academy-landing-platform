package com.academia.backend.dto;

import com.academia.backend.domain.PaymentMethod;
import com.academia.backend.domain.TransactionStatus;
import java.math.BigDecimal;
import java.util.UUID;

public record TransactionDto(
        UUID id,
        UUID userId,
        String userName,
        UUID courseId,
        String courseTitle,
        UUID enrollmentId,
        String transactionReference,
        String externalTransactionId,
        BigDecimal amount,
        String currency,
        PaymentMethod paymentMethod,
        TransactionStatus status,
        String paymentGateway,
        String description,
        String errorMessage,
        String createdAt,
        String updatedAt,
        String completedAt,
        String refundedAt,
        String refundReason) {
}
