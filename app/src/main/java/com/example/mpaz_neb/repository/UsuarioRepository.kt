package com.example.mpaz_neb.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.mpaz_neb.model.CuentaPrueba
import com.example.mpaz_neb.model.PerfilUsuario
import com.example.mpaz_neb.model.UsuarioSesion
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.usuarioDataStore by preferencesDataStore(name = "usuario_sesion")

/**
 * Maneja las cuentas de prueba del equipo pedagógico y guarda en DataStore
 * qué usuario tiene la sesión abierta y su fecha de último acceso.
 */
class UsuarioRepository(context: Context) {

    private val appContext = context.applicationContext

    val usuarioActual: Flow<UsuarioSesion?> =
        appContext.usuarioDataStore.data.map { preferencias ->
            val id = preferencias[USUARIO_ID] ?: return@map null
            val cuenta = CUENTAS_PRUEBA.firstOrNull { it.id == id && it.activa }
                ?: return@map null
            UsuarioSesion(
                id = cuenta.id,
                alias = cuenta.alias,
                perfil = cuenta.perfil,
                ultimoAcceso = preferencias[ULTIMO_ACCESO] ?: 0L
            )
        }

    suspend fun iniciarSesion(correo: String, clave: String): UsuarioSesion {
        val cuenta = CUENTAS_PRUEBA.firstOrNull {
            it.correo.equals(correo.trim(), ignoreCase = true) && it.clave == clave
        } ?: throw IllegalArgumentException("Correo o contraseña incorrectos.")

        check(cuenta.activa) { "Esta cuenta de prueba está desactivada." }

        val ahora = System.currentTimeMillis()
        appContext.usuarioDataStore.edit { preferencias ->
            preferencias[USUARIO_ID] = cuenta.id
            preferencias[ULTIMO_ACCESO] = ahora
        }
        return UsuarioSesion(cuenta.id, cuenta.alias, cuenta.perfil, ahora)
    }

    suspend fun cerrarSesion() {
        appContext.usuarioDataStore.edit { it.remove(USUARIO_ID) }
    }

    companion object {
        private val USUARIO_ID = stringPreferencesKey("usuario_id")
        private val ULTIMO_ACCESO = longPreferencesKey("ultimo_acceso")

        /** Cuentas ficticias para la demostración del MVP. */
        val CUENTAS_PRUEBA = listOf(
            CuentaPrueba(
                id = "usr-asis-01",
                alias = "Asistente Aula A",
                correo = "asistente@mpaz.cl",
                clave = "Asistente123",
                perfil = PerfilUsuario.ASISTENTE
            ),
            CuentaPrueba(
                id = "usr-utp-01",
                alias = "Docente UTP",
                correo = "utp@mpaz.cl",
                clave = "Docente123",
                perfil = PerfilUsuario.DOCENTE_UTP
            )
        )
    }
}
