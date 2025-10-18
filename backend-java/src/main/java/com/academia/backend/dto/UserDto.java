package com.academia.backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

public class UserDto {
    @JsonIgnore // No se envía en requests
    public UUID id;

    @Email(message = "Email inválido")
    public String correo;

    @Size(min = 3, max = 50, message = "El nombre de usuario debe tener entre 3 y 50 caracteres")
    public String username;

    public String nombre;

    public String apellido;

    public String telefono;

    public String nacionalidad;

    public String direccion;

    public String dondeNosViste;

    public Boolean isAdmin;

    @JsonIgnore
    public Instant createdAt;
}