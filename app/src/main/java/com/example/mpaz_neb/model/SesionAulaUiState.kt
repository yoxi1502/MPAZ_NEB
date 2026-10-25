package com.example.mpaz_neb.model

data class SesionAulaUiState(
    val sesion: SesionAula? = null,
    val modulo: ModuloActividad = ModuloActividad.LECTURA,
    val curso: String = "",
    val estudiante: String = "",
    val errorCurso: String? = null,
    val errorEstudiante: String? = null,
    val cargando: Boolean = false,
    val guardando: Boolean = false,
    val error: String? = null,
    // Formulario de observación pedagógica
    val observacion: String = "",
    val observacionEditada: Boolean = false,
    val errorObservacion: String? = null,
    val guardandoObservacion: Boolean = false,
    val observacionGuardada: Boolean = false
)
