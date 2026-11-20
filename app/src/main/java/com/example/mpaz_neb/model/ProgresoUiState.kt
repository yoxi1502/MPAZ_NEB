package com.example.mpaz_neb.model

data class ResultadoActividad(
    val aciertos: Int,
    val total: Int,
    val fecha: Long
)

data class ProgresoUiState(
    // Último resultado de cada módulo en este dispositivo (DataStore)
    val lectura: ResultadoActividad? = null,
    val matematica: ResultadoActividad? = null,
    val cargando: Boolean = true,
    val error: String? = null,
    // Historial de sesiones del servidor (ms-sesiones)
    val sesiones: List<SesionAula> = emptyList(),
    val filtroEstudiante: String? = null,
    val cargandoHistorial: Boolean = false,
    val errorHistorial: String? = null,
    // Mensaje breve tras editar o eliminar (Snackbar)
    val mensaje: String? = null,
    // Edición de la observación de una sesión del historial
    val sesionEditando: SesionAula? = null,
    val observacionEditada: String = "",
    val errorObservacion: String? = null,
    val guardandoObservacion: Boolean = false,
    // Sesión que espera confirmación para eliminarse
    val sesionAEliminar: SesionAula? = null,
    val eliminandoId: Long? = null
) {
    /** Historial ordenado de la sesión más reciente a la más antigua. */
    val historial: List<SesionAula>
        get() = sesiones.sortedByDescending { it.creadaEn }
}
