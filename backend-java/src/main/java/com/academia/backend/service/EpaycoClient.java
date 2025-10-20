package com.academia.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class EpaycoClient {

    private final WebClient client;
    private final EpaycoAuthService auth;
    private final com.academia.backend.config.EpaycoProperties props;

    public EpaycoClient(WebClient.Builder builder, EpaycoAuthService auth,
            com.academia.backend.config.EpaycoProperties props) {
        this.auth = auth;
        this.props = props;
        // Filtro que inyecta el Bearer token en cada request
        ExchangeFilterFunction bearer = (request, next) -> auth.getToken()
                .flatMap(tok -> next.exchange(ClientRequest.from(request)
                        .headers(h -> h.set(HttpHeaders.AUTHORIZATION, "Bearer " + tok))
                        .build()));

        this.client = builder
                .baseUrl(props.getBaseUrl())
                .filter(bearer)
                .build();
    }

    /** Ejemplo: listar bancos */
    public Mono<JsonNode> listBanks() {
        return client.get()
                .uri("/bank")
                .retrieve()
                .bodyToMono(JsonNode.class)
                .onErrorResume(e -> {
                    System.err.println("Error calling ePayco API: " + e.getMessage());
                    return Mono.just(
                            JsonMapper.builder().build().createObjectNode().put("error", "Error interno del servidor"));
                });
    }

    /** Ejemplo: crear un cobro/intent, ajusta ruta/body según tu caso */
    public Mono<JsonNode> createCharge(JsonNode body) {
        return client.post()
                .uri("/payment/link/create")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(JsonNode.class);
    }

    /** Crear un pago con tarjeta */
    public Mono<JsonNode> createCardPayment(JsonNode body) {
        return client.post()
                .uri("/payment/process")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(JsonNode.class);
    }

    /** Consultar estado de un pago */
    public Mono<JsonNode> getPaymentStatus(String transactionId) {
        return client.get()
                .uri("/payment/status/{transactionId}", transactionId)
                .retrieve()
                .bodyToMono(JsonNode.class);
    }

    /** Listar métodos de pago disponibles */
    public Mono<JsonNode> listPaymentMethods() {
        return client.get()
                .uri("/payment/methods")
                .retrieve()
                .bodyToMono(JsonNode.class);
    }
}