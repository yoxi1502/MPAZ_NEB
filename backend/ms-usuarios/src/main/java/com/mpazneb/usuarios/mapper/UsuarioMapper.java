package com.mpazneb.usuarios.mapper;

import com.mpazneb.usuarios.dto.UsuarioResponse;
import com.mpazneb.usuarios.model.Usuario;

/** Convierte la entidad en el DTO de respuesta (sin la contraseña). */
public final class UsuarioMapper {

    private UsuarioMapper() {
    }

    public static UsuarioResponse aResponse(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getAlias(),
                usuario.getCorreo(),
                usuario.getPerfil(),
                usuario.isActiva(),
                usuario.getUltimoAcceso()
        );
    }
}
