package com.example.mpaz_neb.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mpaz_neb.model.EstadoSesion
import com.example.mpaz_neb.model.MatematicaUiState
import com.example.mpaz_neb.model.ModuloActividad
import com.example.mpaz_neb.model.PreguntaMatematica
import com.example.mpaz_neb.repository.ProgresoRepository
import com.example.mpaz_neb.repository.SesionAulaRepository
import java.util.UUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MatematicaViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = ProgresoRepository(application)

    private val sesionRepository =
        SesionAulaRepository(application)

    private var intentoId = UUID.randomUUID().toString()

    private var contextoSesion = cargarSesionMatematica()

    private fun cargarSesionMatematica() = viewModelScope.async {
        val sesion = sesionRepository.sesionActual.first()

        sesion?.takeIf {
            it.modulo == ModuloActividad.MATEMATICA &&
                    (
                            it.estado == EstadoSesion.EN_CURSO ||
                                    it.estado == EstadoSesion.COMPLETADA
                            )
        }?.id
    }

    private val preguntas = listOf(
        PreguntaMatematica(
            enunciado = "¿Cuánto es 3 + 2?",
            opciones = listOf(4, 5, 6),
            respuestaCorrecta = 5
        ),
        PreguntaMatematica(
            enunciado = "¿Cuánto es 7 - 3?",
            opciones = listOf(3, 4, 5),
            respuestaCorrecta = 4
        ),
        PreguntaMatematica(
            enunciado = "¿Cuánto es 2 + 6?",
            opciones = listOf(6, 7, 8),
            respuestaCorrecta = 8
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

        val contextoDelIntento = contextoSesion
        val idDelIntento = intentoId

        viewModelScope.launch {
            try {
                repository.guardarMatematica(
                    aciertos = estado.aciertos,
                    total = estado.preguntas.size,
                    sesionId = contextoDelIntento.await(),
                    intentoId = idDelIntento
                )

                _uiState.update { actual ->
                    actual.copy(
                        finalizada = true,
                        guardando = false,
                        errorGuardado = null
                    )
                }
            } catch (cancelacion: CancellationException) {
                throw cancelacion
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

        intentoId = UUID.randomUUID().toString()
        contextoSesion = cargarSesionMatematica()

        _uiState.value = MatematicaUiState(
            preguntas = preguntas
        )
    }
}