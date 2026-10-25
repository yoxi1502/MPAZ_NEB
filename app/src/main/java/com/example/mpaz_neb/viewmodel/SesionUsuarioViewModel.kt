package com.example.mpaz_neb.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
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
    application: Application
) : AndroidViewModel(application) {

    private val repository = UsuarioRepository(application)

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
}
