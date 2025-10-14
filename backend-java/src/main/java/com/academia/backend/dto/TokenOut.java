package com.academia.backend.dto;

public class TokenOut {
  public String access_token;
  public String token_type = "bearer";
  public String csrf_token;

  public TokenOut(String access, String csrf) {
    this.access_token = access; this.csrf_token = csrf;
  }
}

