// src/main/java/com/academia/backend/dto/RegisterIn.java
package com.academia.backend.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

public class RegisterIn extends UserDto {

  @NotBlank(message = "La contraseña es requerida")
  @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres")
  @Pattern(
    regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,72}$",
    message = "Debe contener letras y números"
  )
  @JsonAlias({"contrasena"})
  @Schema(description = "Contraseña en texto plano al registrarse", example = "S3gura2025")
  private String password;

  public String getPassword() { return password; }
  public void setPassword(String password) { this.password = password; }
}
