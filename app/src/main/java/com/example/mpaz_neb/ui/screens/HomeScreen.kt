package com.example.mpaz_neb.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.mpaz_neb.navigation.Screen

private val InicioAzul = Color(0xFF2456A6)
private val InicioVerde = Color(0xFF176B60)
private val InicioTexto = Color(0xFF24374B)
private val InicioTextoSecundario = Color(0xFF526273)

@Composable
fun HomeScreen(
    navController: NavController
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFFFFAF2)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
        ) {
            val pantallaAncha = maxWidth >= 760.dp

            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .widthIn(max = 1100.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(
                        horizontal = if (pantallaAncha) 40.dp else 24.dp,
                        vertical = 24.dp
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFE3EFE9)
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = InicioVerde,
                            modifier = Modifier.padding(12.dp).size(26.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Escuela Marcela Paz",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = InicioTexto
                        )

                        Text(
                            text = "Aprendemos a nuestro ritmo",
                            fontSize = 13.sp,
                            color = InicioTextoSecundario
                        )
                    }
                }

                if (pantallaAncha) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(48.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BienvenidaInicio(
                            modifier = Modifier.weight(1f)
                        )

                        PerfilesInicio(
                            navController = navController,
                            modifier = Modifier.weight(1f)
                        )
                    }
                } else {
                    BienvenidaInicio()

                    PerfilesInicio(
                        navController = navController
                    )
                }

                Text(
                    text = "Lectura y Matemática • San Bernardo",
                    fontSize = 12.sp,
                    color = InicioTextoSecundario,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun BienvenidaInicio(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        IlustracionInicio()

        Text(
            text = "¡Aprendamos juntos!",
            fontSize = 30.sp,
            lineHeight = 36.sp,
            fontWeight = FontWeight.ExtraBold,
            color = InicioTexto,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Lee, descubre y practica.\nCada pequeño paso es un gran logro.",
            fontSize = 16.sp,
            lineHeight = 24.sp,
            color = InicioTextoSecundario,
            textAlign = TextAlign.Center
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            EtiquetaInicio(
                texto = "ABC · Lectura",
                fondo = Color(0xFFE5EEF9),
                colorTexto = InicioAzul
            )

            EtiquetaInicio(
                texto = "123 · Matemática",
                fondo = Color(0xFFFFE8CB),
                colorTexto = Color(0xFF85460D)
            )
        }
    }
}

@Composable
private fun IlustracionInicio() {
    Box(
        modifier = Modifier
            .width(260.dp)
            .height(156.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(150.dp)
                .background(
                    color = Color(0xFFDCEFE7),
                    shape = CircleShape
                )
        )

        Icon(
            imageVector = Icons.Default.AutoStories,
            contentDescription = null,
            tint = InicioVerde,
            modifier = Modifier.size(100.dp)
        )

        Surface(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 18.dp, top = 10.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFE5EEF9)
        ) {
            Text(
                text = "ABC",
                modifier = Modifier.padding(
                    horizontal = 14.dp,
                    vertical = 10.dp
                ),
                color = InicioAzul,
                fontSize = 23.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }

        Surface(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 14.dp, bottom = 8.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFFFE8CB)
        ) {
            Text(
                text = "123",
                modifier = Modifier.padding(
                    horizontal = 14.dp,
                    vertical = 10.dp
                ),
                color = Color(0xFF85460D),
                fontSize = 23.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }

        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = null,
            tint = Color(0xFFE8A32E),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 24.dp, top = 14.dp)
                .size(28.dp)
        )

        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = null,
            tint = Color(0xFFE8A32E),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 32.dp, bottom = 12.dp)
                .size(20.dp)
        )
    }
}

@Composable
private fun EtiquetaInicio(
    texto: String,
    fondo: Color,
    colorTexto: Color
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = fondo
    ) {
        Text(
            text = texto,
            modifier = Modifier.padding(
                horizontal = 12.dp,
                vertical = 8.dp
            ),
            color = colorTexto,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun PerfilesInicio(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .widthIn(max = 480.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "¿Quién va a comenzar?",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = InicioTexto,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Elige tu perfil",
            fontSize = 14.sp,
            color = InicioTextoSecundario
        )

        BotonPerfilInicio(
            titulo = "Soy estudiante",
            descripcion = "¡Vamos a aprender!",
            icono = Icons.Default.Face,
            fondo = InicioAzul,
            colorTexto = Color.White,
            fondoIcono = Color.White.copy(alpha = 0.16f),
            onClick = {
                navController.navigate(Screen.StudentCode.route) {
                    launchSingleTop = true
                }
            }
        )

        BotonPerfilInicio(
            titulo = "Equipo pedagógico",
            descripcion = "Docentes y asistentes",
            icono = Icons.Default.Groups,
            fondo = Color.White,
            colorTexto = InicioVerde,
            fondoIcono = Color(0xFFE3EFE9),
            onClick = {
                navController.navigate(Screen.TeacherLogin.route) {
                    launchSingleTop = true
                }
            }
        )
    }
}

@Composable
private fun BotonPerfilInicio(
    titulo: String,
    descripcion: String,
    icono: ImageVector,
    fondo: Color,
    colorTexto: Color,
    fondoIcono: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 94.dp),
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(
            width = 1.dp,
            color = if (fondo == Color.White) {
                Color(0xFFCFDED8)
            } else {
                fondo
            }
        ),
        colors = ButtonDefaults.buttonColors(
            containerColor = fondo,
            contentColor = colorTexto
        ),
        contentPadding = PaddingValues(18.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = fondoIcono
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = colorTexto,
                modifier = Modifier
                    .padding(12.dp)
                    .size(28.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = titulo,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = descripcion,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Icon(
            imageVector = Icons.Default.ArrowForward,
            contentDescription = null,
            modifier = Modifier.size(22.dp)
        )
    }
}