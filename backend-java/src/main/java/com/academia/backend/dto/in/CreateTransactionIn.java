package com.academia.backend.dto.in;

import com.academia.backend.domain.PaymentMethod;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.UUID;

public record CreateTransactionIn(
                @NotNull(message = "El ID del curso es requerido") UUID courseId,

                @NotNull(message = "El monto es requerido") @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0") BigDecimal amount,

                @NotBlank(message = "La moneda es requerida") @Size(min = 3, max = 3, message = "La moneda debe tener 3 caracteres") String currency,

                @NotNull(message = "El método de pago es requerido") PaymentMethod paymentMethod,

                @NotBlank(message = "La pasarela de pago es requerida") String paymentGateway,

                String description,

                String metadata) {
}
