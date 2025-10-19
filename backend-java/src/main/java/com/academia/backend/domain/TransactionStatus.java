package com.academia.backend.domain;

public enum TransactionStatus {
    PENDING, // Pendiente
    PROCESSING, // Procesando
    COMPLETED, // Completada
    FAILED, // Fallida
    REFUNDED // Reembolsada
}
