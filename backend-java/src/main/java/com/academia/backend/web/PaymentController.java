package com.academia.backend.web;

import com.academia.backend.service.LogService;
import com.academia.backend.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
@Tag(name = "Payments", description = "Manejo de pagos y confirmaciones")
public class PaymentController {

    private final LogService logService;
    private final PaymentService paymentService;

    public PaymentController(LogService logService, PaymentService paymentService) {
        this.logService = logService;
        this.paymentService = paymentService;
    }

    @PostMapping("/confirm")
    @Operation(summary = "Confirmación de pago desde ePayco", description = "Endpoint que recibe las confirmaciones de pago desde ePayco")
    public ResponseEntity<String> confirmPayment(@RequestBody Map<String, Object> paymentData) {
        try {
            logService.logInfo("PAYMENT", "CONFIRMATION_RECEIVED",
                    "Confirmación de pago recibida: " + paymentData.toString(), null);

            // Procesar la confirmación usando el servicio de pagos
            paymentService.processPaymentConfirmation(paymentData);

            return ResponseEntity.ok("Payment confirmation processed successfully");

        } catch (Exception e) {
            logService.logError("PAYMENT", "CONFIRMATION_ERROR",
                    "Error procesando confirmación de pago: " + e.getMessage(), (UUID) null);
            return ResponseEntity.badRequest().body("Error processing payment confirmation");
        }
    }

    @PostMapping("/response")
    @Operation(summary = "Respuesta de pago desde ePayco", description = "Endpoint que maneja la respuesta del usuario después del pago")
    public ResponseEntity<String> paymentResponse(@RequestBody Map<String, Object> responseData) {
        logService.logInfo("PAYMENT", "RESPONSE_RECEIVED",
                "Respuesta de pago recibida: " + responseData.toString(), null);

        // Este endpoint maneja cuando el usuario regresa de ePayco
        // Aquí puedes redirigir al frontend con el resultado del pago

        return ResponseEntity.ok("Payment response processed");
    }
}