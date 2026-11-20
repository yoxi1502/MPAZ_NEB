package com.mpazneb.usuarios.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mpazneb.usuarios.dto.ActualizarUsuarioRequest;
import com.mpazneb.usuarios.dto.LoginRequest;
import com.mpazneb.usuarios.dto.UsuarioRequest;
import com.mpazneb.usuarios.dto.UsuarioResponse;
import com.mpazneb.usuarios.exception.CredencialesInvalidasException;
import com.mpazneb.usuarios.exception.CuentaDesactivadaException;
import com.mpazneb.usuarios.exception.RecursoNoEncontradoException;
import com.mpazneb.usuarios.exception.ReglaNegocioException;
import com.mpazneb.usuarios.model.PerfilUsuario;
import com.mpazneb.usuarios.model.Usuario;
import com.mpazneb.usuarios.repository.UsuarioRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

/** Prueba el login y la administración de cuentas con el repositorio y BCrypt simulados. */
@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository repository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService service;

    @Test
    void loginCorrectoDevuelveLaCuentaYRegistraElAcceso() {
        Usuario usuario = usuario(true);
        when(repository.findByCorreoIgnoreCase("asistente@mpaz.cl")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("Asistente123", "hash")).thenReturn(true);
        when(repository.save(usuario)).thenReturn(usuario);

        UsuarioResponse respuesta = service.login(new LoginRequest(" asistente@mpaz.cl ", "Asistente123"));

        assertThat(respuesta.alias()).isEqualTo("Asistente Aula A");
        assertThat(respuesta.ultimoAcceso()).isNotNull();
    }

    @Test
    void loginConClaveIncorrectaLanzaCredencialesInvalidas() {
        when(repository.findByCorreoIgnoreCase("asistente@mpaz.cl")).thenReturn(Optional.of(usuario(true)));
        when(passwordEncoder.matches("otraClave", "hash")).thenReturn(false);

        assertThatThrownBy(() -> service.login(new LoginRequest("asistente@mpaz.cl", "otraClave")))
                .isInstanceOf(CredencialesInvalidasException.class)
                .hasMessage("Correo o contraseña incorrectos.");
        verify(repository, never()).save(any());
    }

    @Test
    void loginConCorreoInexistenteLanzaElMismoError() {
        when(repository.findByCorreoIgnoreCase("nadie@mpaz.cl")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.login(new LoginRequest("nadie@mpaz.cl", "Asistente123")))
                .isInstanceOf(CredencialesInvalidasException.class);
    }

    @Test
    void loginConCuentaDesactivadaLanzaCuentaDesactivada() {
        when(repository.findByCorreoIgnoreCase("asistente@mpaz.cl")).thenReturn(Optional.of(usuario(false)));
        when(passwordEncoder.matches("Asistente123", "hash")).thenReturn(true);

        assertThatThrownBy(() -> service.login(new LoginRequest("asistente@mpaz.cl", "Asistente123")))
                .isInstanceOf(CuentaDesactivadaException.class);
    }

    @Test
    void crearGuardaLaClaveCifradaYElCorreoEnMinusculas() {
        when(repository.existsByCorreoIgnoreCase("nueva@mpaz.cl")).thenReturn(false);
        when(passwordEncoder.encode("Clave123")).thenReturn("hash-nuevo");
        when(repository.save(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));

        UsuarioResponse creada = service.crear(
                new UsuarioRequest("Asistente Aula B", "Nueva@MPAZ.cl", "Clave123", PerfilUsuario.ASISTENTE));

        assertThat(creada.correo()).isEqualTo("nueva@mpaz.cl");
        assertThat(creada.activa()).isTrue();
        verify(repository).save(argThat(u -> "hash-nuevo".equals(u.getClaveHash())));
    }

    @Test
    void crearConCorreoRepetidoLanzaReglaDeNegocio() {
        when(repository.existsByCorreoIgnoreCase("asistente@mpaz.cl")).thenReturn(true);

        assertThatThrownBy(() -> service.crear(
                new UsuarioRequest("Otra", "asistente@mpaz.cl", "Clave123", PerfilUsuario.ASISTENTE)))
                .isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    void actualizarSinClaveMantieneLaAnterior() {
        Usuario usuario = usuario(true);
        when(repository.findById(1L)).thenReturn(Optional.of(usuario));
        when(repository.save(usuario)).thenReturn(usuario);

        UsuarioResponse respuesta = service.actualizar(1L,
                new ActualizarUsuarioRequest("Asistente Aula C", PerfilUsuario.ASISTENTE, false, null));

        assertThat(respuesta.alias()).isEqualTo("Asistente Aula C");
        assertThat(respuesta.activa()).isFalse();
        assertThat(usuario.getClaveHash()).isEqualTo("hash");
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void actualizarConClaveNuevaLaCifra() {
        Usuario usuario = usuario(true);
        when(repository.findById(1L)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.encode("NuevaClave1")).thenReturn("hash-2");
        when(repository.save(usuario)).thenReturn(usuario);

        service.actualizar(1L, new ActualizarUsuarioRequest("Asistente", PerfilUsuario.ASISTENTE, true, "NuevaClave1"));

        assertThat(usuario.getClaveHash()).isEqualTo("hash-2");
    }

    @Test
    void eliminarUnaCuentaInexistenteLanzaNoEncontrado() {
        when(repository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.eliminar(5L)).isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void listarNoExponeLaClave() {
        when(repository.findAllByOrderByAliasAsc()).thenReturn(List.of(usuario(true)));

        assertThat(service.listar()).singleElement()
                .hasFieldOrPropertyWithValue("correo", "asistente@mpaz.cl");
    }

    private Usuario usuario(boolean activa) {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setAlias("Asistente Aula A");
        usuario.setCorreo("asistente@mpaz.cl");
        usuario.setClaveHash("hash");
        usuario.setPerfil(PerfilUsuario.ASISTENTE);
        usuario.setActiva(activa);
        return usuario;
    }
}
