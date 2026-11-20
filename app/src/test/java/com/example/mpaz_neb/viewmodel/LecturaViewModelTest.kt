package com.example.mpaz_neb.viewmodel

import com.example.mpaz_neb.data.remote.ErrorServidor
import com.example.mpaz_neb.data.remote.MENSAJE_SIN_CONEXION
import com.example.mpaz_neb.model.EstadoSesion
import com.example.mpaz_neb.model.ModuloActividad
import com.example.mpaz_neb.repository.ProgresoRepository
import com.example.mpaz_neb.repository.SesionAulaRepository
import com.example.mpaz_neb.util.DatosPrueba.sesion
import com.example.mpaz_neb.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** Prueba el flujo de la lectura y el envío del resultado a ms-sesiones. */
class LecturaViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val progresoRepository = mockk<ProgresoRepository>()
    private val sesionRepository = mockk<SesionAulaRepository>()
    private lateinit var viewModel: LecturaViewModel

    @Before
    fun preparar() {
        coEvery { progresoRepository.guardarLectura(any(), any()) } returns Unit
        viewModel = LecturaViewModel(progresoRepository, sesionRepository)
    }

    /** Responde todas las preguntas; [correctas] indica cuántas acierta desde el inicio. */
    private fun responderTodo(correctas: Int) {
        val preguntas = viewModel.uiState.value.preguntas
        preguntas.forEachIndexed { indice, pregunta ->
            val respuesta = if (indice < correctas) {
                pregunta.respuestaCorrecta
            } else {
                pregunta.opciones.first { it != pregunta.respuestaCorrecta }
            }
            viewModel.seleccionarRespuesta(respuesta)
            viewModel.comprobarRespuesta()
            viewModel.siguientePregunta()
        }
    }

    @Test
    fun `una opcion que no existe no se selecciona`() {
        viewModel.seleccionarRespuesta("Respuesta inventada")

        assertNull(viewModel.uiState.value.respuestaSeleccionada)
    }

    @Test
    fun `no se puede avanzar sin comprobar la respuesta`() {
        val pregunta = viewModel.uiState.value.preguntas.first()
        viewModel.seleccionarRespuesta(pregunta.respuestaCorrecta)

        viewModel.siguientePregunta()

        assertEquals(0, viewModel.uiState.value.indiceActual)
    }

    @Test
    fun `comprobar una respuesta correcta suma un acierto`() {
        val pregunta = viewModel.uiState.value.preguntas.first()
        viewModel.seleccionarRespuesta(pregunta.respuestaCorrecta)

        viewModel.comprobarRespuesta()
        // Comprobar dos veces no suma dos veces
        viewModel.comprobarRespuesta()

        assertTrue(viewModel.uiState.value.respuestaComprobada)
        assertEquals(1, viewModel.uiState.value.aciertos)
    }

    @Test
    fun `sin sesion abierta el resultado se guarda solo en el dispositivo`() {
        coEvery { sesionRepository.obtenerSesionActual() } returns null

        responderTodo(correctas = 2)

        val estado = viewModel.uiState.value
        assertTrue(estado.finalizada)
        assertFalse(estado.enviadoAlServidor)
        assertEquals(2, estado.aciertos)
        coVerify { progresoRepository.guardarLectura(2, 3) }
        coVerify(exactly = 0) { sesionRepository.registrarResultado(any(), any(), any(), any()) }
    }

    @Test
    fun `con una sesion de lectura en curso el resultado se envia al servidor`() {
        coEvery { sesionRepository.obtenerSesionActual() } returns sesion(modulo = ModuloActividad.LECTURA)
        coEvery { sesionRepository.registrarResultado(10, any(), 3, 3) } returns
                sesion(estado = EstadoSesion.COMPLETADA)

        responderTodo(correctas = 3)

        assertTrue(viewModel.uiState.value.enviadoAlServidor)
        coVerify(exactly = 1) { sesionRepository.registrarResultado(10, any(), 3, 3) }
    }

    @Test
    fun `una sesion de otro modulo no recibe el resultado de lectura`() {
        coEvery { sesionRepository.obtenerSesionActual() } returns sesion(modulo = ModuloActividad.MATEMATICA)

        responderTodo(correctas = 1)

        assertFalse(viewModel.uiState.value.enviadoAlServidor)
        coVerify(exactly = 0) { sesionRepository.registrarResultado(any(), any(), any(), any()) }
    }

    @Test
    fun `si el envio falla se puede reintentar con el mismo id de intento`() {
        val ids = mutableListOf<String>()
        val id = slot<String>()
        coEvery { sesionRepository.obtenerSesionActual() } returns sesion()
        coEvery { sesionRepository.registrarResultado(10, capture(id), any(), any()) } answers {
            ids += id.captured
            if (ids.size == 1) throw ErrorServidor(MENSAJE_SIN_CONEXION) else sesion(estado = EstadoSesion.COMPLETADA)
        }

        responderTodo(correctas = 3)
        assertFalse(viewModel.uiState.value.finalizada)
        assertNotNull(viewModel.uiState.value.errorGuardado)

        // El estudiante vuelve a presionar "Enviar"
        viewModel.siguientePregunta()

        assertTrue(viewModel.uiState.value.finalizada)
        assertEquals(2, ids.size)
        assertEquals(ids[0], ids[1])
    }

    @Test
    fun `reiniciar vuelve a la primera pregunta`() {
        coEvery { sesionRepository.obtenerSesionActual() } returns null
        responderTodo(correctas = 3)

        viewModel.reiniciarActividad()

        val estado = viewModel.uiState.value
        assertFalse(estado.finalizada)
        assertEquals(0, estado.indiceActual)
        assertEquals(0, estado.aciertos)
    }
}
