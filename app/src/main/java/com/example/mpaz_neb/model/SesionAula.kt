package com.example.mpaz_neb.model

enum class ModuloActividad(val etiqueta: String, val contenido: String) {
    LECTURA("Lectura", "Comprensión lectora: Una tarde en el parque"),
    MATEMATICA("Matemática", "Sumas y restas: Nivel 1")
}

enum class EstadoSesion(val etiqueta: String) {
    CREADA("Esperando al estudiante"),
    EN_CURSO("En curso"),
    COMPLETADA("Actividad completada"),
    FINALIZADA("Sesión finalizada")
}

/** Resultado de un intento. [id] es el id del intento que genera la app. */
data class ResultadoIntento(
    val id: String,
    val aciertos: Int,
    val total: Int,
    val fecha: Long
) {
    val errores: Int get() = total - aciertos
}

/** Sesión de práctica guardada en el microservicio ms-sesiones. */
data class SesionAula(
    val id: Long,
    val codigo: String,
    val estudiante: String,
    val asistente: String,
    val curso: String,
    val modulo: ModuloActividad,
    val contenido: String,
    val estado: EstadoSesion,
    val creadaEn: Long,
    val resultados: List<ResultadoIntento> = emptyList(),
    val observacion: String = "",
    val finalizadaEn: Long? = null,
    val actividadId: Long? = null,
    /** Intentos que permite la actividad asignada. Null = sin límite (sesiones antiguas). */
    val intentosPermitidos: Int? = null
) {
    val ultimoResultado: ResultadoIntento? get() = resultados.lastOrNull()
    val intentosCompletados: Int get() = resultados.size

    /** Intentos que quedan, o null si la sesión no tiene límite. */
    val intentosRestantes: Int?
        get() = intentosPermitidos?.let { (it - intentosCompletados).coerceAtLeast(0) }

    /** True cuando la sesión tiene límite y ya se usaron todos los intentos. */
    val sinIntentos: Boolean get() = intentosRestantes == 0
    val abierta: Boolean get() = estado != EstadoSesion.FINALIZADA
}
