package com.example.mpaz_neb.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mpaz_neb.model.PremioPokemonUiState
import com.example.mpaz_neb.repository.PokemonRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Premio al terminar una actividad: trae un Pokémon aleatorio de PokeAPI.
 * Es independiente de los microservicios: si PokeAPI falla, el resultado igual se guarda.
 */
class PremioPokemonViewModel(
    private val repository: PokemonRepository = PokemonRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(PremioPokemonUiState())
    val uiState: StateFlow<PremioPokemonUiState> = _uiState.asStateFlow()

    /**
     * Pide un Pokémon solo si todavía no hay uno.
     * Así, al girar la pantalla no cambia el compañero.
     */
    fun cargarPremio() {
        val actual = _uiState.value
        if (actual.cargando || actual.pokemon != null) return
        sortear()
    }

    /** Vuelve a intentarlo después de un error de red. */
    fun reintentar() = sortear()

    /** Al practicar otra vez, el próximo resultado trae un compañero nuevo. */
    fun reiniciar() {
        _uiState.value = PremioPokemonUiState()
    }

    private fun sortear() {
        _uiState.update { it.copy(cargando = true, error = null) }
        viewModelScope.launch {
            try {
                val pokemon = repository.obtenerAleatorio()
                _uiState.update { it.copy(cargando = false, pokemon = pokemon) }
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        cargando = false,
                        error = "No pudimos traer a tu compañero Pokémon. Revisa la conexión a internet."
                    )
                }
            }
        }
    }
}
