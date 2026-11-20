package com.mpazneb.usuarios.dto;

import com.mpazneb.usuarios.model.PerfilUsuario;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Datos para editar una cuenta. El correo no se cambia (identifica la cuenta)
 * y la contraseña es opcional: si viene vacía, se mantiene la anterior.
 */
public record ActualizarUsuarioRequest(

        @NotBlank(message = "Escribe el nombre visible de la cuenta.")
        @Size(min = 3, max = 60, message = "El nombre debe tener entre 3 y 60 caracteres.")
        String alias,

        @NotNull(message = "Elige el perfil.")
        PerfilUsuario perfil,

        @NotNull(message = "Indica si la cuenta está activa.")
        Boolean activa,

        @Size(min = 6, max = 60, message = "La contraseña debe tener entre 6 y 60 caracteres.")
        String clave
) {
}
