package com.example.mpaz_neb.data.model

import com.example.mpaz_neb.model.PerfilUsuario
import com.example.mpaz_neb.model.UsuarioSesion

/** Cuerpo del POST /api/auth/login. */
data class LoginDto(
    val correo: String,
    val clave: String
)

/** Cuenta que responde ms-usuarios. Nunca trae la contraseña ni su hash. */
data class UsuarioDto(
    val id: Long,
    val alias: String,
    val correo: String,
    val perfil: PerfilUsuario,
    val activa: Boolean,
    val ultimoAcceso: String?       // Fecha ISO-8601
)

fun UsuarioDto.aUsuarioSesion(): UsuarioSesion = UsuarioSesion(
    id = id,
    alias = alias,
    perfil = perfil,
    ultimoAcceso = isoAMilis(ultimoAcceso) ?: System.currentTimeMillis()
)
