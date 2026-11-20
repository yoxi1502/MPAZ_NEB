package com.example.mpaz_neb.data.remote

import com.example.mpaz_neb.data.model.IngresoDto
import com.example.mpaz_neb.data.model.ObservacionDto
import com.example.mpaz_neb.data.model.ResultadoRequestDto
import com.example.mpaz_neb.data.model.SesionDto
import com.example.mpaz_neb.data.model.SesionRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/** Endpoints del microservicio ms-sesiones (puerto 8082). */
interface SesionesApi {

    // GET /api/sesiones?estudiante= → historial, de la más reciente a la más antigua
    @GET("api/sesiones")
    suspend fun listar(@Query("estudiante") estudiante: String?): List<SesionDto>

    // GET /api/sesiones/{id} → una sesión con sus resultados
    @GET("api/sesiones/{id}")
    suspend fun obtener(@Path("id") id: Long): SesionDto

    // POST /api/sesiones → crea la sesión; el servidor genera el código MPAZ-XXXXXX
    @POST("api/sesiones")
    suspend fun crear(@Body sesion: SesionRequestDto): SesionDto

    // POST /api/sesiones/ingreso → el estudiante entra con su código
    @POST("api/sesiones/ingreso")
    suspend fun ingresar(@Body ingreso: IngresoDto): SesionDto

    // POST /api/sesiones/{id}/resultados → guarda el resultado de un intento
    @POST("api/sesiones/{id}/resultados")
    suspend fun registrarResultado(@Path("id") id: Long, @Body resultado: ResultadoRequestDto): SesionDto

    // PATCH /api/sesiones/{id}/observacion → observación pedagógica
    @PATCH("api/sesiones/{id}/observacion")
    suspend fun actualizarObservacion(@Path("id") id: Long, @Body observacion: ObservacionDto): SesionDto

    // PATCH /api/sesiones/{id}/finalizar → cierra la sesión
    @PATCH("api/sesiones/{id}/finalizar")
    suspend fun finalizar(@Path("id") id: Long): SesionDto

    // DELETE /api/sesiones/{id} → solo sesiones finalizadas (si no, 409)
    @DELETE("api/sesiones/{id}")
    suspend fun eliminar(@Path("id") id: Long): Response<Unit>
}
