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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** Prueba el desafío de matemática y el envío del resultado a ms-sesiones. */
class MatematicaViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val progresoRepository = mockk<ProgresoRepository>()
    private val sesionRepository = mockk<SesionAulaRepository>()
    private lateinit var viewModel: MatematicaViewModel

    @Before
    fun preparar() {
        coEvery { progresoRepository.guardarMatematica(any(), any()) } returns Unit
        viewModel = MatematicaViewModel(progresoRepository, sesionRepository)
    }

    private fun responderTodo(correctas: Int) {
        viewModel.uiState.value.preguntas.forEachIndexed { indice, pregunta ->
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
    fun `una opcion que no esta en la pregunta no se selecciona`() {
        viewModel.seleccionarRespuesta(999)

        assertNull(viewModel.uiState.value.respuestaSeleccionada)
    }

    @Test
    fun `despues de comprobar no se puede cambiar la respuesta`() {
        val pregunta = viewModel.uiState.value.preguntas.first()
        viewModel.seleccionarRespuesta(pregunta.respuestaCorrecta)
        viewModel.comprobarRespuesta()

        viewModel.seleccionarRespuesta(pregunta.opciones.first { it != pregunta.respuestaCorrecta })

        assertEquals(pregunta.respuestaCorrecta, viewModel.uiState.value.respuestaSeleccionada)
    }

    @Test
    fun `sin sesion el resultado queda solo en el dispositivo`() {
        coEvery { sesionRepository.obtenerSesionActual() } returns null

        responderTodo(correctas = 2)

        assertTrue(viewModel.uiState.value.finalizada)
        assertFalse(viewModel.uiState.value.enviadoAlServidor)
        coVerify { progresoRepository.guardarMatematica(2, 3) }
    }

    @Test
    fun `con una sesion de matematica en curso el resultado se envia`() {
        coEvery { sesionRepository.obtenerSesionActual() } returns sesion(modulo = ModuloActividad.MATEMATICA)
        coEvery { sesionRepository.registrarResultado(10, any(), 3, 3) } returns sesion(estado = EstadoSesion.COMPLETADA)

        responderTodo(correctas = 3)

        assertTrue(viewModel.uiState.value.enviadoAlServidor)
        coVerify(exactly = 1) { sesionRepository.registrarResultado(10, any(), 3, 3) }
    }

    @Test
    fun `una sesion finalizada no recibe resultados`() {
        coEvery { sesionRepository.obtenerSesionActual() } returns
                sesion(modulo = ModuloActividad.MATEMATICA, estado = EstadoSesion.FINALIZADA)

        responderTodo(correctas = 3)

        assertFalse(viewModel.uiState.value.enviadoAlServidor)
        coVerify(exactly = 0) { sesionRepository.registrarResultado(any(), any(), any(), any()) }
    }

    @Test
    fun `si el servidor no responde se muestra el error y no termina`() {
        coEvery { sesionRepository.obtenerSesionActual() } throws ErrorServidor(MENSAJE_SIN_CONEXION)

        responderTodo(correctas = 3)

        assertFalse(viewModel.uiState.value.finalizada)
        assertNotNull(viewModel.uiState.value.errorGuardado)
    }

    @Test
    fun `reiniciar deja la actividad como nueva`() {
        coEvery { sesionRepository.obtenerSesionActual() } returns null
        responderTodo(correctas = 1)

        viewModel.reiniciarActividad()

        assertEquals(0, viewModel.uiState.value.aciertos)
        assertFalse(viewModel.uiState.value.finalizada)
    }
}
