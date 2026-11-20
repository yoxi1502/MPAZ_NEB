package com.example.mpaz_neb.viewmodel

import androidx.lifecycle.ViewModel
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

/**
 * Catálogo de actividades del microservicio ms-actividades:
 * lista (GET), activa/desactiva (PATCH) y elimina (DELETE).
 * Crear y editar se hacen en ActividadFormViewModel.
 */
class CatalogoViewModel(
    private val repository: ActividadRepository = ActividadRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(CatalogoUiState())
    val uiState: StateFlow<CatalogoUiState> = _uiState.asStateFlow()

    /** GET: la pantalla lo llama al abrirse y al volver del formulario. */
    fun cargar() {
        _uiState.update { it.copy(cargando = true, error = null) }
        viewModelScope.launch {
            try {
                val lista = repository.listar()
                _uiState.update { it.copy(actividades = ordenar(lista), cargando = false) }
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(cargando = false, error = e.message ?: "No se pudo cargar el catálogo.")
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

    /** PATCH: activa (Disponible) o desactiva una actividad sin borrarla. */
    fun cambiarDisponible(actividad: Actividad, disponible: Boolean) {
        val id = actividad.id ?: return
        if (_uiState.value.procesandoId != null) return
        val nuevoEstado = if (disponible) EstadoActividad.DISPONIBLE else EstadoActividad.DESACTIVADA

        _uiState.update { it.copy(procesandoId = id) }
        viewModelScope.launch {
            try {
                val actualizada = repository.cambiarEstado(id, nuevoEstado)
                _uiState.update { estado ->
                    estado.copy(
                        actividades = estado.actividades.map { if (it.id == id) actualizada else it },
                        procesandoId = null,
                        mensaje = "\"${actualizada.nombre}\" quedó ${actualizada.estado.etiqueta.lowercase()}."
                    )
                }
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(procesandoId = null, mensaje = e.message ?: "No se pudo actualizar.")
                }
            }
        }
    }

    // ---- Eliminar: primero se pide confirmación, luego DELETE ----

    fun pedirEliminar(actividad: Actividad) {
        _uiState.update { it.copy(actividadAEliminar = actividad) }
    }

    fun cancelarEliminar() {
        _uiState.update { it.copy(actividadAEliminar = null) }
    }

    fun confirmarEliminar() {
        val actividad = _uiState.value.actividadAEliminar ?: return
        val id = actividad.id ?: return

        _uiState.update { it.copy(actividadAEliminar = null, procesandoId = id) }
        viewModelScope.launch {
            try {
                repository.eliminar(id)
                _uiState.update { estado ->
                    estado.copy(
                        actividades = estado.actividades.filterNot { it.id == id },
                        procesandoId = null,
                        mensaje = "Se eliminó \"${actividad.nombre}\"."
                    )
                }
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(procesandoId = null, mensaje = e.message ?: "No se pudo eliminar.")
                }
            }
        }
    }

    fun limpiarMensaje() {
        _uiState.update { it.copy(mensaje = null) }
    }

    private fun ordenar(lista: List<Actividad>): List<Actividad> =
        lista.sortedWith(compareBy<Actividad> { it.modulo }.thenBy { it.curso }.thenBy { it.nombre })
}
