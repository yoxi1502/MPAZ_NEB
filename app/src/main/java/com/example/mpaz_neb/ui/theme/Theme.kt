package com.example.mpaz_neb.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF2456A6),
    secondary = Color(0xFF176B60),
    tertiary = Color(0xFF85460D),
    background = Color(0xFFFFFAF2),
    surface = Color.White,
    onBackground = Color(0xFF24374B),
    onSurface = Color(0xFF24374B)
)

@Composable
fun MPAZ_NEBTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
