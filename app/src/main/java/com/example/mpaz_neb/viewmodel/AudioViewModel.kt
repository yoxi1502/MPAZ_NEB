package com.example.mpaz_neb.viewmodel

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.os.SystemClock
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mpaz_neb.model.AudioUiState
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AudioViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val contexto = application.applicationContext

    private val archivoAudio = File(
        contexto.filesDir,
        "ultima_lectura.m4a"
    )

    private val archivoTemporal = File(
        contexto.filesDir,
        "lectura_temporal.m4a"
    )

    private val _uiState = MutableStateFlow(
        AudioUiState(
            audioDisponible = archivoAudio.isFile &&
                    archivoAudio.length() > 0L
        )
    )

    val uiState: StateFlow<AudioUiState> = _uiState.asStateFlow()

    private var grabador: MediaRecorder? = null
    private var reproductor: MediaPlayer? = null
    private var temporizador: Job? = null
    private var inicioGrabacion = 0L

    init {
        archivoTemporal.delete()
    }

    @Suppress("DEPRECATION")
    private fun crearGrabador(): MediaRecorder {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(contexto)
        } else {
            MediaRecorder()
        }
    }

    fun iniciarGrabacion() {
        if (_uiState.value.grabando) return

        val permisoConcedido = ContextCompat.checkSelfPermission(
            contexto,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (!permisoConcedido) {
            mostrarError("Necesitas permitir el micrófono para grabar.")
            return
        }

        if (!contexto.packageManager.hasSystemFeature(
                PackageManager.FEATURE_MICROPHONE
            )
        ) {
            mostrarError("Este dispositivo no tiene un micrófono disponible.")
            return
        }

        detenerReproduccion()

        try {
            archivoTemporal.delete()

            val nuevoGrabador = crearGrabador()
            grabador = nuevoGrabador

            nuevoGrabador.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(96000)
                setAudioSamplingRate(44100)
                setOutputFile(archivoTemporal.absolutePath)

                setOnErrorListener { origen, _, _ ->
                    if (grabador === origen) {
                        descartarGrabacion(
                            "La grabación se interrumpió. Intenta nuevamente."
                        )
                    }
                }

                prepare()
                start()
            }

            inicioGrabacion = SystemClock.elapsedRealtime()

            _uiState.update {
                it.copy(
                    grabando = true,
                    reproduciendo = false,
                    segundosGrabados = 0,
                    mensaje = null,
                    error = null
                )
            }

            temporizador = viewModelScope.launch {
                while (_uiState.value.grabando) {
                    delay(250)

                    val segundos = (
                            (SystemClock.elapsedRealtime() - inicioGrabacion) / 1000
                            ).toInt()

                    _uiState.update {
                        it.copy(segundosGrabados = segundos.coerceAtMost(120))
                    }

                    if (segundos >= 120) {
                        detenerGrabacion()
                        break
                    }
                }
            }
        } catch (e: Exception) {
            descartarGrabacion(
                "No se pudo iniciar la grabación. Revisa el permiso del micrófono."
            )
        }
    }

    fun detenerGrabacion() {
        val grabadorActual = grabador ?: return
        grabador = null

        temporizador?.cancel()
        temporizador = null

        val segundos = (
                (SystemClock.elapsedRealtime() - inicioGrabacion) / 1000
                ).toInt().coerceIn(0, 120)

        val grabacionValida = runCatching {
            grabadorActual.stop()
        }.isSuccess

        runCatching {
            grabadorActual.release()
        }

        if (!grabacionValida || archivoTemporal.length() == 0L) {
            archivoTemporal.delete()

            _uiState.update {
                it.copy(
                    grabando = false,
                    segundosGrabados = 0,
                    audioDisponible = archivoAudio.isFile &&
                            archivoAudio.length() > 0L,
                    mensaje = null,
                    error = "No se pudo guardar. Graba unos segundos e intenta otra vez."
                )
            }

            return
        }

        try {
            Files.move(
                archivoTemporal.toPath(),
                archivoAudio.toPath(),
                StandardCopyOption.REPLACE_EXISTING
            )

            _uiState.update {
                it.copy(
                    grabando = false,
                    audioDisponible = true,
                    segundosGrabados = segundos,
                    mensaje = "¡Lectura guardada! Ya puedes escucharla.",
                    error = null
                )
            }
        } catch (e: Exception) {
            archivoTemporal.delete()

            _uiState.update {
                it.copy(
                    grabando = false,
                    audioDisponible = archivoAudio.isFile &&
                            archivoAudio.length() > 0L,
                    mensaje = null,
                    error = "No se pudo guardar el audio. Intenta nuevamente."
                )
            }
        }
    }

    fun reproducirAudio() {
        if (_uiState.value.grabando ||
            _uiState.value.reproduciendo
        ) {
            return
        }

        if (!archivoAudio.isFile || archivoAudio.length() == 0L) {
            _uiState.update {
                it.copy(
                    audioDisponible = false,
                    mensaje = null,
                    error = "Primero graba tu lectura."
                )
            }
            return
        }

        try {
            val nuevoReproductor = MediaPlayer()
            reproductor = nuevoReproductor

            _uiState.update {
                it.copy(
                    reproduciendo = true,
                    mensaje = null,
                    error = null
                )
            }

            nuevoReproductor.apply {
                setDataSource(archivoAudio.absolutePath)

                setOnPreparedListener { preparado ->
                    if (reproductor === preparado) {
                        preparado.start()
                    }
                }

                setOnCompletionListener { terminado ->
                    if (reproductor === terminado) {
                        detenerReproduccion()

                        _uiState.update {
                            it.copy(mensaje = "¡Terminaste de escuchar tu lectura!")
                        }
                    }
                }

                setOnErrorListener { origen, _, _ ->
                    if (reproductor === origen) {
                        detenerReproduccion()
                        mostrarError("No se pudo reproducir el audio.")
                    }
                    true
                }

                prepareAsync()
            }
        } catch (e: Exception) {
            detenerReproduccion()
            mostrarError("No se pudo abrir la grabación.")
        }
    }

    fun detenerReproduccion() {
        val reproductorActual = reproductor
        reproductor = null

        runCatching {
            reproductorActual?.release()
        }

        _uiState.update {
            it.copy(reproduciendo = false)
        }
    }

    fun detenerAudio() {
        detenerGrabacion()
        detenerReproduccion()
    }

    fun mostrarError(mensaje: String) {
        _uiState.update {
            it.copy(
                mensaje = null,
                error = mensaje
            )
        }
    }

    private fun descartarGrabacion(mensaje: String) {
        temporizador?.cancel()
        temporizador = null

        val grabadorActual = grabador
        grabador = null

        runCatching {
            grabadorActual?.release()
        }

        archivoTemporal.delete()

        _uiState.update {
            it.copy(
                grabando = false,
                segundosGrabados = 0,
                audioDisponible = archivoAudio.isFile &&
                        archivoAudio.length() > 0L,
                mensaje = null,
                error = mensaje
            )
        }
    }

    override fun onCleared() {
        detenerAudio()
        super.onCleared()
    }
}