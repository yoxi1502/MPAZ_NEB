package com.mpazneb.usuarios.dto;

import com.mpazneb.usuarios.model.PerfilUsuario;
import java.time.Instant;

/** Cuenta tal como la ve la app. Nunca incluye la contraseña ni su hash. */
public record UsuarioResponse(
        Long id,
        String alias,
        String correo,
        PerfilUsuario perfil,
        boolean activa,
        Instant ultimoAcceso
) {
}
