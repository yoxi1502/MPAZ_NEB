package com.example.mpaz_neb.data.remote

import com.example.mpaz_neb.data.model.ErrorDto
import com.google.gson.Gson
import com.google.gson.JsonParseException
import java.io.IOException
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import retrofit2.Response

/** Error al comunicarse con un microservicio, con un mensaje listo para mostrar en pantalla. */
class ErrorServidor(
    mensaje: String,
    val codigoHttp: Int? = null,
    causa: Throwable? = null
) : Exception(mensaje, causa)

const val MENSAJE_SIN_CONEXION =
    "No hay conexión con el servidor. Revisa que los microservicios estén encendidos."

/**
 * Ejecuta una llamada de Retrofit y traduce sus errores a [ErrorServidor]:
 * - Sin red o servidor apagado (IOException) → mensaje de conexión.
 * - Respuesta 4xx/5xx (HttpException) → el "mensaje" que envió el microservicio.
 * Los repositorios la usan para que los ViewModels reciban siempre un error claro.
 */
suspend fun <T> llamarServidor(llamada: suspend () -> T): T =
    try {
        llamada()
    } catch (cancelacion: CancellationException) {
        throw cancelacion
    } catch (e: HttpException) {
        throw ErrorServidor(mensajeDeError(e.code(), e.response()?.errorBody()?.string()), e.code(), e)
    } catch (e: IOException) {
        throw ErrorServidor(MENSAJE_SIN_CONEXION, causa = e)
    }

/** Para los DELETE que devuelven Response<Unit>: si el código no es 2xx, lanza HttpException. */
fun Response<*>.exigirExito() {
    if (!isSuccessful) throw HttpException(this)
}

/** Lee el JSON de error del microservicio; si no se puede, usa un mensaje genérico. */
internal fun mensajeDeError(codigo: Int, cuerpo: String?): String {
    val error = try {
        cuerpo?.let { Gson().fromJson(it, ErrorDto::class.java) }
    } catch (e: JsonParseException) {
        null
    }
    // Si hay errores por campo, el primero es el más útil para la persona
    return error?.campos?.values?.firstOrNull()
        ?: error?.mensaje
        ?: "El servidor respondió con error $codigo."
}
