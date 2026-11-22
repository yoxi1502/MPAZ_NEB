package com.example.mpaz_neb.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.mpaz_neb.data.local.SesionDispositivoStore
import com.example.mpaz_neb.data.remote.ErrorServidor
import com.example.mpaz_neb.model.EstadoSesion
import com.example.mpaz_neb.model.LecturaUiState
import com.example.mpaz_neb.model.ModuloActividad
import com.example.mpaz_neb.model.SesionAula
import com.example.mpaz_neb.model.PreguntaLectura
import com.example.mpaz_neb.model.TipoPreguntaLectura
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
 * Actividad de lectura del estudiante. Al terminar guarda el último resultado en el
 * dispositivo y, si hay una sesión de Lectura abierta, lo envía a ms-sesiones.
 */
class LecturaViewModel(
    private val progresoRepository: ProgresoRepository,
    private val sesionRepository: SesionAulaRepository
) : ViewModel() {

    // Id del intento: si se reintenta el envío, el servidor no duplica el resultado
    private var intentoId = UUID.randomUUID().toString()

    private val titulo = "Una tarde en el parque"

    private val texto =
        "Ana fue al parque con su hermano Tomás. " +
                "Llevaron una pelota roja. " +
                "Después de jugar, se sentaron bajo un árbol " +
                "para comer manzanas. " +
                "Antes de volver a casa, recogieron sus cosas " +
                "y dejaron el lugar limpio."

    // Una pregunta explícita, una implícita y una de vocabulario (contenido ficticio)
    private val preguntas = listOf(
        PreguntaLectura(
            enunciado = "¿Con quién fue Ana al parque?",
            tipo = TipoPreguntaLectura.EXPLICITA,
            opciones = listOf(
                "Con su amiga",
                "Con su hermano Tomás",
                "Con su mamá"
            ),
            respuestaCorrecta = "Con su hermano Tomás",
            retroalimentacion = "La historia dice: «Ana fue al parque con su hermano Tomás»."
        ),
        PreguntaLectura(
            enunciado = "¿Por qué crees que dejaron el lugar limpio?",
            tipo = TipoPreguntaLectura.IMPLICITA,
            opciones = listOf(
                "Porque cuidan el parque",
                "Porque estaba lloviendo",
                "Porque perdieron la pelota"
            ),
            respuestaCorrecta = "Porque cuidan el parque",
            retroalimentacion = "Recoger sus cosas y dejar todo limpio muestra que cuidan el parque."
        ),
        PreguntaLectura(
            enunciado = "En la historia, ¿qué significa «recogieron»?",
            tipo = TipoPreguntaLectura.VOCABULARIO,
            opciones = listOf(
                "Juntaron sus cosas",
                "Botaron sus cosas",
                "Pintaron sus cosas"
            ),
            respuestaCorrecta = "Juntaron sus cosas",
            retroalimentacion = "«Recoger» es juntar las cosas para llevarlas contigo."
        )
    )

    private fun crearEstadoInicial(): LecturaUiState {
        return LecturaUiState(
            titulo = titulo,
            texto = texto,
            preguntas = preguntas
        )
    }

    private val _uiState = MutableStateFlow(
        crearEstadoInicial()
    )

    val uiState: StateFlow<LecturaUiState> =
        _uiState.asStateFlow()

    fun seleccionarRespuesta(respuesta: String) {
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
                progresoRepository.guardarLectura(aciertos = estado.aciertos, total = total)

                // Solo se envía si la sesión abierta en el dispositivo es de Lectura
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

        _uiState.value = crearEstadoInicial().copy(
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
        modulo == ModuloActividad.LECTURA &&
                (estado == EstadoSesion.EN_CURSO || estado == EstadoSesion.COMPLETADA)

    companion object {
        const val MENSAJE_SIN_INTENTOS =
            "Ya usaste todos los intentos de esta actividad. Este resultado quedó solo en el dispositivo."

        /** Entrega al ViewModel sus repositorios (necesitan el Context para DataStore). */
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val aplicacion = checkNotNull(this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                LecturaViewModel(
                    progresoRepository = ProgresoRepository(aplicacion),
                    sesionRepository = SesionAulaRepository(SesionDispositivoStore(aplicacion))
                )
            }
        }
    }
}