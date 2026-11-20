package com.mpazneb.usuarios;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import com.mpazneb.usuarios.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/** Crea una cuenta, inicia sesión con BCrypt real, la desactiva y la elimina. */
@SpringBootTest
@AutoConfigureMockMvc
class UsuariosIntegracionTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository repository;

    @Test
    void flujoCompletoDeUnaCuenta() throws Exception {
        String creada = mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"alias":"Asistente Prueba","correo":"prueba@mpaz.cl",
                                 "clave":"Prueba123","perfil":"ASISTENTE"}
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Integer id = JsonPath.read(creada, "$.id");

        // La clave quedó cifrada en la base de datos
        String hash = repository.findById(id.longValue()).orElseThrow().getClaveHash();
        assertThat(hash).startsWith("$2").isNotEqualTo("Prueba123");

        // Login correcto e incorrecto
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"prueba@mpaz.cl\",\"clave\":\"Prueba123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ultimoAcceso").exists());
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"prueba@mpaz.cl\",\"clave\":\"Otra1234\"}"))
                .andExpect(status().isUnauthorized());

        // Desactivar la cuenta impide el login
        mockMvc.perform(put("/api/usuarios/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"alias\":\"Asistente Prueba\",\"perfil\":\"ASISTENTE\",\"activa\":false}"))
                .andExpect(jsonPath("$.activa").value(false));
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"prueba@mpaz.cl\",\"clave\":\"Prueba123\"}"))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/usuarios/" + id)).andExpect(status().isNoContent());
    }
}
