package com.academia.backend.web;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import java.util.Map;

@RestController
@RequestMapping("/api/epayco")
@Tag(name = "ePayco", description = "Integración con pasarela de pagos ePayco")
public class EpaycoController {

    private final com.academia.backend.service.EpaycoClient epayco;
    private final com.academia.backend.service.EpaycoAuthService auth;

    public EpaycoController(com.academia.backend.service.EpaycoClient epayco,
            com.academia.backend.service.EpaycoAuthService auth) {
        this.epayco = epayco;
        this.auth = auth;
    }

    @GetMapping("/banks")
    @Operation(summary = "Listar bancos disponibles", description = "Obtiene la lista de bancos disponibles para transferencias")
    public Mono<JsonNode> banks() {
        return epayco.listBanks();
    }

    @PostMapping("/charge")
    @Operation(summary = "Crear enlace de pago", description = "Crea un enlace de pago para un curso o servicio")
    public Mono<JsonNode> createCharge(@RequestBody JsonNode body) {
        // body: construye en frontend solo datos no sensibles
        return epayco.createCharge(body);
    }

    @PostMapping("/payment")
    @Operation(summary = "Procesar pago con tarjeta", description = "Procesa un pago directamente con tarjeta de crédito/débito")
    public Mono<JsonNode> createCardPayment(@RequestBody JsonNode body) {
        return epayco.createCardPayment(body);
    }

    @GetMapping("/payment/{transactionId}/status")
    @Operation(summary = "Consultar estado de pago", description = "Obtiene el estado de una transacción de pago")
    public Mono<JsonNode> getPaymentStatus(@PathVariable String transactionId) {
        return epayco.getPaymentStatus(transactionId);
    }

    @GetMapping("/test")
    @Operation(summary = "Endpoint de prueba", description = "Endpoint simple para verificar que la integración funciona")
    public Mono<String> test() {
        return Mono.just("ePayco integration is working!");
    }

    @GetMapping("/token")
    @Operation(summary = "Obtener token de autenticación", description = "Obtiene el token actual de ePayco, realizando login si es necesario")
    public Mono<Map<String, String>> getToken() {
        return auth.getToken().map(t -> Map.of("token", t));
    }

    @GetMapping("/payment-methods")
    @Operation(summary = "Listar métodos de pago", description = "Obtiene los métodos de pago disponibles")
    public Mono<JsonNode> listPaymentMethods() {
        return epayco.listPaymentMethods();
    }
}