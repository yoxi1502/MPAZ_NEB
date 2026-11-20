package com.mpazneb.usuarios.dto;

import com.mpazneb.usuarios.model.PerfilUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Datos para crear una cuenta del equipo pedagógico. */
public record UsuarioRequest(

        @NotBlank(message = "Escribe el nombre visible de la cuenta.")
        @Size(min = 3, max = 60, message = "El nombre debe tener entre 3 y 60 caracteres.")
        String alias,

        @NotBlank(message = "Escribe el correo.")
        @Email(message = "Ingresa un correo válido.")
        @Size(max = 100, message = "El correo admite hasta 100 caracteres.")
        String correo,

        @NotBlank(message = "Escribe la contraseña.")
        @Size(min = 6, max = 60, message = "La contraseña debe tener entre 6 y 60 caracteres.")
        String clave,

        @NotNull(message = "Elige el perfil.")
        PerfilUsuario perfil
) {
}
