//Aquí funciona


package com.academia.backend.service;

import com.academia.backend.config.EpaycoProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class EpaycoAuthService {
  private static final Logger log = LoggerFactory.getLogger(EpaycoAuthService.class);

  private final WebClient client;
  private final EpaycoProperties props;

  private final AtomicReference<String> cachedToken = new AtomicReference<>();
  private volatile Instant expiresAt = Instant.EPOCH;

  public EpaycoAuthService(@Qualifier("epaycoWebClient") WebClient client,
                           EpaycoProperties props) {
    this.client = client;
    this.props = props;
  }

  public Mono<String> getToken() {
    // 1) devolver de caché si sigue vigente
    if (Instant.now().isBefore(expiresAt)) {
      String t = cachedToken.get();
      if (t != null && !t.isBlank()) {
        return Mono.just(t);
      }
    }

    // 2) pedir a ePayco con Basic Auth
    return client.post()
        .uri("/login")
        .headers(h -> h.setBasicAuth(props.getPublicKey(), props.getPrivateKey()))
        .retrieve()
        .onStatus(HttpStatusCode::isError, r ->
            r.bodyToMono(String.class)
             .map(body -> new IllegalStateException("ePayco /login " + r.statusCode() + " -> " + body))
        )
        .bodyToMono(JsonNode.class)
        .map(json -> {
          // algunas respuestas vienen en token, otras en data.token o access_token
          String token = first(
              json.path("token").asText(null),
              json.path("data").path("token").asText(null),
              json.path("access_token").asText(null)
          );
          if (token == null) {
            throw new IllegalStateException("Respuesta sin token: " + json);
          }
          cacheWithExpIfJwt(token);
          return token;
        });
  }

  private static String first(String... vals) {
    for (String v : vals) if (v != null && !v.isBlank()) return v;
    return null;
  }

  private void cacheWithExpIfJwt(String token) {
    cachedToken.set(token);
    try {
      String[] parts = token.split("\\.");
      if (parts.length == 3) {
        String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
        long exp = JsonMapper.builder().build().readTree(payload).path("exp").asLong(0);
        expiresAt = (exp > 0)
            ? Instant.ofEpochSecond(exp).minusSeconds(60)  // refrescar 60s antes
            : Instant.now().plusSeconds(25 * 60);
      } else {
        expiresAt = Instant.now().plusSeconds(25 * 60);
      }
    } catch (Exception ignore) {
      expiresAt = Instant.now().plusSeconds(25 * 60);
    }
  }
}
