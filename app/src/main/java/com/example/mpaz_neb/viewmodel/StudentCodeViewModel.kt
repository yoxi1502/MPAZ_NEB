package com.example.mpaz_neb.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mpaz_neb.model.SesionAula
import com.example.mpaz_neb.model.StudentCodeUiState
import com.example.mpaz_neb.repository.SesionAulaRepository
import com.example.mpaz_neb.validacion.Validadores
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class StudentCodeViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = SesionAulaRepository(application)

    private val _uiState = MutableStateFlow(StudentCodeUiState())
    val uiState: StateFlow<StudentCodeUiState> = _uiState.asStateFlow()

    fun actualizarCodigo(codigo: String) {
        _uiState.update {
            it.copy(
                codigo = Validadores.normalizarCodigo(codigo),
                errorCodigo = null
            )
        }
    }

    /**
     * Valida el formato del código y luego verifica en la persistencia local
     * que corresponda a la sesión abierta. El estudiante solo puede entrar a la
     * actividad que le asignó la asistente.
     */
    fun validarCodigo(onExito: (SesionAula) -> Unit) {
        if (_uiState.value.validando) return
        val codigo = Validadores.normalizarCodigo(_uiState.value.codigo)

        val error = Validadores.validarCodigoSesion(codigo)
        if (error != null) {
            _uiState.update { it.copy(errorCodigo = error) }
            return
        }

        _uiState.update { it.copy(validando = true, errorCodigo = null) }

        viewModelScope.launch {
            try {
                val sesion = repository.ingresarConCodigo(codigo)
                _uiState.update { it.copy(validando = false) }
                onExito(sesion)
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        validando = false,
                        errorCodigo = e.message
                            ?: "El código no es correcto. Revísalo e intenta nuevamente."
                    )
                }
            }
        }
    }
}
