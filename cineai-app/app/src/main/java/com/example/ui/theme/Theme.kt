package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = CineGoldPrimary,
    onPrimary = CineOnContainerText,
    primaryContainer = CinePrimaryContainer,
    onPrimaryContainer = CineGoldLight,
    secondary = CineTealAccent,
    onSecondary = Color(0xFF332D41),
    secondaryContainer = CineSurfaceVariant,
    onSecondaryContainer = CineTealLight,
    tertiary = CineGoldLight,
    onTertiary = CineOnContainerText,
    background = CineObsidian,
    onBackground = CineTextPrimary,
    surface = CineSurface,
    onSurface = CineTextPrimary,
    surfaceVariant = CineSurfaceVariant,
    onSurfaceVariant = CineTextSecondary,
    outline = CineBorder,
    error = CineRedRecord,
    onError = Color(0xFF601410)
)

private val LightColorScheme = DarkColorScheme

@Composable
fun CineStudioTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    CineStudioTheme(content = content)
}
