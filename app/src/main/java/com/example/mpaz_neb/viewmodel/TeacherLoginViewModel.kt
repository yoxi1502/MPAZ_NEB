package com.example.mpaz_neb.viewmodel

import android.util.Patterns
import androidx.lifecycle.ViewModel
import com.example.mpaz_neb.model.TeacherLoginUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class TeacherLoginViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(
        TeacherLoginUiState()
    )

    val uiState: StateFlow<TeacherLoginUiState> =
        _uiState.asStateFlow()

    fun actualizarCorreo(correo: String) {
        _uiState.update { estado ->
            estado.copy(
                correo = correo,
                errorCorreo = null
            )
        }
    }

    fun actualizarClave(clave: String) {
        _uiState.update { estado ->
            estado.copy(
                clave = clave,
                errorClave = null
            )
        }
    }

    fun validarFormulario(): Boolean {
        val estado = _uiState.value
        val correo = estado.correo.trim()

        val errorCorreo = when {
            correo.isBlank() -> {
                "Escribe tu correo."
            }

            !Patterns.EMAIL_ADDRESS.matcher(correo).matches() -> {
                "Ingresa un correo válido."
            }

            else -> null
        }

        val errorClave = when {
            estado.clave.isBlank() -> {
                "Escribe tu contraseña."
            }

            estado.clave.length < 6 -> {
                "La contraseña debe tener al menos 6 caracteres."
            }

            else -> null
        }

        _uiState.update { actual ->
            actual.copy(
                correo = correo,
                errorCorreo = errorCorreo,
                errorClave = errorClave
            )
        }

        return errorCorreo == null && errorClave == null
    }
}