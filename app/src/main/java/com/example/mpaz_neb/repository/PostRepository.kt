package com.example.mpaz_neb.repository

import com.example.mpaz_neb.data.model.Post
import com.example.mpaz_neb.data.remote.ApiService
import com.example.mpaz_neb.data.remote.RetrofitInstance

/**
 * Accede a los comunicados de la API REST usando Retrofit.
 * Recibe el ApiService por parámetro para poder reemplazarlo en pruebas.
 */
class PostRepository(
    private val api: ApiService = RetrofitInstance.api
) {

    suspend fun getPosts(userId: Int = USUARIO_EQUIPO): List<Post> =
        api.getPosts(userId)

    suspend fun createPost(titulo: String, cuerpo: String): Post =
        api.createPost(Post(userId = USUARIO_EQUIPO, title = titulo, body = cuerpo))

    suspend fun updatePost(id: Int, titulo: String, cuerpo: String): Post =
        api.updatePost(id, Post(userId = USUARIO_EQUIPO, id = id, title = titulo, body = cuerpo))

    suspend fun deletePost(id: Int) {
        val respuesta = api.deletePost(id)
        if (!respuesta.isSuccessful) {
            throw IllegalStateException("El servidor respondió con error ${respuesta.code()}.")
        }
    }

    companion object {
        /** Usuario ficticio de JSONPlaceholder que representa al equipo pedagógico. */
        const val USUARIO_EQUIPO = 1
    }
}
