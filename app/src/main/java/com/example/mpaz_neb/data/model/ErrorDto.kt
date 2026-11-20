package com.example.mpaz_neb.data.model

/**
 * Cuerpo de error que devuelven los microservicios (ErrorResponse en Spring Boot).
 * "campos" trae el mensaje de cada campo que no pasó la validación.
 */
data class ErrorDto(
    val estado: Int?,
    val mensaje: String?,
    val campos: Map<String, String>?
)
