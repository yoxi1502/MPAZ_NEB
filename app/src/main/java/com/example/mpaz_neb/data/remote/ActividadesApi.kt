package com.example.mpaz_neb.data.remote

import com.example.mpaz_neb.data.model.ActividadDto
import com.example.mpaz_neb.data.model.ActividadRequestDto
import com.example.mpaz_neb.data.model.CambioEstadoDto
import com.example.mpaz_neb.model.EstadoActividad
import com.example.mpaz_neb.model.ModuloActividad
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/** Endpoints del microservicio ms-actividades (puerto 8081). */
interface ActividadesApi {

    // GET /api/actividades?modulo=&curso=&estado= → catálogo (los filtros null no se envían)
    @GET("api/actividades")
    suspend fun listar(
        @Query("modulo") modulo: ModuloActividad?,
        @Query("curso") curso: String?,
        @Query("estado") estado: EstadoActividad?
    ): List<ActividadDto>

    // GET /api/actividades/{id} → una actividad
    @GET("api/actividades/{id}")
    suspend fun obtener(@Path("id") id: Long): ActividadDto

    // POST /api/actividades → crea (responde 201 con la actividad y su id)
    @POST("api/actividades")
    suspend fun crear(@Body actividad: ActividadRequestDto): ActividadDto

    // PUT /api/actividades/{id} → reemplaza todos los datos
    @PUT("api/actividades/{id}")
    suspend fun actualizar(@Path("id") id: Long, @Body actividad: ActividadRequestDto): ActividadDto

    // PATCH /api/actividades/{id}/estado → solo cambia el estado (activar / desactivar)
    @PATCH("api/actividades/{id}/estado")
    suspend fun cambiarEstado(@Path("id") id: Long, @Body cambio: CambioEstadoDto): ActividadDto

    // DELETE /api/actividades/{id} → responde 204 sin contenido
    @DELETE("api/actividades/{id}")
    suspend fun eliminar(@Path("id") id: Long): Response<Unit>
}
