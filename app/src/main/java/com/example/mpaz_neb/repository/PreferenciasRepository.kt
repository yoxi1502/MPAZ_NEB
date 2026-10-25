package com.example.mpaz_neb.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.example.mpaz_neb.model.PreferenciasDispositivo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.preferenciasDataStore by preferencesDataStore(name = "preferencias_dispositivo")

/** Guarda en DataStore si el dispositivo usa lectura en voz alta. */
class PreferenciasRepository(context: Context) {

    private val appContext = context.applicationContext

    val preferencias: Flow<PreferenciasDispositivo> =
        appContext.preferenciasDataStore.data.map { datos ->
            PreferenciasDispositivo(
                lecturaEnVoz = datos[LECTURA_EN_VOZ] ?: true
            )
        }

    suspend fun guardarLecturaEnVoz(activa: Boolean) {
        appContext.preferenciasDataStore.edit { it[LECTURA_EN_VOZ] = activa }
    }

    private companion object {
        val LECTURA_EN_VOZ = booleanPreferencesKey("lectura_en_voz")
    }
}
