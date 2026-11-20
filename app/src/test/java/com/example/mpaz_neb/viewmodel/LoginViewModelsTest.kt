package com.example.mpaz_neb.viewmodel

import com.example.mpaz_neb.data.remote.ErrorServidor
import com.example.mpaz_neb.model.PerfilUsuario
import com.example.mpaz_neb.model.UsuarioSesion
import com.example.mpaz_neb.repository.UsuarioRepository
import com.example.mpaz_neb.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** Prueba el login (TeacherLoginViewModel) y la sesión compartida (SesionUsuarioViewModel). */
class LoginViewModelsTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = mockk<UsuarioRepository>()
    private val asistente = UsuarioSesion(1, "Asistente Aula A", PerfilUsuario.ASISTENTE, 1_000L)

    @Test
    fun `login con campos invalidos no llama al servidor`() {
        val viewModel = TeacherLoginViewModel(repository)
        viewModel.actualizarCorreo("asistente@")
        viewModel.actualizarClave("123")

        viewModel.ingresar { }

        assertNotNull(viewModel.uiState.value.errorCorreo)
        assertNotNull(viewModel.uiState.value.errorClave)
        coVerify(exactly = 0) { repository.iniciarSesion(any(), any()) }
    }

    @Test
    fun `login correcto limpia la clave y avisa el exito`() {
        coEvery { repository.iniciarSesion("asistente@mpaz.cl", "Asistente123") } returns asistente
        val viewModel = TeacherLoginViewModel(repository)
        viewModel.actualizarCorreo("  asistente@mpaz.cl ")
        viewModel.actualizarClave("Asistente123")
        var exito = false

        viewModel.ingresar { exito = true }

        assertTrue(exito)
        assertEquals("", viewModel.uiState.value.clave)
        assertFalse(viewModel.uiState.value.ingresando)
    }

    @Test
    fun `login rechazado muestra el mensaje del servidor`() {
        coEvery { repository.iniciarSesion(any(), any()) } throws
                ErrorServidor("Correo o contraseña incorrectos.", 401)
        val viewModel = TeacherLoginViewModel(repository)
        viewModel.actualizarCorreo("asistente@mpaz.cl")
        viewModel.actualizarClave("otraClave")
        var exito = false

        viewModel.ingresar { exito = true }

        assertFalse(exito)
        assertEquals("Correo o contraseña incorrectos.", viewModel.uiState.value.errorGeneral)

        // Al volver a escribir se limpia el error
        viewModel.actualizarClave("Asistente123")
        assertNull(viewModel.uiState.value.errorGeneral)
    }

    @Test
    fun `la sesion compartida refleja el usuario guardado y permite cerrarla`() {
        val usuario = MutableStateFlow<UsuarioSesion?>(asistente)
        every { repository.usuarioActual } returns usuario
        coEvery { repository.cerrarSesion() } answers { usuario.value = null }
        val viewModel = SesionUsuarioViewModel(repository)

        assertEquals(asistente, viewModel.uiState.value.usuario)
        assertFalse(viewModel.uiState.value.cargando)

        viewModel.cerrarSesion()

        assertNull(viewModel.uiState.value.usuario)
        coVerify { repository.cerrarSesion() }
    }
}
