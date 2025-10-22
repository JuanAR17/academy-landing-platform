package com.academia.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;

@Component
@ConfigurationProperties(prefix = "epayco")
@Validated
public class EpaycoProperties {
    @NotBlank
    private String baseUrl;

    @NotBlank
    private String publicKey;   // username (Basic Auth)

    @NotBlank
    private String privateKey;  // password (Basic Auth)

    // getters/setters
    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }

    public String getPublicKey() { return publicKey; }
    public void setPublicKey(String publicKey) { this.publicKey = publicKey; }

    public String getPrivateKey() { return privateKey; }
    public void setPrivateKey(String privateKey) { this.privateKey = privateKey; }
}
