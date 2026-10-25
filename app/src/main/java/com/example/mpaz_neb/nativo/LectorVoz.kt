package com.example.mpaz_neb.nativo

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class EstadoLectorVoz {
    INICIANDO,
    LISTO,
    SIN_IDIOMA,
    NO_DISPONIBLE
}

/**
 * Recurso nativo 1: motor de texto a voz (TextToSpeech) del dispositivo.
 * Lee en voz alta textos y preguntas para apoyar a estudiantes que aún
 * no leen con fluidez. No requiere permisos peligrosos ni usa micrófono.
 */
class LectorVoz(context: Context) {

    private val _estado = MutableStateFlow(EstadoLectorVoz.INICIANDO)
    val estado: StateFlow<EstadoLectorVoz> = _estado.asStateFlow()

    private val _hablando = MutableStateFlow(false)
    val hablando: StateFlow<Boolean> = _hablando.asStateFlow()

    private var motor: TextToSpeech? = null

    init {
        motor = TextToSpeech(
            context.applicationContext,
            TextToSpeech.OnInitListener { estadoInicio -> alIniciar(estadoInicio) }
        )
    }

    private fun alIniciar(estadoInicio: Int) {
        val tts = motor
        if (estadoInicio != TextToSpeech.SUCCESS || tts == null) {
            _estado.value = EstadoLectorVoz.NO_DISPONIBLE
            return
        }

        // Se intenta español de Chile y, si no está instalado, español general.
        var resultado = tts.setLanguage(Locale.forLanguageTag("es-CL"))
        if (resultado < 0) {
            resultado = tts.setLanguage(Locale.forLanguageTag("es"))
        }
        if (resultado < 0) {
            _estado.value = EstadoLectorVoz.SIN_IDIOMA
            return
        }

        tts.setSpeechRate(0.85f) // un poco más lento para niños y niñas
        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                _hablando.value = true
            }

            override fun onDone(utteranceId: String?) {
                _hablando.value = false
            }

            @Deprecated("Requerido por versiones anteriores de Android")
            override fun onError(utteranceId: String?) {
                _hablando.value = false
            }
        })
        _estado.value = EstadoLectorVoz.LISTO
    }

    fun leer(texto: String) {
        if (_estado.value != EstadoLectorVoz.LISTO || texto.isBlank()) return
        motor?.speak(texto, TextToSpeech.QUEUE_FLUSH, null, "mpaz-${System.nanoTime()}")
    }

    fun detener() {
        motor?.stop()
        _hablando.value = false
    }

    /** Libera el motor al salir de la pantalla para no consumir recursos. */
    fun liberar() {
        motor?.stop()
        motor?.shutdown()
        motor = null
        _hablando.value = false
    }
}
