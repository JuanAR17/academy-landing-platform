package com.academia.backend.dto;

import java.util.UUID;

public class TokenOut {
  private String accessToken;
  private String tokenType = "bearer";
  private String csrfToken;
  private UUID userId;

  public TokenOut(String access, String csrf, UUID userId) {
    this.accessToken = access; this.csrfToken = csrf; this.userId = userId;
  }

  public String getAccessToken() { return accessToken; }
  public String getTokenType() { return tokenType; }
  public String getCsrfToken() { return csrfToken; }
  public UUID getUserId() { return userId; }
}

