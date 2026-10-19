package com.example.mpaz_neb.repository

import android.content.Context
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.mpaz_neb.model.EstadoSesion
import com.example.mpaz_neb.model.ModuloActividad
import com.example.mpaz_neb.model.ResultadoIntento
import com.example.mpaz_neb.model.SesionAula
import java.security.SecureRandom
import java.util.Locale
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

class SesionAulaRepository(context: Context) {
    private val appContext = context.applicationContext

    val sesiones: Flow<List<SesionAula>> = appContext.progresoDataStore.data.map {
        leerSesiones(it)
    }

    val sesionActual: Flow<SesionAula?> = appContext.progresoDataStore.data.map {
        val id = it[SESION_ACTUAL_ID]
        leerSesiones(it).firstOrNull { sesion -> sesion.id == id }
    }

    suspend fun crearSesion(
        modulo: ModuloActividad,
        curso: String,
        estudiante: String
    ): SesionAula {
        require(curso in CURSOS && estudiante in ESTUDIANTES) {
            "Selecciona un curso y un estudiante de la lista."
        }
        lateinit var creada: SesionAula
        appContext.progresoDataStore.edit { preferencias ->
            val anteriores = leerSesiones(preferencias)
            val actual = anteriores.firstOrNull { it.id == preferencias[SESION_ACTUAL_ID] }
            check(actual == null || actual.estado == EstadoSesion.FINALIZADA) {
                "Finaliza la sesión actual antes de generar otra."
            }
            val codigosUsados = anteriores.map { it.codigo }.toSet()
            var codigo: String
            do {
                codigo = "MPAZ-" + (1..6).map { ALFABETO[RANDOM.nextInt(ALFABETO.length)] }
                    .joinToString("")
            } while (codigo in codigosUsados)
            creada = SesionAula(
                id = UUID.randomUUID().toString(), codigo = codigo,
                estudiante = estudiante, curso = curso, modulo = modulo,
                contenido = modulo.contenido, estado = EstadoSesion.CREADA,
                creadaEn = System.currentTimeMillis()
            )
            escribirSesiones(preferencias, anteriores + creada)
            preferencias[SESION_ACTUAL_ID] = creada.id
        }
        return creada
    }

    suspend fun ingresarConCodigo(
        codigo: String,
        moduloRequerido: ModuloActividad? = null
    ): SesionAula {
        val normalizado = codigo.trim().uppercase(Locale.ROOT)
        require(normalizado.isNotBlank()) { "Escribe el código de tu sesión." }
        lateinit var ingresada: SesionAula
        appContext.progresoDataStore.edit { preferencias ->
            val todas = leerSesiones(preferencias)
            val actual = todas.firstOrNull { it.id == preferencias[SESION_ACTUAL_ID] }
            require(actual != null && actual.codigo == normalizado &&
                    actual.estado != EstadoSesion.FINALIZADA) {
                "El código no corresponde a una sesión abierta en este dispositivo."
            }
            require(moduloRequerido == null || actual.modulo == moduloRequerido) {
                "Este código es para ${actual.modulo.etiqueta}. Pulsa Comenzar actividad asignada."
            }
            ingresada = actual.copy(estado = EstadoSesion.EN_CURSO)
            escribirSesiones(preferencias, todas.map {
                if (it.id == actual.id) ingresada else it
            })
        }
        return ingresada
    }

    suspend fun finalizarSesion(id: String) {
        appContext.progresoDataStore.edit { preferencias ->
            val todas = leerSesiones(preferencias)
            require(todas.any { it.id == id }) { "No se encontró la sesión." }
            escribirSesiones(preferencias, todas.map {
                if (it.id == id) it.copy(
                    estado = EstadoSesion.FINALIZADA,
                    finalizadaEn = it.finalizadaEn ?: System.currentTimeMillis()
                ) else it
            })
        }
    }

    suspend fun guardarObservacion(id: String, observacion: String) {
        require(observacion.length <= 1000) { "La observación admite hasta 1000 caracteres." }
        appContext.progresoDataStore.edit { preferencias ->
            val todas = leerSesiones(preferencias)
            require(todas.any { it.id == id }) { "No se encontró la sesión." }
            escribirSesiones(preferencias, todas.map {
                if (it.id == id) it.copy(observacion = observacion.trim()) else it
            })
        }
    }

