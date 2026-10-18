package com.example.mpaz_neb.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel as obtenerViewModel
import androidx.navigation.NavController
import com.example.mpaz_neb.ui.theme.components.NivelCard
import com.example.mpaz_neb.viewmodel.LecturaViewModel
import com.example.mpaz_neb.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LecturaScreen(
    navController: NavController,
    viewModel: MainViewModel,
    lecturaViewModel: LecturaViewModel = obtenerViewModel()
) {
    val estado by lecturaViewModel.uiState.collectAsState()

    var actividadIniciada by rememberSaveable {
        mutableStateOf(false)
    }

    val scrollState = rememberScrollState()

    LaunchedEffect(
        actividadIniciada,
        estado.indiceActual,
        estado.finalizada
    ) {
        scrollState.scrollTo(0)
    }

    BackHandler(
        enabled = actividadIniciada || estado.guardando
    ) {
        if (!estado.guardando) {
            actividadIniciada = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (actividadIniciada) {
                            "Practiquemos Lectura"
                        } else {
                            "Módulo de Lectura"
                        }
                    )
                },
                navigationIcon = {
                    IconButton(
                        enabled = !estado.guardando,
                        onClick = {
                            if (actividadIniciada) {
                                actividadIniciada = false
                            } else {
                                navController.popBackStack()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (!actividadIniciada) {
                Text(
                    text = "¡Vamos a leer!",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Selecciona la actividad. Lee el texto y responde tres preguntas.",
                    style = MaterialTheme.typography.bodyLarge
                )

                NivelCard(
                    titulo = "Nivel 1: Comprensión lectora",
                    subtitulo = estado.titulo,
                    cardColor = MaterialTheme.colorScheme.secondaryContainer,
                    textColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    onClick = {
                        lecturaViewModel.reiniciarActividad()
                        actividadIniciada = true
                    }
                )
            } else if (estado.finalizada) {
                Text(
                    text = "¡Actividad terminada!",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Tu resultado",
                            style = MaterialTheme.typography.titleLarge
                        )

                        Text(
                            text = "${estado.aciertos} de ${estado.preguntas.size} respuestas correctas",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = if (
                                estado.aciertos == estado.preguntas.size
                            ) {
                                "¡Excelente comprensión del texto!"
                            } else {
                                "Puedes volver a leer el texto e intentarlo nuevamente."
                            }
                        )

                        Text(
                            text = "Resultado guardado en este dispositivo.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Button(
                    onClick = lecturaViewModel::reiniciarActividad,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Repetir actividad")
                }

                OutlinedButton(
                    onClick = { actividadIniciada = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Volver a las actividades")
                }
            } else {
                val pregunta = estado.preguntas.getOrNull(
                    estado.indiceActual
                )

                if (pregunta != null) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = estado.titulo,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = estado.texto,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }

                    Text(
                        text = "Pregunta ${estado.indiceActual + 1} de ${estado.preguntas.size}",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = "Aciertos: ${estado.aciertos}",
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Text(
                        text = pregunta.enunciado,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    pregunta.opciones.forEach { opcion ->
                        OutlinedButton(
                            onClick = {
                                lecturaViewModel.seleccionarRespuesta(opcion)
                            },
                            enabled = estado.respuestaSeleccionada == null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 56.dp)
                        ) {
                            Text(
                                text = opcion,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }

                    AnimatedVisibility(
                        visible = estado.respuestaSeleccionada != null
                    ) {
                        val esCorrecta =
                            estado.respuestaSeleccionada ==
                                    pregunta.respuestaCorrecta

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (esCorrecta) {
                                    MaterialTheme.colorScheme.secondaryContainer
                                } else {
                                    MaterialTheme.colorScheme.errorContainer
                                },
                                contentColor = if (esCorrecta) {
                                    MaterialTheme.colorScheme.onSecondaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onErrorContainer
                                }
                            )
                        ) {
                            Text(
                                text = if (esCorrecta) {
                                    "¡Correcto! Muy bien."
                                } else {
                                    "La respuesta correcta es: ${pregunta.respuestaCorrecta}."
                                },
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }

                    if (estado.guardando) {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    estado.errorGuardado?.let { mensaje ->
                        Text(
                            text = mensaje,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    Button(
                        onClick = lecturaViewModel::siguientePregunta,
                        enabled = estado.respuestaSeleccionada != null &&
                                !estado.guardando,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = when {
                                estado.guardando -> "Guardando…"
                                estado.errorGuardado != null ->
                                    "Reintentar guardado"
                                estado.indiceActual ==
                                        estado.preguntas.lastIndex ->
                                    "Ver resultado"
                                else -> "Siguiente pregunta"
                            }
                        )
                    }
                }
            }
        }
    }
}