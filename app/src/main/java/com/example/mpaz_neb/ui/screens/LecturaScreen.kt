package com.example.mpaz_neb.ui.screens

import com.example.mpaz_neb.ui.theme.*
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
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.CheckCircle
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
import androidx.navigation.NavController
import com.example.mpaz_neb.model.LecturaUiState
import com.example.mpaz_neb.viewmodel.LecturaViewModel
import com.example.mpaz_neb.viewmodel.PreferenciasViewModel
import com.example.mpaz_neb.nativo.LectorVoz
import com.example.mpaz_neb.ui.components.BotonEscuchar
import com.example.mpaz_neb.ui.components.ContadorAnimado
import com.example.mpaz_neb.ui.components.EstrellaLogro
import com.example.mpaz_neb.ui.components.rememberLectorVoz
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LecturaScreen(
    navController: NavController,
    lecturaViewModel: LecturaViewModel = obtenerViewModel(),
    preferenciasViewModel: PreferenciasViewModel = obtenerViewModel()
) {
    val estado by lecturaViewModel.uiState.collectAsState()
    val preferencias by preferenciasViewModel.uiState.collectAsState()

    // Recurso nativo: texto a voz
    val lector = rememberLectorVoz()

    var confirmarEnvio by rememberSaveable { mutableStateOf(false) }

    // Al cambiar de pregunta se detiene la lectura en voz alta
    LaunchedEffect(estado.indiceActual, estado.finalizada) {
        lector.detener()
    }

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
        containerColor = FondoCalido,
        contentColor = TextoPrincipal,
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Mi aventura de lectura",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = FondoCalido,
                    titleContentColor = TextoPrincipal,
                    navigationIconContentColor = AzulPrimario
                ),
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
    ) { padding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            val pantallaAncha = maxWidth >= 760.dp

            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .widthIn(max = 1100.dp)
                    .fillMaxWidth()
                    .verticalScroll(scroll)
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                when {
                    !actividadIniciada -> {
                        InicioActividadLectura(
                            titulo = estado.titulo,
                            onComenzar = {
                                lecturaViewModel.reiniciarActividad()
                                actividadIniciada = true
                            }
                        )
                    }

                    estado.finalizada -> {
                        ResultadoLectura(
                            estado = estado,
                            onRepetir = lecturaViewModel::reiniciarActividad,
                            onVolver = {
                                actividadIniciada = false
                            }
                        )
                    }

                    else -> {
                        val avance by animateFloatAsState(
                            targetValue = if (estado.preguntas.isEmpty()) {
                                0f
                            } else {
                                (
                                        estado.indiceActual +
                                                if (estado.respuestaComprobada) 1 else 0
                                        ).toFloat() / estado.preguntas.size
                            },
                            label = "Avance de lectura"
                        )

                        Text(
                            text = "Pregunta ${estado.indiceActual + 1} " +
                                    "de ${estado.preguntas.size}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )

                        LinearProgressIndicator(
                            progress = { avance },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(50)),
                            color = AzulPrimario,
                            trackColor = PistaProgreso
                        )

                        val lectorVisible = if (preferencias.lecturaEnVoz) lector else null
                        val alPresionar: () -> Unit = {
                            if (
                                estado.respuestaComprobada &&
                                estado.indiceActual == estado.preguntas.lastIndex
                            ) {
                                confirmarEnvio = true
                            } else if (estado.respuestaComprobada) {
                                lecturaViewModel.siguientePregunta()
                            } else {
                                lecturaViewModel.comprobarRespuesta()
                            }
                        }

                        if (pantallaAncha) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(24.dp)
                            ) {
                                TextoLectura(
                                    estado = estado,
                                    lector = lectorVisible,
                                    modifier = Modifier.weight(1f)
                                )

                                PreguntaLecturaPanel(
                                    estado = estado,
                                    lecturaViewModel = lecturaViewModel,
                                    lector = lectorVisible,
                                    onBotonPrincipal = alPresionar,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        } else {
                            TextoLectura(
                                estado = estado,
                                lector = lectorVisible
                            )

                            PreguntaLecturaPanel(
                                estado = estado,
                                lecturaViewModel = lecturaViewModel,
                                lector = lectorVisible,
                                onBotonPrincipal = alPresionar
                            )
                        }
                    }
                }
            }
        }
    }

    // Confirmación antes de enviar las respuestas (requisito de accesibilidad del caso)
    if (confirmarEnvio) {
        AlertDialog(
            onDismissRequest = { confirmarEnvio = false },
            icon = { Icon(Icons.Default.CheckCircle, contentDescription = null) },
            title = { Text("¿Terminaste?") },
            text = { Text("Vamos a guardar tus respuestas para que tu profesora pueda verlas.") },
            confirmButton = {
                Button(onClick = {
                    confirmarEnvio = false
                    lecturaViewModel.siguientePregunta()
                }) { Text("Sí, enviar") }
            },
            dismissButton = {
                TextButton(onClick = { confirmarEnvio = false }) { Text("Todavía no") }
            }
        )
    }
}

