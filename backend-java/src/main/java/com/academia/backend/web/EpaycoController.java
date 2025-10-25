package com.academia.backend.web;

import com.academia.backend.dto.in.CardPaymentIn;
import com.academia.backend.service.EpaycoAuthService;
import com.academia.backend.service.EpaycoClient;
import com.academia.backend.domain.EpaycoCardEntity;
import com.academia.backend.domain.EpaycoCustomerEntity;
import com.academia.backend.domain.EpaycoTransactionEntity;
import com.academia.backend.repo.EpaycoCardRepo;
import com.academia.backend.repo.EpaycoCustomerRepo;
import com.academia.backend.repo.EpaycoTransactionRepo;
import com.academia.backend.service.JwtService;
import com.academia.backend.service.CheckoutUserService;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@RestController
@RequestMapping("/api/epayco")
@Tag(name = "ePayco", description = "Integración con pasarela de pagos ePayco")
public class EpaycoController {

    // deps
    private final EpaycoClient epayco;
    private final EpaycoAuthService auth;
    private final ObjectMapper mapper;
    private final EpaycoTransactionRepo trxRepo;
    private final EpaycoCardRepo cardRepo;
    private final EpaycoCustomerRepo customerRepo;
    private final JwtService jwtService; // para extraer userId del JWT
    private final CheckoutUserService checkoutUserService; // registro implícito

    public EpaycoController(
            EpaycoClient epayco,
            EpaycoAuthService auth,
            ObjectMapper mapper,
            EpaycoTransactionRepo trxRepo,
            EpaycoCardRepo cardRepo,
            EpaycoCustomerRepo customerRepo,
            JwtService jwtService,
            CheckoutUserService checkoutUserService
    ) {
        this.epayco = epayco;
        this.auth = auth;
        this.mapper = mapper;
        this.trxRepo = trxRepo;
        this.cardRepo = cardRepo;
        this.customerRepo = customerRepo;
        this.jwtService = jwtService;
        this.checkoutUserService = checkoutUserService;
    }

    private static Instant parseEpaycoDate(String yyyyMMddHHmmss) {
        if (yyyyMMddHHmmss == null || yyyyMMddHHmmss.isBlank()) return null;
        var fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return LocalDateTime.parse(yyyyMMddHHmmss, fmt).atZone(ZoneId.systemDefault()).toInstant();
    }

    // ---------- Catálogos / utilidades ----------
    @GetMapping("/banks")
    @Operation(summary = "Listar bancos")
    public Mono<JsonNode> banks() { return epayco.listBanks(); }

    @GetMapping("/payment-methods")
    @Operation(summary = "Listar métodos de pago")
    public Mono<JsonNode> listPaymentMethods() { return epayco.listPaymentMethods(); }

    @GetMapping("/payment/{referencePayco}/status")
    @Operation(
        summary = "Consultar estado de pago",
        description = "Consulta detalle en ePayco con GET /transaction/detail (el Bearer se inyecta automáticamente)."
    )
    @ApiResponses(@ApiResponse(responseCode = "200", description = "OK",
        content = @Content(mediaType = "application/json",
            examples = @ExampleObject(name = "Ejemplo éxito", value = """
                {
                  "success": true,
                  "titleResponse": "Successful consult",
                  "data": {
                    "transaction": { "status": true, "object": "payment",
                      "data": { "ref_payco": 315160539, "estado": "Aprobada" } }
                  }
                }
            """))))
    public Mono<JsonNode> getPaymentStatus(@PathVariable String referencePayco) {
        return epayco.getTransactionDetail(referencePayco);
    }

    @GetMapping("/document-types")
    @Operation(summary = "Listar tipos de documentos",
        description = "Proxy a {{url_apify}}/type/documents (Bearer manejado en backend).")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json"))
    public Mono<JsonNode> listDocumentTypes() { return epayco.listDocumentTypes(); }

