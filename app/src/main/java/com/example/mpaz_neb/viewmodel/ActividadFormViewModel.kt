package com.example.mpaz_neb.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.mpaz_neb.model.Actividad
import com.example.mpaz_neb.model.ActividadFormUiState
import com.example.mpaz_neb.model.CampoActividad
import com.example.mpaz_neb.model.Dificultad
import com.example.mpaz_neb.model.EstadoActividad
import com.example.mpaz_neb.model.ModuloActividad
import com.example.mpaz_neb.repository.ActividadRepository
import com.example.mpaz_neb.validacion.Validadores
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Formulario de creación/edición de actividades (perfil docente/UTP).
 * Recibe el id de la actividad por la ruta de navegación; si no viene, es una actividad nueva.
 * Guarda con POST (nueva) o PUT (edición) en el microservicio ms-actividades.
 */
class ActividadFormViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: ActividadRepository = ActividadRepository()
) : ViewModel() {

    private val idActividad: Long? = savedStateHandle.get<String>(ARG_ID)?.toLongOrNull()

    private val _uiState = MutableStateFlow(ActividadFormUiState())
    val uiState: StateFlow<ActividadFormUiState> = _uiState.asStateFlow()

    init {
        if (idActividad != null) cargar(idActividad)
    }

    /** GET /api/actividades/{id}: llena el formulario con los datos del servidor. */
    private fun cargar(id: Long) {
        _uiState.update { it.copy(cargando = true) }
        viewModelScope.launch {
            try {
                val actividad = repository.obtener(id)
                _uiState.value = ActividadFormUiState(
                    id = actividad.id,
                    nombre = actividad.nombre,
                    modulo = actividad.modulo,
                    curso = actividad.curso,
                    contenido = actividad.contenido,
                    objetivo = actividad.objetivo,
                    dificultad = actividad.dificultad,
                    instrucciones = actividad.instrucciones,
                    cantidadPreguntas = actividad.cantidadPreguntas.toString(),
                    intentosPermitidos = actividad.intentosPermitidos.toString(),
                    estado = actividad.estado
                )
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(cargando = false, errorGeneral = e.message ?: "No se encontró la actividad.")
                }
            }
        }
    }

    // ---- Cambios de campos: cada cambio limpia el error de ese campo ----

    private fun limpiar(campo: CampoActividad, cambio: (ActividadFormUiState) -> ActividadFormUiState) {
        _uiState.update { cambio(it).copy(errores = it.errores - campo, errorGeneral = null) }
    }

    fun cambiarNombre(valor: String) = limpiar(CampoActividad.NOMBRE) { it.copy(nombre = valor) }
    fun cambiarContenido(valor: String) = limpiar(CampoActividad.CONTENIDO) { it.copy(contenido = valor) }
    fun cambiarObjetivo(valor: String) = limpiar(CampoActividad.OBJETIVO) { it.copy(objetivo = valor) }
    fun cambiarInstrucciones(valor: String) =
        limpiar(CampoActividad.INSTRUCCIONES) { it.copy(instrucciones = valor) }

    fun cambiarCantidadPreguntas(valor: String) =
        limpiar(CampoActividad.CANTIDAD_PREGUNTAS) { it.copy(cantidadPreguntas = valor.take(2)) }

    fun cambiarIntentos(valor: String) =
        limpiar(CampoActividad.INTENTOS) { it.copy(intentosPermitidos = valor.take(2)) }

    fun cambiarModulo(valor: ModuloActividad) = _uiState.update { it.copy(modulo = valor) }
    fun cambiarCurso(valor: String) = _uiState.update { it.copy(curso = valor) }
    fun cambiarDificultad(valor: Dificultad) = _uiState.update { it.copy(dificultad = valor) }
    fun cambiarEstado(valor: EstadoActividad) = _uiState.update { it.copy(estado = valor) }

    /** Valida todos los campos; si hay errores los muestra y no envía nada al servidor. */
    fun guardar(onExito: () -> Unit) {
        val estado = _uiState.value
        if (estado.guardando) return

        val errores = buildMap {
            Validadores.validarNombreActividad(estado.nombre)?.let { put(CampoActividad.NOMBRE, it) }
            Validadores.validarContenido(estado.contenido)?.let { put(CampoActividad.CONTENIDO, it) }
            Validadores.validarTextoPedagogico(estado.objetivo, "el objetivo pedagógico")
                ?.let { put(CampoActividad.OBJETIVO, it) }
            Validadores.validarTextoPedagogico(estado.instrucciones, "las instrucciones")
                ?.let { put(CampoActividad.INSTRUCCIONES, it) }
            Validadores.validarEnteroEnRango(estado.cantidadPreguntas, 1, Validadores.PREGUNTAS_MAX)
                ?.let { put(CampoActividad.CANTIDAD_PREGUNTAS, it) }
            Validadores.validarEnteroEnRango(estado.intentosPermitidos, 1, Validadores.INTENTOS_MAX)
                ?.let { put(CampoActividad.INTENTOS, it) }
        }

        if (errores.isNotEmpty()) {
            _uiState.update {
                it.copy(errores = errores, errorGeneral = "Revisa los campos marcados en rojo.")
            }
            return
        }

        _uiState.update { it.copy(guardando = true, errorGeneral = null) }
        viewModelScope.launch {
            try {
                repository.guardar(
                    Actividad(
                        id = estado.id,
                        nombre = estado.nombre.trim(),
                        modulo = estado.modulo,
                        curso = estado.curso,
                        contenido = estado.contenido.trim(),
                        objetivo = estado.objetivo.trim(),
                        dificultad = estado.dificultad,
                        instrucciones = estado.instrucciones.trim(),
                        cantidadPreguntas = estado.cantidadPreguntas.trim().toInt(),
                        intentosPermitidos = estado.intentosPermitidos.trim().toInt(),
                        estado = estado.estado,
                        actualizadaEn = 0L
                    )
                )
                _uiState.update { it.copy(guardando = false) }
                onExito()
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (e: Exception) {
                // Por ejemplo: 409 "Ya existe una actividad con ese nombre..." o sin conexión
                _uiState.update {
                    it.copy(guardando = false, errorGeneral = e.message ?: "No se pudo guardar.")
                }
            }
        }
    }

    companion object {
        const val ARG_ID = "id"

        /** Crea el ViewModel entregándole el SavedStateHandle con el id de la ruta. */
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer { ActividadFormViewModel(createSavedStateHandle()) }
        }
    }
}
