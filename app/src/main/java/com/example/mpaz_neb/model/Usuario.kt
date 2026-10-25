package com.example.mpaz_neb.model

/** Perfiles con acceso protegido definidos en el caso. */
enum class PerfilUsuario(val etiqueta: String) {
    ASISTENTE("Asistente de aula"),
    DOCENTE_UTP("Docente / UTP")
}

/** Cuenta de prueba ficticia (no contiene datos personales reales). */
data class CuentaPrueba(
    val id: String,
    val alias: String,
    val correo: String,
    val clave: String,
    val perfil: PerfilUsuario,
    val activa: Boolean = true
)

/** Usuario con sesión iniciada en el dispositivo. */
data class UsuarioSesion(
    val id: String,
    val alias: String,
    val perfil: PerfilUsuario,
    val ultimoAcceso: Long
)

data class UsuarioSesionUiState(
    val cargando: Boolean = true,
    val usuario: UsuarioSesion? = null
)
