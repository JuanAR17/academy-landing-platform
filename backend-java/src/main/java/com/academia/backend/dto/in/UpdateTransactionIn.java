package com.academia.backend.dto.in;

import com.academia.backend.domain.TransactionStatus;

public record UpdateTransactionIn(
        String externalTransactionId,
        TransactionStatus status,
        String errorMessage,
        String metadata) {
}
