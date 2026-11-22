package com.example.mpaz_neb.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.mpaz_neb.data.local.SesionDispositivoStore
import com.example.mpaz_neb.data.remote.ErrorServidor
import com.example.mpaz_neb.model.EstadoSesion
import com.example.mpaz_neb.model.MatematicaUiState
import com.example.mpaz_neb.model.ModuloActividad
import com.example.mpaz_neb.model.SesionAula
import com.example.mpaz_neb.model.PreguntaMatematica
import com.example.mpaz_neb.repository.ProgresoRepository
import com.example.mpaz_neb.repository.SesionAulaRepository
import java.util.UUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Desafío de matemática del estudiante. Al terminar guarda el último resultado en el
 * dispositivo y, si hay una sesión de Matemática abierta, lo envía a ms-sesiones.
 */
class MatematicaViewModel(
    private val progresoRepository: ProgresoRepository,
    private val sesionRepository: SesionAulaRepository
) : ViewModel() {

    // Id del intento: si se reintenta el envío, el servidor no duplica el resultado
    private var intentoId = UUID.randomUUID().toString()

    private val preguntas = listOf(
        PreguntaMatematica(
            enunciado = "¿Cuánto es 3 + 2?",
            opciones = listOf(4, 5, 6),
            respuestaCorrecta = 5,
            procedimiento = "Tienes 3 y agregas 2 más. Cuenta: 4, 5. ¡Son 5!"
        ),
        PreguntaMatematica(
            enunciado = "¿Cuánto es 7 - 3?",
            opciones = listOf(3, 4, 5),
            respuestaCorrecta = 4,
            procedimiento = "Tienes 7 y quitas 3. Cuenta hacia atrás: 6, 5, 4. Quedan 4."
        ),
        PreguntaMatematica(
            enunciado = "¿Cuánto es 2 + 6?",
            opciones = listOf(6, 7, 8),
            respuestaCorrecta = 8,
            procedimiento = "Parte desde el número mayor, 6, y suma 2: 7, 8. ¡Son 8!"
        )
    )

    private val _uiState = MutableStateFlow(
        MatematicaUiState(preguntas = preguntas)
    )

    val uiState: StateFlow<MatematicaUiState> =
        _uiState.asStateFlow()

    fun seleccionarRespuesta(respuesta: Int) {
        _uiState.update { estado ->
            if (
                estado.finalizada ||
                estado.guardando ||
                estado.respuestaComprobada
            ) {
                return@update estado
            }

            val pregunta = estado.preguntas
                .getOrNull(estado.indiceActual)
                ?: return@update estado

            if (respuesta !in pregunta.opciones) {
                return@update estado
            }

            estado.copy(
                respuestaSeleccionada = respuesta
            )
        }
    }

    fun comprobarRespuesta() {
        _uiState.update { estado ->
            if (
                estado.finalizada ||
                estado.guardando ||
                estado.respuestaComprobada ||
                estado.respuestaSeleccionada == null
            ) {
                return@update estado
            }

            val pregunta = estado.preguntas
                .getOrNull(estado.indiceActual)
                ?: return@update estado

            val esCorrecta =
                estado.respuestaSeleccionada ==
                        pregunta.respuestaCorrecta

            estado.copy(
                respuestaComprobada = true,
                aciertos = estado.aciertos +
                        if (esCorrecta) 1 else 0
            )
        }
    }

    fun siguientePregunta() {
        val estado = _uiState.value

        if (
            estado.finalizada ||
            estado.guardando ||
            !estado.respuestaComprobada ||
            estado.respuestaSeleccionada == null
        ) {
            return
        }

        if (estado.indiceActual < estado.preguntas.lastIndex) {
            _uiState.update { actual ->
                actual.copy(
                    indiceActual = actual.indiceActual + 1,
                    respuestaSeleccionada = null,
                    respuestaComprobada = false,
                    errorGuardado = null
                )
            }
            return
        }

        _uiState.update { actual ->
            actual.copy(
                guardando = true,
                errorGuardado = null
            )
        }

        val idDelIntento = intentoId

        viewModelScope.launch {
            try {
                val total = estado.preguntas.size
                progresoRepository.guardarMatematica(aciertos = estado.aciertos, total = total)

                // Solo se envía si la sesión abierta en el dispositivo es de Matemática
                val sesion = sesionRepository.obtenerSesionActual()?.takeIf { it.esDeEsteModulo() }
                // Si ya se usaron todos los intentos no se envía (salvo que sea un reenvío de este mismo intento)
                val limiteAlcanzado = sesion != null && sesion.sinIntentos &&
                        sesion.resultados.none { it.id == idDelIntento }
                val actualizada = if (sesion != null && !limiteAlcanzado) {
                    sesionRepository.registrarResultado(sesion.id, idDelIntento, estado.aciertos, total)
                } else {
                    null
                }

                _uiState.update { actual ->
                    actual.copy(
                        finalizada = true,
                        guardando = false,
                        errorGuardado = null,
                        enviadoAlServidor = actualizada != null,
                        intentosRestantes = (actualizada ?: sesion)?.intentosRestantes,
                        sinIntentos = (actualizada ?: sesion)?.sinIntentos == true,
                        avisoServidor = if (limiteAlcanzado) MENSAJE_SIN_INTENTOS else null
                    )
                }
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (error: ErrorServidor) {
                if (error.codigoHttp == 409) {
                    // El servidor rechazó el resultado (límite alcanzado o sesión cerrada): queda solo en el dispositivo
                    _uiState.update { actual ->
                        actual.copy(
                            finalizada = true,
                            guardando = false,
                            errorGuardado = null,
                            enviadoAlServidor = false,
                            sinIntentos = true,
                            intentosRestantes = 0,
                            avisoServidor = error.message
                        )
                    }
                } else {
                    _uiState.update { actual ->
                        actual.copy(
                            guardando = false,
                            errorGuardado = "No se pudo guardar el resultado. Intenta nuevamente."
                        )
                    }
                }
            } catch (error: Exception) {
                _uiState.update { actual ->
                    actual.copy(
                        guardando = false,
                        errorGuardado = "No se pudo guardar el resultado. Intenta nuevamente."
                    )
                }
            }
        }
    }

    fun reiniciarActividad() {
        if (_uiState.value.guardando) {
            return
        }

        val anterior = _uiState.value
        // Con el límite de intentos alcanzado no se puede practicar otra vez en esta sesión
        if (anterior.sinIntentos) {
            return
        }

        intentoId = UUID.randomUUID().toString()

        _uiState.value = MatematicaUiState(
            preguntas = preguntas
        ).copy(
            intentosRestantes = anterior.intentosRestantes
        )
    }

    /** Lee la sesión abierta para saber cuántos intentos quedan antes de comenzar. */
    fun cargarIntentos() {
        viewModelScope.launch {
            val sesion = try {
                sesionRepository.obtenerSesionActual()?.takeIf { it.esDeEsteModulo() }
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (error: Exception) {
                // Sin conexión no se bloquea la práctica; el servidor valida al enviar
                null
            }
            _uiState.update { actual ->
                actual.copy(
                    intentosRestantes = sesion?.intentosRestantes,
                    sinIntentos = sesion?.sinIntentos == true
                )
            }
        }
    }

    private fun SesionAula.esDeEsteModulo(): Boolean =
        modulo == ModuloActividad.MATEMATICA &&
                (estado == EstadoSesion.EN_CURSO || estado == EstadoSesion.COMPLETADA)

    companion object {
        const val MENSAJE_SIN_INTENTOS =
            "Ya usaste todos los intentos de esta actividad. Este resultado quedó solo en el dispositivo."

        /** Entrega al ViewModel sus repositorios (necesitan el Context para DataStore). */
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val aplicacion = checkNotNull(this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                MatematicaViewModel(
                    progresoRepository = ProgresoRepository(aplicacion),
                    sesionRepository = SesionAulaRepository(SesionDispositivoStore(aplicacion))
                )
            }
        }
    }
}