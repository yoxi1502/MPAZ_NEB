package com.example.mpaz_neb.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Esquema Material 3 de la app. Se usa solo modo claro para asegurar
// buen contraste y legibilidad en tabletas de sala de clases.
private val EsquemaClaro = lightColorScheme(
    primary = AzulPrimario,
    onPrimary = Color.White,
    primaryContainer = AzulClaro,
    onPrimaryContainer = AzulPrimario,
    secondary = VerdeLogro,
    onSecondary = Color.White,
    secondaryContainer = VerdeClaro,
    onSecondaryContainer = VerdeLogro,
    tertiary = NaranjaMatematica,
    onTertiary = Color.White,
    tertiaryContainer = NaranjaClaro,
    onTertiaryContainer = NaranjaMatematica,
    error = RojoSuave,
    onError = Color.White,
    errorContainer = RojoClaro,
    onErrorContainer = RojoSuave,
    background = FondoCalido,
    onBackground = TextoPrincipal,
    surface = Color.White,
    onSurface = TextoPrincipal,
    onSurfaceVariant = TextoSecundario,
    outline = BordeCampo,
    outlineVariant = BordeSuave
)

@Composable
fun MPAZ_NEBTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = EsquemaClaro,
        typography = Typography,
        content = content
    )
}
