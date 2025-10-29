package com.academia.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.academia.backend.dto.in.CardPaymentIn;
import com.academia.backend.dto.in.PsePaymentIn;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import org.springframework.http.HttpMethod;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class EpaycoClient {

  private final WebClient client;
  private final EpaycoAuthService auth;
  private static final Logger log = LoggerFactory.getLogger(EpaycoClient.class);

  public EpaycoClient(@Qualifier("epaycoWebClient") WebClient baseClient,
                      EpaycoAuthService auth) {
    this.auth = auth;

    this.client = baseClient.mutate()
        .filter((request, next) ->
            auth.getToken().flatMap(tok -> {
              // log seguro (solo primeros 8 chars)
              if (log.isDebugEnabled()) {
                String shown = tok.length() > 12 ? tok.substring(0, 8) + "…" : tok;
                log.debug("-> {} {}  Authorization: Bearer {}", request.method(), request.url(), shown);
              }
              return next.exchange(
                  ClientRequest.from(request)
                      .headers(h -> h.setBearerAuth(tok))
                      .build()
              );
            })
        )
        .build();
  }

  /** Listar bancos disponibles (PSE) */
  public Mono<JsonNode> listBanks() {
    return client.get()
        .uri("/banks")
        .retrieve()
        .bodyToMono(JsonNode.class);
  }

  // ====== TARJETA ======

  /** POST crudo (tarjeta) */
  public Mono<JsonNode> createCardPayment(JsonNode body) {
    return client.post()
        .uri("/payment/process")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(body)
        .retrieve()
        .bodyToMono(JsonNode.class);
  }

  /** Pago con tarjeta (primera vez o con token) */
  public Mono<JsonNode> createCardPayment(CardPaymentIn in) {
    Map<String, Object> body = new LinkedHashMap<>();

    // --- Requeridos ---
    body.put("value", in.getValue());
    body.put("docType", in.getDocType());
    body.put("docNumber", in.getDocNumber());
    body.put("name", in.getName());
    body.put("lastName", in.getLastName());
    body.put("email", in.getEmail());
    body.put("cellPhone", in.getCellPhone());
    if (in.getPhone() != null) body.put("phone", in.getPhone());
    body.put("address", in.getAddress());
    body.put("country", in.getCountry());
    body.put("city", in.getCity());

    // --- Token vs tarjeta cruda ---
    if (in.getCardTokenId() != null && !in.getCardTokenId().isBlank()) {
      body.put("cardTokenId", in.getCardTokenId());
      if (in.getCustomerId() != null) body.put("customerId", in.getCustomerId());
    } else {
      body.put("cardNumber", in.getCardNumber());
      body.put("cardExpYear", in.getCardExpYear());
      body.put("cardExpMonth", in.getCardExpMonth());
      body.put("cardCvc", in.getCardCvc());
    }

    // --- Opcionales ---
    if (in.getCurrency() != null) body.put("currency", in.getCurrency());
    if (in.getDues() != null) body.put("dues", in.getDues());
    if (in.getTestMode() != null) body.put("testMode", in.getTestMode());
    if (in.getIp() != null) body.put("ip", in.getIp());
    if (in.getUrlResponse() != null) body.put("urlResponse", in.getUrlResponse());
    if (in.getUrlConfirmation() != null) body.put("urlConfirmation", in.getUrlConfirmation());
    if (in.getMethodConfirmation() != null) body.put("methodConfirmation", in.getMethodConfirmation());

    if (in.getExtra1() != null) body.put("extra1", in.getExtra1());
    if (in.getExtra2() != null) body.put("extra2", in.getExtra2());
    if (in.getExtra3() != null) body.put("extra3", in.getExtra3());
    if (in.getExtra4() != null) body.put("extra4", in.getExtra4());
    if (in.getExtra5() != null) body.put("extra5", in.getExtra5());
    if (in.getExtra6() != null) body.put("extra6", in.getExtra6());
    if (in.getExtra7() != null) body.put("extra7", in.getExtra7());
    if (in.getExtra8() != null) body.put("extra8", in.getExtra8());
    if (in.getExtra9() != null) body.put("extra9", in.getExtra9());
    if (in.getExtra10() != null) body.put("extra10", in.getExtra10());

    return client.post()
        .uri("/payment/process")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(body)
        .retrieve()
        .bodyToMono(JsonNode.class);
  }

  /** Tipos de documento */
  public Mono<JsonNode> listDocumentTypes() {
    return client.get()
        .uri("/type/documents")
        .accept(MediaType.APPLICATION_JSON)
        .retrieve()
        .bodyToMono(JsonNode.class);
  }

  /** Detalle de transacción (GET con body según doc) */
  public Mono<JsonNode> getTransactionDetail(String referencePayco) {
    Map<String, Object> payload = Map.of(
        "filter", Map.of("referencePayco", referencePayco)
    );

    return client
        .method(HttpMethod.GET)
        .uri("/transaction/detail")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(payload)
        .retrieve()
        .bodyToMono(JsonNode.class);
  }

  /** Métodos de pago disponibles */
  public Mono<JsonNode> listPaymentMethods() {
    return client.get()
        .uri("/transaction/payment/methods")
        .retrieve()
        .bodyToMono(JsonNode.class);
  }

  // ====== PSE ======

  /** POST crudo (PSE) */
  public Mono<JsonNode> createPsePayment(JsonNode body) {
    return client.post()
        .uri("/payment/process/pse")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(body)
        .retrieve()
        .bodyToMono(JsonNode.class);
  }

  /** Pago PSE con DTO tipado */
  public Mono<JsonNode> createPsePayment(PsePaymentIn in, String clientIp) {
    Map<String, Object> body = new LinkedHashMap<>();

    // ----- OBLIGATORIOS -----
    body.put("bank", in.getBank());
    body.put("value", in.getValue() == null ? null : in.getValue().toPlainString());
    body.put("docType", in.getDocType());
    body.put("docNumber", in.getDocNumber());
    body.put("name", in.getName());
    body.put("email", in.getEmail());
    body.put("cellPhone", in.getCellPhone());
    body.put("address", in.getAddress());
    body.put("ip", clientIp);               // IP desde backend (seguridad)
    body.put("urlResponse", in.getUrlResponse());

    // ----- OPCIONALES (solo si vienen) -----
    if (in.getLastName() != null && !in.getLastName().isBlank()) body.put("lastName", in.getLastName());
    if (in.getPhone() != null) body.put("phone", in.getPhone());
    if (in.getDescription() != null) body.put("description", in.getDescription());
    if (in.getInvoice() != null && !in.getInvoice().isBlank()) body.put("invoice", in.getInvoice());
    body.put("currency", in.getCurrency() == null ? "COP" : in.getCurrency());
    if (in.getTypePerson() != null) body.put("typePerson", in.getTypePerson());
    if (in.getUrlConfirmation() != null) body.put("urlConfirmation", in.getUrlConfirmation());
    if (in.getMethodConfirmation() != null) {
      body.put("methodConfirmation", in.getMethodConfirmation());
      // Compatibilidad con typo de doc oficial
      body.put("methodConfimation", in.getMethodConfirmation());
    }
    if (Boolean.TRUE.equals(in.getTestMode())) body.put("testMode", true);

    // Extras 1..10
    if (in.getExtra1() != null) body.put("extra1", in.getExtra1());
    if (in.getExtra2() != null) body.put("extra2", in.getExtra2());
    if (in.getExtra3() != null) body.put("extra3", in.getExtra3());
    if (in.getExtra4() != null) body.put("extra4", in.getExtra4());
    if (in.getExtra5() != null) body.put("extra5", in.getExtra5());
    if (in.getExtra6() != null) body.put("extra6", in.getExtra6());
    if (in.getExtra7() != null) body.put("extra7", in.getExtra7());
    if (in.getExtra8() != null) body.put("extra8", in.getExtra8());
    if (in.getExtra9() != null) body.put("extra9", in.getExtra9());
    if (in.getExtra10() != null) body.put("extra10", in.getExtra10());

    return client.post()
        .uri("/payment/process/pse")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(body)
        .retrieve()
        .bodyToMono(JsonNode.class);
  }

  //--------------------------------------Confirmar Transacción PSE------------------------------
  /** Confirmar transacción PSE: POST /payment/pse/transaction */
  public Mono<JsonNode> confirmPseTransaction(Long transactionId) {
    Map<String, Object> body = Map.of("transactionID", transactionId);
    return client.post()
        .uri("/payment/pse/transaction")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(body)
        .retrieve()
        .bodyToMono(JsonNode.class);
  }

}
