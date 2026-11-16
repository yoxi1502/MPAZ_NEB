package com.example.mpaz_neb.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mpaz_neb.data.model.Post
import com.example.mpaz_neb.model.ComunicadosUiState
import com.example.mpaz_neb.repository.PostRepository
import com.example.mpaz_neb.validacion.Validadores
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException

/** ViewModel que mantiene el estado de los comunicados obtenidos desde la API REST. */
class PostViewModel(
    private val repository: PostRepository = PostRepository()
) : ViewModel() {

    // Flujo mutable interno y flujo público de solo lectura
    private val _uiState = MutableStateFlow(ComunicadosUiState())
    val uiState: StateFlow<ComunicadosUiState> = _uiState.asStateFlow()

    // JSONPlaceholder siempre responde id 101 al crear y no conoce esos posts después,
    // así que les damos un id local único y los editamos/eliminamos solo en la lista.
    private val idsLocales = mutableSetOf<Int>()

    // Se llama automáticamente al crear el ViewModel
    init {
        cargarPosts()
    }

    /** GET: obtiene los comunicados en segundo plano con una corrutina. */
    fun cargarPosts() {
        _uiState.update { it.copy(cargando = true, error = null) }
        viewModelScope.launch {
            try {
                val posts = repository.getPosts()
                _uiState.update { it.copy(cargando = false, posts = posts) }
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (e: Exception) {
                _uiState.update { it.copy(cargando = false, error = mensajeDeError(e)) }
            }
        }
    }

    fun onTituloChange(valor: String) =
        _uiState.update { it.copy(titulo = valor, errorTitulo = null) }

    fun onCuerpoChange(valor: String) =
        _uiState.update { it.copy(cuerpo = valor, errorCuerpo = null) }

    fun limpiarMensaje() = _uiState.update { it.copy(mensaje = null) }

    /** Carga un comunicado en el formulario para editarlo. */
    fun editar(post: Post) = _uiState.update {
        it.copy(
            editandoId = post.id, titulo = post.title, cuerpo = post.body,
            errorTitulo = null, errorCuerpo = null
        )
    }

    fun cancelarEdicion() = _uiState.update {
        it.copy(editandoId = null, titulo = "", cuerpo = "", errorTitulo = null, errorCuerpo = null)
    }

    /** POST si es un comunicado nuevo, PUT si se está editando uno existente. */
    fun guardar() {
        val actual = _uiState.value
        val errorTitulo = Validadores.validarTituloComunicado(actual.titulo)
        val errorCuerpo = Validadores.validarCuerpoComunicado(actual.cuerpo)
        if (errorTitulo != null || errorCuerpo != null) {
            _uiState.update { it.copy(errorTitulo = errorTitulo, errorCuerpo = errorCuerpo) }
            return
        }
        val titulo = actual.titulo.trim()
        val cuerpo = actual.cuerpo.trim()
        val idEditado = actual.editandoId

        _uiState.update { it.copy(guardando = true) }
        viewModelScope.launch {
            try {
                if (idEditado == null) {
                    val respuesta = repository.createPost(titulo, cuerpo)
                    val idsActuales = _uiState.value.posts.mapNotNull { it.id }
                    val idLocal = (idsActuales.maxOrNull() ?: 0) + 1
                    val creado = respuesta.copy(id = idLocal)
                    idsLocales += idLocal
                    // JSONPlaceholder no guarda los cambios: actualizamos la lista local
                    _uiState.update {
                        it.copy(posts = listOf(creado) + it.posts, mensaje = "Comunicado publicado (POST, el servidor respondió id ${respuesta.id}).")
                    }
                } else {
                    val actualizado = if (idEditado in idsLocales) {
                        Post(userId = PostRepository.USUARIO_EQUIPO, id = idEditado, title = titulo, body = cuerpo)
                    } else {
                        repository.updatePost(idEditado, titulo, cuerpo)
                    }
                    _uiState.update { estado ->
                        estado.copy(
                            posts = estado.posts.map { if (it.id == idEditado) actualizado else it },
                            mensaje = "Comunicado actualizado (PUT)."
                        )
                    }
                }
                _uiState.update {
                    it.copy(guardando = false, editandoId = null, titulo = "", cuerpo = "")
                }
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (e: Exception) {
                _uiState.update { it.copy(guardando = false, mensaje = mensajeDeError(e)) }
            }
        }
    }

    /** DELETE: elimina el comunicado en el servidor y lo quita de la lista. */
    fun eliminar(post: Post) {
        val id = post.id ?: return
        viewModelScope.launch {
            try {
                if (id !in idsLocales) repository.deletePost(id)
                idsLocales -= id
                _uiState.update { estado ->
                    estado.copy(
                        posts = estado.posts.filterNot { it.id == id },
                        mensaje = "Comunicado eliminado (DELETE).",
                        editandoId = if (estado.editandoId == id) null else estado.editandoId
                    )
                }
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (e: Exception) {
                _uiState.update { it.copy(mensaje = mensajeDeError(e)) }
            }
        }
    }

    /** Traduce los errores de red a mensajes claros para el usuario. */
    private fun mensajeDeError(e: Exception): String = when (e) {
        is IOException -> "Sin conexión a internet. Revisa la red e intenta de nuevo."
        is HttpException -> "El servidor respondió con error ${e.code()}."
        else -> e.message ?: "Ocurrió un error inesperado."
    }
}
