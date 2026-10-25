package com.example.mpaz_neb.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mpaz_neb.model.ModuloActividad
import com.example.mpaz_neb.model.SesionAulaUiState
import com.example.mpaz_neb.repository.SesionAulaRepository
import com.example.mpaz_neb.validacion.Validadores
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
                            error = null,
                            // Si la asistente no está escribiendo, se muestra lo guardado
                            observacion = if (actual.observacionEditada) {
                                actual.observacion
                            } else {
                                sesion?.observacion.orEmpty()
                            }
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

    fun actualizarObservacion(texto: String) {
        _uiState.update {
            it.copy(
                observacion = texto,
                observacionEditada = true,
                errorObservacion = null,
                observacionGuardada = false
            )
        }
    }

    /** Valida la observación pedagógica y la guarda en la sesión actual. */
    fun guardarObservacion() {
        val estado = _uiState.value
        val sesionId = estado.sesion?.id ?: return
        if (estado.guardandoObservacion) return

        val error = Validadores.validarObservacion(estado.observacion)
        if (error != null) {
            _uiState.update { it.copy(errorObservacion = error, observacionGuardada = false) }
            return
        }

        _uiState.update { it.copy(guardandoObservacion = true, errorObservacion = null) }
        viewModelScope.launch {
            try {
                repository.guardarObservacion(sesionId, estado.observacion)
                _uiState.update {
                    it.copy(
                        observacion = estado.observacion.trim(),
                        guardandoObservacion = false,
                        observacionEditada = false,
                        observacionGuardada = true
                    )
                }
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        guardandoObservacion = false,
                        errorObservacion = e.message ?: "No se pudo guardar la observación."
                    )
                }
            }
        }
    }

    fun generarCodigo(asistente: String, onExito: () -> Unit) {
        if (!validarSeleccion()) return
        val estadoActual = _uiState.value
        if (estadoActual.guardando) return
        _uiState.update { it.copy(guardando = true, error = null) }
        viewModelScope.launch {
            try {
                repository.crearSesion(
                    modulo = estadoActual.modulo,
                    curso = estadoActual.curso,
                    estudiante = estadoActual.estudiante,
                    asistente = asistente
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
