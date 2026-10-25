package com.example.mpaz_neb.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.mpaz_neb.nativo.EstadoLectorVoz
import com.example.mpaz_neb.nativo.LectorVoz

/** Crea el lector de voz ligado a la pantalla y lo libera al salir de ella. */
@Composable
fun rememberLectorVoz(): LectorVoz {
    val contexto = LocalContext.current
    val lector = remember { LectorVoz(contexto) }
    DisposableEffect(lector) {
        onDispose { lector.liberar() }
    }
    return lector
}

/**
 * Botón grande para escuchar un texto. Muestra el estado del motor de voz
 * (preparando, hablando o no disponible) para que la interfaz sea coherente.
 */
@Composable
fun BotonEscuchar(
    texto: String,
    lector: LectorVoz,
    color: Color,
    modifier: Modifier = Modifier,
    etiqueta: String = "Escuchar"
) {
    val estado by lector.estado.collectAsState()
    val hablando by lector.hablando.collectAsState()

    // Animación de "latido" del ícono mientras se está leyendo en voz alta
    val pulso = rememberInfiniteTransition(label = "Pulso voz")
    val escala by pulso.animateFloat(
        initialValue = 1f,
        targetValue = if (hablando) 1.25f else 1f,
        animationSpec = infiniteRepeatable(tween(450), RepeatMode.Reverse),
        label = "Escala ícono voz"
    )

    val disponible = estado == EstadoLectorVoz.LISTO

    OutlinedButton(
        onClick = { if (hablando) lector.detener() else lector.leer(texto) },
        enabled = disponible,
        modifier = modifier.heightIn(min = 52.dp)
    ) {
        Icon(
            imageVector = when {
                !disponible -> Icons.AutoMirrored.Filled.VolumeOff
                hablando -> Icons.Default.Stop
                else -> Icons.AutoMirrored.Filled.VolumeUp
            },
            contentDescription = null,
            tint = if (disponible) color else Color.Gray,
            modifier = Modifier.scale(if (hablando) escala else 1f)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = when (estado) {
                EstadoLectorVoz.INICIANDO -> "Preparando voz…"
                EstadoLectorVoz.SIN_IDIOMA -> "Voz en español no instalada"
                EstadoLectorVoz.NO_DISPONIBLE -> "Voz no disponible"
                EstadoLectorVoz.LISTO -> if (hablando) "Detener" else etiqueta
            },
            color = if (disponible) color else Color.Gray
        )
    }
}
