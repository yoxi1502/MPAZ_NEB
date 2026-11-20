package com.example.mpaz_neb.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.mpaz_neb.model.PerfilUsuario
import com.example.mpaz_neb.model.UsuarioSesion
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.usuarioDataStore by preferencesDataStore(name = "usuario_sesion")

/**
 * Recuerda en DataStore el usuario que inició sesión (login persistente).
 * Solo guarda id, nombre, perfil y último acceso: nunca la contraseña.
 */
class UsuarioSesionStore(context: Context) {

    private val dataStore = context.applicationContext.usuarioDataStore

    val usuario: Flow<UsuarioSesion?> = dataStore.data.map { preferencias ->
        val id = preferencias[USUARIO_ID] ?: return@map null
        val perfil = preferencias[PERFIL]
            ?.let { nombre -> PerfilUsuario.entries.firstOrNull { it.name == nombre } }
            ?: return@map null
        UsuarioSesion(
            id = id,
            alias = preferencias[ALIAS].orEmpty(),
            perfil = perfil,
            ultimoAcceso = preferencias[ULTIMO_ACCESO] ?: 0L
        )
    }

    suspend fun guardar(usuario: UsuarioSesion) {
        dataStore.edit { preferencias ->
            preferencias[USUARIO_ID] = usuario.id
            preferencias[ALIAS] = usuario.alias
            preferencias[PERFIL] = usuario.perfil.name
            preferencias[ULTIMO_ACCESO] = usuario.ultimoAcceso
        }
    }

    suspend fun limpiar() {
        dataStore.edit { it.clear() }
    }

    private companion object {
        // Clave nueva: la versión anterior guardaba el id como texto
        val USUARIO_ID = longPreferencesKey("usuario_id_servidor")
        val ALIAS = stringPreferencesKey("alias")
        val PERFIL = stringPreferencesKey("perfil")
        val ULTIMO_ACCESO = longPreferencesKey("ultimo_acceso")
    }
}
