package com.academia.backend.domain;

public enum PaymentStatus {
    PENDING, // Pendiente de pago
    APPROVED, // Pago aprobado
    REJECTED, // Pago rechazado
    CANCELLED, // Pago cancelado
    REFUNDED // Pago reembolsado
}