package com.mpazneb.actividades.dto;

import com.mpazneb.actividades.model.Dificultad;
import com.mpazneb.actividades.model.EstadoActividad;
import com.mpazneb.actividades.model.Modulo;
import java.time.Instant;

/** Respuesta JSON que recibe la app: nunca se expone la entidad directamente. */
public record ActividadResponse(
        Long id,
        String nombre,
        Modulo modulo,
        String curso,
        String contenido,
        String objetivo,
        Dificultad dificultad,
        String instrucciones,
        Integer cantidadPreguntas,
        Integer intentosPermitidos,
        EstadoActividad estado,
        Instant actualizadaEn
) {
}
