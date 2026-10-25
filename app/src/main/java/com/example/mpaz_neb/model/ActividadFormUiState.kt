package com.example.mpaz_neb.model

import com.example.mpaz_neb.repository.SesionAulaRepository

/** Estado del formulario para crear o editar una actividad del catálogo. */
data class ActividadFormUiState(
    val id: String? = null,
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
    val error: String? = null
) {
    val filtradas: List<Actividad>
        get() = actividades.filter { actividad ->
            (filtroModulo == null || actividad.modulo == filtroModulo) &&
                    (filtroCurso == null || actividad.curso == filtroCurso)
        }
}
