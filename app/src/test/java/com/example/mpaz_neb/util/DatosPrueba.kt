package com.example.mpaz_neb.util

import com.example.mpaz_neb.data.model.ActividadDto
import com.example.mpaz_neb.data.model.ResultadoDto
import com.example.mpaz_neb.data.model.SesionDto
import com.example.mpaz_neb.model.Actividad
import com.example.mpaz_neb.model.Dificultad
import com.example.mpaz_neb.model.EstadoActividad
import com.example.mpaz_neb.model.EstadoSesion
import com.example.mpaz_neb.model.ModuloActividad
import com.example.mpaz_neb.model.ResultadoIntento
import com.example.mpaz_neb.model.SesionAula

/** Datos ficticios reutilizables en las pruebas. */
object DatosPrueba {

    const val CURSO = "2.° Año Básico"
    const val ESTUDIANTE = "Estudiante 01 - Aula A"
    const val ASISTENTE = "Asistente Aula A"

    fun actividad(
        id: Long? = 1L,
        nombre: String = "Una tarde en el parque",
        modulo: ModuloActividad = ModuloActividad.LECTURA,
        estado: EstadoActividad = EstadoActividad.DISPONIBLE,
        curso: String = CURSO
    ) = Actividad(
        id = id,
        nombre = nombre,
        modulo = modulo,
        curso = curso,
        contenido = "Comprensión lectora",
        objetivo = "Responder preguntas de un texto breve.",
        dificultad = Dificultad.INICIAL,
        instrucciones = "Lee con calma y responde.",
        cantidadPreguntas = 3,
        intentosPermitidos = 2,
        estado = estado,
        actualizadaEn = 0L
    )

    fun actividadDto(id: Long = 1L, estado: EstadoActividad = EstadoActividad.DISPONIBLE) = ActividadDto(
        id = id,
        nombre = "Una tarde en el parque",
        modulo = ModuloActividad.LECTURA,
        curso = CURSO,
        contenido = "Comprensión lectora",
        objetivo = "Responder preguntas de un texto breve.",
        dificultad = Dificultad.INICIAL,
        instrucciones = "Lee con calma y responde.",
        cantidadPreguntas = 3,
        intentosPermitidos = 2,
        estado = estado,
        actualizadaEn = "2026-11-16T22:10:00Z"
    )

    fun sesion(
        id: Long = 10L,
        modulo: ModuloActividad = ModuloActividad.LECTURA,
        estado: EstadoSesion = EstadoSesion.EN_CURSO,
        observacion: String = "",
        creadaEn: Long = 1_000L,
        intentosPermitidos: Int? = null,
        resultados: List<ResultadoIntento> = emptyList()
    ) = SesionAula(
        id = id,
        codigo = "MPAZ-AB12CD",
        estudiante = ESTUDIANTE,
        asistente = ASISTENTE,
        curso = CURSO,
        modulo = modulo,
        contenido = "Una tarde en el parque · Inicial",
        estado = estado,
        creadaEn = creadaEn,
        observacion = observacion,
        actividadId = 1L,
        intentosPermitidos = intentosPermitidos,
        resultados = resultados
    )

    fun sesionDto(id: Long = 10L, estado: EstadoSesion = EstadoSesion.CREADA) = SesionDto(
        id = id,
        codigo = "MPAZ-AB12CD",
        estudiante = ESTUDIANTE,
        asistente = ASISTENTE,
        curso = CURSO,
        modulo = ModuloActividad.LECTURA,
        contenido = "Una tarde en el parque · Inicial",
        actividadId = 1L,
        estado = estado,
        observacion = null,
        creadaEn = "2026-11-16T22:10:00Z",
        finalizadaEn = null,
        resultados = listOf(ResultadoDto("intento-1", 2, 3, "2026-11-16T22:15:00Z"))
    )
}