    companion object {
        val CURSOS = listOf("1.° Año Básico", "2.° Año Básico", "3.° Año Básico", "4.° Año Básico")
        val ESTUDIANTES = listOf("Estudiante 01 - Aula A", "Estudiante 04 - Aula B")
        private val SESIONES_JSON = stringPreferencesKey("sesiones_aula_json_v1")
        private val SESION_ACTUAL_ID = stringPreferencesKey("sesion_aula_actual_id")
        private const val ALFABETO = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        private val RANDOM = SecureRandom()

        internal fun registrarResultadoEn(
            preferencias: MutablePreferences,
            sesionId: String?,
            intentoId: String,
            modulo: ModuloActividad,
            aciertos: Int,
            total: Int,
            fecha: Long
        ) {
            if (sesionId == null) return
            require(total > 0 && aciertos in 0..total)
            val todas = leerSesiones(preferencias)
            val sesion = todas.firstOrNull { it.id == sesionId }
                ?: error("No se encontró la sesión de esta actividad.")
            require(sesion.modulo == modulo) { "La actividad no corresponde a la sesión." }
            if (sesion.resultados.any { it.id == intentoId }) return
            check(sesion.estado == EstadoSesion.EN_CURSO ||
                    sesion.estado == EstadoSesion.COMPLETADA) {
                "La sesión ya no admite respuestas."
            }
            val actualizada = sesion.copy(
                estado = EstadoSesion.COMPLETADA,
                resultados = sesion.resultados + ResultadoIntento(intentoId, aciertos, total, fecha)
            )
            escribirSesiones(preferencias, todas.map {
                if (it.id == sesionId) actualizada else it
            })
        }

        private fun leerSesiones(preferencias: Preferences): List<SesionAula> {
            val lista = JSONArray(preferencias[SESIONES_JSON] ?: "[]")
            return List(lista.length()) { indice ->
                val objeto = lista.getJSONObject(indice)
                val intentos = objeto.getJSONArray("resultados")
                SesionAula(
                    id = objeto.getString("id"), codigo = objeto.getString("codigo"),
                    estudiante = objeto.getString("estudiante"), curso = objeto.getString("curso"),
                    modulo = ModuloActividad.valueOf(objeto.getString("modulo")),
                    contenido = objeto.getString("contenido"),
                    estado = EstadoSesion.valueOf(objeto.getString("estado")),
                    creadaEn = objeto.getLong("creadaEn"),
                    observacion = objeto.optString("observacion", ""),
                    finalizadaEn = if (objeto.isNull("finalizadaEn")) null
                    else objeto.getLong("finalizadaEn"),
                    resultados = List(intentos.length()) { i ->
                        val intento = intentos.getJSONObject(i)
                        ResultadoIntento(intento.getString("id"), intento.getInt("aciertos"),
                            intento.getInt("total"), intento.getLong("fecha"))
                    }
                )
            }
        }

        private fun escribirSesiones(preferencias: MutablePreferences, sesiones: List<SesionAula>) {
            val lista = JSONArray()
            sesiones.forEach { sesion ->
                val intentos = JSONArray()
                sesion.resultados.forEach {
                    intentos.put(JSONObject().put("id", it.id).put("aciertos", it.aciertos)
                        .put("total", it.total).put("fecha", it.fecha))
                }
                lista.put(JSONObject().put("id", sesion.id).put("codigo", sesion.codigo)
                    .put("estudiante", sesion.estudiante).put("curso", sesion.curso)
                    .put("modulo", sesion.modulo.name).put("contenido", sesion.contenido)
                    .put("estado", sesion.estado.name).put("creadaEn", sesion.creadaEn)
                    .put("observacion", sesion.observacion)
                    .put("finalizadaEn", sesion.finalizadaEn ?: JSONObject.NULL)
                    .put("resultados", intentos))
            }
            preferencias[SESIONES_JSON] = lista.toString()
        }
    }
}