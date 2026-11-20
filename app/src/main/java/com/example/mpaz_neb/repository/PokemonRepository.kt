package com.example.mpaz_neb.repository

import com.example.mpaz_neb.data.model.aModelo
import com.example.mpaz_neb.data.remote.PokeApi
import com.example.mpaz_neb.data.remote.RetrofitInstance
import com.example.mpaz_neb.model.Pokemon
import kotlin.random.Random

/**
 * Obtiene un Pokémon aleatorio desde la API externa PokeAPI.
 * Recibe la API y el sorteo por parámetro para poder reemplazarlos en las pruebas.
 */
class PokemonRepository(
    private val api: PokeApi = RetrofitInstance.pokeApi,
    private val sortearId: () -> Int = { Random.nextInt(PRIMER_ID, ULTIMO_ID + 1) }
) {

    suspend fun obtenerAleatorio(): Pokemon = api.obtenerPokemon(sortearId()).aModelo()

    companion object {
        // Primera generación (1 a 151): Pokémon conocidos y con imagen oficial
        const val PRIMER_ID = 1
        const val ULTIMO_ID = 151
    }
}
