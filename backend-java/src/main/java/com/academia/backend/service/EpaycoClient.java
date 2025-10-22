package com.academia.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class EpaycoClient {

  private final WebClient client;
  private final EpaycoAuthService auth;

  public EpaycoClient(@Qualifier("epaycoWebClient") WebClient baseClient,
                      EpaycoAuthService auth) {
    this.auth = auth;

    // Derivamos el WebClient base y le añadimos un filtro que inyecta el Bearer token
    this.client = baseClient.mutate()
        .filter((request, next) ->
            auth.getToken().flatMap(tok ->
                next.exchange(
                    ClientRequest.from(request)
                        .headers(h -> h.setBearerAuth(tok))
                        .build()
                )
            )
        )
        .build();
  }

  /** Listar bancos disponibles */
  public Mono<JsonNode> listBanks() {
    return client.get()
        .uri("/banks") // <-- corregido
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

  /** Pago con tarjeta */
  public Mono<JsonNode> createCardPayment(JsonNode body) {
    return client.post()
        .uri("/payment/process")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(body)
        .retrieve()
        .bodyToMono(JsonNode.class);
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
        .uri("/payment/methods")
        .retrieve()
        .bodyToMono(JsonNode.class);
  }
}
