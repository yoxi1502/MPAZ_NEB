package com.example.mpaz_neb.ui.screens

import com.example.mpaz_neb.ui.theme.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel as obtenerViewModel
import androidx.navigation.NavController
import com.example.mpaz_neb.model.ModuloActividad
import com.example.mpaz_neb.navigation.Screen
import com.example.mpaz_neb.viewmodel.StudentCodeViewModel


private data class OpcionActividadCodigo(
    val titulo: String,
    val descripcion: String,
    val icono: ImageVector,
    val fondo: Color,
    val color: Color,
    val destino: Screen
)

@Composable
fun StudentCodeScreen(
    navController: NavController,
    studentCodeViewModel: StudentCodeViewModel = obtenerViewModel()
) {
    val estado by studentCodeViewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    LaunchedEffect(estado.errorCodigo) {
        if (estado.errorCodigo != null) scrollState.animateScrollTo(0)
    }

    val actividades = listOf(
        OpcionActividadCodigo(
            titulo = "Lectura",
            descripcion = "Leer y responder",
            icono = Icons.Default.AutoStories,
            fondo = AzulClaro,
            color = AzulPrimario,
            destino = Screen.Lectura
        ),
        OpcionActividadCodigo(
            titulo = "Matemática",
            descripcion = "Practicar números",
            icono = Icons.Default.Calculate,
            fondo = NaranjaClaro,
            color = NaranjaMatematica,
            destino = Screen.Matematica
        ),
        OpcionActividadCodigo(
            titulo = "Tomar foto",
            descripcion = "Mostrar mi trabajo",
            icono = Icons.Default.CameraAlt,
            fondo = VerdeClaro,
            color = VerdeLogro,
            destino = Screen.Camara
        ),
        OpcionActividadCodigo(
            titulo = "Mi voz",
            descripcion = "Grabar y escuchar",
            icono = Icons.Default.Mic,
            fondo = RojoClaro,
            color = RojoSuave,
            destino = Screen.Audio
        )
    )

    fun abrirActividad(destino: Screen? = null) {
        val moduloRequerido = when (destino) {
            Screen.Lectura -> ModuloActividad.LECTURA
            Screen.Matematica -> ModuloActividad.MATEMATICA
            else -> null
        }
        studentCodeViewModel.validarCodigo(moduloRequerido) { sesion ->
            focusManager.clearFocus()
            val ruta = destino?.route ?: when (sesion?.modulo) {
                ModuloActividad.LECTURA -> Screen.Lectura.route
                ModuloActividad.MATEMATICA -> Screen.Matematica.route
                null -> Screen.Lectura.route
            }
            navController.navigate(ruta) { launchSingleTop = true }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = FondoCalido
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .imePadding()
        ) {
            val columnas = if (maxWidth >= 760.dp) 4 else 2

            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .widthIn(max = 1000.dp)
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(
                        onClick = { navController.popBackStack() },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = AzulPrimario
                        )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text("Volver")
                    }
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = AzulClaro
                ) {
                    Icon(
                        imageVector = Icons.Default.VpnKey,
                        contentDescription = null,
                        tint = AzulPrimario,
                        modifier = Modifier
                            .padding(16.dp)
                            .size(32.dp)
                    )
                }

                Text(
                    text = "¡Tu aventura comienza!",
                    fontSize = 26.sp,
                    lineHeight = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextoPrincipal,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Escribe el código que te entregó tu profesora " +
                            "para abrir tu actividad asignada.",
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    color = TextoSecundario,
                    textAlign = TextAlign.Center
                )

                Surface(
                    modifier = Modifier
                        .widthIn(max = 500.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    color = Color.White,
                    border = BorderStroke(
                        1.dp,
                        BordeSuave
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Mi código de sesión",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextoPrincipal
                        )

                        OutlinedTextField(
                            value = estado.codigo,
                            onValueChange = studentCodeViewModel::actualizarCodigo,
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            enabled = !estado.validando,
                            label = { Text("Código") },
                            shape = RoundedCornerShape(14.dp),
                            isError = estado.errorCodigo != null,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.VpnKey,
                                    contentDescription = null
                                )
                            },
                            trailingIcon = {
                                if (estado.errorCodigo != null) {
                                    Icon(
                                        imageVector = Icons.Default.Error,
                                        contentDescription = "Error en el código"
                                    )
                                }
                            },
                            supportingText = {
                                estado.errorCodigo?.let { mensaje ->
                                    Text(text = mensaje)
                                }
                            },
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Characters,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = { focusManager.clearFocus() }
                            ),
                            textStyle = LocalTextStyle.current.copy(
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextoPrincipal,
                                unfocusedTextColor = TextoPrincipal,
                                errorTextColor = TextoPrincipal,
                                focusedBorderColor = AzulPrimario,
                                unfocusedBorderColor = BordeCampo,
                                focusedLabelColor = AzulPrimario,
                                unfocusedLabelColor = TextoSecundario,
                                focusedLeadingIconColor = AzulPrimario,
                                unfocusedLeadingIconColor = TextoSecundario,
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                errorContainerColor = Color.White
                            )
                        )
                    }
                }

                Button(
                    onClick = { abrirActividad() },
                    enabled = !estado.validando,
                    modifier = Modifier.widthIn(max = 500.dp).fillMaxWidth().heightIn(min = 56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AzulPrimario)
                ) {
                    Text(if (estado.validando) "Comprobando…" else "Comenzar actividad asignada")
                }

                Text(
                    text = "¿Qué vamos a hacer hoy?",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextoPrincipal,
                    textAlign = TextAlign.Center
                )

                Column(
                    modifier = Modifier
                        .widthIn(
                            max = if (columnas == 4) 1000.dp else 560.dp
                        )
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    actividades.chunked(columnas).forEach { fila ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            fila.forEach { actividad ->
                                TarjetaActividadCodigo(
                                    actividad = actividad,
                                    enabled = !estado.validando,
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        abrirActividad(actividad.destino)
                                    }
                                )
                            }
                        }
                    }
                }

                Text(
                    text = "¿No tienes código? Pídeselo a tu profesora o asistente.",
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                    color = TextoSecundario,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Escuela Marcela Paz • San Bernardo",
                    fontSize = 12.sp,
                    color = TextoSecundario,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun TarjetaActividadCodigo(
    actividad: OpcionActividadCodigo,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.heightIn(min = 154.dp),
        shape = RoundedCornerShape(22.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = actividad.fondo,
            contentColor = actividad.color
        ),
        contentPadding = PaddingValues(
            horizontal = 10.dp,
            vertical = 20.dp
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = actividad.icono,
                contentDescription = null,
                modifier = Modifier.size(36.dp)
            )

            Text(
                text = actividad.titulo,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Text(
                text = actividad.descripcion,
                fontSize = 12.sp,
                lineHeight = 17.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
