package com.example.mpaz_neb.model

/** Pokémon "compañero" que se muestra como premio al terminar una actividad. */
data class Pokemon(
    val id: Int,
    val nombre: String,
    val imagenUrl: String?,
    val tipos: List<String>
) {
    val tiposTexto: String get() = tipos.joinToString(" / ")

    /** Mensaje de ánimo según el resultado: siempre positivo, sin calificar al estudiante. */
    fun mensajeAnimo(aciertos: Int, total: Int): String = when {
        total > 0 && aciertos == total -> "¡Excelente! $nombre celebra contigo."
        total > 0 && aciertos * 2 >= total -> "¡Muy bien! $nombre dice que vas por buen camino."
        else -> "$nombre te acompaña: ¡cada intento te ayuda a aprender!"
    }
}

data class PremioPokemonUiState(
    val cargando: Boolean = false,
    val pokemon: Pokemon? = null,
    val error: String? = null
)
