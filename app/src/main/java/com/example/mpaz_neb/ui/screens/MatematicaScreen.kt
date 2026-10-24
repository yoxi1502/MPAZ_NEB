package com.example.mpaz_neb.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel as obtenerViewModel
import androidx.navigation.NavHostController
import com.example.mpaz_neb.model.MatematicaUiState
import com.example.mpaz_neb.navigation.NavigationEvent
import com.example.mpaz_neb.viewmodel.MatematicaViewModel

private val MateFondo = Color(0xFFFFFAF2)
private val MateMorado = Color(0xFF6B45A5)
private val MateTexto = Color(0xFF24374B)
private val MateVerde = Color(0xFF176B60)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatematicaScreen(
    navController: NavHostController,
    onNavigate: (NavigationEvent) -> Unit,
    matematicaViewModel: MatematicaViewModel = obtenerViewModel()
) {
    val estado by matematicaViewModel.uiState.collectAsState()

    var actividadIniciada by rememberSaveable {
        mutableStateOf(false)
    }

    val scroll = rememberScrollState()

    LaunchedEffect(
        actividadIniciada,
        estado.indiceActual,
        estado.finalizada
    ) {
        scroll.scrollTo(0)
    }

    BackHandler(
        enabled = actividadIniciada || estado.guardando
    ) {
        if (!estado.guardando) {
            actividadIniciada = false
        }
    }

    Scaffold(
        containerColor = MateFondo,
        contentColor = MateTexto,
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Aventura matemática",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MateFondo,
                    titleContentColor = MateTexto,
                    navigationIconContentColor = MateMorado
                ),
                navigationIcon = {
                    IconButton(
                        enabled = !estado.guardando,
                        onClick = {
                            if (actividadIniciada) {
                                actividadIniciada = false
                            } else {
                                onNavigate(
                                    NavigationEvent.PopBackStack
                                )
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
        },
        bottomBar = {
            if (
                actividadIniciada &&
                !estado.finalizada
            ) {
                Surface(
                    color = MateFondo,
                    contentColor = MateTexto
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(
                                horizontal = 24.dp,
                                vertical = 12.dp
                            ),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Button(
                            onClick = {
                                if (estado.respuestaComprobada) {
                                    matematicaViewModel.siguientePregunta()
                                } else {
                                    matematicaViewModel.comprobarRespuesta()
                                }
                            },
                            enabled = estado.respuestaSeleccionada != null &&
                                    !estado.guardando,
                            modifier = Modifier
                                .widthIn(max = 720.dp)
                                .fillMaxWidth()
                                .heightIn(min = 60.dp),
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MateMorado,
                                contentColor = Color.White
                            )
                        ) {
                            Text(
                                text = when {
                                    estado.guardando ->
                                        "Guardando…"

                                    estado.errorGuardado != null ->
                                        "Reintentar guardado"

                                    !estado.respuestaComprobada ->
                                        "Comprobar respuesta"

                                    estado.indiceActual ==
                                            estado.preguntas.lastIndex ->
                                        "Ver mi resultado"

                                    else ->
                                        "Siguiente pregunta"
                                },
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .widthIn(max = 768.dp)
                    .fillMaxWidth()
                    .verticalScroll(scroll)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                when {
                    !actividadIniciada -> {
                        InicioMatematica(
                            onComenzar = {
                                matematicaViewModel.reiniciarActividad()
                                actividadIniciada = true
                            }
                        )
                    }

                    estado.finalizada -> {
                        ResultadoMatematica(
                            estado = estado,
                            onRepetir = matematicaViewModel::reiniciarActividad,
                            onVolver = {
                                actividadIniciada = false
                            }
                        )
                    }

                    else -> {
                        PreguntaMatematicaPanel(
                            estado = estado,
                            onSeleccionar = matematicaViewModel::seleccionarRespuesta
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InicioMatematica(
    onComenzar: () -> Unit
) {
    Surface(
        shape = CircleShape,
        color = Color(0xFFEEE5FA)
    ) {
        Icon(
            imageVector = Icons.Default.Calculate,
            contentDescription = null,
            tint = MateMorado,
            modifier = Modifier
                .padding(28.dp)
                .size(72.dp)
        )
    }

    Text(
        text = "¡Juguemos con números!",
        fontSize = 30.sp,
        lineHeight = 36.sp,
        fontWeight = FontWeight.ExtraBold,
        textAlign = TextAlign.Center
    )

    Text(
        text = "Piensa, elige y descubre cuánto puedes aprender.",
        fontSize = 18.sp,
        color = Color(0xFF526273),
        textAlign = TextAlign.Center
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        contentColor = MateTexto,
        border = BorderStroke(
            width = 1.dp,
            color = Color(0xFFDCE4E9)
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Text(
                text = "MATEMÁTICA · NIVEL 1",
                color = MateMorado,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Sumas y restas",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "1. Resuelve la operación con calma.\n" +
                        "2. Elige una respuesta.\n" +
                        "3. Pulsa Comprobar respuesta.",
                fontSize = 18.sp,
                lineHeight = 30.sp
            )

            Text(
                text = "Te esperan 3 preguntas. ¡Tú puedes!",
                fontSize = 17.sp
            )

            Button(
                onClick = onComenzar,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 60.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MateMorado,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "¡Comenzar matemática!",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun PreguntaMatematicaPanel(
    estado: MatematicaUiState,
    onSeleccionar: (Int) -> Unit
) {
    val pregunta = estado.preguntas
        .getOrNull(estado.indiceActual)
        ?: return

    val avance by animateFloatAsState(
        targetValue = (
                estado.indiceActual +
                        if (estado.respuestaComprobada) 1 else 0
                ).toFloat() / estado.preguntas.size,
        label = "Avance de matemática"
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = "Pregunta ${estado.indiceActual + 1} de ${estado.preguntas.size}",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        )

        LinearProgressIndicator(
            progress = { avance },
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(50)),
            color = MateMorado,
            trackColor = Color(0xFFE8E0F2)
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFFEEE5FA),
            contentColor = MateTexto
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Calculate,
                    contentDescription = null,
                    tint = MateMorado
                )

                Text(
                    text = pregunta.enunciado,
                    fontSize = 32.sp,
                    lineHeight = 40.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Puedes contar con tus dedos si lo necesitas.",
                    textAlign = TextAlign.Center
                )
            }
        }

        Text(
            text = "Toca la respuesta que elegiste:",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Column(
            modifier = Modifier.selectableGroup(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            pregunta.opciones.forEach { opcion ->
                val seleccionada =
                    estado.respuestaSeleccionada == opcion

                val correcta =
                    estado.respuestaComprobada &&
                            opcion == pregunta.respuestaCorrecta

                val incorrecta =
                    estado.respuestaComprobada &&
                            seleccionada &&
                            !correcta

                val fondo by animateColorAsState(
                    targetValue = when {
                        correcta ->
                            Color(0xFFE0EFE7)

                        incorrecta ->
                            Color(0xFFF9E4E2)

                        seleccionada ->
                            Color(0xFFEEE5FA)

                        else ->
                            Color.White
                    },
                    label = "Color de respuesta matemática"
                )

                val borde = when {
                    correcta ->
                        MateVerde

                    incorrecta ->
                        Color(0xFF963C46)

                    seleccionada ->
                        MateMorado

                    else ->
                        Color(0xFFDCE4E9)
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = seleccionada,
                            enabled = !estado.respuestaComprobada &&
                                    !estado.guardando,
                            role = Role.RadioButton,
                            onClick = {
                                onSeleccionar(opcion)
                            }
                        ),
                    shape = RoundedCornerShape(18.dp),
                    color = fondo,
                    contentColor = MateTexto,
                    border = BorderStroke(
                        width = if (seleccionada || correcta) {
                            2.dp
                        } else {
                            1.dp
                        },
                        color = borde
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .heightIn(min = 68.dp)
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        RadioButton(
                            selected = seleccionada,
                            onClick = null,
                            colors = RadioButtonDefaults.colors(
                                selectedColor = borde,
                                unselectedColor = MateMorado
                            )
                        )

                        Text(
                            text = opcion.toString(),
                            modifier = Modifier.weight(1f),
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold
                        )

                        if (correcta) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Respuesta correcta",
                                tint = MateVerde
                            )
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = estado.respuestaComprobada
        ) {
            val correcta =
                estado.respuestaSeleccionada ==
                        pregunta.respuestaCorrecta

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = if (correcta) {
                    Color(0xFFE0EFE7)
                } else {
                    Color(0xFFFFE8CB)
                },
                contentColor = MateTexto
            ) {
                Text(
                    text = if (correcta) {
                        "¡Muy bien! Encontraste la respuesta."
                    } else {
                        "Sigamos aprendiendo. La respuesta es ${pregunta.respuestaCorrecta}."
                    },
                    modifier = Modifier.padding(18.dp),
                    fontSize = 18.sp,
                    lineHeight = 26.sp
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
                color = Color(0xFF963C46),
                fontSize = 16.sp
            )
        }
    }
}

@Composable
private fun ResultadoMatematica(
    estado: MatematicaUiState,
    onRepetir: () -> Unit,
    onVolver: () -> Unit
) {
    Icon(
        imageVector = Icons.Default.Star,
        contentDescription = null,
        tint = Color(0xFFB97412),
        modifier = Modifier.size(80.dp)
    )

    Text(
        text = "¡Terminaste tu desafío!",
        fontSize = 28.sp,
        lineHeight = 36.sp,
        fontWeight = FontWeight.ExtraBold,
        textAlign = TextAlign.Center
    )

    Text(
        text = "Cada práctica te ayuda a aprender.",
        fontSize = 18.sp,
        textAlign = TextAlign.Center
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFFE0EFE7),
        contentColor = MateTexto
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "${estado.aciertos} de ${estado.preguntas.size}",
                fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MateVerde
            )

            Text(
                text = "Respuestas correctas",
                fontSize = 18.sp
            )

            Text(
                text = "Tu resultado quedó guardado en este dispositivo.",
                textAlign = TextAlign.Center
            )
        }
    }

    Button(
        onClick = onRepetir,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 58.dp),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MateMorado,
            contentColor = Color.White
        )
    ) {
        Text(
            text = "Practicar otra vez",
            fontSize = 18.sp
        )
    }

    TextButton(
        onClick = onVolver
    ) {
        Text(
            text = "Volver a las actividades",
            color = MateMorado,
            fontSize = 17.sp
        )
    }
}