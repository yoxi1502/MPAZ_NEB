package com.example.mpaz_neb.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel as obtenerViewModel
import androidx.navigation.NavController
import com.example.mpaz_neb.viewmodel.AudioViewModel
import com.example.mpaz_neb.viewmodel.LecturaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioScreen(
    navController: NavController,
    audioViewModel: AudioViewModel = obtenerViewModel(),
    lecturaViewModel: LecturaViewModel = obtenerViewModel()
) {
    val estado by audioViewModel.uiState.collectAsState()
    val lectura by lecturaViewModel.uiState.collectAsState()

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var permisoConcedido by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val solicitarPermiso = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { concedido ->
        permisoConcedido = concedido

        if (concedido) {
            audioViewModel.iniciarGrabacion()
        } else {
            audioViewModel.mostrarError(
                "No se autorizó el micrófono. Puedes leer el cuento sin grabarlo."
            )
        }
    }

    // Detiene el audio cuando la app pasa a segundo plano.
    DisposableEffect(lifecycleOwner, audioViewModel) {
        val observador = LifecycleEventObserver { _, evento ->
            when (evento) {
                Lifecycle.Event.ON_STOP -> {
                    audioViewModel.detenerAudio()
                }

                Lifecycle.Event.ON_RESUME -> {
                    permisoConcedido = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.RECORD_AUDIO
                    ) == PackageManager.PERMISSION_GRANTED
                }

                else -> Unit
            }
        }

        lifecycleOwner.lifecycle.addObserver(observador)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observador)
            audioViewModel.detenerAudio()
        }
    }

    val minutos = estado.segundosGrabados / 60
    val segundos = estado.segundosGrabados % 60
    val tiempo = "%02d:%02d".format(minutos, segundos)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lectura en voz alta") },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            audioViewModel.detenerAudio()
                            navController.popBackStack()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }
    ) { paddingInterior ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingInterior)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "¡Escucha cómo lees!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Presiona Grabar, lee el cuento en voz alta " +
                        "y después escucha tu lectura.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = lectura.titulo,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = lectura.texto,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            Text(
                text = if (estado.grabando) {
                    "Grabando: $tiempo"
                } else {
                    "Tiempo de grabación: $tiempo"
                },
                style = MaterialTheme.typography.titleMedium,
                color = if (estado.grabando) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )

            Text(
                text = "Puedes grabar hasta 2 minutos.",
                style = MaterialTheme.typography.bodyMedium
            )

            Button(
                enabled = !estado.reproduciendo,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp),
                onClick = {
                    if (estado.grabando) {
                        audioViewModel.detenerGrabacion()
                    } else {
                        val tienePermiso = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED

                        permisoConcedido = tienePermiso

                        if (tienePermiso) {
                            audioViewModel.iniciarGrabacion()
                        } else {
                            solicitarPermiso.launch(
                                Manifest.permission.RECORD_AUDIO
                            )
                        }
                    }
                }
            ) {
                Icon(
                    imageVector = if (estado.grabando) {
                        Icons.Default.Stop
                    } else {
                        Icons.Default.Mic
                    },
                    contentDescription = null
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = when {
                        estado.grabando -> "Detener y guardar"
                        estado.audioDisponible -> "Volver a grabar"
                        else -> "Grabar mi lectura"
                    }
                )
            }

            OutlinedButton(
                enabled = estado.audioDisponible && !estado.grabando,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp),
                onClick = {
                    if (estado.reproduciendo) {
                        audioViewModel.detenerReproduccion()
                    } else {
                        audioViewModel.reproducirAudio()
                    }
                }
            ) {
                Icon(
                    imageVector = if (estado.reproduciendo) {
                        Icons.Default.Stop
                    } else {
                        Icons.Default.PlayArrow
                    },
                    contentDescription = null
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = if (estado.reproduciendo) {
                        "Detener audio"
                    } else {
                        "Escuchar mi lectura"
                    }
                )
            }

            if (!permisoConcedido) {
                TextButton(
                    onClick = {
                        val intent = Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.parse("package:${context.packageName}")
                        )
                        context.startActivity(intent)
                    }
                ) {
                    Text("Abrir ajustes del micrófono")
                }
            }

            estado.mensaje?.let { mensaje ->
                Text(
                    text = mensaje,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
            }

            estado.error?.let { mensaje ->
                Text(
                    text = mensaje,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            }

            Text(
                text = "La app conserva tu última grabación en este dispositivo. " +
                        "Una nueva grabación guardada reemplaza la anterior.",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        }
    }
}

