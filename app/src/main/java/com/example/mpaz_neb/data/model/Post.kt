package com.example.mpaz_neb.data.model

/**
 * Publicación que se intercambia con la API REST (JSONPlaceholder /posts).
 * En la app se muestra como un "comunicado" del equipo pedagógico.
 *
 * `id` es nulo al crear un comunicado: el servidor es quien lo asigna.
 * Gson omite los campos nulos al convertir a JSON.
 */
data class Post(
    val userId: Int,          // ID del usuario que creó el post
    val id: Int? = null,      // ID del post (lo asigna el servidor)
    val title: String,        // Título del post
    val body: String          // Cuerpo o contenido del post
)
