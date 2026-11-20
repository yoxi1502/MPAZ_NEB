package com.example.mpaz_neb.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.mpaz_neb.data.local.SesionDispositivoStore
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
    private val repository: SesionAulaRepository
) : ViewModel() {

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
     * Valida el formato del código en la app y luego lo envía a ms-sesiones,
     * que verifica que corresponda a una sesión abierta. El estudiante solo
     * puede entrar a la actividad que le asignó la asistente.
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

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val aplicacion = checkNotNull(this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                StudentCodeViewModel(SesionAulaRepository(SesionDispositivoStore(aplicacion)))
            }
        }
    }
}
