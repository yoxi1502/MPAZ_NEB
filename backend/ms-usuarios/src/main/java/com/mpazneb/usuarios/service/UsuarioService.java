package com.mpazneb.usuarios.service;

import com.mpazneb.usuarios.dto.ActualizarUsuarioRequest;
import com.mpazneb.usuarios.dto.LoginRequest;
import com.mpazneb.usuarios.dto.UsuarioRequest;
import com.mpazneb.usuarios.dto.UsuarioResponse;
import com.mpazneb.usuarios.exception.CredencialesInvalidasException;
import com.mpazneb.usuarios.exception.CuentaDesactivadaException;
import com.mpazneb.usuarios.exception.RecursoNoEncontradoException;
import com.mpazneb.usuarios.exception.ReglaNegocioException;
import com.mpazneb.usuarios.mapper.UsuarioMapper;
import com.mpazneb.usuarios.model.Usuario;
import com.mpazneb.usuarios.repository.UsuarioRepository;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Login y administración de las cuentas del equipo pedagógico. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UsuarioService {

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Compara la clave con el hash BCrypt guardado. Si es correcta y la cuenta
     * está activa, registra el último acceso y devuelve la cuenta (sin la clave).
     */
    @Transactional
    public UsuarioResponse login(LoginRequest request) {
        Usuario usuario = repository.findByCorreoIgnoreCase(request.correo().trim())
                .filter(u -> passwordEncoder.matches(request.clave(), u.getClaveHash()))
                .orElseThrow(CredencialesInvalidasException::new);

        if (!usuario.isActiva()) {
            throw new CuentaDesactivadaException();
        }
        usuario.setUltimoAcceso(Instant.now());
        return UsuarioMapper.aResponse(repository.save(usuario));
    }

    public List<UsuarioResponse> listar() {
        return repository.findAllByOrderByAliasAsc().stream()
                .map(UsuarioMapper::aResponse)
                .toList();
    }

    public UsuarioResponse obtener(Long id) {
        return UsuarioMapper.aResponse(buscarPorId(id));
    }

    @Transactional
    public UsuarioResponse crear(UsuarioRequest request) {
        String correo = request.correo().trim().toLowerCase(Locale.ROOT);
        if (repository.existsByCorreoIgnoreCase(correo)) {
            throw new ReglaNegocioException("Ya existe una cuenta con ese correo.");
        }
        Usuario usuario = new Usuario();
        usuario.setAlias(request.alias().trim());
        usuario.setCorreo(correo);
        usuario.setClaveHash(passwordEncoder.encode(request.clave()));
        usuario.setPerfil(request.perfil());
        usuario.setActiva(true);
        return UsuarioMapper.aResponse(repository.save(usuario));
    }

    @Transactional
    public UsuarioResponse actualizar(Long id, ActualizarUsuarioRequest request) {
        Usuario usuario = buscarPorId(id);
        usuario.setAlias(request.alias().trim());
        usuario.setPerfil(request.perfil());
        usuario.setActiva(request.activa());
        if (request.clave() != null && !request.clave().isBlank()) {
            usuario.setClaveHash(passwordEncoder.encode(request.clave()));
        }
        return UsuarioMapper.aResponse(repository.save(usuario));
    }

    @Transactional
    public void eliminar(Long id) {
        repository.delete(buscarPorId(id));
    }

    private Usuario buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró la cuenta con id " + id + "."));
    }
}
