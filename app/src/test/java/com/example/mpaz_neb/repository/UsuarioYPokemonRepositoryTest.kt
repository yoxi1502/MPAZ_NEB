package com.example.mpaz_neb.repository

import com.example.mpaz_neb.data.local.UsuarioSesionStore
import com.example.mpaz_neb.data.model.ArteDto
import com.example.mpaz_neb.data.model.LoginDto
import com.example.mpaz_neb.data.model.OtrosSpritesDto
import com.example.mpaz_neb.data.model.PokemonDto
import com.example.mpaz_neb.data.model.SpritesDto
import com.example.mpaz_neb.data.model.TipoDto
import com.example.mpaz_neb.data.model.TipoSlotDto
import com.example.mpaz_neb.data.model.UsuarioDto
import com.example.mpaz_neb.data.remote.ErrorServidor
import com.example.mpaz_neb.data.remote.PokeApi
import com.example.mpaz_neb.data.remote.UsuariosApi
import com.example.mpaz_neb.model.PerfilUsuario
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.fail
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

/** Prueba el login contra ms-usuarios y el Pokémon de PokeAPI, con las APIs simuladas. */
class UsuarioYPokemonRepositoryTest {

    private val usuariosApi = mockk<UsuariosApi>()
    private val local = mockk<UsuarioSesionStore>(relaxed = true)

    @Test
    fun `login correcto guarda el usuario en el dispositivo`() = runTest {
        every { local.usuario } returns flowOf(null)
        coEvery { usuariosApi.login(LoginDto("utp@mpaz.cl", "Docente123")) } returns
                UsuarioDto(2, "Docente UTP", "utp@mpaz.cl", PerfilUsuario.DOCENTE_UTP, true, "2026-11-16T22:10:00Z")
        val repository = UsuarioRepository(local, usuariosApi)

        val usuario = repository.iniciarSesion(" utp@mpaz.cl ", "Docente123")

        assertEquals(PerfilUsuario.DOCENTE_UTP, usuario.perfil)
        coVerify { local.guardar(usuario) }
        assertNull(repository.usuarioActual.first())
    }

    @Test
    fun `login con 401 no guarda nada y entrega el mensaje del servidor`() = runTest {
        every { local.usuario } returns flowOf(null)
        val cuerpo = """{"estado":401,"mensaje":"Correo o contraseña incorrectos."}"""
        coEvery { usuariosApi.login(any()) } throws HttpException(Response.error<Any>(401, cuerpo.toResponseBody()))
        val repository = UsuarioRepository(local, usuariosApi)

        try {
            repository.iniciarSesion("utp@mpaz.cl", "incorrecta")
            fail("Debía lanzar ErrorServidor")
        } catch (e: ErrorServidor) {
            assertEquals("Correo o contraseña incorrectos.", e.message)
        }
        coVerify(exactly = 0) { local.guardar(any()) }
    }

    @Test
    fun `cerrar sesion limpia el dispositivo`() = runTest {
        every { local.usuario } returns flowOf(null)
        val repository = UsuarioRepository(local, usuariosApi)

        repository.cerrarSesion()

        coVerify { local.limpiar() }
    }

    @Test
    fun `el pokemon sorteado se pide a PokeAPI y se traduce`() = runTest {
        val pokeApi = mockk<PokeApi>()
        coEvery { pokeApi.obtenerPokemon(120) } returns PokemonDto(
            id = 120,
            name = "staryu",
            sprites = SpritesDto("https://img/120.png", OtrosSpritesDto(ArteDto("https://img/arte/120.png"))),
            types = listOf(TipoSlotDto(1, TipoDto("water")))
        )
        val repository = PokemonRepository(pokeApi) { 120 }

        val pokemon = repository.obtenerAleatorio()

        assertEquals("Staryu", pokemon.nombre)
        assertEquals("https://img/arte/120.png", pokemon.imagenUrl)
        assertEquals(listOf("Agua"), pokemon.tipos)
    }
}
