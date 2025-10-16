package com.academia.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ChangePasswordIn {
  @NotBlank(message = "La contraseña actual es requerida")
  public String currentPassword;
  
  @NotBlank(message = "La nueva contraseña es requerida")
  @Size(min = 8, message = "La nueva contraseña debe tener al menos 8 caracteres")
  public String newPassword;
}
