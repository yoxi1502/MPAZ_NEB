package com.example.mpaz_neb.model

/** Perfiles con acceso protegido definidos en el caso. */
enum class PerfilUsuario(val etiqueta: String) {
    ASISTENTE("Asistente de aula"),
    DOCENTE_UTP("Docente / UTP")
}

/**
 * Usuario con sesión iniciada en el dispositivo.
 * Lo entrega ms-usuarios al hacer login (sin la contraseña) y se recuerda en DataStore.
 */
data class UsuarioSesion(
    val id: Long,
    val alias: String,
    val perfil: PerfilUsuario,
    val ultimoAcceso: Long
)

data class UsuarioSesionUiState(
    val cargando: Boolean = true,
    val usuario: UsuarioSesion? = null
)
