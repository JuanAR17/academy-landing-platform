//Aquí funciona

package com.academia.backend.web;

import com.academia.backend.dto.in.CardPaymentIn;
import com.academia.backend.service.EpaycoAuthService;
import com.academia.backend.service.EpaycoClient;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api/epayco")
@Tag(name = "ePayco", description = "Integración con pasarela de pagos ePayco")
public class EpaycoController {

    private final EpaycoClient epayco;
    private final EpaycoAuthService auth;

    public EpaycoController(EpaycoClient epayco,
                            EpaycoAuthService auth) {
        this.epayco = epayco;
        this.auth = auth;
    }

    // --------------------------- Catálogos / utilidades ---------------------------

    @GetMapping("/banks")
    @Operation(summary = "Listar bancos disponibles",
               description = "Obtiene la lista de bancos disponibles para transferencias")
    public Mono<JsonNode> banks() {
        return epayco.listBanks();
    }

    @GetMapping("/payment-methods")
    @Operation(summary = "Listar métodos de pago", description = "Obtiene los métodos de pago disponibles")
    public Mono<JsonNode> listPaymentMethods() {
        return epayco.listPaymentMethods();
    }

    @GetMapping("/payment/{referencePayco}/status")
    @Operation(
        summary = "Consultar estado de pago",
        description = "Consulta el detalle de la transacción en ePayco usando referencePayco. "
                    + "El backend envía GET /transaction/detail con body {\"filter\":{\"referencePayco\":..}} y maneja el Bearer automáticamente."
    )
    @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Respuesta de ePayco (éxito o error en JSON)",
        content = @Content(mediaType = "application/json",
        examples = @ExampleObject(
            name = "Ejemplo éxito",
            value = """
            {
            "success": true,
            "titleResponse": "Successful consult",
            "data": {
                "transaction": { "status": true, "object": "payment", "data": { "ref_payco": 315160539, "estado": "Aprobada" } }
            }
            }
            """
        )
        )
    )
    })
    public Mono<JsonNode> getPaymentStatus(@PathVariable String referencePayco) {
        return epayco.getTransactionDetail(referencePayco);
    }

    // -----------------------------Tipos de documentos.-----------------------------

    @GetMapping("/document-types")
    @Operation(
        summary = "Listar tipos de documentos",
        description = "Proxy a ePayco {{url_apify}}/type/documents. "
                    + "El backend inyecta automáticamente el Bearer obtenido vía /login."
    )
    @ApiResponse(responseCode = "200", description = "OK",
        content = @Content(mediaType = "application/json"))
    public Mono<JsonNode> listDocumentTypes() {
        return epayco.listDocumentTypes();
    }



    // --------------------------- Pago con tarjeta ---------------------------

    @PostMapping("/payment")
    @Operation(
        summary = "Procesar pago con tarjeta de crédito/débito (/payment/process)",
        description = "El backend obtiene/renueva el Bearer con /login (Basic Auth) y lo envía automáticamente.\n" +
                    "• Primera vez: manda tarjeta cruda (cardNumber, cardExpYear, cardExpMonth, cardCvc).\n" +
                    "• Siguientes cobros: manda cardTokenId (y opcional customerId).")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Respuesta de ePayco (éxito o error en JSON)",
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(name = "Éxito (resumen)",
                        value = """
                                {
                                "success": true,
                                "titleResponse": "Success transaction",
                                "data": {
                                    "transaction": {
                                    "status": true,
                                    "object": "payment",
                                    "data": {
                                        "ref_payco": 315036111,
                                        "factura": "QR-APIFY1761133606",
                                        "valor": 10000,
                                        "estado": "Pendiente",
                                        "respuesta": "Transacción Pendiente por aprobación del comercio",
                                        "recibo": "3150361111761133683"
                                    }
                                    },
                                    "tokenCard": {
                                    "email": "fenon214@hotmail.com",
                                    "cardTokenId": "8f8c426a78073a19f063f82"
                                    }
                                }
                                }
                                """
                    ),
                    @ExampleObject(name = "Error (validación)",
                        value = """
                                {
                                "success": false,
                                "titleResponse": "Error",
                                "textResponse": "Some fields are required, please correct the errors and try again",
                                "data": {
                                    "errors": [
                                    {"errorMessage": "field value is type string"},
                                    {"errorMessage": "field dues is type string"}
                                    ]
                                }
                                }
                                """
                    )
                }
            )
        )
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        required = true,
        content = @Content(
            mediaType = "application/json",
            examples = {
                @ExampleObject(
                    name = "Primera vez (sin token, con tarjeta)",
                    value = """
                            {
                            "value": "10000.00",
                            "docType": "CC",
                            "docNumber": "10987521867",
                            "name": "Julián Eduardo",
                            "lastName": "Villamizar Peña",
                            "email": "fenon214@hotmail.com",
                            "cellPhone": "3234323296",
                            "phone": "6076830034",
                            "address": "Cra 23 # 33-22, Antonia Santos",
                            "country": "CO",
                            "city": "Bucaramanga",
                            "currency": "COP",
                            "cardNumber": "5176400189059206",
                            "cardExpYear": "2029",
                            "cardExpMonth": "08",
                            "cardCvc": "258",
                            "dues": "2",
                            "testMode": false,
                            "ip": "110.60.95.38"
                            }
                            """
                ),
                @ExampleObject(
                    name = "Siguientes cobros (con token)",
                    value = """
                            {
                            "value": "10000.00",
                            "docType": "CC",
                            "docNumber": "10987521867",
                            "name": "Julián Eduardo",
                            "lastName": "Villamizar Peña",
                            "email": "fenon214@hotmail.com",
                            "cellPhone": "3234323296",
                            "address": "Cra 23 # 33-22, Antonia Santos",
                            "country": "CO",
                            "city": "Bucaramanga",
                            "currency": "COP",
                            "cardTokenId": "ahYQb8SkKSzyM9cmmvC",
                            "customerId": "cst_9rT5Vq1abcde",
                            "dues": "2",
                            "testMode": false
                            }
                            """
                )
            }
        )
    )
    public Mono<JsonNode> createCardPayment(@Valid @RequestBody CardPaymentIn body) {
        // Validación mínima de flujo (primer uso vs siguientes cobros)
        boolean hasToken   = body.getCardTokenId() != null && !body.getCardTokenId().isBlank();
        boolean hasRawCard = body.getCardNumber() != null
                        && body.getCardExpYear() != null
                        && body.getCardExpMonth() != null
                        && body.getCardCvc() != null;

        if (!(hasToken || hasRawCard)) {
            throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.BAD_REQUEST,
                "Debes enviar 'cardTokenId' (siguientes cobros) o los 4 campos de tarjeta (primera vez)"
            );
        }

        return epayco.createCardPayment(body);
    }
}
