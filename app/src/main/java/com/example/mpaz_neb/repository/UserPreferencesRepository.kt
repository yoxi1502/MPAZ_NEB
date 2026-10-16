package com.example.mpaz_neb.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Extensión para inicializar DataStore
val Context.dataStore by preferencesDataStore(name = "user_preferences")

class UserPreferencesRepository(private val context: Context) {

    companion object {
        val MODO_ESPECIAL_KEY = booleanPreferencesKey("modo_especial_activo")
    }

    // Flujo para leer el estado guardado
    val modoEspecialFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[MODO_ESPECIAL_KEY] ?: false
    }

    // Función para guardar el nuevo estado
    suspend fun guardarModoEspecial(activo: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[MODO_ESPECIAL_KEY] = activo
        }
    }
}