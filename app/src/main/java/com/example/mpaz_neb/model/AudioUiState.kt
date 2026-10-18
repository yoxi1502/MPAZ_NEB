package com.example.mpaz_neb.model

data class AudioUiState(
    val grabando: Boolean = false,
    val reproduciendo: Boolean = false,
    val audioDisponible: Boolean = false,
    val segundosGrabados: Int = 0,
    val mensaje: String? = null,
    val error: String? = null
)

