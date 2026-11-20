package com.example.mpaz_neb.model

import com.example.mpaz_neb.repository.SesionAulaRepository

/** Estado del formulario para crear o editar una actividad del catálogo. */
data class ActividadFormUiState(
    val id: Long? = null,
    val nombre: String = "",
    val modulo: ModuloActividad = ModuloActividad.LECTURA,
    val curso: String = SesionAulaRepository.CURSOS.first(),
    val contenido: String = "",
    val objetivo: String = "",
    val dificultad: Dificultad = Dificultad.INICIAL,
    val instrucciones: String = "",
    val cantidadPreguntas: String = "3",
    val intentosPermitidos: String = "2",
    val estado: EstadoActividad = EstadoActividad.BORRADOR,
    val errores: Map<CampoActividad, String> = emptyMap(),
    val cargando: Boolean = false,
    val guardando: Boolean = false,
    val errorGeneral: String? = null
) {
    val esEdicion: Boolean get() = id != null
}

enum class CampoActividad {
    NOMBRE,
    CONTENIDO,
    OBJETIVO,
    INSTRUCCIONES,
    CANTIDAD_PREGUNTAS,
    INTENTOS
}

data class CatalogoUiState(
    val actividades: List<Actividad> = emptyList(),
    val filtroModulo: ModuloActividad? = null,
    val filtroCurso: String? = null,
    val cargando: Boolean = true,
    // Error al cargar la lista (se muestra con el botón Reintentar)
    val error: String? = null,
    // Mensaje breve tras una acción (Snackbar): "Actividad eliminada", errores del servidor, etc.
    val mensaje: String? = null,
    // Actividad que espera confirmación para eliminarse (muestra el diálogo)
    val actividadAEliminar: Actividad? = null,
    // Id de la actividad que se está actualizando o eliminando en el servidor
    val procesandoId: Long? = null
) {
    val filtradas: List<Actividad>
        get() = actividades.filter { actividad ->
            (filtroModulo == null || actividad.modulo == filtroModulo) &&
                    (filtroCurso == null || actividad.curso == filtroCurso)
        }
}
