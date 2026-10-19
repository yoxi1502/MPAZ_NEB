package com.example.mpaz_neb.model

import com.example.mpaz_neb.repository.SesionAulaRepository

data class SesionAulaUiState(
    val sesion: SesionAula? = null,
    val modulo: ModuloActividad = ModuloActividad.LECTURA,
    val curso: String = SesionAulaRepository.CURSOS.first(),
    val estudiante: String = SesionAulaRepository.ESTUDIANTES.first(),
    val cargando: Boolean = false,
    val guardando: Boolean = false,
    val error: String? = null
)
