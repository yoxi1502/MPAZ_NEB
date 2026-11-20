package com.example.mpaz_neb.viewmodel

import com.example.mpaz_neb.model.Pokemon
import com.example.mpaz_neb.repository.PokemonRepository
import com.example.mpaz_neb.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.io.IOException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

/** Prueba el premio con la API externa PokeAPI simulada. */
class PremioPokemonViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = mockk<PokemonRepository>()
    private val viewModel = PremioPokemonViewModel(repository)
    private val staryu = Pokemon(120, "Staryu", "https://img/120.png", listOf("Agua"))

    @Test
    fun `cargarPremio trae un pokemon`() {
        coEvery { repository.obtenerAleatorio() } returns staryu

        viewModel.cargarPremio()

        assertEquals(staryu, viewModel.uiState.value.pokemon)
        assertFalse(viewModel.uiState.value.cargando)
    }

    @Test
    fun `si ya hay premio no se pide otro, por ejemplo al girar la pantalla`() {
        coEvery { repository.obtenerAleatorio() } returns staryu

        viewModel.cargarPremio()
        viewModel.cargarPremio()

        coVerify(exactly = 1) { repository.obtenerAleatorio() }
    }

    @Test
    fun `sin internet se muestra un mensaje y se puede reintentar`() {
        coEvery { repository.obtenerAleatorio() } throws IOException("sin red") andThen staryu

        viewModel.cargarPremio()
        assertNotNull(viewModel.uiState.value.error)
        assertNull(viewModel.uiState.value.pokemon)

        viewModel.reintentar()
        assertEquals(staryu, viewModel.uiState.value.pokemon)
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `reiniciar permite un compañero nuevo en el proximo resultado`() {
        coEvery { repository.obtenerAleatorio() } returns staryu
        viewModel.cargarPremio()

        viewModel.reiniciar()
        assertNull(viewModel.uiState.value.pokemon)

        viewModel.cargarPremio()
        coVerify(exactly = 2) { repository.obtenerAleatorio() }
    }
}
