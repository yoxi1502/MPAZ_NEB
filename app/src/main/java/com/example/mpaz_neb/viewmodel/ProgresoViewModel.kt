package com.example.mpaz_neb.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mpaz_neb.model.ProgresoUiState
import com.example.mpaz_neb.repository.ProgresoRepository
import com.example.mpaz_neb.repository.SesionAulaRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Une los últimos resultados y el historial de sesiones guardados en DataStore. */
class ProgresoViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = ProgresoRepository(application)
    private val sesionRepository = SesionAulaRepository(application)

    private val _uiState = MutableStateFlow(ProgresoUiState())
    val uiState: StateFlow<ProgresoUiState> = _uiState.asStateFlow()

    private var observacion: Job? = null

    init {
        cargarProgreso()
    }

    fun cargarProgreso() {
        observacion?.cancel()
        _uiState.update { it.copy(cargando = true, error = null) }

        observacion = viewModelScope.launch {
            try {
                combine(repository.progreso, sesionRepository.sesiones) { progreso, sesiones ->
                    progreso to sesiones
                }.collect { (progreso, sesiones) ->
                    _uiState.update {
                        it.copy(
                            lectura = progreso.lectura,
                            matematica = progreso.matematica,
                            sesiones = sesiones,
                            cargando = false,
                            error = null
                        )
                    }
                }
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (error: Exception) {
                _uiState.update {
                    it.copy(cargando = false, error = "No se pudieron recuperar los resultados.")
                }
            }
        }
    }

    fun filtrarEstudiante(estudiante: String?) {
        _uiState.update { it.copy(filtroEstudiante = estudiante) }
    }
}
