package com.mpazneb.actividades.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.mpazneb.actividades.dto.ActividadRequest;
import com.mpazneb.actividades.dto.ActividadResponse;
import com.mpazneb.actividades.exception.RecursoNoEncontradoException;
import com.mpazneb.actividades.model.Dificultad;
import com.mpazneb.actividades.model.EstadoActividad;
import com.mpazneb.actividades.model.Modulo;
import com.mpazneb.actividades.service.ActividadService;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/** Prueba solo la capa web: rutas, códigos HTTP, JSON y validaciones. El servicio es un mock. */
@WebMvcTest(ActividadController.class)
class ActividadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ActividadService service;

    private static final String JSON_VALIDO = """
            {"nombre":"Sumas hasta 20","modulo":"MATEMATICA","curso":"2.° Año Básico",
             "contenido":"Adición","objetivo":"Resolver sumas simples hasta 20.",
             "dificultad":"INTERMEDIA","instrucciones":"Resuelve cada suma y comprueba.",
             "cantidadPreguntas":5,"intentosPermitidos":2,"estado":"DISPONIBLE"}
            """;

    @Test
    void listarDevuelveLasActividadesEnJson() throws Exception {
        when(service.listar(Modulo.LECTURA, null, null)).thenReturn(List.of(respuesta(1L)));

        mockMvc.perform(get("/api/actividades").param("modulo", "LECTURA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Sumas hasta 20"));
    }

    @Test
    void crearResponde201ConLaUbicacionDelRecurso() throws Exception {
        when(service.crear(any(ActividadRequest.class))).thenReturn(respuesta(10L));

        mockMvc.perform(post("/api/actividades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/actividades/10"))
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    void crearConDatosInvalidosResponde400ConLosCampos() throws Exception {
        String invalido = """
                {"nombre":"ab","modulo":"MATEMATICA","curso":"2.° Año Básico","contenido":"",
                 "objetivo":"corto","dificultad":"INICIAL","instrucciones":"Resuelve y comprueba.",
                 "cantidadPreguntas":0,"intentosPermitidos":2,"estado":"BORRADOR"}
                """;

        mockMvc.perform(post("/api/actividades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalido))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.nombre").exists())
                .andExpect(jsonPath("$.campos.contenido").exists())
                .andExpect(jsonPath("$.campos.cantidadPreguntas").exists());
    }

    @Test
    void obtenerUnIdInexistenteResponde404() throws Exception {
        when(service.obtener(99L)).thenThrow(new RecursoNoEncontradoException("No se encontró la actividad con id 99."));

        mockMvc.perform(get("/api/actividades/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("No se encontró la actividad con id 99."));
    }

    @Test
    void cambiarEstadoUsaElValorEnviado() throws Exception {
        when(service.cambiarEstado(eq(1L), eq(EstadoActividad.DESACTIVADA))).thenReturn(respuesta(1L));

        mockMvc.perform(patch("/api/actividades/1/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\":\"DESACTIVADA\"}"))
                .andExpect(status().isOk());

        verify(service).cambiarEstado(1L, EstadoActividad.DESACTIVADA);
    }

    @Test
    void eliminarResponde204SinContenido() throws Exception {
        mockMvc.perform(delete("/api/actividades/1"))
                .andExpect(status().isNoContent());

        verify(service).eliminar(1L);
    }

    @Test
    void unValorDeEnumInvalidoResponde400() throws Exception {
        mockMvc.perform(get("/api/actividades").param("modulo", "HISTORIA"))
                .andExpect(status().isBadRequest());
    }

    private ActividadResponse respuesta(Long id) {
        return new ActividadResponse(id, "Sumas hasta 20", Modulo.MATEMATICA, "2.° Año Básico",
                "Adición", "Resolver sumas simples hasta 20.", Dificultad.INTERMEDIA,
                "Resuelve cada suma y comprueba.", 5, 2, EstadoActividad.DISPONIBLE, Instant.now());
    }
}
