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

data class ResultadoIntento(
    val id: String,
    val aciertos: Int,
    val total: Int,
    val fecha: Long
) {
    val errores: Int get() = total - aciertos
}

data class SesionAula(
    val id: String,
    val codigo: String,
    val estudiante: String,
    val curso: String,
    val modulo: ModuloActividad,
    val contenido: String,
    val estado: EstadoSesion,
    val creadaEn: Long,
    val resultados: List<ResultadoIntento> = emptyList(),
    val observacion: String = "",
    val finalizadaEn: Long? = null
) {
    val ultimoResultado: ResultadoIntento? get() = resultados.lastOrNull()
    val intentosCompletados: Int get() = resultados.size
}