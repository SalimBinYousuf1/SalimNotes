package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SalimLightColorScheme = lightColorScheme(
    primary = AccentBlue,
    onPrimary = Color.White,
    primaryContainer = ChipSelectedFill,
    onPrimaryContainer = ChipSelectedText,
    secondary = PillMicCircle,
    onSecondary = Color.White,
    background = Color.Transparent,
    onBackground = BodyText,
    surface = GlassBackground,
    onSurface = BodyText,
    surfaceVariant = GlassBackgroundElevated,
    onSurfaceVariant = BodySubtle,
    outline = GlassBorder
)

private val SalimDarkColorScheme = darkColorScheme(
    primary = DarkAccentBlue,
    onPrimary = Color.White,
    primaryContainer = ChipSelectedFill,
    onPrimaryContainer = ChipSelectedText,
    secondary = PillMicCircle,
    onSecondary = Color.White,
    background = Color.Transparent,
    onBackground = DarkBodyText,
    surface = DarkGlassBackground,
    onSurface = DarkBodyText,
    surfaceVariant = DarkGlassBackground,
    onSurfaceVariant = Color(0xFF9CA3AF),
    outline = DarkGlassBorder
)

@Composable
fun SalimTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) SalimDarkColorScheme else SalimLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    SalimTheme(darkTheme = darkTheme, content = content)
}
