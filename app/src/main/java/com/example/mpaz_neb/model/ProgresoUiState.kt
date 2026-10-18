package com.example.mpaz_neb.model

data class ResultadoActividad(
    val aciertos: Int,
    val total: Int,
    val fecha: Long
)

data class ProgresoUiState(
    val lectura: ResultadoActividad? = null,
    val matematica: ResultadoActividad? = null,
    val cargando: Boolean = true,
    val error: String? = null
)