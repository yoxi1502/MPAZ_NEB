package com.example.mpaz_neb.viewmodel

import com.example.mpaz_neb.data.remote.ErrorServidor
import com.example.mpaz_neb.data.remote.MENSAJE_SIN_CONEXION
import com.example.mpaz_neb.model.EstadoSesion
import com.example.mpaz_neb.model.ProgresoUiState
import com.example.mpaz_neb.model.ResultadoActividad
import com.example.mpaz_neb.repository.ProgresoRepository
import com.example.mpaz_neb.repository.SesionAulaRepository
import com.example.mpaz_neb.util.DatosPrueba.ESTUDIANTE
import com.example.mpaz_neb.util.DatosPrueba.sesion
import com.example.mpaz_neb.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** Prueba el historial de resultados: GET, PATCH de observación y DELETE de sesiones. */
class ProgresoViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val progresoRepository = mockk<ProgresoRepository>()
    private val sesionRepository = mockk<SesionAulaRepository>()
    private lateinit var viewModel: ProgresoViewModel

    private val finalizada = sesion(id = 1, estado = EstadoSesion.FINALIZADA, creadaEn = 1_000)
    private val abierta = sesion(id = 2, estado = EstadoSesion.EN_CURSO, creadaEn = 2_000)

    @Before
    fun preparar() {
        every { progresoRepository.progreso } returns flowOf(
            ProgresoUiState(lectura = ResultadoActividad(2, 3, 5_000L), cargando = false)
        )
        coEvery { sesionRepository.listar(null) } returns listOf(finalizada, abierta)
        viewModel = ProgresoViewModel(progresoRepository, sesionRepository)
    }

    @Test
    fun `al abrir muestra el progreso local y el historial del servidor`() {
        val estado = viewModel.uiState.value

        assertEquals(2, estado.lectura?.aciertos)
        assertNull(estado.matematica)
        assertFalse(estado.cargando)
        // La más reciente primero
        assertEquals(listOf(2L, 1L), estado.historial.map { it.id })
    }

    @Test
    fun `si el progreso local falla se muestra el error`() {
        every { progresoRepository.progreso } returns flow { throw IllegalStateException("DataStore") }

        viewModel.cargarProgreso()

        assertEquals("No se pudieron recuperar los resultados.", viewModel.uiState.value.error)
    }

    @Test
    fun `filtrar por estudiante vuelve a pedir el historial al servidor`() {
        coEvery { sesionRepository.listar(ESTUDIANTE) } returns listOf(finalizada)

        viewModel.filtrarEstudiante(ESTUDIANTE)

        assertEquals(ESTUDIANTE, viewModel.uiState.value.filtroEstudiante)
        assertEquals(listOf(1L), viewModel.uiState.value.historial.map { it.id })
        coVerify { sesionRepository.listar(ESTUDIANTE) }
    }

    @Test
    fun `sin servidor el historial muestra el error`() {
        coEvery { sesionRepository.listar(null) } throws ErrorServidor(MENSAJE_SIN_CONEXION)

        viewModel.cargarHistorial()

        assertEquals(MENSAJE_SIN_CONEXION, viewModel.uiState.value.errorHistorial)
        assertFalse(viewModel.uiState.value.cargandoHistorial)
    }

    @Test
    fun `editar la observacion envia el PATCH y actualiza la tarjeta`() {
        val texto = "Avanzó sin apoyo en la segunda pregunta."
        coEvery { sesionRepository.guardarObservacion(1, texto) } returns finalizada.copy(observacion = texto)

        viewModel.editarObservacion(finalizada)
        viewModel.cambiarObservacion(texto)
        viewModel.guardarObservacion()

        val estado = viewModel.uiState.value
        assertNull(estado.sesionEditando)
        assertEquals(texto, estado.sesiones.first { it.id == 1L }.observacion)
        assertEquals("Observación actualizada.", estado.mensaje)
    }

    @Test
    fun `una observacion con RUT no se envia`() {
        viewModel.editarObservacion(finalizada)
        viewModel.cambiarObservacion("El estudiante 12.345.678-9 avanzó bien")
        viewModel.guardarObservacion()

        assertNotNull(viewModel.uiState.value.errorObservacion)
        coVerify(exactly = 0) { sesionRepository.guardarObservacion(any(), any()) }
    }

    @Test
    fun `si el PATCH falla el dialogo sigue abierto con el error`() {
        coEvery { sesionRepository.guardarObservacion(any(), any()) } throws ErrorServidor(MENSAJE_SIN_CONEXION)

        viewModel.editarObservacion(finalizada)
        viewModel.cambiarObservacion("Avanzó sin apoyo en la segunda pregunta.")
        viewModel.guardarObservacion()

        assertEquals(finalizada, viewModel.uiState.value.sesionEditando)
        assertEquals(MENSAJE_SIN_CONEXION, viewModel.uiState.value.errorObservacion)

        viewModel.cancelarEdicion()
        assertNull(viewModel.uiState.value.sesionEditando)
    }

    @Test
    fun `una sesion abierta no se puede eliminar`() {
        viewModel.pedirEliminar(abierta)

        assertNull(viewModel.uiState.value.sesionAEliminar)
        assertEquals("Finaliza la sesión antes de eliminarla.", viewModel.uiState.value.mensaje)
    }

    @Test
    fun `eliminar una sesion finalizada la quita del historial`() {
        coEvery { sesionRepository.eliminar(1) } returns Unit

        viewModel.pedirEliminar(finalizada)
        viewModel.confirmarEliminar()

        assertEquals(listOf(2L), viewModel.uiState.value.sesiones.map { it.id })
        assertNull(viewModel.uiState.value.eliminandoId)
    }

    @Test
    fun `si el DELETE falla la sesion se mantiene`() {
        coEvery { sesionRepository.eliminar(1) } throws ErrorServidor("Finaliza la sesión antes de eliminarla.", 409)

        viewModel.pedirEliminar(finalizada)
        viewModel.confirmarEliminar()

        assertEquals(2, viewModel.uiState.value.sesiones.size)
        assertEquals("Finaliza la sesión antes de eliminarla.", viewModel.uiState.value.mensaje)

        viewModel.limpiarMensaje()
        assertNull(viewModel.uiState.value.mensaje)
    }

    @Test
    fun `cancelar la eliminacion no llama al servidor`() {
        viewModel.pedirEliminar(finalizada)
        viewModel.cancelarEliminar()
        viewModel.confirmarEliminar()

        coVerify(exactly = 0) { sesionRepository.eliminar(any()) }
    }
}
