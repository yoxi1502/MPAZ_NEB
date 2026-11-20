package com.example.mpaz_neb.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.mpaz_neb.data.local.SesionDispositivoStore
import com.example.mpaz_neb.model.ProgresoUiState
import com.example.mpaz_neb.model.SesionAula
import com.example.mpaz_neb.repository.ProgresoRepository
import com.example.mpaz_neb.repository.SesionAulaRepository
import com.example.mpaz_neb.validacion.Validadores
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Pantalla de resultados:
 * - Último resultado por módulo guardado en el dispositivo (DataStore).
 * - Historial de sesiones del servidor (GET), con edición de la observación (PATCH)
 *   y eliminación de sesiones finalizadas (DELETE).
 */
class ProgresoViewModel(
    private val progresoRepository: ProgresoRepository,
    private val sesionRepository: SesionAulaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProgresoUiState())
    val uiState: StateFlow<ProgresoUiState> = _uiState.asStateFlow()

    private var observacionLocal: Job? = null

    init {
        cargarProgreso()
    }

    /** Carga (o vuelve a cargar) el progreso local y el historial del servidor. */
    fun cargarProgreso() {
        observarProgresoLocal()
        cargarHistorial()
    }

    private fun observarProgresoLocal() {
        observacionLocal?.cancel()
        _uiState.update { it.copy(cargando = true, error = null) }
        observacionLocal = viewModelScope.launch {
            try {
                progresoRepository.progreso.collect { progreso ->
                    _uiState.update {
                        it.copy(lectura = progreso.lectura, matematica = progreso.matematica, cargando = false)
                    }
                }
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(cargando = false, error = "No se pudieron recuperar los resultados.")
                }
            }
        }
    }

    /** GET /api/sesiones?estudiante=: el filtro lo aplica el servidor. */
    fun cargarHistorial() {
        val filtro = _uiState.value.filtroEstudiante
        _uiState.update { it.copy(cargandoHistorial = true, errorHistorial = null) }
        viewModelScope.launch {
            try {
                val sesiones = sesionRepository.listar(filtro)
                _uiState.update { it.copy(sesiones = sesiones, cargandoHistorial = false) }
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        cargandoHistorial = false,
                        errorHistorial = e.message ?: "No se pudo cargar el historial."
                    )
                }
            }
        }
    }

    fun filtrarEstudiante(estudiante: String?) {
        _uiState.update { it.copy(filtroEstudiante = estudiante) }
        cargarHistorial()
    }

    // ---- Editar la observación de una sesión (PATCH) ----

    fun editarObservacion(sesion: SesionAula) {
        _uiState.update {
            it.copy(sesionEditando = sesion, observacionEditada = sesion.observacion, errorObservacion = null)
        }
    }

    fun cambiarObservacion(texto: String) {
        _uiState.update { it.copy(observacionEditada = texto, errorObservacion = null) }
    }

    fun cancelarEdicion() {
        _uiState.update { it.copy(sesionEditando = null, errorObservacion = null) }
    }

    fun guardarObservacion() {
        val estado = _uiState.value
        val sesion = estado.sesionEditando ?: return
        if (estado.guardandoObservacion) return

        val error = Validadores.validarObservacion(estado.observacionEditada)
        if (error != null) {
            _uiState.update { it.copy(errorObservacion = error) }
            return
        }

        _uiState.update { it.copy(guardandoObservacion = true) }
        viewModelScope.launch {
            try {
                val actualizada = sesionRepository.guardarObservacion(sesion.id, estado.observacionEditada)
                _uiState.update { actual ->
                    actual.copy(
                        sesiones = actual.sesiones.map { if (it.id == actualizada.id) actualizada else it },
                        sesionEditando = null,
                        guardandoObservacion = false,
                        mensaje = "Observación actualizada."
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

    // ---- Eliminar una sesión finalizada (DELETE) ----

    fun pedirEliminar(sesion: SesionAula) {
        if (sesion.abierta) {
            _uiState.update { it.copy(mensaje = "Finaliza la sesión antes de eliminarla.") }
            return
        }
        _uiState.update { it.copy(sesionAEliminar = sesion) }
    }

    fun cancelarEliminar() {
        _uiState.update { it.copy(sesionAEliminar = null) }
    }

    fun confirmarEliminar() {
        val sesion = _uiState.value.sesionAEliminar ?: return
        _uiState.update { it.copy(sesionAEliminar = null, eliminandoId = sesion.id) }
        viewModelScope.launch {
            try {
                sesionRepository.eliminar(sesion.id)
                _uiState.update { actual ->
                    actual.copy(
                        sesiones = actual.sesiones.filterNot { it.id == sesion.id },
                        eliminandoId = null,
                        mensaje = "Sesión eliminada del historial."
                    )
                }
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(eliminandoId = null, mensaje = e.message ?: "No se pudo eliminar la sesión.")
                }
            }
        }
    }

    fun limpiarMensaje() {
        _uiState.update { it.copy(mensaje = null) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val aplicacion = checkNotNull(this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                ProgresoViewModel(
                    progresoRepository = ProgresoRepository(aplicacion),
                    sesionRepository = SesionAulaRepository(SesionDispositivoStore(aplicacion))
                )
            }
        }
    }
}
