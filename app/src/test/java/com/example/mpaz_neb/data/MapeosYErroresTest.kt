package com.example.mpaz_neb.data

import com.example.mpaz_neb.data.model.PokemonDto
import com.example.mpaz_neb.data.model.SpritesDto
import com.example.mpaz_neb.data.model.TipoDto
import com.example.mpaz_neb.data.model.TipoSlotDto
import com.example.mpaz_neb.data.model.aModelo
import com.example.mpaz_neb.data.model.aRequest
import com.example.mpaz_neb.data.model.isoAMilis
import com.example.mpaz_neb.data.model.traducirTipo
import com.example.mpaz_neb.data.remote.mensajeDeError
import com.example.mpaz_neb.model.Pokemon
import com.example.mpaz_neb.util.DatosPrueba.actividad
import com.example.mpaz_neb.util.DatosPrueba.sesionDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Conversión de JSON a modelos, fechas ISO, traducción de tipos y mensajes de error. */
class MapeosYErroresTest {

    @Test
    fun `las fechas ISO se convierten a milisegundos`() {
        assertEquals(0L, isoAMilis("1970-01-01T00:00:00Z"))
        assertNull(isoAMilis(null))
        assertNull(isoAMilis("no es una fecha"))
    }

    @Test
    fun `la actividad se convierte al cuerpo del POST sin id`() {
        val request = actividad(id = 5, nombre = "Lectura guiada").aRequest()

        assertEquals("Lectura guiada", request.nombre)
        assertEquals(3, request.cantidadPreguntas)
    }

    @Test
    fun `la sesion del servidor se convierte con sus resultados`() {
        val sesion = sesionDto().aModelo()

        assertEquals("", sesion.observacion)
        assertEquals(1, sesion.intentosCompletados)
        assertEquals(1, sesion.ultimoResultado?.errores)
        assertTrue(sesion.abierta)
    }

    @Test
    fun `los tipos de pokemon se traducen y si no se conoce se capitaliza`() {
        assertEquals("Agua", traducirTipo("water"))
        assertEquals("Eléctrico", traducirTipo("electric"))
        assertEquals("Stellar", traducirTipo("stellar"))
    }

    @Test
    fun `sin arte oficial se usa el sprite normal`() {
        val pokemon = PokemonDto(
            id = 25,
            name = "pikachu",
            sprites = SpritesDto("https://img/25.png"),
            types = listOf(TipoSlotDto(2, TipoDto("flying")), TipoSlotDto(1, TipoDto("electric")))
        ).aModelo()

        assertEquals("https://img/25.png", pokemon.imagenUrl)
        assertEquals("Eléctrico / Volador", pokemon.tiposTexto)
    }

    @Test
    fun `el mensaje de animo depende del resultado`() {
        val pokemon = Pokemon(1, "Bulbasaur", null, emptyList())

        assertTrue(pokemon.mensajeAnimo(3, 3).startsWith("¡Excelente!"))
        assertTrue(pokemon.mensajeAnimo(2, 3).startsWith("¡Muy bien!"))
        assertTrue(pokemon.mensajeAnimo(0, 3).contains("te acompaña"))
    }

    @Test
    fun `el error del microservicio se traduce a un mensaje para la persona`() {
        assertEquals("Mensaje del servidor", mensajeDeError(409, """{"mensaje":"Mensaje del servidor"}"""))
        assertEquals("Campo inválido", mensajeDeError(400, """{"mensaje":"x","campos":{"nombre":"Campo inválido"}}"""))
        assertEquals("El servidor respondió con error 500.", mensajeDeError(500, "<html>error</html>"))
        assertEquals("El servidor respondió con error 503.", mensajeDeError(503, null))
    }
}
