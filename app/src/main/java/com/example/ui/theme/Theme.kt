package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val ForensicsLightColorScheme = lightColorScheme(
    primary = ForensicsPalette.GreenPrimary,
    onPrimary = ForensicsPalette.CardSurface,
    primaryContainer = ForensicsPalette.GreenContainer,
    onPrimaryContainer = ForensicsPalette.GreenPrimary,
    secondary = ForensicsPalette.BluePrimary,
    onSecondary = ForensicsPalette.CardSurface,
    secondaryContainer = ForensicsPalette.BlueContainer,
    onSecondaryContainer = ForensicsPalette.BluePrimary,
    tertiary = ForensicsPalette.PurplePrimary,
    onTertiary = ForensicsPalette.CardSurface,
    tertiaryContainer = ForensicsPalette.PurpleContainer,
    onTertiaryContainer = ForensicsPalette.PurplePrimary,
    background = ForensicsPalette.ScreenBackground,
    onBackground = ForensicsPalette.TextPrimary,
    surface = ForensicsPalette.CardSurface,
    onSurface = ForensicsPalette.TextPrimary,
    surfaceVariant = ForensicsPalette.SubtleSurface,
    onSurfaceVariant = ForensicsPalette.TextSecondary,
    outline = ForensicsPalette.BorderSubtle,
    error = ForensicsPalette.RedPrimary,
    errorContainer = ForensicsPalette.RedContainer,
    onErrorContainer = ForensicsPalette.RedPrimary
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ForensicsLightColorScheme,
        typography = Typography,
        content = content
    )
}
