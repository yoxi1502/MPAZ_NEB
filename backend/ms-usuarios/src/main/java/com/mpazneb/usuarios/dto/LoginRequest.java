package com.mpazneb.usuarios.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Credenciales que envía la pantalla de login de la app. */
public record LoginRequest(

        @NotBlank(message = "Escribe tu correo.")
        @Email(message = "Ingresa un correo válido (ej: asistente@mpaz.cl).")
        String correo,

        @NotBlank(message = "Escribe tu contraseña.")
        @Size(min = 6, max = 60, message = "La contraseña debe tener entre 6 y 60 caracteres.")
        String clave
) {
}
