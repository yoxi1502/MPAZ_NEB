package com.example.mpaz_neb.repository

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.mpaz_neb.model.Actividad
import com.example.mpaz_neb.model.Dificultad
import com.example.mpaz_neb.model.EstadoActividad
import com.example.mpaz_neb.model.ModuloActividad
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.catalogoDataStore by preferencesDataStore(name = "catalogo_actividades")

/**
 * Catálogo de actividades guardado localmente en DataStore (como JSON).
 * La primera vez se cargan dos actividades de ejemplo, una por módulo.
 */
class ActividadRepository(context: Context) {

    private val appContext = context.applicationContext

    val actividades: Flow<List<Actividad>> =
        appContext.catalogoDataStore.data.map { leer(it) }

    suspend fun guardar(actividad: Actividad): Actividad {
        val guardada = actividad.copy(
            id = actividad.id.ifBlank { UUID.randomUUID().toString() },
            actualizadaEn = System.currentTimeMillis()
        )
        appContext.catalogoDataStore.edit { preferencias ->
            val actuales = leer(preferencias)
            val existe = actuales.any { it.id == guardada.id }
            val nuevas = if (existe) {
                actuales.map { if (it.id == guardada.id) guardada else it }
            } else {
                actuales + guardada
            }
            preferencias[CATALOGO_JSON] = escribir(nuevas)
        }
        return guardada
    }

    suspend fun cambiarEstado(id: String, estado: EstadoActividad) {
        appContext.catalogoDataStore.edit { preferencias ->
            val actuales = leer(preferencias)
            require(actuales.any { it.id == id }) { "No se encontró la actividad." }
            preferencias[CATALOGO_JSON] = escribir(
                actuales.map {
                    if (it.id == id) it.copy(estado = estado, actualizadaEn = System.currentTimeMillis())
                    else it
                }
            )
        }
    }

    private fun leer(preferencias: Preferences): List<Actividad> {
        val json = preferencias[CATALOGO_JSON] ?: return ACTIVIDADES_INICIALES
        val lista = JSONArray(json)
        return List(lista.length()) { i ->
            val o = lista.getJSONObject(i)
            Actividad(
                id = o.getString("id"),
                nombre = o.getString("nombre"),
                modulo = ModuloActividad.valueOf(o.getString("modulo")),
                curso = o.getString("curso"),
                contenido = o.getString("contenido"),
                objetivo = o.getString("objetivo"),
                dificultad = Dificultad.valueOf(o.getString("dificultad")),
                instrucciones = o.getString("instrucciones"),
                cantidadPreguntas = o.getInt("cantidadPreguntas"),
                intentosPermitidos = o.getInt("intentosPermitidos"),
                estado = EstadoActividad.valueOf(o.getString("estado")),
                actualizadaEn = o.getLong("actualizadaEn")
            )
        }
    }

    private fun escribir(actividades: List<Actividad>): String {
        val lista = JSONArray()
        actividades.forEach { a ->
            lista.put(
                JSONObject()
                    .put("id", a.id)
                    .put("nombre", a.nombre)
                    .put("modulo", a.modulo.name)
                    .put("curso", a.curso)
                    .put("contenido", a.contenido)
                    .put("objetivo", a.objetivo)
                    .put("dificultad", a.dificultad.name)
                    .put("instrucciones", a.instrucciones)
                    .put("cantidadPreguntas", a.cantidadPreguntas)
                    .put("intentosPermitidos", a.intentosPermitidos)
                    .put("estado", a.estado.name)
                    .put("actualizadaEn", a.actualizadaEn)
            )
        }
        return lista.toString()
    }

    private companion object {
        val CATALOGO_JSON = stringPreferencesKey("catalogo_json_v1")

        val ACTIVIDADES_INICIALES = listOf(
            Actividad(
                id = "act-lectura-01",
                nombre = "Una tarde en el parque",
                modulo = ModuloActividad.LECTURA,
                curso = "2.° Año Básico",
                contenido = "Comprensión lectora",
                objetivo = "Responder preguntas explícitas, implícitas y de vocabulario de un texto breve.",
                dificultad = Dificultad.INICIAL,
                instrucciones = "Lee la historia con calma, elige una alternativa y comprueba tu respuesta.",
                cantidadPreguntas = 3,
                intentosPermitidos = 2,
                estado = EstadoActividad.DISPONIBLE,
                actualizadaEn = 0L
            ),
            Actividad(
                id = "act-mate-01",
                nombre = "Sumas y restas hasta 10",
                modulo = ModuloActividad.MATEMATICA,
                curso = "1.° Año Básico",
                contenido = "Adición y sustracción",
                objetivo = "Resolver sumas y restas simples con números hasta 10.",
                dificultad = Dificultad.INICIAL,
                instrucciones = "Resuelve la operación, elige una respuesta y comprueba.",
                cantidadPreguntas = 3,
                intentosPermitidos = 2,
                estado = EstadoActividad.DISPONIBLE,
                actualizadaEn = 0L
            )
        )
    }
}
