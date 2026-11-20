package com.example.mpaz_neb.viewmodel

import com.example.mpaz_neb.data.model.Post
import com.example.mpaz_neb.data.remote.ApiService
import com.example.mpaz_neb.repository.PostRepository
import com.example.mpaz_neb.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.io.IOException
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import retrofit2.Response

/** Prueba los comunicados de la Guía 14 (JSONPlaceholder) con la API simulada. */
class PostViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val api = mockk<ApiService>()
    private lateinit var viewModel: PostViewModel

    private val existente = Post(userId = 1, id = 1, title = "Reunión de apoderados", body = "El jueves a las 18:00 en la biblioteca.")

    @Before
    fun preparar() {
        coEvery { api.getPosts(1) } returns listOf(existente)
        viewModel = PostViewModel(PostRepository(api))
    }

    @Test
    fun `al crearse carga los comunicados (GET)`() {
        assertEquals(listOf(existente), viewModel.uiState.value.posts)
        assertFalse(viewModel.uiState.value.cargando)
    }

    @Test
    fun `sin internet muestra el error`() {
        coEvery { api.getPosts(1) } throws IOException("sin red")

        viewModel.cargarPosts()

        assertNotNull(viewModel.uiState.value.error)
    }

    @Test
    fun `un comunicado invalido no se envia`() {
        viewModel.onTituloChange("Hola")
        viewModel.onCuerpoChange("Corto")

        viewModel.guardar()

        assertNotNull(viewModel.uiState.value.errorTitulo)
        assertNotNull(viewModel.uiState.value.errorCuerpo)
        coVerify(exactly = 0) { api.createPost(any()) }
    }

    @Test
    fun `publicar (POST) agrega el comunicado arriba con un id local`() {
        coEvery { api.createPost(any()) } answers { firstArg<Post>().copy(id = 101) }
        viewModel.onTituloChange("Práctica de lectura")
        viewModel.onCuerpoChange("El viernes se practica lectura en voz alta.")

        viewModel.guardar()

        val primero = viewModel.uiState.value.posts.first()
        assertEquals("Práctica de lectura", primero.title)
        assertEquals(2, primero.id)
        assertFalse(viewModel.uiState.value.guardando)
    }

    @Test
    fun `editar (PUT) reemplaza el comunicado`() {
        coEvery { api.updatePost(1, any()) } answers { secondArg() }
        viewModel.editar(existente)
        assertTrue(viewModel.uiState.value.editando)
        viewModel.onTituloChange("Reunión cambiada")

        viewModel.guardar()

        assertEquals("Reunión cambiada", viewModel.uiState.value.posts.single().title)
        coVerify { api.updatePost(1, any()) }
    }

    @Test
    fun `eliminar (DELETE) quita el comunicado`() {
        coEvery { api.deletePost(1) } returns Response.success(200, Unit)

        viewModel.eliminar(existente)

        assertTrue(viewModel.uiState.value.posts.isEmpty())
    }

    @Test
    fun `si el DELETE responde error el comunicado se mantiene`() {
        coEvery { api.deletePost(1) } returns Response.error(500, "".toResponseBody())

        viewModel.eliminar(existente)

        assertEquals(1, viewModel.uiState.value.posts.size)
        assertNotNull(viewModel.uiState.value.mensaje)

        viewModel.limpiarMensaje()
        viewModel.cancelarEdicion()
        assertEquals(null, viewModel.uiState.value.mensaje)
    }
}
