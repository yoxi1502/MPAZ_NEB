package com.example.mpaz_neb.data.model

import com.example.mpaz_neb.model.Pokemon
import com.google.gson.annotations.SerializedName
import java.util.Locale

/**
 * Respuesta de GET https://pokeapi.co/api/v2/pokemon/{id}.
 * Solo se declaran los campos que usa la app; Gson ignora el resto del JSON.
 * @SerializedName conecta los nombres del JSON (en inglés y con guiones) con los de Kotlin.
 */
data class PokemonDto(
    val id: Int,
    val name: String,
    val sprites: SpritesDto?,
    val types: List<TipoSlotDto>?
)

data class SpritesDto(
    @SerializedName("front_default") val frente: String?,
    val other: OtrosSpritesDto? = null
)

data class OtrosSpritesDto(
    @SerializedName("official-artwork") val arteOficial: ArteDto?
)

data class ArteDto(
    @SerializedName("front_default") val frente: String?
)

data class TipoSlotDto(
    val slot: Int,
    val type: TipoDto
)

data class TipoDto(
    val name: String
)

/** Convierte el JSON de PokeAPI al modelo simple que muestra la app. */
fun PokemonDto.aModelo(): Pokemon = Pokemon(
    id = id,
    nombre = name.replaceFirstChar { it.titlecase(Locale.ROOT) },
    // Se prefiere el arte oficial (más grande); si no existe, el sprite normal
    imagenUrl = sprites?.other?.arteOficial?.frente ?: sprites?.frente,
    tipos = types.orEmpty()
        .sortedBy { it.slot }
        .map { traducirTipo(it.type.name) }
)

private val TIPOS_EN_ESPANOL = mapOf(
    "normal" to "Normal", "fire" to "Fuego", "water" to "Agua", "grass" to "Planta",
    "electric" to "Eléctrico", "ice" to "Hielo", "fighting" to "Lucha", "poison" to "Veneno",
    "ground" to "Tierra", "flying" to "Volador", "psychic" to "Psíquico", "bug" to "Bicho",
    "rock" to "Roca", "ghost" to "Fantasma", "dragon" to "Dragón", "dark" to "Siniestro",
    "steel" to "Acero", "fairy" to "Hada"
)

internal fun traducirTipo(tipo: String): String =
    TIPOS_EN_ESPANOL[tipo] ?: tipo.replaceFirstChar { it.titlecase(Locale.ROOT) }
