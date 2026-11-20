package com.example.mpaz_neb.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.mpaz_neb.data.local.UsuarioSesionStore
import com.example.mpaz_neb.model.TeacherLoginUiState
import com.example.mpaz_neb.repository.UsuarioRepository
import com.example.mpaz_neb.validacion.Validadores
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Login del equipo pedagógico: valida en la app y autentica contra ms-usuarios. */
class TeacherLoginViewModel(
    private val repository: UsuarioRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TeacherLoginUiState())
    val uiState: StateFlow<TeacherLoginUiState> = _uiState.asStateFlow()

    fun actualizarCorreo(correo: String) {
        _uiState.update {
            it.copy(correo = correo, errorCorreo = null, errorGeneral = null)
        }
    }

    fun actualizarClave(clave: String) {
        _uiState.update {
            it.copy(clave = clave, errorClave = null, errorGeneral = null)
        }
    }

    /** Valida cada campo y, si todo es correcto, envía el login al microservicio. */
    fun ingresar(onExito: () -> Unit) {
        val estado = _uiState.value
        if (estado.ingresando) return

        val correo = estado.correo.trim()
        val errorCorreo = Validadores.validarCorreo(correo)
        val errorClave = Validadores.validarClave(estado.clave)

        _uiState.update {
            it.copy(
                correo = correo,
                errorCorreo = errorCorreo,
                errorClave = errorClave,
                errorGeneral = null
            )
        }
        if (errorCorreo != null || errorClave != null) return

        _uiState.update { it.copy(ingresando = true) }

        viewModelScope.launch {
            try {
                repository.iniciarSesion(correo, estado.clave)
                _uiState.update { it.copy(ingresando = false, clave = "") }
                onExito()
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (error: Exception) {
                _uiState.update {
                    it.copy(
                        ingresando = false,
                        errorGeneral = error.message ?: "No se pudo iniciar sesión."
                    )
                }
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val aplicacion = checkNotNull(this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                TeacherLoginViewModel(UsuarioRepository(UsuarioSesionStore(aplicacion)))
            }
        }
    }
}
