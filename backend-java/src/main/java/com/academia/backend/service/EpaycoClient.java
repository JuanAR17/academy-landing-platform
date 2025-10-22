//Aquí funciona

package com.academia.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.academia.backend.dto.in.CardPaymentIn;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

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

  /** Listar bancos disponibles */
  public Mono<JsonNode> listBanks() {
    return client.get()
        .uri("/banks")
        .retrieve()
        .bodyToMono(JsonNode.class);
  }

  /** Crear enlace/cobro (ajusta body según tu caso) */
  public Mono<JsonNode> createCharge(JsonNode body) {
    return client.post()
        .uri("/payment/link/create")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(body)
        .retrieve()
        .bodyToMono(JsonNode.class);
  }

  /** Pago con tarjeta (primera vez con datos de tarjeta o siguientes con cardTokenId) */
  public Mono<JsonNode> createCardPayment(CardPaymentIn in) {
    Map<String, Object> body = new LinkedHashMap<>();

    // --- Requeridos siempre (tal como los pide ePayco) ---
    body.put("value", in.getValue());               // STRING
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

    // --- Primera vez (sin token) vs siguientes cobros ---
    if (in.getCardTokenId() != null && !in.getCardTokenId().isBlank()) {
      body.put("cardTokenId", in.getCardTokenId());
      if (in.getCustomerId() != null) body.put("customerId", in.getCustomerId());
    } else {
      // Datos de tarjeta cruda (no recomendado en producción sin PCI)
      body.put("cardNumber", in.getCardNumber());
      body.put("cardExpYear", in.getCardExpYear());
      body.put("cardExpMonth", in.getCardExpMonth());
      body.put("cardCvc", in.getCardCvc());
    }

    // --- Opcionales según doc ---
    if (in.getCurrency() != null) body.put("currency", in.getCurrency());
    if (in.getDues() != null) body.put("dues", in.getDues());     // STRING
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
        // Nota: ePayco devuelve 200 incluso con errores (success=false).
        // Aquí devolvemos el JSON crudo para que el controlador/cliente lo muestre.
  }

  /** Estado de un pago */
  public Mono<JsonNode> getPaymentStatus(String transactionId) {
    return client.get()
        .uri("/payment/status/{transactionId}", transactionId)
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
}
