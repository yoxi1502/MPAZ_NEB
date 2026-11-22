package com.example.mpaz_neb.data.model

import com.example.mpaz_neb.model.EstadoSesion
import com.example.mpaz_neb.model.ModuloActividad
import com.example.mpaz_neb.model.ResultadoIntento
import com.example.mpaz_neb.model.SesionAula

/** Sesión de aula tal como la entrega ms-sesiones. */
data class SesionDto(
    val id: Long,
    val codigo: String,
    val estudiante: String,
    val asistente: String,
    val curso: String,
    val modulo: ModuloActividad,
    val contenido: String,
    val actividadId: Long?,
    val intentosPermitidos: Int? = null,
    val estado: EstadoSesion,
    val observacion: String?,
    val creadaEn: String,           // Fecha ISO-8601
    val finalizadaEn: String?,
    val resultados: List<ResultadoDto>?
)

data class ResultadoDto(
    val intentoId: String,
    val aciertos: Int,
    val total: Int,
    val fecha: String
)

/** Cuerpo del POST /api/sesiones. El código y el estado los asigna el servidor. */
data class SesionRequestDto(
    val modulo: ModuloActividad,
    val curso: String,
    val estudiante: String,
    val asistente: String,
    val contenido: String,
    val actividadId: Long?,
    val intentosPermitidos: Int? = null
)

/** Cuerpo del POST /api/sesiones/ingreso. */
data class IngresoDto(
    val codigo: String,
    val modulo: ModuloActividad?
)

/** Cuerpo del POST /api/sesiones/{id}/resultados. */
data class ResultadoRequestDto(
    val intentoId: String,
    val aciertos: Int,
    val total: Int
)

/** Cuerpo del PATCH /api/sesiones/{id}/observacion. */
data class ObservacionDto(
    val observacion: String
)

// ---- Conversión entre el JSON del microservicio y el modelo de la app ----

fun SesionDto.aModelo(): SesionAula = SesionAula(
    id = id,
    codigo = codigo,
    estudiante = estudiante,
    asistente = asistente,
    curso = curso,
    modulo = modulo,
    contenido = contenido,
    estado = estado,
    creadaEn = isoAMilis(creadaEn) ?: 0L,
    resultados = resultados.orEmpty().map { it.aModelo() },
    observacion = observacion.orEmpty(),
    finalizadaEn = isoAMilis(finalizadaEn),
    actividadId = actividadId,
    intentosPermitidos = intentosPermitidos
)

fun ResultadoDto.aModelo(): ResultadoIntento = ResultadoIntento(
    id = intentoId,
    aciertos = aciertos,
    total = total,
    fecha = isoAMilis(fecha) ?: 0L
)
