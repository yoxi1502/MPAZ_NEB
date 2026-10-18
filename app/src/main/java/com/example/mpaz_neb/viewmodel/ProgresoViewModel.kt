package com.example.mpaz_neb.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mpaz_neb.model.ProgresoUiState
import com.example.mpaz_neb.repository.ProgresoRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProgresoViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = ProgresoRepository(application)

    private val _uiState = MutableStateFlow(
        ProgresoUiState()
    )

    val uiState: StateFlow<ProgresoUiState> =
        _uiState.asStateFlow()

    private var observacion: Job? = null

    init {
        cargarProgreso()
    }

    fun cargarProgreso() {
        observacion?.cancel()

        _uiState.update { estado ->
            estado.copy(
                cargando = true,
                error = null
            )
        }

        observacion = viewModelScope.launch {
            try {
                repository.progreso.collect { progreso ->
                    _uiState.value = progreso
                }
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (error: Exception) {
                _uiState.update { estado ->
                    estado.copy(
                        cargando = false,
                        error = "No se pudieron recuperar los resultados."
                    )
                }
            }
        }
    }
}

