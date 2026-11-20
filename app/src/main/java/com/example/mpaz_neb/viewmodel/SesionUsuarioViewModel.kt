package com.example.mpaz_neb.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.mpaz_neb.data.local.UsuarioSesionStore
import com.example.mpaz_neb.model.UsuarioSesionUiState
import com.example.mpaz_neb.repository.UsuarioRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel compartido por toda la app (vive en la Activity).
 * Expone qué usuario del equipo pedagógico tiene la sesión abierta.
 */
class SesionUsuarioViewModel(
    private val repository: UsuarioRepository
) : ViewModel() {

    val uiState: StateFlow<UsuarioSesionUiState> =
        repository.usuarioActual
            .map { usuario -> UsuarioSesionUiState(cargando = false, usuario = usuario) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = UsuarioSesionUiState()
            )

    fun cerrarSesion() {
        viewModelScope.launch {
            repository.cerrarSesion()
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val aplicacion = checkNotNull(this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                SesionUsuarioViewModel(UsuarioRepository(UsuarioSesionStore(aplicacion)))
            }
        }
    }
}
