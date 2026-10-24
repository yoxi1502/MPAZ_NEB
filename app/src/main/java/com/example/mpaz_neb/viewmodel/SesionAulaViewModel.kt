package com.example.mpaz_neb.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mpaz_neb.model.ModuloActividad
import com.example.mpaz_neb.model.SesionAulaUiState
import com.example.mpaz_neb.repository.SesionAulaRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SesionAulaViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = SesionAulaRepository(application)

    private val _uiState = MutableStateFlow(SesionAulaUiState())
    val uiState: StateFlow<SesionAulaUiState> = _uiState.asStateFlow()

    private var cargarJob: Job? = null

    init {
        cargarSesion()
    }

    fun cargarSesion() {
        cargarJob?.cancel()
        cargarJob = viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, error = null) }
            try {
                repository.sesionActual.collect { sesion ->
                    _uiState.update { actual ->
                        actual.copy(
                            sesion = sesion,
                            cargando = false,
                            error = null
                        )
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { actual ->
                    actual.copy(
                        cargando = false,
                        error = e.message ?: "Error al cargar la sesión."
                    )
                }
            }
        }
    }

    fun seleccionarModulo(modulo: ModuloActividad) {
        _uiState.update { it.copy(modulo = modulo) }
    }

    fun seleccionarCurso(curso: String) {
        _uiState.update { it.copy(curso = curso, errorCurso = null) }
    }

    fun seleccionarEstudiante(estudiante: String) {
        _uiState.update { it.copy(estudiante = estudiante, errorEstudiante = null) }
    }

    /**
     * Valida la selección del formulario de asignación.
     * La lógica vive en el ViewModel; la interfaz solo muestra los errores del estado.
     */
    fun validarSeleccion(): Boolean {
        val estado = _uiState.value
        val errorCurso = if (estado.curso !in SesionAulaRepository.CURSOS) {
            "Selecciona un nivel o curso."
        } else null
        val errorEstudiante = if (estado.estudiante !in SesionAulaRepository.ESTUDIANTES) {
            "Selecciona un estudiante."
        } else null

        _uiState.update {
            it.copy(errorCurso = errorCurso, errorEstudiante = errorEstudiante)
        }
        return errorCurso == null && errorEstudiante == null
    }

    fun generarCodigo(onExito: () -> Unit) {
        if (!validarSeleccion()) return
        val estadoActual = _uiState.value
        _uiState.update { it.copy(guardando = true, error = null) }
        viewModelScope.launch {
            try {
                repository.crearSesion(
                    modulo = estadoActual.modulo,
                    curso = estadoActual.curso,
                    estudiante = estadoActual.estudiante
                )
                _uiState.update { it.copy(guardando = false) }
                onExito()
            } catch (e: Exception) {
                _uiState.update { actual ->
                    actual.copy(
                        guardando = false,
                        error = e.message ?: "No se pudo generar el código de la sesión."
                    )
                }
            }
        }
    }

    fun finalizarSesion(onExito: () -> Unit) {
        val sesionId = _uiState.value.sesion?.id ?: return
        _uiState.update { it.copy(guardando = true, error = null) }
        viewModelScope.launch {
            try {
                repository.finalizarSesion(sesionId)
                _uiState.update { it.copy(guardando = false) }
                onExito()
            } catch (e: Exception) {
                _uiState.update { actual ->
                    actual.copy(
                        guardando = false,
                        error = e.message ?: "No se pudo finalizar la sesión."
                    )
                }
            }
        }
    }
}
