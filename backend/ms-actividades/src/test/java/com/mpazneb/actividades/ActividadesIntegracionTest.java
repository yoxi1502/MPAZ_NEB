package com.mpazneb.actividades;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Prueba de integración: levanta el microservicio completo con una base H2
 * en memoria y recorre el CRUD de punta a punta (controlador, servicio y JPA).
 */
@SpringBootTest
@AutoConfigureMockMvc
class ActividadesIntegracionTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void flujoCompletoDeCrearEditarDesactivarYEliminar() throws Exception {
        String json = """
                {"nombre":"Restas hasta 10","modulo":"MATEMATICA","curso":"1.° Año Básico",
                 "contenido":"Sustracción","objetivo":"Resolver restas simples hasta 10.",
                 "dificultad":"INICIAL","instrucciones":"Resuelve cada resta y comprueba.",
                 "cantidadPreguntas":3,"intentosPermitidos":2,"estado":"BORRADOR"}
                """;

        // Crear
        String creada = mockMvc.perform(post("/api/actividades")
                        .contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Integer id = JsonPath.read(creada, "$.id");

        // El mismo nombre en el mismo curso no se permite
        mockMvc.perform(post("/api/actividades")
                        .contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isConflict());

        // Editar
        mockMvc.perform(put("/api/actividades/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.replace("BORRADOR", "DISPONIBLE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("DISPONIBLE"));

        // Filtrar por estado
        mockMvc.perform(get("/api/actividades").param("estado", "DISPONIBLE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        // Desactivar
        mockMvc.perform(patch("/api/actividades/" + id + "/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\":\"DESACTIVADA\"}"))
                .andExpect(jsonPath("$.estado").value("DESACTIVADA"));

        // Eliminar y comprobar que ya no existe
        mockMvc.perform(delete("/api/actividades/" + id)).andExpect(status().isNoContent());
        mockMvc.perform(get("/api/actividades/" + id)).andExpect(status().isNotFound());
    }
}
