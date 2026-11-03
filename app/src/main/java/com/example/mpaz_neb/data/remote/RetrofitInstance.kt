package com.example.mpaz_neb.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/** Singleton que contiene la configuración de Retrofit. */
object RetrofitInstance {

    // URL base de la API pública de prueba (datos ficticios)
    const val BASE_URL = "https://jsonplaceholder.typicode.com/"

    // Se instancia el servicio de la API una sola vez (lazy)
    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()) // Conversor JSON <-> Kotlin
            .build()
            .create(ApiService::class.java) // Implementa la interfaz ApiService
    }
}
