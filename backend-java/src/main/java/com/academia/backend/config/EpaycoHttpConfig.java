package com.academia.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class EpaycoHttpConfig {

  @Bean("epaycoWebClient")
  public WebClient epaycoWebClient(EpaycoProperties props) {
    return WebClient.builder()
        .baseUrl(props.getBaseUrl()) // p.ej. https://apify.epayco.co
        .exchangeStrategies(
            ExchangeStrategies.builder()
                .codecs(c -> c.defaultCodecs().maxInMemorySize(2 * 1024 * 1024))
                .build()
        )
        .build();
  }
}
