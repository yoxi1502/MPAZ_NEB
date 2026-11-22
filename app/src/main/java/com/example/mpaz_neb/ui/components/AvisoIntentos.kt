package com.example.mpaz_neb.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mpaz_neb.ui.theme.AzulClaro
import com.example.mpaz_neb.ui.theme.RojoClaro
import com.example.mpaz_neb.ui.theme.TextoPrincipal

/**
 * Aviso del límite de intentos de la sesión asignada.
 * No muestra nada si la sesión no tiene límite y no hay aviso del servidor.
 */
@Composable
fun AvisoIntentos(
    intentosRestantes: Int?,
    sinIntentos: Boolean,
    aviso: String? = null,
    modifier: Modifier = Modifier
) {
    val texto = when {
        aviso != null -> aviso
        sinIntentos -> "Ya usaste todos los intentos de esta actividad. Pide ayuda a tu asistente."
        intentosRestantes == 1 -> "Te queda 1 intento en esta actividad."
        intentosRestantes != null -> "Te quedan $intentosRestantes intentos en esta actividad."
        else -> return
    }

    Surface(
        modifier = modifier
            .widthIn(max = 600.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = if (sinIntentos || aviso != null) RojoClaro else AzulClaro,
        contentColor = TextoPrincipal
    ) {
        Text(
            text = texto,
            modifier = Modifier.padding(16.dp),
            fontSize = 16.sp,
            textAlign = TextAlign.Center
        )
    }
}
