package com.example.mpaz_neb.model

data class ResultadoActividad(
    val aciertos: Int,
    val total: Int,
    val fecha: Long
)

data class ProgresoUiState(
    val lectura: ResultadoActividad? = null,
    val matematica: ResultadoActividad? = null,
    val sesiones: List<SesionAula> = emptyList(),
    val filtroEstudiante: String? = null,
    val cargando: Boolean = true,
    val error: String? = null
) {
    /** Historial ordenado de la sesión más reciente a la más antigua. */
    val historial: List<SesionAula>
        get() = sesiones
            .filter { filtroEstudiante == null || it.estudiante == filtroEstudiante }
            .sortedByDescending { it.creadaEn }
}
