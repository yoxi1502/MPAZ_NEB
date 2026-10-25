package com.example.mpaz_neb.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mpaz_neb.model.Actividad
import com.example.mpaz_neb.model.CatalogoUiState
import com.example.mpaz_neb.model.EstadoActividad
import com.example.mpaz_neb.model.ModuloActividad
import com.example.mpaz_neb.repository.ActividadRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Lista y filtra el catálogo; permite activar o desactivar actividades. */
class CatalogoViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = ActividadRepository(application)

    private val _uiState = MutableStateFlow(CatalogoUiState())
    val uiState: StateFlow<CatalogoUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                repository.actividades.collect { lista ->
                    _uiState.update {
                        it.copy(
                            actividades = lista.sortedWith(
                                compareBy<Actividad> { a -> a.modulo }.thenBy { a -> a.curso }
                            ),
                            cargando = false
                        )
                    }
                }
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(cargando = false, error = "No se pudo cargar el catálogo.")
                }
            }
        }
    }

    fun filtrarModulo(modulo: ModuloActividad?) {
        _uiState.update { it.copy(filtroModulo = modulo) }
    }

    fun filtrarCurso(curso: String?) {
        _uiState.update { it.copy(filtroCurso = curso) }
    }

    /** Activa (Disponible) o desactiva una actividad sin borrarla. */
    fun cambiarDisponible(actividad: Actividad, disponible: Boolean) {
        val nuevoEstado = if (disponible) EstadoActividad.DISPONIBLE else EstadoActividad.DESACTIVADA
        viewModelScope.launch {
            try {
                repository.cambiarEstado(actividad.id, nuevoEstado)
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "No se pudo actualizar.") }
            }
        }
    }
}
