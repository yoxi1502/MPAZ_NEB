package com.example.mpaz_neb.model

data class PreguntaMatematica(
    val enunciado: String,
    val opciones: List<Int>,
    val respuestaCorrecta: Int,
    val procedimiento: String
)

data class MatematicaUiState(
    val preguntas: List<PreguntaMatematica> = emptyList(),
    val indiceActual: Int = 0,
    val respuestaSeleccionada: Int? = null,
    val respuestaComprobada: Boolean = false,
    val aciertos: Int = 0,
    val finalizada: Boolean = false,
    val guardando: Boolean = false,
    val errorGuardado: String? = null
)