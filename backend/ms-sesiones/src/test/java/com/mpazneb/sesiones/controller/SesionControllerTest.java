package com.mpazneb.sesiones.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.mpazneb.sesiones.dto.IngresoRequest;
import com.mpazneb.sesiones.dto.ResultadoRequest;
import com.mpazneb.sesiones.dto.SesionRequest;
import com.mpazneb.sesiones.dto.SesionResponse;
import com.mpazneb.sesiones.exception.ReglaNegocioException;
import com.mpazneb.sesiones.model.EstadoSesion;
import com.mpazneb.sesiones.model.Modulo;
import com.mpazneb.sesiones.service.SesionService;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/** Prueba la capa web de las sesiones: rutas, validaciones y códigos HTTP. */
@WebMvcTest(SesionController.class)
class SesionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SesionService service;

    @Test
    void crearResponde201ConElCodigoGenerado() throws Exception {
        when(service.crear(any(SesionRequest.class))).thenReturn(respuesta(EstadoSesion.CREADA));

        mockMvc.perform(post("/api/sesiones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"modulo":"LECTURA","curso":"2.° Año Básico",
                                 "estudiante":"Estudiante 01 - Aula A","asistente":"Asistente Aula A",
                                 "contenido":"Una tarde en el parque · Inicial","actividadId":1}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigo").value("MPAZ-AB12CD"));
    }

    @Test
    void crearSinEstudianteResponde400() throws Exception {
        mockMvc.perform(post("/api/sesiones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"modulo":"LECTURA","curso":"2.° Año Básico","estudiante":"",
                                 "asistente":"Asistente Aula A","contenido":"Lectura"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.estudiante").value("Selecciona un estudiante."));
    }

    @Test
    void ingresarConCodigoMalEscritoResponde400() throws Exception {
        mockMvc.perform(post("/api/sesiones/ingreso")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigo\":\"MPAZ-12\"}"))
                .andExpect(status().isBadRequest());

        verify(service, never()).ingresar(any());
    }

    @Test
    void ingresarConCodigoValidoDevuelveLaSesion() throws Exception {
        when(service.ingresar(new IngresoRequest("MPAZ-AB12CD", null))).thenReturn(respuesta(EstadoSesion.EN_CURSO));

        mockMvc.perform(post("/api/sesiones/ingreso")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigo\":\"MPAZ-AB12CD\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("EN_CURSO"));
    }

    @Test
    void resultadoConMasAciertosQuePreguntasResponde400() throws Exception {
        mockMvc.perform(post("/api/sesiones/1/resultados")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"intentoId\":\"i-1\",\"aciertos\":4,\"total\":3}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.aciertosValidos").exists());
    }

    @Test
    void resultadoValidoSeEnviaAlServicio() throws Exception {
        when(service.registrarResultado(eq(1L), any(ResultadoRequest.class)))
                .thenReturn(respuesta(EstadoSesion.COMPLETADA));

        mockMvc.perform(post("/api/sesiones/1/resultados")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"intentoId\":\"i-1\",\"aciertos\":2,\"total\":3}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("COMPLETADA"));
    }

    @Test
    void observacionConRutResponde400() throws Exception {
        mockMvc.perform(patch("/api/sesiones/1/observacion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"observacion\":\"El estudiante 12.345.678-9 avanzó bien\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.sinDatosPersonales")
                        .value("No incluyas RUT ni otros datos personales reales."));
    }

    @Test
    void eliminarUnaSesionAbiertaResponde409() throws Exception {
        doThrow(new ReglaNegocioException("Finaliza la sesión antes de eliminarla."))
                .when(service).eliminar(1L);

        mockMvc.perform(delete("/api/sesiones/1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").value("Finaliza la sesión antes de eliminarla."));
    }

    @Test
    void listarFiltraPorEstudiante() throws Exception {
        when(service.listar("Estudiante 01 - Aula A", null)).thenReturn(List.of(respuesta(EstadoSesion.FINALIZADA)));

        mockMvc.perform(get("/api/sesiones").param("estudiante", "Estudiante 01 - Aula A"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].estudiante").value("Estudiante 01 - Aula A"));
    }

    private SesionResponse respuesta(EstadoSesion estado) {
        return new SesionResponse(1L, "MPAZ-AB12CD", "Estudiante 01 - Aula A", "Asistente Aula A",
                "2.° Año Básico", Modulo.LECTURA, "Una tarde en el parque · Inicial", 1L, 2, estado, "",
                Instant.now(), null, List.of());
    }
}
