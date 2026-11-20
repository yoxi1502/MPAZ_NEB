package com.example.mpaz_neb.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.mpaz_neb.model.PremioPokemonUiState
import com.example.mpaz_neb.ui.theme.TextoPrincipal
import com.example.mpaz_neb.ui.theme.TextoSecundario
import com.example.mpaz_neb.ui.theme.VerdeClaro
import com.example.mpaz_neb.ui.theme.VerdeLogro

/**
 * Tarjeta "Tu compañero Pokémon" que aparece bajo el resultado.
 * No guarda estado propio: lo recibe del PremioPokemonViewModel a través de la pantalla.
 */
@Composable
fun PremioPokemon(
    estado: PremioPokemonUiState,
    aciertos: Int,
    total: Int,
    onReintentar: () -> Unit,
    modifier: Modifier = Modifier,
    colorAcento: Color = VerdeLogro
) {
    Surface(
        modifier = modifier
            .widthIn(max = 600.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = VerdeClaro,
        contentColor = TextoPrincipal
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Tu compañero Pokémon",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = colorAcento
            )

            when {
                estado.cargando -> {
                    CircularProgressIndicator(color = colorAcento, modifier = Modifier.padding(16.dp))
                    Text(text = "Buscando a tu compañero…", color = TextoSecundario)
                }

                estado.error != null -> {
                    Text(
                        text = estado.error,
                        color = TextoSecundario,
                        textAlign = TextAlign.Center
                    )
                    TextButton(onClick = onReintentar) {
                        Text(text = "Intentar de nuevo", color = colorAcento)
                    }
                }

                estado.pokemon != null -> {
                    val pokemon = estado.pokemon
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + scaleIn(initialScale = 0.6f)
                    ) {
                        AsyncImage(
                            model = pokemon.imagenUrl,
                            contentDescription = "Imagen de ${pokemon.nombre}",
                            modifier = Modifier.size(140.dp)
                        )
                    }
                    Text(
                        text = pokemon.nombre,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = colorAcento
                    )
                    if (pokemon.tipos.isNotEmpty()) {
                        Text(
                            text = "Tipo: ${pokemon.tiposTexto}",
                            fontSize = 14.sp,
                            color = TextoSecundario
                        )
                    }
                    Text(
                        text = pokemon.mensajeAnimo(aciertos, total),
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
