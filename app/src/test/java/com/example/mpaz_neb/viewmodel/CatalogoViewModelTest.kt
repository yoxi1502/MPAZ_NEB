package com.example.mpaz_neb.viewmodel

import com.example.mpaz_neb.data.remote.ErrorServidor
import com.example.mpaz_neb.data.remote.MENSAJE_SIN_CONEXION
import com.example.mpaz_neb.model.EstadoActividad
import com.example.mpaz_neb.model.ModuloActividad
import com.example.mpaz_neb.repository.ActividadRepository
import com.example.mpaz_neb.util.DatosPrueba.actividad
import com.example.mpaz_neb.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** Prueba el catálogo simulando (mock) el repositorio que llama a ms-actividades. */
class CatalogoViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = mockk<ActividadRepository>()
    private val viewModel = CatalogoViewModel(repository)

    @Test
    fun `cargar muestra las actividades ordenadas por modulo y curso`() {
        coEvery { repository.listar() } returns listOf(
            actividad(id = 2, nombre = "Sumas", modulo = ModuloActividad.MATEMATICA),
            actividad(id = 1, nombre = "Lectura", modulo = ModuloActividad.LECTURA)
        )

        viewModel.cargar()

        val estado = viewModel.uiState.value
        assertFalse(estado.cargando)
        assertNull(estado.error)
        assertEquals(listOf(1L, 2L), estado.actividades.map { it.id })
    }

    @Test
    fun `cargar sin servidor muestra el error para reintentar`() {
        coEvery { repository.listar() } throws ErrorServidor(MENSAJE_SIN_CONEXION)

        viewModel.cargar()

        assertEquals(MENSAJE_SIN_CONEXION, viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.cargando)
    }

    @Test
    fun `los filtros reducen la lista visible`() {
        coEvery { repository.listar() } returns listOf(
            actividad(id = 1, modulo = ModuloActividad.LECTURA, curso = "2.° Año Básico"),
            actividad(id = 2, modulo = ModuloActividad.MATEMATICA, curso = "1.° Año Básico")
        )
        viewModel.cargar()

        viewModel.filtrarModulo(ModuloActividad.MATEMATICA)
        assertEquals(listOf(2L), viewModel.uiState.value.filtradas.map { it.id })

        viewModel.filtrarModulo(null)
        viewModel.filtrarCurso("2.° Año Básico")
        assertEquals(listOf(1L), viewModel.uiState.value.filtradas.map { it.id })
    }

    @Test
    fun `desactivar envia el PATCH y reemplaza la actividad en la lista`() {
        val original = actividad(id = 1)
        coEvery { repository.listar() } returns listOf(original)
        coEvery { repository.cambiarEstado(1, EstadoActividad.DESACTIVADA) } returns
                original.copy(estado = EstadoActividad.DESACTIVADA)
        viewModel.cargar()

        viewModel.cambiarDisponible(original, disponible = false)

        val estado = viewModel.uiState.value
        assertEquals(EstadoActividad.DESACTIVADA, estado.actividades.single().estado)
        assertNull(estado.procesandoId)
        assertTrue(estado.mensaje!!.contains("desactivada"))
    }

    @Test
    fun `si el cambio de estado falla se informa el mensaje del servidor`() {
        val original = actividad(id = 1)
        coEvery { repository.cambiarEstado(any(), any()) } throws ErrorServidor("No se encontró la actividad con id 1.", 404)

        viewModel.cambiarDisponible(original, disponible = true)

        assertEquals("No se encontró la actividad con id 1.", viewModel.uiState.value.mensaje)
    }

    @Test
    fun `eliminar pide confirmacion y luego llama al DELETE`() {
        val aEliminar = actividad(id = 3, nombre = "Borrar")
        coEvery { repository.listar() } returns listOf(aEliminar, actividad(id = 4))
        coEvery { repository.eliminar(3) } returns Unit
        viewModel.cargar()

        viewModel.pedirEliminar(aEliminar)
        assertEquals(aEliminar, viewModel.uiState.value.actividadAEliminar)
        coVerify(exactly = 0) { repository.eliminar(any()) }

        viewModel.confirmarEliminar()

        val estado = viewModel.uiState.value
        assertNull(estado.actividadAEliminar)
        assertEquals(listOf(4L), estado.actividades.map { it.id })
        coVerify(exactly = 1) { repository.eliminar(3) }
    }

    @Test
    fun `cancelar la eliminacion no borra nada`() {
        viewModel.pedirEliminar(actividad(id = 3))

        viewModel.cancelarEliminar()
        viewModel.confirmarEliminar()

        assertNull(viewModel.uiState.value.actividadAEliminar)
        coVerify(exactly = 0) { repository.eliminar(any()) }
    }

    @Test
    fun `si el DELETE falla la actividad sigue en la lista`() {
        val aEliminar = actividad(id = 3)
        coEvery { repository.listar() } returns listOf(aEliminar)
        coEvery { repository.eliminar(3) } throws ErrorServidor(MENSAJE_SIN_CONEXION)
        viewModel.cargar()

        viewModel.pedirEliminar(aEliminar)
        viewModel.confirmarEliminar()

        assertEquals(1, viewModel.uiState.value.actividades.size)
        assertEquals(MENSAJE_SIN_CONEXION, viewModel.uiState.value.mensaje)

        viewModel.limpiarMensaje()
        assertNull(viewModel.uiState.value.mensaje)
    }
}
