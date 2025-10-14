package com.academia.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class LoginIn {
  @Email public String email;
  @NotBlank public String password;
}

