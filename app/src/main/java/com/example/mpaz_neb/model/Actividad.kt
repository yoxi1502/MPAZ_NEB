package com.example.mpaz_neb.model

enum class Dificultad(val etiqueta: String) {
    INICIAL("Inicial"),
    INTERMEDIA("Intermedia"),
    AVANZADA("Avanzada")
}

enum class EstadoActividad(val etiqueta: String) {
    BORRADOR("Borrador"),
    DISPONIBLE("Disponible"),
    DESACTIVADA("Desactivada")
}

/**
 * Actividad del catálogo pedagógico (datos ficticios definidos por docente/UTP).
 * Se guarda en el microservicio ms-actividades; [id] es null mientras no se ha creado.
 */
data class Actividad(
    val id: Long?,
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
    val actualizadaEn: Long
) {
    val descripcionCorta: String
        get() = "$nombre · ${dificultad.etiqueta}"
}
