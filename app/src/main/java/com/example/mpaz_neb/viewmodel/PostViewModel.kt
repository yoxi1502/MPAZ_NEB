package com.example.mpaz_neb.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mpaz_neb.model.ComunicadosUiState
import com.example.mpaz_neb.repository.PostRepository
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

    /** Traduce los errores de red a mensajes claros para el usuario. */
    private fun mensajeDeError(e: Exception): String = when (e) {
        is IOException -> "Sin conexión a internet. Revisa la red e intenta de nuevo."
        is HttpException -> "El servidor respondió con error ${e.code()}."
        else -> e.message ?: "Ocurrió un error inesperado."
    }
}
