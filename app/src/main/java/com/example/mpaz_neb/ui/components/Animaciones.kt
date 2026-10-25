package com.example.mpaz_neb.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mpaz_neb.ui.theme.Dorado

/** Estrella que aparece con un rebote y un giro al terminar una actividad. */
@Composable
fun EstrellaLogro(
    modifier: Modifier = Modifier,
    color: Color = Dorado
) {
    val escala = remember { Animatable(0f) }
    val giro = remember { Animatable(-90f) }

    LaunchedEffect(Unit) {
        escala.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }
    LaunchedEffect(Unit) {
        giro.animateTo(0f, tween(durationMillis = 700, easing = FastOutSlowInEasing))
    }

    Icon(
        imageVector = Icons.Default.Star,
        contentDescription = null,
        tint = color,
        modifier = modifier
            .size(88.dp)
            .scale(escala.value)
            .rotate(giro.value)
    )
}

/** Número de aciertos que "cuenta" desde 0 hasta el valor final. */
@Composable
fun ContadorAnimado(
    valor: Int,
    total: Int,
    color: Color
) {
    var objetivo by remember { mutableStateOf(0) }
    LaunchedEffect(valor) { objetivo = valor }

    val mostrado by animateIntAsState(
        targetValue = objetivo,
        animationSpec = tween(durationMillis = 250 * total.coerceAtLeast(1)),
        label = "Contador de aciertos"
    )

    Text(
        text = mostrado.toString(),
        fontSize = 44.sp,
        fontWeight = FontWeight.ExtraBold,
        color = color
    )
}
