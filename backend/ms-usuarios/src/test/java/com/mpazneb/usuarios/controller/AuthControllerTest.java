package com.mpazneb.usuarios.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.mpazneb.usuarios.dto.LoginRequest;
import com.mpazneb.usuarios.dto.UsuarioResponse;
import com.mpazneb.usuarios.exception.CredencialesInvalidasException;
import com.mpazneb.usuarios.exception.CuentaDesactivadaException;
import com.mpazneb.usuarios.model.PerfilUsuario;
import com.mpazneb.usuarios.service.UsuarioService;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/** Prueba el endpoint de login: respuestas 200, 400, 401 y 403. */
@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UsuarioService service;

    @Test
    void loginCorrectoResponde200SinLaClave() throws Exception {
        when(service.login(new LoginRequest("utp@mpaz.cl", "Docente123"))).thenReturn(
                new UsuarioResponse(2L, "Docente UTP", "utp@mpaz.cl", PerfilUsuario.DOCENTE_UTP, true, Instant.now()));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"utp@mpaz.cl\",\"clave\":\"Docente123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.perfil").value("DOCENTE_UTP"))
                .andExpect(jsonPath("$.clave").doesNotExist())
                .andExpect(jsonPath("$.claveHash").doesNotExist());
    }

    @Test
    void loginConClaveIncorrectaResponde401() throws Exception {
        when(service.login(any())).thenThrow(new CredencialesInvalidasException());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"utp@mpaz.cl\",\"clave\":\"incorrecta\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value("Correo o contraseña incorrectos."));
    }

    @Test
    void loginConCuentaDesactivadaResponde403() throws Exception {
        when(service.login(any())).thenThrow(new CuentaDesactivadaException());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"utp@mpaz.cl\",\"clave\":\"Docente123\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void loginConCorreoMalEscritoResponde400SinLlamarAlServicio() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"utp@\",\"clave\":\"123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.correo").exists())
                .andExpect(jsonPath("$.campos.clave").exists());

        verify(service, never()).login(any());
    }
}
