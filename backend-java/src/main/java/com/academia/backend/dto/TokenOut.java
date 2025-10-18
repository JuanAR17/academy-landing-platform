package com.academia.backend.dto;

import java.util.UUID;

public class TokenOut {
  public String access_token;
  public String token_type = "bearer";
  public String csrf_token;
  public UUID user_id;

  public TokenOut(String access, String csrf, UUID userId) {
    this.access_token = access; this.csrf_token = csrf; this.user_id = userId;
  }
}

