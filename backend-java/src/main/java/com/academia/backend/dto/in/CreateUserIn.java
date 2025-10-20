package com.academia.backend.dto.in;

import com.academia.backend.domain.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateUserIn(
        @NotBlank(message = "El email es requerido") @Email(message = "Email inválido") String email,

        @NotBlank(message = "El nombre de usuario es requerido") @Size(min = 3, max = 50, message = "El nombre de usuario debe tener entre 3 y 50 caracteres") String username,

        @NotBlank(message = "La contraseña es requerida") @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres") String password,

        @NotBlank(message = "El nombre es requerido") String firstName,

        String lastName,

        String phone,

        String nationality,

        @NotNull(message = "El rol es requerido") Role role) {
}