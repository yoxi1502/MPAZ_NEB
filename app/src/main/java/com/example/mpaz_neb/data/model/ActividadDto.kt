package com.example.mpaz_neb.data.model

import com.example.mpaz_neb.model.Actividad
import com.example.mpaz_neb.model.Dificultad
import com.example.mpaz_neb.model.EstadoActividad
import com.example.mpaz_neb.model.ModuloActividad

/**
 * JSON que intercambia la app con ms-actividades.
 * Los DTO son la forma de los datos "en la red"; la app trabaja con model/Actividad.
 */
data class ActividadDto(
    val id: Long,
    val nombre: String,
    val modulo: ModuloActividad,
    val curso: String,
    val contenido: String,
    val objetivo: String,
    val dificultad: Dificultad,
    val instrucciones: String,
    val cantidadPreguntas: Int,
    val intentosPermitidos: Int,
    val estado: EstadoActividad,
    val actualizadaEn: String?      // Fecha ISO-8601, por ejemplo 2026-11-16T22:10:00Z
)

/** Cuerpo de POST y PUT: no lleva id ni fecha, porque los define el servidor. */
data class ActividadRequestDto(
    val nombre: String,
    val modulo: ModuloActividad,
    val curso: String,
    val contenido: String,
    val objetivo: String,
    val dificultad: Dificultad,
    val instrucciones: String,
    val cantidadPreguntas: Int,
    val intentosPermitidos: Int,
    val estado: EstadoActividad
)

/** Cuerpo del PATCH /estado. */
data class CambioEstadoDto(
    val estado: EstadoActividad
)

// ---- Conversión entre el JSON del microservicio y el modelo de la app ----

fun ActividadDto.aModelo(): Actividad = Actividad(
    id = id,
    nombre = nombre,
    modulo = modulo,
    curso = curso,
    contenido = contenido,
    objetivo = objetivo,
    dificultad = dificultad,
    instrucciones = instrucciones,
    cantidadPreguntas = cantidadPreguntas,
    intentosPermitidos = intentosPermitidos,
    estado = estado,
    actualizadaEn = isoAMilis(actualizadaEn) ?: 0L
)

fun Actividad.aRequest(): ActividadRequestDto = ActividadRequestDto(
    nombre = nombre,
    modulo = modulo,
    curso = curso,
    contenido = contenido,
    objetivo = objetivo,
    dificultad = dificultad,
    instrucciones = instrucciones,
    cantidadPreguntas = cantidadPreguntas,
    intentosPermitidos = intentosPermitidos,
    estado = estado
)
