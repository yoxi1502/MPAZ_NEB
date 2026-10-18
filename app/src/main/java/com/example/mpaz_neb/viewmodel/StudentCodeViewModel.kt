package com.example.mpaz_neb.viewmodel

import androidx.lifecycle.ViewModel
import com.example.mpaz_neb.model.StudentCodeUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class StudentCodeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(
        StudentCodeUiState()
    )

    val uiState: StateFlow<StudentCodeUiState> =
        _uiState.asStateFlow()

    fun actualizarCodigo(codigo: String) {
        _uiState.update { estado ->
            estado.copy(
                codigo = codigo,
                errorCodigo = null
            )
        }
    }

    fun validarCodigo(): Boolean {
        val codigo = _uiState.value.codigo
            .trim()
            .uppercase()

        val error = when {
            codigo.isBlank() -> {
                "Escribe el código que te entregó tu profesora."
            }

            codigo != "MPAZ-402" -> {
                "El código no es correcto. Revísalo e intenta nuevamente."
            }

            else -> null
        }

        _uiState.update { estado ->
            estado.copy(
                codigo = codigo,
                errorCodigo = error
            )
        }

        return error == null
    }
}