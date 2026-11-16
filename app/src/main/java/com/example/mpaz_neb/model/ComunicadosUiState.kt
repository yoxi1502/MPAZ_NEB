package com.example.mpaz_neb.model

import com.example.mpaz_neb.data.model.Post

/** Estado de la pantalla de comunicados que se obtienen desde la API REST. */
data class ComunicadosUiState(
    val cargando: Boolean = true,
    val posts: List<Post> = emptyList(),
    val error: String? = null,
    val mensaje: String? = null
)
