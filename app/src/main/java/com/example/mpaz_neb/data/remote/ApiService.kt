package com.example.mpaz_neb.data.remote

import com.example.mpaz_neb.data.model.Post
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Endpoints HTTP de la API REST. Cada función es una petición:
 * Retrofit genera la implementación y las corrutinas (suspend)
 * hacen que la llamada no bloquee la interfaz.
 */
interface ApiService {

    // GET /posts?userId=1 → lista de publicaciones de un usuario
    @GET("posts")
    suspend fun getPosts(@Query("userId") userId: Int): List<Post>

    // GET /posts/{id} → una publicación
    @GET("posts/{id}")
    suspend fun getPost(@Path("id") id: Int): Post

    // POST /posts → crea una publicación (el servidor devuelve el id asignado)
    @POST("posts")
    suspend fun createPost(@Body post: Post): Post

    // PUT /posts/{id} → reemplaza una publicación existente
    @PUT("posts/{id}")
    suspend fun updatePost(@Path("id") id: Int, @Body post: Post): Post

    // DELETE /posts/{id} → elimina una publicación (responde 200 sin contenido útil)
    @DELETE("posts/{id}")
    suspend fun deletePost(@Path("id") id: Int): Response<Unit>
}
