package com.mpazneb.usuarios.config;

import com.mpazneb.usuarios.model.PerfilUsuario;
import com.mpazneb.usuarios.model.Usuario;
import com.mpazneb.usuarios.repository.UsuarioRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Si la tabla está vacía crea las 2 cuentas ficticias de la demostración.
 * Las claves se guardan cifradas con BCrypt, igual que una cuenta nueva.
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "mpaz.datos-iniciales.habilitado", havingValue = "true", matchIfMissing = true)
public class DatosIniciales implements CommandLineRunner {

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (repository.count() > 0) {
            return;
        }
        repository.saveAll(List.of(
                crear("Asistente Aula A", "asistente@mpaz.cl", "Asistente123", PerfilUsuario.ASISTENTE),
                crear("Docente UTP", "utp@mpaz.cl", "Docente123", PerfilUsuario.DOCENTE_UTP)
        ));
    }

    private Usuario crear(String alias, String correo, String clave, PerfilUsuario perfil) {
        Usuario usuario = new Usuario();
        usuario.setAlias(alias);
        usuario.setCorreo(correo);
        usuario.setClaveHash(passwordEncoder.encode(clave));
        usuario.setPerfil(perfil);
        usuario.setActiva(true);
        return usuario;
    }
}
