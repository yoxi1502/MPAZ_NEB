package com.example.mpaz_neb.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mpaz_neb.model.ModuloActividad
import com.example.mpaz_neb.model.SesionAula
import com.example.mpaz_neb.model.StudentCodeUiState
import com.example.mpaz_neb.repository.SesionAulaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class StudentCodeViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = SesionAulaRepository(application)

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

    fun validarCodigo(
        moduloRequerido: ModuloActividad? = null,
        onExito: (SesionAula?) -> Unit
    ) {
        val codigo = _uiState.value.codigo.trim().uppercase()

        if (codigo.isBlank()) {
            _uiState.update {
                it.copy(errorCodigo = "Escribe el código que te entregó tu profesora.")
            }
            return
        }

        if (codigo == CODIGO_DEMO) {
            _uiState.update { it.copy(errorCodigo = null) }
            onExito(null)
            return
        }

        _uiState.update { it.copy(validando = true, errorCodigo = null) }

        viewModelScope.launch {
            try {
                val sesion = repository.ingresarConCodigo(codigo, moduloRequerido)
                _uiState.update { it.copy(validando = false) }
                onExito(sesion)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        validando = false,
                        errorCodigo = e.message ?: "El código no es correcto. Revísalo e intenta nuevamente."
                    )
                }
            }
        }
    }

    companion object {
        /** Código de demostración (dato ficticio) para probar la app sin generar una sesión. */
        const val CODIGO_DEMO = "MPAZ-402"
    }
}
