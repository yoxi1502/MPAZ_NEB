package com.mpazneb.sesiones;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/** Recorre el ciclo de vida completo de una sesión contra una base H2 en memoria. */
@SpringBootTest
@AutoConfigureMockMvc
class SesionesIntegracionTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void cicloCompletoDeUnaSesion() throws Exception {
        String sesionJson = """
                {"modulo":"MATEMATICA","curso":"1.° Año Básico",
                 "estudiante":"Estudiante 04 - Aula B","asistente":"Asistente Integracion",
                 "contenido":"Sumas y restas hasta 10 · Inicial","actividadId":2}
                """;

        // 1. La asistente crea la sesión y recibe el código
        String creada = mockMvc.perform(post("/api/sesiones")
                        .contentType(MediaType.APPLICATION_JSON).content(sesionJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("CREADA"))
                .andReturn().getResponse().getContentAsString();
        Integer id = JsonPath.read(creada, "$.id");
        String codigo = JsonPath.read(creada, "$.codigo");

        // No puede crear otra mientras esta siga abierta
        mockMvc.perform(post("/api/sesiones")
                        .contentType(MediaType.APPLICATION_JSON).content(sesionJson))
                .andExpect(status().isConflict());

        // 2. El estudiante entra con el código
        mockMvc.perform(post("/api/sesiones/ingreso")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigo\":\"" + codigo + "\",\"modulo\":\"MATEMATICA\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("EN_CURSO"));

        // 3. Envía su resultado (dos veces el mismo intento: no se duplica)
        for (int i = 0; i < 2; i++) {
            mockMvc.perform(post("/api/sesiones/" + id + "/resultados")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"intentoId\":\"intento-a\",\"aciertos\":2,\"total\":3}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.resultados", hasSize(1)));
        }

        // 4. La asistente registra su observación
        mockMvc.perform(patch("/api/sesiones/" + id + "/observacion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"observacion\":\"Resolvió las sumas con apoyo concreto.\"}"))
                .andExpect(jsonPath("$.observacion").value("Resolvió las sumas con apoyo concreto."));

        // No se puede eliminar mientras está abierta
        mockMvc.perform(delete("/api/sesiones/" + id)).andExpect(status().isConflict());

        // 5. Finaliza y luego elimina
        mockMvc.perform(patch("/api/sesiones/" + id + "/finalizar"))
                .andExpect(jsonPath("$.estado").value("FINALIZADA"));
        mockMvc.perform(delete("/api/sesiones/" + id)).andExpect(status().isNoContent());
        mockMvc.perform(get("/api/sesiones/" + id)).andExpect(status().isNotFound());
    }
}
