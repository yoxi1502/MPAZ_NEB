package com.mpazneb.sesiones.dto;

import com.mpazneb.sesiones.model.EstadoSesion;
import com.mpazneb.sesiones.model.Modulo;
import java.time.Instant;
import java.util.List;

/** Sesión con sus resultados, tal como la recibe la app. */
public record SesionResponse(
        Long id,
        String codigo,
        String estudiante,
        String asistente,
        String curso,
        Modulo modulo,
        String contenido,
        Long actividadId,
        EstadoSesion estado,
        String observacion,
        Instant creadaEn,
        Instant finalizadaEn,
        List<ResultadoResponse> resultados
) {
}
