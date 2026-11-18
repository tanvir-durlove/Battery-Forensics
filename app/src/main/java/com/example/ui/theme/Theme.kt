package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color

private val ForensicsLightColorScheme = lightColorScheme(
    primary = ForensicsLightColors.GreenPrimary,
    onPrimary = ForensicsLightColors.CardSurface,
    primaryContainer = ForensicsLightColors.GreenContainer,
    onPrimaryContainer = ForensicsLightColors.GreenPrimary,
    secondary = ForensicsLightColors.BluePrimary,
    onSecondary = ForensicsLightColors.CardSurface,
    secondaryContainer = ForensicsLightColors.BlueContainer,
    onSecondaryContainer = ForensicsLightColors.BluePrimary,
    tertiary = ForensicsLightColors.PurplePrimary,
    onTertiary = ForensicsLightColors.CardSurface,
    tertiaryContainer = ForensicsLightColors.PurpleContainer,
    onTertiaryContainer = ForensicsLightColors.PurplePrimary,
    background = ForensicsLightColors.ScreenBackground,
    onBackground = ForensicsLightColors.TextPrimary,
    surface = ForensicsLightColors.CardSurface,
    onSurface = ForensicsLightColors.TextPrimary,
    surfaceVariant = ForensicsLightColors.SubtleSurface,
    onSurfaceVariant = ForensicsLightColors.TextSecondary,
    outline = ForensicsLightColors.BorderSubtle,
    error = ForensicsLightColors.RedPrimary,
    errorContainer = ForensicsLightColors.RedContainer,
    onErrorContainer = ForensicsLightColors.RedPrimary
)

private val ForensicsDarkColorScheme = darkColorScheme(
    primary = ForensicsDarkColors.GreenPrimary,
    onPrimary = Color(0xFF10141A),
    primaryContainer = ForensicsDarkColors.GreenContainer,
    onPrimaryContainer = ForensicsDarkColors.GreenPrimary,
    secondary = ForensicsDarkColors.BluePrimary,
    onSecondary = Color(0xFF10141A),
    secondaryContainer = ForensicsDarkColors.BlueContainer,
    onSecondaryContainer = ForensicsDarkColors.BluePrimary,
    tertiary = ForensicsDarkColors.PurplePrimary,
    onTertiary = Color(0xFF10141A),
    tertiaryContainer = ForensicsDarkColors.PurpleContainer,
    onTertiaryContainer = ForensicsDarkColors.PurplePrimary,
    background = ForensicsDarkColors.ScreenBackground,
    onBackground = ForensicsDarkColors.TextPrimary,
    surface = ForensicsDarkColors.CardSurface,
    onSurface = ForensicsDarkColors.TextPrimary,
    surfaceVariant = ForensicsDarkColors.SubtleSurface,
    onSurfaceVariant = ForensicsDarkColors.TextSecondary,
    outline = ForensicsDarkColors.BorderSubtle,
    error = ForensicsDarkColors.RedPrimary,
    errorContainer = ForensicsDarkColors.RedContainer,
    onErrorContainer = ForensicsDarkColors.RedPrimary
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    // Sync the plain state-backed palette before children compose, so the
    // first frame already uses the right set. remember() re-runs only when
    // darkTheme changes; the write is idempotent (same instance), so it
    // cannot loop.
    remember(darkTheme) { ForensicsPalette.setDark(darkTheme) }
    MaterialTheme(
        colorScheme = if (darkTheme) ForensicsDarkColorScheme else ForensicsLightColorScheme,
        typography = Typography,
        content = content
    )
}
