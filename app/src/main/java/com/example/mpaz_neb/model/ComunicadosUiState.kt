package com.example.mpaz_neb.model

import com.example.mpaz_neb.data.model.Post

/** Estado de la pantalla de comunicados que se obtienen desde la API REST. */
data class ComunicadosUiState(
    val cargando: Boolean = true,
    val posts: List<Post> = emptyList(),
    val error: String? = null,
    val mensaje: String? = null,
    // Formulario para crear (POST) o editar (PUT) un comunicado
    val titulo: String = "",
    val cuerpo: String = "",
    val errorTitulo: String? = null,
    val errorCuerpo: String? = null,
    val editandoId: Int? = null,
    val guardando: Boolean = false
) {
    val editando: Boolean get() = editandoId != null
}
