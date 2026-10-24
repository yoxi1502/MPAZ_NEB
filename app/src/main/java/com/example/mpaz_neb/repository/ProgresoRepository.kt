package com.example.mpaz_neb.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.mpaz_neb.model.ModuloActividad
import java.util.UUID
import com.example.mpaz_neb.model.ProgresoUiState
import com.example.mpaz_neb.model.ResultadoActividad
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal val Context.progresoDataStore by preferencesDataStore(
    name = "progreso_actividades"
)

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

            val lecturaAciertos = preferencias[LECTURA_ACIERTOS]
            val lecturaTotal = preferencias[LECTURA_TOTAL]
            val lecturaFecha = preferencias[LECTURA_FECHA]

            val matematicaAciertos = preferencias[MATEMATICA_ACIERTOS]
            val matematicaTotal = preferencias[MATEMATICA_TOTAL]
            val matematicaFecha = preferencias[MATEMATICA_FECHA]

            val lectura = if (
                lecturaAciertos != null &&
                lecturaTotal != null &&
                lecturaFecha != null
            ) {
                ResultadoActividad(
                    aciertos = lecturaAciertos,
                    total = lecturaTotal,
                    fecha = lecturaFecha
                )
            } else {
                null
            }

            val matematica = if (
                matematicaAciertos != null &&
                matematicaTotal != null &&
                matematicaFecha != null
            ) {
                ResultadoActividad(
                    aciertos = matematicaAciertos,
                    total = matematicaTotal,
                    fecha = matematicaFecha
                )
            } else {
                null
            }

            ProgresoUiState(
                lectura = lectura,
                matematica = matematica,
                cargando = false
            )
        }

    suspend fun guardarLectura(
        aciertos: Int,
        total: Int,
        sesionId: String? = null,
        intentoId: String = UUID.randomUUID().toString()
    ) {
        require(total > 0 && aciertos in 0..total)

        appContext.progresoDataStore.edit { preferencias ->
            preferencias[LECTURA_ACIERTOS] = aciertos
            preferencias[LECTURA_TOTAL] = total
            val fecha = System.currentTimeMillis()
            preferencias[LECTURA_FECHA] = fecha
            SesionAulaRepository.registrarResultadoEn(
                preferencias, sesionId, intentoId, ModuloActividad.LECTURA,
                aciertos, total, fecha
            )
        }
    }

    suspend fun guardarMatematica(
        aciertos: Int,
        total: Int,
        sesionId: String? = null,
        intentoId: String = UUID.randomUUID().toString()
    ) {
        require(total > 0 && aciertos in 0..total)

        appContext.progresoDataStore.edit { preferencias ->
            preferencias[MATEMATICA_ACIERTOS] = aciertos
            preferencias[MATEMATICA_TOTAL] = total
            val fecha = System.currentTimeMillis()
            preferencias[MATEMATICA_FECHA] = fecha
            SesionAulaRepository.registrarResultadoEn(
                preferencias, sesionId, intentoId, ModuloActividad.MATEMATICA,
                aciertos, total, fecha
            )
        }
    }
}