package com.example.mpaz_neb.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.sesionDispositivoDataStore by preferencesDataStore(name = "sesion_dispositivo")

/**
 * Recuerda en DataStore qué sesión está abierta en ESTE dispositivo.
 * Los datos de la sesión viven en ms-sesiones; aquí solo se guarda su id,
 * para que la actividad del estudiante sepa a qué sesión enviar el resultado.
 */
class SesionDispositivoStore(context: Context) {

    private val dataStore = context.applicationContext.sesionDispositivoDataStore

    suspend fun obtenerSesionActualId(): Long? =
        dataStore.data.map { it[SESION_ACTUAL_ID] }.first()

    suspend fun guardarSesionActual(id: Long) {
        dataStore.edit { it[SESION_ACTUAL_ID] = id }
    }

    suspend fun limpiar() {
        dataStore.edit { it.remove(SESION_ACTUAL_ID) }
    }

    private companion object {
        val SESION_ACTUAL_ID = longPreferencesKey("sesion_actual_id")
    }
}
