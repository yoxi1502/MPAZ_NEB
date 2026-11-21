package com.mpazneb.usuarios.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.mpazneb.usuarios.dto.UsuarioResponse;
import com.mpazneb.usuarios.exception.RecursoNoEncontradoException;
import com.mpazneb.usuarios.exception.ReglaNegocioException;
import com.mpazneb.usuarios.model.PerfilUsuario;
import com.mpazneb.usuarios.service.UsuarioService;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Prueba la administración de cuentas (/api/usuarios): listar, ver, crear,
 * editar y eliminar, con sus respuestas 200, 201, 204, 400, 404 y 409.
 */
@WebMvcTest(UsuarioController.class)
class UsuarioControllerTest {

    private static final String CUENTA_NUEVA = """
            {"alias":"Asistente Aula B","correo":"asistente.b@mpaz.cl","clave":"Asistente456","perfil":"ASISTENTE"}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UsuarioService service;

    private static UsuarioResponse cuenta(Long id, String alias, String correo, PerfilUsuario perfil, boolean activa) {
        return new UsuarioResponse(id, alias, correo, perfil, activa, Instant.now());
    }

    @Test
    void listarDevuelveLasCuentasSinClaves() throws Exception {
        when(service.listar()).thenReturn(List.of(
                cuenta(1L, "Asistente Aula A", "asistente@mpaz.cl", PerfilUsuario.ASISTENTE, true),
                cuenta(2L, "Docente UTP", "utp@mpaz.cl", PerfilUsuario.DOCENTE_UTP, true)));

        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[1].perfil").value("DOCENTE_UTP"))
                .andExpect(jsonPath("$[0].claveHash").doesNotExist());
    }

    @Test
    void obtenerUnaCuentaQueNoExisteResponde404() throws Exception {
        when(service.obtener(99L)).thenThrow(new RecursoNoEncontradoException("No se encontró la cuenta con id 99."));

        mockMvc.perform(get("/api/usuarios/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("No se encontró la cuenta con id 99."));
    }

    @Test
    void obtenerConUnIdQueNoEsNumeroResponde400() throws Exception {
        mockMvc.perform(get("/api/usuarios/abc"))
                .andExpect(status().isBadRequest());

        verify(service, never()).obtener(any());
    }

    @Test
    void crearUnaCuentaValidaResponde201ConSuUbicacion() throws Exception {
        when(service.crear(any())).thenReturn(
                cuenta(3L, "Asistente Aula B", "asistente.b@mpaz.cl", PerfilUsuario.ASISTENTE, true));

        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CUENTA_NUEVA))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/usuarios/3"))
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.activa").value(true))
                .andExpect(jsonPath("$.clave").doesNotExist());
    }

    @Test
    void crearConDatosInvalidosResponde400SinLlamarAlServicio() throws Exception {
        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"alias\":\"AB\",\"correo\":\"no-es-correo\",\"clave\":\"123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.alias").exists())
                .andExpect(jsonPath("$.campos.correo").exists())
                .andExpect(jsonPath("$.campos.clave").exists())
                .andExpect(jsonPath("$.campos.perfil").exists());

        verify(service, never()).crear(any());
    }

    @Test
    void crearConUnCorreoRepetidoResponde409() throws Exception {
        when(service.crear(any())).thenThrow(new ReglaNegocioException("Ya existe una cuenta con ese correo."));

        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CUENTA_NUEVA))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").value("Ya existe una cuenta con ese correo."));
    }

    @Test
    void actualizarUnaCuentaResponde200ConLosCambios() throws Exception {
        when(service.actualizar(eq(1L), any())).thenReturn(
                cuenta(1L, "Asistente Aula A2", "asistente@mpaz.cl", PerfilUsuario.ASISTENTE, false));

        mockMvc.perform(put("/api/usuarios/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"alias\":\"Asistente Aula A2\",\"perfil\":\"ASISTENTE\",\"activa\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alias").value("Asistente Aula A2"))
                .andExpect(jsonPath("$.activa").value(false));
    }

    @Test
    void actualizarConUnaClaveDemasiadoCortaResponde400() throws Exception {
        mockMvc.perform(put("/api/usuarios/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"alias\":\"Asistente Aula A\",\"perfil\":\"ASISTENTE\",\"activa\":true,\"clave\":\"123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.clave").exists());

        verify(service, never()).actualizar(any(), any());
    }

    @Test
    void eliminarUnaCuentaResponde204() throws Exception {
        mockMvc.perform(delete("/api/usuarios/1"))
                .andExpect(status().isNoContent());

        verify(service).eliminar(1L);
    }

    @Test
    void eliminarUnaCuentaQueNoExisteResponde404() throws Exception {
        doThrow(new RecursoNoEncontradoException("No se encontró la cuenta con id 99."))
                .when(service).eliminar(99L);

        mockMvc.perform(delete("/api/usuarios/99"))
                .andExpect(status().isNotFound());
    }
}
