package com.example.mpaz_neb.model

/** Tipos de pregunta de comprensión definidos en el caso. */
enum class TipoPreguntaLectura(val etiqueta: String) {
    EXPLICITA("Busca en el texto"),
    IMPLICITA("Piensa y descubre"),
    VOCABULARIO("Vocabulario")
}

data class PreguntaLectura(
    val enunciado: String,
    val tipo: TipoPreguntaLectura,
    val opciones: List<String>,
    val respuestaCorrecta: String,
    val retroalimentacion: String
)

data class LecturaUiState(
    val titulo: String = "",
    val texto: String = "",
    val preguntas: List<PreguntaLectura> = emptyList(),
    val indiceActual: Int = 0,
    val respuestaSeleccionada: String? = null,
    val respuestaComprobada: Boolean = false,
    val aciertos: Int = 0,
    val finalizada: Boolean = false,
    val guardando: Boolean = false,
    val errorGuardado: String? = null
)