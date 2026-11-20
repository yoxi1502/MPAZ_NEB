package com.example.mpaz_neb.data.remote

import com.example.mpaz_neb.BuildConfig
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Singleton con la configuración de Retrofit.
 * Hay un Retrofit por cada URL base (API externa y cada microservicio),
 * pero todos comparten el mismo cliente HTTP.
 */
object RetrofitInstance {

    // URL base de la API pública de prueba (datos ficticios)
    const val BASE_URL = "https://jsonplaceholder.typicode.com/"

    // API externa pública de Pokémon (sin clave)
    const val URL_POKEAPI = "https://pokeapi.co/api/v2/"

    // Microservicios propios (Spring Boot) que corren en el computador
    private val HOST = BuildConfig.HOST_SERVIDOR
    val URL_ACTIVIDADES = "http://$HOST:8081/"
    val URL_SESIONES = "http://$HOST:8082/"

    /** Cliente HTTP compartido: tiempos de espera y registro de las peticiones en Logcat. */
    private val clienteHttp: OkHttpClient by lazy {
        val registro = HttpLoggingInterceptor().apply {
            // BASIC muestra método, URL, código y tiempo, sin el cuerpo (no expone contraseñas)
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BASIC
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(registro)
            .build()
    }

    private fun crearRetrofit(urlBase: String): Retrofit =
        Retrofit.Builder()
            .baseUrl(urlBase)
            .client(clienteHttp)
            .addConverterFactory(GsonConverterFactory.create()) // Conversor JSON <-> Kotlin
            .build()

    // Cada servicio se instancia una sola vez (lazy), la primera vez que se usa
    val api: ApiService by lazy {
        crearRetrofit(BASE_URL).create(ApiService::class.java)
    }

    val pokeApi: PokeApi by lazy {
        crearRetrofit(URL_POKEAPI).create(PokeApi::class.java)
    }

    val actividadesApi: ActividadesApi by lazy {
        crearRetrofit(URL_ACTIVIDADES).create(ActividadesApi::class.java)
    }

    val sesionesApi: SesionesApi by lazy {
        crearRetrofit(URL_SESIONES).create(SesionesApi::class.java)
    }
}
