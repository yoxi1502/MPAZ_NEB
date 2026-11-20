package com.example.mpaz_neb.repository

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.mpaz_neb.model.ProgresoUiState
import com.example.mpaz_neb.model.ResultadoActividad
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.progresoDataStore by preferencesDataStore(
    name = "progreso_actividades"
)

/**
 * Último resultado de cada módulo en ESTE dispositivo (DataStore).
 * El historial completo de las sesiones se guarda en el microservicio ms-sesiones.
 */
class ProgresoRepository(context: Context) {

    private val appContext = context.applicationContext

    private companion object {
        val LECTURA_ACIERTOS = intPreferencesKey("lectura_aciertos")
        val LECTURA_TOTAL = intPreferencesKey("lectura_total")
        val LECTURA_FECHA = longPreferencesKey("lectura_fecha")

        val MATEMATICA_ACIERTOS = intPreferencesKey("matematica_aciertos")
        val MATEMATICA_TOTAL = intPreferencesKey("matematica_total")
        val MATEMATICA_FECHA = longPreferencesKey("matematica_fecha")
    }

    val progreso: Flow<ProgresoUiState> =
        appContext.progresoDataStore.data.map { preferencias ->
            ProgresoUiState(
                lectura = leerResultado(preferencias, LECTURA_ACIERTOS, LECTURA_TOTAL, LECTURA_FECHA),
                matematica = leerResultado(
                    preferencias, MATEMATICA_ACIERTOS, MATEMATICA_TOTAL, MATEMATICA_FECHA
                ),
                cargando = false
            )
        }

    suspend fun guardarLectura(aciertos: Int, total: Int) {
        require(total > 0 && aciertos in 0..total)
        appContext.progresoDataStore.edit { preferencias ->
            preferencias[LECTURA_ACIERTOS] = aciertos
            preferencias[LECTURA_TOTAL] = total
            preferencias[LECTURA_FECHA] = System.currentTimeMillis()
        }
    }

    suspend fun guardarMatematica(aciertos: Int, total: Int) {
        require(total > 0 && aciertos in 0..total)
        appContext.progresoDataStore.edit { preferencias ->
            preferencias[MATEMATICA_ACIERTOS] = aciertos
            preferencias[MATEMATICA_TOTAL] = total
            preferencias[MATEMATICA_FECHA] = System.currentTimeMillis()
        }
    }

    private fun leerResultado(
        preferencias: Preferences,
        claveAciertos: Preferences.Key<Int>,
        claveTotal: Preferences.Key<Int>,
        claveFecha: Preferences.Key<Long>
    ): ResultadoActividad? {
        val aciertos = preferencias[claveAciertos] ?: return null
        val total = preferencias[claveTotal] ?: return null
        val fecha = preferencias[claveFecha] ?: return null
        return ResultadoActividad(aciertos = aciertos, total = total, fecha = fecha)
    }
}
