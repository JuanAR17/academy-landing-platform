package com.academia.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterIn {
  @NotBlank(message = "El nombre es requerido")
  public String nombre;

  @NotBlank(message = "El apellido es requerido")
  public String apellido;

  @NotBlank(message = "El email es requerido")
  @Email(message = "Email inválido")
  public String correo;

  @NotBlank(message = "El teléfono es requerido")
  public String telefono;

  @NotBlank(message = "La nacionalidad es requerida")
  public String nacionalidad;

  @NotBlank(message = "La dirección es requerida")
  public String direccion;

  @NotBlank(message = "El nombre de usuario es requerido")
  @Size(min = 3, max = 50, message = "El nombre de usuario debe tener entre 3 y 50 caracteres")
  public String username;

  @NotBlank(message = "La contraseña es requerida")
  @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
  public String contrasena;

  public String dondeNosViste; // Campo opcional del select
}
