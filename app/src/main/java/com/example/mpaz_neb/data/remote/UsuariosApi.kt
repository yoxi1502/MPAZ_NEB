package com.example.mpaz_neb.data.remote

import com.example.mpaz_neb.data.model.LoginDto
import com.example.mpaz_neb.data.model.UsuarioDto
import retrofit2.http.Body
import retrofit2.http.POST

/** Endpoints del microservicio ms-usuarios (puerto 8083). */
interface UsuariosApi {

    // POST /api/auth/login → 200 con la cuenta, 401 clave incorrecta, 403 cuenta desactivada
    @POST("api/auth/login")
    suspend fun login(@Body credenciales: LoginDto): UsuarioDto
}