@Composable
private fun InicioActividadLectura(
    titulo: String,
    onComenzar: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = VerdeClaro
        ) {
            Icon(
                imageVector = Icons.Default.AutoStories,
                contentDescription = null,
                tint = VerdeLogro,
                modifier = Modifier
                    .padding(28.dp)
                    .size(72.dp)
            )
        }

        Text(
            text = "¡Vamos a leer!",
            fontSize = 30.sp,
            fontWeight = FontWeight.ExtraBold
        )

        Text(
            text = "Una historia, tres preguntas y mucho por descubrir.",
            color = TextoSecundario,
            fontSize = 17.sp,
            textAlign = TextAlign.Center
        )

        Surface(
            modifier = Modifier
                .widthIn(max = 620.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            contentColor = TextoPrincipal,
            border = BorderStroke(
                width = 1.dp,
                color = BordeSuave
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "LECTURA · NIVEL 1",
                    color = AzulPrimario,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = titulo,
                    fontSize = 25.sp,
                    lineHeight = 32.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "1. Lee la historia con calma.\n" +
                            "2. Elige tu respuesta.\n" +
                            "3. Pulsa Comprobar respuesta.",
                    fontSize = 18.sp,
                    lineHeight = 30.sp
                )

                Button(
                    onClick = onComenzar,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 58.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AzulPrimario,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "¡Comenzar lectura!",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun TextoLectura(
    estado: LecturaUiState,
    lector: LectorVoz?,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        contentColor = TextoPrincipal,
        border = BorderStroke(
            width = 1.dp,
            color = BordeSuave
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoStories,
                    contentDescription = null,
                    tint = VerdeLogro
                )

                Text(
                    text = "Mi historia",
                    color = VerdeLogro,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = estado.titulo,
                fontSize = 24.sp,
                lineHeight = 30.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = estado.texto,
                fontSize = 20.sp,
                lineHeight = 32.sp
            )

            if (lector != null) {
                BotonEscuchar(
                    texto = "${estado.titulo}. ${estado.texto}",
                    lector = lector,
                    color = VerdeLogro,
                    etiqueta = "Escuchar la historia",
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun PreguntaLecturaPanel(
    estado: LecturaUiState,
    lecturaViewModel: LecturaViewModel,
    lector: LectorVoz?,
    onBotonPrincipal: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pregunta = estado.preguntas
        .getOrNull(estado.indiceActual)
        ?: return

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // La pregunta entra deslizándose cada vez que cambia
        AnimatedContent(
            targetState = estado.indiceActual,
            transitionSpec = {
                (slideInHorizontally { ancho -> ancho / 2 } + fadeIn()) togetherWith
                        (slideOutHorizontally { ancho -> -ancho / 2 } + fadeOut())
            },
            label = "Cambio de pregunta"
        ) { indice ->
            val preguntaMostrada = estado.preguntas.getOrNull(indice) ?: pregunta
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = preguntaMostrada.tipo.etiqueta.uppercase(),
                    color = AzulPrimario,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = preguntaMostrada.enunciado,
                    fontSize = 23.sp,
                    lineHeight = 30.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (lector != null) {
            BotonEscuchar(
                texto = pregunta.enunciado + " " + pregunta.opciones
                    .mapIndexed { i, opcion -> "Opción ${('A'.code + i).toChar()}: $opcion." }
                    .joinToString(" "),
                lector = lector,
                color = AzulPrimario,
                etiqueta = "Escuchar la pregunta"
            )
        }

        Column(
            modifier = Modifier.selectableGroup(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            pregunta.opciones.forEachIndexed { indice, opcion ->
                val seleccionada =
                    estado.respuestaSeleccionada == opcion

                val correcta =
                    estado.respuestaComprobada &&
                            opcion == pregunta.respuestaCorrecta

                val incorrecta =
                    estado.respuestaComprobada &&
                            seleccionada &&
                            !correcta

                val color = when {
                    correcta -> VerdeClaro
                    incorrecta -> RojoClaro
                    seleccionada -> AzulClaro
                    else -> Color.White
                }

                val fondo by animateColorAsState(
                    targetValue = color,
                    label = "Color de alternativa"
                )

                val borde = when {
                    correcta -> VerdeLogro
                    incorrecta -> RojoSuave
                    seleccionada -> AzulPrimario
                    else -> BordeSuave
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
                                lecturaViewModel.seleccionarRespuesta(opcion)
                            }
                        ),
                    shape = RoundedCornerShape(18.dp),
                    color = fondo,
                    contentColor = TextoPrincipal,
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
                        Surface(
                            shape = CircleShape,
                            color = Color.White
                        ) {
                            Box(
                                modifier = Modifier.size(36.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = ('A'.code + indice)
                                        .toChar()
                                        .toString(),
                                    color = borde,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Text(
                            text = opcion,
                            modifier = Modifier.weight(1f),
                            fontSize = 18.sp,
                            lineHeight = 26.sp
                        )

                        if (correcta) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Respuesta correcta",
                                tint = VerdeLogro
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
                estado.respuestaSeleccionada == pregunta.respuestaCorrecta

            Surface(
                color = if (correcta) {
                    VerdeClaro
                } else {
                    NaranjaClaro
                },
                contentColor = TextoPrincipal,
                shape = RoundedCornerShape(18.dp)
            ) {
                Text(
                    text = if (correcta) {
                        "¡Muy bien! ${pregunta.retroalimentacion}"
                    } else {
                        "¡Buen intento! La respuesta es: ${pregunta.respuestaCorrecta}. " +
                                pregunta.retroalimentacion
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    fontSize = 18.sp,
                    lineHeight = 26.sp
                )
            }
        }

        estado.errorGuardado?.let { mensaje ->
            Text(
                text = mensaje,
                color = RojoSuave,
                fontSize = 16.sp
            )
        }

        if (estado.guardando) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth()
            )
        }

        Button(
            onClick = onBotonPrincipal,
            enabled = estado.respuestaSeleccionada != null &&
                    !estado.guardando,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 60.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AzulPrimario,
                contentColor = Color.White
            )
        ) {
            Text(
                text = when {
                    estado.guardando -> "Guardando…"
                    estado.errorGuardado != null -> "Reintentar guardado"
                    !estado.respuestaComprobada -> "Comprobar respuesta"
                    estado.indiceActual == estado.preguntas.lastIndex ->
                        "Ver mi resultado"

                    else -> "Siguiente pregunta"
                },
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ResultadoLectura(
    estado: LecturaUiState,
    onRepetir: () -> Unit,
    onVolver: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        EstrellaLogro()

        Text(
            text = "¡Terminaste tu lectura!",
            fontSize = 28.sp,
            lineHeight = 34.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Cada práctica te ayuda a aprender.",
            color = TextoSecundario,
            fontSize = 18.sp,
            textAlign = TextAlign.Center
        )

        Surface(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = VerdeClaro,
            contentColor = TextoPrincipal
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ContadorAnimado(
                    valor = estado.aciertos,
                    total = estado.preguntas.size,
                    color = VerdeLogro
                )

                Text(
                    text = "de ${estado.preguntas.size}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = VerdeLogro
                )

                Text(
                    text = "Respuestas correctas",
                    fontSize = 18.sp
                )

                Text(
                    text = "Tu resultado quedó guardado.",
                    fontSize = 15.sp
                )
            }
        }

        Button(
            onClick = onRepetir,
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxWidth()
                .heightIn(min = 56.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AzulPrimario,
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
                color = AzulPrimario,
                fontSize = 17.sp
            )
        }
    }
}