    // ---------- Pago con tarjeta ----------
    @PostMapping("/payment")
    @Operation(
        summary = "Procesar pago con tarjeta de crédito/débito (/payment/process)",
        description = "El backend obtiene/renueva el Bearer con /login y lo envía automáticamente.\n" +
                      "• Primera vez (guest o logueado): tarjeta cruda (cardNumber, cardExpYear, cardExpMonth, cardCvc). " +
                      "Si no hay sesión, se crea el usuario con los datos del checkout y se asocia el pago.\n" +
                      "• Siguientes cobros (tokenizados): cardTokenId (+ opcional customerId) — **requiere sesión**."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Respuesta de ePayco (éxito o error)",
            content = @Content(mediaType = "application/json", examples = {
                @ExampleObject(name = "Éxito (resumen)", value = """
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
                """),
                @ExampleObject(name = "Error (validación)", value = """
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
                """)
            }))
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        required = true,
        content = @Content(mediaType = "application/json", examples = {
            @ExampleObject(name = "Primera vez (sin token, con tarjeta)", value = """
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
            """),
            @ExampleObject(name = "Siguientes cobros (con token)", value = """
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
            """)
        })
    )
    public Mono<JsonNode> createCardPayment(
            @Valid @RequestBody CardPaymentIn body,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        // 1) JWT opcional
        UUID jwtUserId = jwtService.extractUserIdFromHeader(authHeader);

        // 2) Validación de flujo mínimo
        boolean hasToken   = body.getCardTokenId() != null && !body.getCardTokenId().isBlank();
        boolean hasRawCard = body.getCardNumber() != null
                && body.getCardExpYear() != null
                && body.getCardExpMonth() != null
                && body.getCardCvc() != null;

        if (hasToken && jwtUserId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                "Debes iniciar sesión para usar una tarjeta tokenizada");
        }
        if (!(hasToken || hasRawCard)) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Debes enviar 'cardTokenId' (siguientes cobros) o los 4 campos de tarjeta (primera vez)"
            );
        }

        // 3) userId definitivo (final) para usar dentro del map(...)
        final UUID userId = (jwtUserId != null)
                ? jwtUserId
                : checkoutUserService.ensureUserForCheckout(body);

        // 4) Llamar a ePayco
        JsonNode payload = mapper.valueToTree(body);

        return epayco.createCardPayment(payload)
            // Persistencia (JPA) en boundedElastic
            .publishOn(Schedulers.boundedElastic())
            .map(resp -> {
                // ---- Guardar token/cliente si viene ----
                try {
                    JsonNode token = resp.path("data").path("tokenCard");
                    if (!token.isMissingNode() && token.hasNonNull("cardTokenId")) {
                        String cardTokenId = token.path("cardTokenId").asText();
                        String email       = token.path("email").asText(null);
                        String customerId  = token.path("customerId").asText(null); // a veces "N/A"

                        if (customerId != null && !"N/A".equalsIgnoreCase(customerId)) {
                            customerRepo.findByCustomerId(customerId).orElseGet(() -> {
                                var c = new EpaycoCustomerEntity();
                                c.setUserId(userId);
                                c.setCustomerId(customerId);
                                c.setEmail(email);
                                return customerRepo.save(c);
                            });
                        }

                        var card = cardRepo.findByCardTokenId(cardTokenId).orElseGet(EpaycoCardEntity::new);
                        card.setUserId(userId);
                        card.setCardTokenId(cardTokenId);

                        // metadatos si vino tarjeta cruda
                        if (hasRawCard) {
                            String num = body.getCardNumber();
                            if (num != null && num.length() >= 4) {
                                card.setLast4(num.substring(num.length() - 4));
                            }
                            card.setExpMonth(body.getCardExpMonth());
                            card.setExpYear(body.getCardExpYear());
                            String holder = (body.getName() == null ? "" : body.getName()) + " " +
                                            (body.getLastName() == null ? "" : body.getLastName());
                            card.setHolderName(holder.trim());
                        }
                        // franquicia desde la transacción
                        String brand = resp.path("data").path("transaction").path("data").path("franquicia").asText(null);
                        if (brand != null && !brand.isBlank()) card.setBrand(brand);

                        cardRepo.save(card);
                    }
                } catch (DataAccessException ignored) {
                    // No romper el flujo por fallos no críticos de persistencia
                }

                // ---- Guardar transacción (éxito o rechazo) ----
                try {
                    JsonNode t = resp.path("data").path("transaction").path("data");
                    if (!t.isMissingNode() && t.has("ref_payco")) {
                        var trx = new EpaycoTransactionEntity();
                        trx.setUserId(userId);
                        trx.setRefPayco(t.path("ref_payco").asLong());
                        trx.setInvoice(t.path("factura").asText(null));
                        trx.setDescription(t.path("descripcion").asText(null));
                        trx.setAmount(new BigDecimal(t.path("valorneto").asText("0")));
                        trx.setCurrency(t.path("moneda").asText(null));
                        trx.setTax(new BigDecimal(t.path("iva").asText("0")));
                        trx.setIco(new BigDecimal(t.path("ico").asText("0")));
                        trx.setBaseTax(new BigDecimal(t.path("baseiva").asText("0")));
                        trx.setBank(t.path("banco").asText(null));
                        trx.setStatus(t.path("estado").asText(null));
                        trx.setResponse(t.path("respuesta").asText(null));
                        trx.setReceipt(t.path("recibo").asText(null));
                        trx.setTxnDate(parseEpaycoDate(t.path("fecha").asText(null)));
                        trx.setFranchise(t.path("franquicia").asText(null));
                        trx.setCodeResponse(t.path("cod_respuesta").asInt());
                        trx.setCodeError(t.path("cod_error").asText(null));
                        trx.setIp(t.path("ip").asText(null));
                        trx.setTestMode(t.path("enpruebas").asInt(0) == 1);

                        // datos del comprador del body
                        trx.setDocType(body.getDocType());
                        trx.setDocNumber(body.getDocNumber());
                        trx.setFirstNames(body.getName());
                        trx.setLastNames(body.getLastName());
                        trx.setEmail(body.getEmail());
                        trx.setCity(body.getCity());
                        trx.setAddress(body.getAddress());
                        trx.setCountryIso2(body.getCountry());

                        trx.setRawPayload(resp.toString());
                        trxRepo.save(trx);
                    }
                } catch (DataAccessException ignored) {
                    // No rompas la respuesta de ePayco por un fallo de auditoría
                }

                return resp; // devolver SIEMPRE la respuesta de ePayco
            });
    }
}
