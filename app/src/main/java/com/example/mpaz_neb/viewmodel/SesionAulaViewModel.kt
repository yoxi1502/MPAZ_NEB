package com.example.mpaz_neb.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.mpaz_neb.data.local.SesionDispositivoStore
import com.example.mpaz_neb.model.Actividad
import com.example.mpaz_neb.model.EstadoActividad
import com.example.mpaz_neb.model.ModuloActividad
import com.example.mpaz_neb.model.SesionAulaUiState
import com.example.mpaz_neb.repository.ActividadRepository
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
 * Panel de la asistente: asigna una actividad (crea la sesión en ms-sesiones),
 * acompaña la sesión, guarda la observación y la finaliza.
 */
class SesionAulaViewModel(
    private val repository: SesionAulaRepository,
    private val actividadRepository: ActividadRepository = ActividadRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(SesionAulaUiState())
    val uiState: StateFlow<SesionAulaUiState> = _uiState.asStateFlow()

    private var cargarJob: Job? = null

    init {
        cargarActividades()
    }

    /** GET a ms-actividades: solo las actividades Disponibles se pueden asignar. */
    fun cargarActividades() {
        viewModelScope.launch {
            try {
                val lista = actividadRepository.listar(estado = EstadoActividad.DISPONIBLE)
                _uiState.update { it.copy(actividades = lista, errorActividades = null) }
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorActividades = e.message ?: "No se pudo cargar el catálogo.")
                }
            }
        }
    }

    /**
     * GET a ms-sesiones de la sesión abierta en este dispositivo.
     * La pantalla lo llama al abrirse y con el botón Actualizar.
     */
    fun cargarSesion() {
        cargarJob?.cancel()
        cargarJob = viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, error = null) }
            try {
                val sesion = repository.obtenerSesionActual()
                _uiState.update { actual ->
                    actual.copy(
                        sesion = sesion,
                        cargando = false,
                        // Si la asistente no está escribiendo, se muestra lo guardado
                        observacion = if (actual.observacionEditada) {
                            actual.observacion
                        } else {
                            sesion?.observacion.orEmpty()
                        }
                    )
                }
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(cargando = false, error = e.message ?: "Error al cargar la sesión.")
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

    fun seleccionarActividad(actividad: Actividad) {
        _uiState.update { it.copy(actividadId = actividad.id) }
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

    /** Valida la observación pedagógica y la envía con PATCH a la sesión actual. */
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
                val actualizada = repository.guardarObservacion(sesionId, estado.observacion)
                _uiState.update {
                    it.copy(
                        sesion = actualizada,
                        observacion = actualizada.observacion,
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

    /** POST: crea la sesión en el servidor, que genera el código MPAZ-XXXXXX. */
    fun generarCodigo(asistente: String, onExito: () -> Unit) {
        if (!validarSeleccion()) return
        val estadoActual = _uiState.value
        if (estadoActual.guardando) return
        // Si no hay actividad Disponible, la pantalla ya muestra el aviso junto al selector
        val actividad = estadoActual.actividadSeleccionada ?: return
        _uiState.update { it.copy(guardando = true, error = null) }
        viewModelScope.launch {
            try {
                val creada = repository.crearSesion(
                    modulo = estadoActual.modulo,
                    curso = estadoActual.curso,
                    estudiante = estadoActual.estudiante,
                    asistente = asistente,
                    contenido = actividad.descripcionCorta,
                    actividadId = actividad.id
                )
                _uiState.update { it.copy(guardando = false, sesion = creada) }
                onExito()
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (e: Exception) {
                // Por ejemplo, 409: "Finaliza la sesión actual antes de generar otra."
                _uiState.update { actual ->
                    actual.copy(
                        guardando = false,
                        error = e.message ?: "No se pudo generar el código de la sesión."
                    )
                }
            }
        }
    }

    /** PATCH /finalizar: cierra la sesión; el código deja de funcionar. */
    fun finalizarSesion(onExito: () -> Unit) {
        val sesionId = _uiState.value.sesion?.id ?: return
        _uiState.update { it.copy(guardando = true, error = null) }
        viewModelScope.launch {
            try {
                val finalizada = repository.finalizarSesion(sesionId)
                _uiState.update { it.copy(guardando = false, sesion = finalizada) }
                onExito()
            } catch (cancelacion: CancellationException) {
                throw cancelacion
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

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val aplicacion = checkNotNull(this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                SesionAulaViewModel(SesionAulaRepository(SesionDispositivoStore(aplicacion)))
            }
        }
    }
}
