package com.example.mpaz_neb.model

data class PreguntaLectura(
    val enunciado: String,
    val opciones: List<String>,
    val respuestaCorrecta: String
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