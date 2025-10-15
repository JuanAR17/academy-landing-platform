package com.academia.backend.dto;

import jakarta.validation.constraints.NotBlank;

public class LoginIn {
  @NotBlank(message = "Email o nombre de usuario requerido")
  public String identifier; // Puede ser email o username
  
  @NotBlank(message = "Contraseña requerida")
  public String password;
}

