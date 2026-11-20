package com.example.mpaz_neb.viewmodel

import com.example.mpaz_neb.data.remote.ErrorServidor
import com.example.mpaz_neb.model.SesionAula
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
import org.junit.Rule
import org.junit.Test

/** Prueba el ingreso del estudiante con su código. */
class StudentCodeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = mockk<SesionAulaRepository>()
    private val viewModel = StudentCodeViewModel(repository)

    @Test
    fun `el codigo se normaliza mientras se escribe`() {
        viewModel.actualizarCodigo(" mpaz-ab12cd ")

        assertEquals("MPAZ-AB12CD", viewModel.uiState.value.codigo)
    }

    @Test
    fun `un codigo mal escrito no se envia al servidor`() {
        viewModel.actualizarCodigo("MPAZ-12")

        viewModel.validarCodigo { }

        assertNotNull(viewModel.uiState.value.errorCodigo)
        coVerify(exactly = 0) { repository.ingresarConCodigo(any(), any()) }
    }

    @Test
    fun `un codigo valido abre la sesion`() {
        coEvery { repository.ingresarConCodigo("MPAZ-AB12CD") } returns sesion()
        viewModel.actualizarCodigo("MPAZ-AB12CD")
        var abierta: SesionAula? = null

        viewModel.validarCodigo { abierta = it }

        assertEquals(10L, abierta?.id)
        assertNull(viewModel.uiState.value.errorCodigo)
        assertFalse(viewModel.uiState.value.validando)
    }

    @Test
    fun `si el servidor no encuentra la sesion se muestra su mensaje`() {
        coEvery { repository.ingresarConCodigo(any()) } throws
                ErrorServidor("El código no corresponde a una sesión abierta.", 404)
        viewModel.actualizarCodigo("MPAZ-ZZZZZZ")

        viewModel.validarCodigo { }

        assertEquals("El código no corresponde a una sesión abierta.", viewModel.uiState.value.errorCodigo)
    }
}
