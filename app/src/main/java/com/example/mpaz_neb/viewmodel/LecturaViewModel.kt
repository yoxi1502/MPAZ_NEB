package com.example.mpaz_neb.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mpaz_neb.model.EstadoSesion
import com.example.mpaz_neb.model.LecturaUiState
import com.example.mpaz_neb.model.ModuloActividad
import com.example.mpaz_neb.model.PreguntaLectura
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

class LecturaViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = ProgresoRepository(application)

    private val sesionRepository =
        SesionAulaRepository(application)

    private var intentoId = UUID.randomUUID().toString()

    private var contextoSesion = cargarSesionLectura()

    private fun cargarSesionLectura() = viewModelScope.async {
        val sesion = sesionRepository.sesionActual.first()

        sesion?.takeIf {
            it.modulo == ModuloActividad.LECTURA &&
                    (
                            it.estado == EstadoSesion.EN_CURSO ||
                                    it.estado == EstadoSesion.COMPLETADA
                            )
        }?.id
    }

    private val titulo = "Una tarde en el parque"

    private val texto =
        "Ana fue al parque con su hermano Tomás. " +
                "Llevaron una pelota roja. " +
                "Después de jugar, se sentaron bajo un árbol " +
                "para comer manzanas. " +
                "Antes de volver a casa, recogieron sus cosas " +
                "y dejaron el lugar limpio."

    private val preguntas = listOf(
        PreguntaLectura(
            enunciado = "¿Con quién fue Ana al parque?",
            opciones = listOf(
                "Con su amiga",
                "Con su hermano Tomás",
                "Con su mamá"
            ),
            respuestaCorrecta = "Con su hermano Tomás"
        ),
        PreguntaLectura(
            enunciado = "¿De qué color era la pelota?",
            opciones = listOf(
                "Azul",
                "Amarilla",
                "Roja"
            ),
            respuestaCorrecta = "Roja"
        ),
        PreguntaLectura(
            enunciado = "¿Qué hicieron antes de volver a casa?",
            opciones = listOf(
                "Recogieron sus cosas",
                "Compraron otra pelota",
                "Se fueron a dormir"
            ),
            respuestaCorrecta = "Recogieron sus cosas"
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

        val contextoDelIntento = contextoSesion
        val idDelIntento = intentoId

        viewModelScope.launch {
            try {
                repository.guardarLectura(
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
        contextoSesion = cargarSesionLectura()

        _uiState.value = crearEstadoInicial()
    }
}