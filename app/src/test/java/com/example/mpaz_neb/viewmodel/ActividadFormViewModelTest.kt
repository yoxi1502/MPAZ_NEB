package com.example.mpaz_neb.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.example.mpaz_neb.data.remote.ErrorServidor
import com.example.mpaz_neb.model.Actividad
import com.example.mpaz_neb.model.CampoActividad
import com.example.mpaz_neb.model.Dificultad
import com.example.mpaz_neb.model.EstadoActividad
import com.example.mpaz_neb.model.ModuloActividad
import com.example.mpaz_neb.repository.ActividadRepository
import com.example.mpaz_neb.util.DatosPrueba.actividad
import com.example.mpaz_neb.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** Prueba la validación y el guardado (POST/PUT) del formulario de actividades. */
class ActividadFormViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = mockk<ActividadRepository>()

    private fun crearViewModel(id: String? = null) = ActividadFormViewModel(
        savedStateHandle = SavedStateHandle(if (id == null) emptyMap() else mapOf(ActividadFormViewModel.ARG_ID to id)),
        repository = repository
    )

    private fun ActividadFormViewModel.llenarFormularioValido() {
        cambiarNombre("Sumas hasta 20")
        cambiarContenido("Adición")
        cambiarObjetivo("Resolver sumas simples hasta 20.")
        cambiarInstrucciones("Resuelve cada suma y comprueba.")
        cambiarCantidadPreguntas("5")
        cambiarIntentos("2")
        cambiarModulo(ModuloActividad.MATEMATICA)
        cambiarCurso("1.° Año Básico")
        cambiarDificultad(Dificultad.INTERMEDIA)
        cambiarEstado(EstadoActividad.DISPONIBLE)
    }

    @Test
    fun `sin id el formulario es de una actividad nueva`() {
        val viewModel = crearViewModel()

        assertFalse(viewModel.uiState.value.esEdicion)
        coVerify(exactly = 0) { repository.obtener(any()) }
    }

    @Test
    fun `con id carga los datos desde el servidor`() {
        coEvery { repository.obtener(7) } returns actividad(id = 7, nombre = "Lectura guiada")

        val viewModel = crearViewModel(id = "7")

        val estado = viewModel.uiState.value
        assertTrue(estado.esEdicion)
        assertEquals("Lectura guiada", estado.nombre)
        assertEquals("3", estado.cantidadPreguntas)
    }

    @Test
    fun `si la actividad no existe se muestra el error`() {
        coEvery { repository.obtener(7) } throws ErrorServidor("No se encontró la actividad con id 7.", 404)

        val viewModel = crearViewModel(id = "7")

        assertEquals("No se encontró la actividad con id 7.", viewModel.uiState.value.errorGeneral)
    }

    @Test
    fun `guardar con campos vacios muestra los errores y no llama al servidor`() {
        val viewModel = crearViewModel()
        var exito = false

        viewModel.guardar { exito = true }

        val errores = viewModel.uiState.value.errores
        assertTrue(CampoActividad.NOMBRE in errores)
        assertTrue(CampoActividad.CONTENIDO in errores)
        assertTrue(CampoActividad.OBJETIVO in errores)
        assertFalse(exito)
        coVerify(exactly = 0) { repository.guardar(any()) }
    }

    @Test
    fun `al corregir un campo se limpia solo su error`() {
        val viewModel = crearViewModel()
        viewModel.guardar { }

        viewModel.cambiarNombre("Sumas hasta 20")

        val errores = viewModel.uiState.value.errores
        assertFalse(CampoActividad.NOMBRE in errores)
        assertTrue(CampoActividad.CONTENIDO in errores)
    }

    @Test
    fun `guardar un formulario valido envia la actividad sin id`() {
        val enviada = slot<Actividad>()
        coEvery { repository.guardar(capture(enviada)) } answers { enviada.captured.copy(id = 99) }
        val viewModel = crearViewModel()
        viewModel.llenarFormularioValido()
        var exito = false

        viewModel.guardar { exito = true }

        assertTrue(exito)
        assertNull(enviada.captured.id)
        assertEquals(5, enviada.captured.cantidadPreguntas)
        assertEquals(ModuloActividad.MATEMATICA, enviada.captured.modulo)
        assertFalse(viewModel.uiState.value.guardando)
    }

    @Test
    fun `si el servidor responde 409 se muestra su mensaje`() {
        coEvery { repository.guardar(any()) } throws
                ErrorServidor("Ya existe una actividad con ese nombre para el mismo módulo y curso.", 409)
        val viewModel = crearViewModel()
        viewModel.llenarFormularioValido()
        var exito = false

        viewModel.guardar { exito = true }

        assertFalse(exito)
        assertTrue(viewModel.uiState.value.errorGeneral!!.startsWith("Ya existe"))
    }

    @Test
    fun `numeros fuera de rango no se envian`() {
        val viewModel = crearViewModel()
        viewModel.llenarFormularioValido()
        viewModel.cambiarCantidadPreguntas("50")
        viewModel.cambiarIntentos("x")

        viewModel.guardar { }

        val errores = viewModel.uiState.value.errores
        assertTrue(CampoActividad.CANTIDAD_PREGUNTAS in errores)
        assertTrue(CampoActividad.INTENTOS in errores)
        coVerify(exactly = 0) { repository.guardar(any()) }
    }
}
