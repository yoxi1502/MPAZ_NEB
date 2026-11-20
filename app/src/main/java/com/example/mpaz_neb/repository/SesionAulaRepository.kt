package com.example.mpaz_neb.repository

import com.example.mpaz_neb.data.local.SesionDispositivoStore
import com.example.mpaz_neb.data.model.IngresoDto
import com.example.mpaz_neb.data.model.ObservacionDto
import com.example.mpaz_neb.data.model.ResultadoRequestDto
import com.example.mpaz_neb.data.model.SesionRequestDto
import com.example.mpaz_neb.data.model.aModelo
import com.example.mpaz_neb.data.remote.ErrorServidor
import com.example.mpaz_neb.data.remote.RetrofitInstance
import com.example.mpaz_neb.data.remote.SesionesApi
import com.example.mpaz_neb.data.remote.exigirExito
import com.example.mpaz_neb.data.remote.llamarServidor
import com.example.mpaz_neb.model.ModuloActividad
import com.example.mpaz_neb.model.SesionAula
import com.example.mpaz_neb.validacion.Validadores

/**
 * Sesiones de aula guardadas en el microservicio ms-sesiones.
 * En el dispositivo solo se recuerda el id de la sesión abierta (SesionDispositivoStore).
 */
class SesionAulaRepository(
    private val dispositivo: SesionDispositivoStore,
    private val api: SesionesApi = RetrofitInstance.sesionesApi
) {

    /** Historial del servidor; si viene [estudiante], el servidor filtra por él. */
    suspend fun listar(estudiante: String? = null): List<SesionAula> = llamarServidor {
        api.listar(estudiante).map { it.aModelo() }
    }

    /**
     * Sesión abierta en este dispositivo, leída desde el servidor.
     * Si el servidor ya no la tiene (404), se olvida el id guardado.
     */
    suspend fun obtenerSesionActual(): SesionAula? {
        val id = dispositivo.obtenerSesionActualId() ?: return null
        return try {
            llamarServidor { api.obtener(id).aModelo() }
        } catch (e: ErrorServidor) {
            if (e.codigoHttp == 404) {
                dispositivo.limpiar()
                null
            } else {
                throw e
            }
        }
    }

    /** POST: el servidor genera el código y valida que la asistente no tenga otra sesión abierta. */
    suspend fun crearSesion(
        modulo: ModuloActividad,
        curso: String,
        estudiante: String,
        asistente: String,
        contenido: String,
        actividadId: Long?
    ): SesionAula {
        require(curso in CURSOS && estudiante in ESTUDIANTES) {
            "Selecciona un curso y un estudiante de la lista."
        }
        val creada = llamarServidor {
            api.crear(SesionRequestDto(modulo, curso, estudiante, asistente, contenido, actividadId)).aModelo()
        }
        dispositivo.guardarSesionActual(creada.id)
        return creada
    }

    /** El estudiante escribe su código: la sesión pasa a "En curso" en el servidor. */
    suspend fun ingresarConCodigo(codigo: String, moduloRequerido: ModuloActividad? = null): SesionAula {
        val normalizado = Validadores.normalizarCodigo(codigo)
        require(normalizado.isNotBlank()) { "Escribe el código de tu sesión." }
        val sesion = llamarServidor { api.ingresar(IngresoDto(normalizado, moduloRequerido)).aModelo() }
        dispositivo.guardarSesionActual(sesion.id)
        return sesion
    }

    /** POST del resultado. Reenviar el mismo [intentoId] no duplica el resultado. */
    suspend fun registrarResultado(sesionId: Long, intentoId: String, aciertos: Int, total: Int): SesionAula {
        require(total > 0 && aciertos in 0..total) { "El resultado no es válido." }
        return llamarServidor {
            api.registrarResultado(sesionId, ResultadoRequestDto(intentoId, aciertos, total)).aModelo()
        }
    }

    suspend fun guardarObservacion(id: Long, observacion: String): SesionAula {
        Validadores.validarObservacion(observacion)?.let { error -> throw IllegalArgumentException(error) }
        return llamarServidor { api.actualizarObservacion(id, ObservacionDto(observacion.trim())).aModelo() }
    }

    suspend fun finalizarSesion(id: Long): SesionAula =
        llamarServidor { api.finalizar(id).aModelo() }

    /** DELETE: el servidor solo permite eliminar sesiones finalizadas. */
    suspend fun eliminar(id: Long) {
        llamarServidor { api.eliminar(id).exigirExito() }
        if (dispositivo.obtenerSesionActualId() == id) dispositivo.limpiar()
    }

    companion object {
        val CURSOS = listOf("1.° Año Básico", "2.° Año Básico", "3.° Año Básico", "4.° Año Básico")
        val ESTUDIANTES = listOf("Estudiante 01 - Aula A", "Estudiante 04 - Aula B")
    }
}
