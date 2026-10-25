package com.example.mpaz_neb.model

data class TeacherLoginUiState(
    val correo: String = "",
    val clave: String = "",
    val errorCorreo: String? = null,
    val errorClave: String? = null,
    val errorGeneral: String? = null,
    val ingresando: Boolean = false
)
