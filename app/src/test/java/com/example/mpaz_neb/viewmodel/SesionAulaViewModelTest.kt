package com.example.mpaz_neb.viewmodel

import com.example.mpaz_neb.data.remote.ErrorServidor
import com.example.mpaz_neb.data.remote.MENSAJE_SIN_CONEXION
import com.example.mpaz_neb.model.EstadoActividad
import com.example.mpaz_neb.model.EstadoSesion
import com.example.mpaz_neb.model.ModuloActividad
import com.example.mpaz_neb.repository.ActividadRepository
import com.example.mpaz_neb.repository.SesionAulaRepository
import com.example.mpaz_neb.util.DatosPrueba.ASISTENTE
import com.example.mpaz_neb.util.DatosPrueba.CURSO
import com.example.mpaz_neb.util.DatosPrueba.ESTUDIANTE
import com.example.mpaz_neb.util.DatosPrueba.actividad
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

/** Prueba el panel de la asistente con los repositorios de sesiones y actividades simulados. */
class SesionAulaViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val sesionRepository = mockk<SesionAulaRepository>()
    private val actividadRepository = mockk<ActividadRepository>()
    private lateinit var viewModel: SesionAulaViewModel

    @Before
    fun preparar() {
        coEvery { actividadRepository.listar(estado = EstadoActividad.DISPONIBLE) } returns listOf(actividad(id = 1))
        viewModel = SesionAulaViewModel(sesionRepository, actividadRepository)
    }

    private fun seleccionarTodo() {
        viewModel.seleccionarModulo(ModuloActividad.LECTURA)
        viewModel.seleccionarCurso(CURSO)
        viewModel.seleccionarEstudiante(ESTUDIANTE)
    }

    @Test
    fun `al crearse carga solo las actividades disponibles`() {
        assertEquals(1, viewModel.uiState.value.actividades.size)
        coVerify { actividadRepository.listar(estado = EstadoActividad.DISPONIBLE) }
    }

    @Test
    fun `si ms-actividades no responde se muestra el error y se puede reintentar`() {
        coEvery { actividadRepository.listar(estado = EstadoActividad.DISPONIBLE) } throws ErrorServidor(MENSAJE_SIN_CONEXION)

        viewModel.cargarActividades()

        assertEquals(MENSAJE_SIN_CONEXION, viewModel.uiState.value.errorActividades)
    }

    @Test
    fun `cargarSesion trae la sesion abierta del servidor con su observacion`() {
        coEvery { sesionRepository.obtenerSesionActual() } returns sesion(observacion = "Leyó con apoyo.")

        viewModel.cargarSesion()

        val estado = viewModel.uiState.value
        assertEquals(10L, estado.sesion?.id)
        assertEquals("Leyó con apoyo.", estado.observacion)
        assertFalse(estado.cargando)
    }

    @Test
    fun `cargarSesion con error deja el mensaje`() {
        coEvery { sesionRepository.obtenerSesionActual() } throws ErrorServidor(MENSAJE_SIN_CONEXION)

        viewModel.cargarSesion()

        assertEquals(MENSAJE_SIN_CONEXION, viewModel.uiState.value.error)
    }

    @Test
    fun `generar codigo sin curso ni estudiante muestra los errores`() {
        viewModel.generarCodigo(ASISTENTE) { }

        assertNotNull(viewModel.uiState.value.errorCurso)
        assertNotNull(viewModel.uiState.value.errorEstudiante)
        coVerify(exactly = 0) { sesionRepository.crearSesion(any(), any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `generar codigo crea la sesion con la actividad elegida`() {
        coEvery {
            sesionRepository.crearSesion(ModuloActividad.LECTURA, CURSO, ESTUDIANTE, ASISTENTE, any(), 1L, 2)
        } returns sesion(estado = EstadoSesion.CREADA)
        seleccionarTodo()
        viewModel.seleccionarActividad(actividad(id = 1))
        var exito = false

        viewModel.generarCodigo(ASISTENTE) { exito = true }

        assertTrue(exito)
        assertEquals("MPAZ-AB12CD", viewModel.uiState.value.sesion?.codigo)
        assertFalse(viewModel.uiState.value.guardando)
    }

    @Test
    fun `si ya hay una sesion abierta se muestra el 409 del servidor`() {
        coEvery { sesionRepository.crearSesion(any(), any(), any(), any(), any(), any(), any()) } throws
                ErrorServidor("Finaliza la sesión actual antes de generar otra.", 409)
        seleccionarTodo()
        var exito = false

        viewModel.generarCodigo(ASISTENTE) { exito = true }

        assertFalse(exito)
        assertEquals("Finaliza la sesión actual antes de generar otra.", viewModel.uiState.value.error)
    }

    @Test
    fun `observacion invalida no se envia`() {
        coEvery { sesionRepository.obtenerSesionActual() } returns sesion()
        viewModel.cargarSesion()

        viewModel.actualizarObservacion("corta")
        viewModel.guardarObservacion()

        assertNotNull(viewModel.uiState.value.errorObservacion)
        coVerify(exactly = 0) { sesionRepository.guardarObservacion(any(), any()) }
    }

    @Test
    fun `observacion valida se guarda en el servidor`() {
        val texto = "Necesitó apoyo para ubicar la idea principal."
        coEvery { sesionRepository.obtenerSesionActual() } returns sesion()
        coEvery { sesionRepository.guardarObservacion(10, texto) } returns sesion(observacion = texto)
        viewModel.cargarSesion()

        viewModel.actualizarObservacion(texto)
        viewModel.guardarObservacion()

        val estado = viewModel.uiState.value
        assertTrue(estado.observacionGuardada)
        assertFalse(estado.observacionEditada)
        assertEquals(texto, estado.sesion?.observacion)
    }

    @Test
    fun `si falla el guardado de la observacion se muestra el error`() {
        coEvery { sesionRepository.obtenerSesionActual() } returns sesion()
        coEvery { sesionRepository.guardarObservacion(any(), any()) } throws ErrorServidor(MENSAJE_SIN_CONEXION)
        viewModel.cargarSesion()

        viewModel.actualizarObservacion("Necesitó apoyo para ubicar la idea principal.")
        viewModel.guardarObservacion()

        assertEquals(MENSAJE_SIN_CONEXION, viewModel.uiState.value.errorObservacion)
    }

    @Test
    fun `finalizar cierra la sesion en el servidor`() {
        coEvery { sesionRepository.obtenerSesionActual() } returns sesion()
        coEvery { sesionRepository.finalizarSesion(10) } returns sesion(estado = EstadoSesion.FINALIZADA)
        viewModel.cargarSesion()
        var exito = false

        viewModel.finalizarSesion { exito = true }

        assertTrue(exito)
        assertEquals(EstadoSesion.FINALIZADA, viewModel.uiState.value.sesion?.estado)
    }

    @Test
    fun `finalizar con error mantiene la sesion y muestra el mensaje`() {
        coEvery { sesionRepository.obtenerSesionActual() } returns sesion()
        coEvery { sesionRepository.finalizarSesion(10) } throws ErrorServidor(MENSAJE_SIN_CONEXION)
        viewModel.cargarSesion()

        viewModel.finalizarSesion { }

        assertEquals(MENSAJE_SIN_CONEXION, viewModel.uiState.value.error)
        assertEquals(EstadoSesion.EN_CURSO, viewModel.uiState.value.sesion?.estado)
    }

    @Test
    fun `sin sesion no se puede finalizar`() {
        viewModel.finalizarSesion { }

        assertNull(viewModel.uiState.value.sesion)
        coVerify(exactly = 0) { sesionRepository.finalizarSesion(any()) }
    }
}
