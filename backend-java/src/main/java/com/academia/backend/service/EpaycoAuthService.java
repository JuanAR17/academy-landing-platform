package com.academia.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class EpaycoAuthService {

    private static final Logger logger = LoggerFactory.getLogger(EpaycoAuthService.class);

    private final WebClient http;
    private final com.academia.backend.config.EpaycoProperties props;

    private final AtomicReference<String> cachedToken = new AtomicReference<>(null);
    private volatile Instant expiresAt = Instant.EPOCH; // vence en el pasado por defecto

    public EpaycoAuthService(WebClient.Builder builder, com.academia.backend.config.EpaycoProperties props) {
        this.http = builder.baseUrl(props.getBaseUrl()).build();
        this.props = props;
    }

    public Mono<String> getToken() {
        logger.info("Verificando si hay token válido en caché");
        // Si tenemos token válido, úsalo
        if (cachedToken.get() != null && Instant.now().isBefore(expiresAt)) {
            logger.info("Usando token de caché");
            return Mono.just(cachedToken.get());
        }
        logger.info("No hay token válido, realizando petición de login a ePayco");
        // Si no, login usando Basic Auth
        String credentials = props.getPublicKey() + ":" + props.getPrivateKey();
        String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());

        return http.post()
                .uri("/login")
                .header("Authorization", "Basic " + encodedCredentials)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{}") // Body vacío ya que la auth va en el header
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(json -> {
                    logger.info("Respuesta de login recibida: {}", json);
                    // ePayco responde { "token": "..." }
                    String token = getFirstNonNull(
                            json.path("token").asText(null),
                            json.path("data").path("token").asText(null),
                            json.path("access_token").asText(null));
                    if (token == null) {
                        logger.error("No se pudo leer el token de la respuesta: {}", json);
                        throw new IllegalStateException("No se pudo leer el token de la respuesta de login: " + json);
                    }
                    logger.info("Token obtenido exitosamente");
                    cachedToken.set(token);

                    // Si el token es JWT y trae exp (opcional):
                    try {
                        String[] parts = token.split("\\.");
                        if (parts.length == 3) {
                            String payloadJson = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
                            JsonNode payload = JsonMapper.builder().build().readTree(payloadJson);
                            long exp = payload.path("exp").asLong(0);
                            if (exp > 0) {
                                // refrescar 60s antes de expirar
                                expiresAt = Instant.ofEpochSecond(exp).minusSeconds(60);
                            } else {
                                expiresAt = Instant.now().plusSeconds(25 * 60); // fallback: 25 min
                            }
                        } else {
                            expiresAt = Instant.now().plusSeconds(25 * 60);
                        }
                    } catch (Exception e) {
                        expiresAt = Instant.now().plusSeconds(25 * 60);
                    }

                    return token;
                });
    }

    private static String getFirstNonNull(String... vals) {
        for (String v : vals)
            if (v != null && !v.isBlank())
                return v;
        return null;
    }
}