package com.example.mpaz_neb.data.remote

import com.example.mpaz_neb.data.model.PokemonDto
import retrofit2.http.GET
import retrofit2.http.Path

/** API externa PokeAPI (pública, sin clave): https://pokeapi.co */
interface PokeApi {

    // GET /pokemon/{id} → nombre, imágenes y tipos de un Pokémon
    @GET("pokemon/{id}")
    suspend fun obtenerPokemon(@Path("id") id: Int): PokemonDto
}
