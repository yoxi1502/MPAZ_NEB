package com.example.mpaz_neb.repository

import com.example.mpaz_neb.data.local.UsuarioSesionStore
import com.example.mpaz_neb.data.model.LoginDto
import com.example.mpaz_neb.data.model.aUsuarioSesion
import com.example.mpaz_neb.data.remote.RetrofitInstance
import com.example.mpaz_neb.data.remote.UsuariosApi
import com.example.mpaz_neb.data.remote.llamarServidor
import com.example.mpaz_neb.model.UsuarioSesion
import kotlinx.coroutines.flow.Flow

/**
 * Login del equipo pedagógico contra el microservicio ms-usuarios
 * (la contraseña se verifica en el servidor con BCrypt).
 * Si el login es correcto, el usuario se recuerda en el dispositivo.
 */
class UsuarioRepository(
    private val local: UsuarioSesionStore,
    private val api: UsuariosApi = RetrofitInstance.usuariosApi
) {

    val usuarioActual: Flow<UsuarioSesion?> = local.usuario

    suspend fun iniciarSesion(correo: String, clave: String): UsuarioSesion {
        val usuario = llamarServidor {
            api.login(LoginDto(correo = correo.trim(), clave = clave)).aUsuarioSesion()
        }
        local.guardar(usuario)
        return usuario
    }

    suspend fun cerrarSesion() {
        local.limpiar()
    }
}
