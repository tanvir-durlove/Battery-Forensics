package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// 1.0.2: the palette resolves against the system theme, so the app follows
// dark mode instead of staying bright white at night. Light values are the
// original 1.0.1 palette, unchanged. Dark values keep the same accents at
// mid-tone saturation: light enough to read as text on dark cards, dark
// enough for the few places an accent is used as a background (e.g. the
// green status banner in MainActivity, which keeps hardcoded white text).
internal object ForensicsLightColors {
    // Distinct cool slate-gray background so pure white cards pop with high contrast
    val ScreenBackground = Color(0xFFEAEEF4)
    val CardSurface = Color(0xFFFFFFFF)
    val SubtleSurface = Color(0xFFF0F3F8)
    val SubtleSurfaceAlt = Color(0xFFE3E8F0)
    val BorderSubtle = Color(0xFFD8E0EB)
    val BorderStrong = Color(0xFFC8D2E0)
    val DividerColor = Color(0xFFE4E9F1)

    val TextPrimary = Color(0xFF14181F)
    val TextSecondary = Color(0xFF4E5663)
    val TextMuted = Color(0xFF7B8492)

    // Forensic Green (Home, Normal, Good, Measured Health)
    val GreenPrimary = Color(0xFF146336)
    val GreenGauge = Color(0xFF196E3E)
    val GreenContainer = Color(0xFFD5F2E3)
    val GreenBorder = Color(0xFFA8E0C2)
    val GreenTrack = Color(0xFFD2EDE0)
    val GreenSoftTile = Color(0xFFE2F6EC)

    // Diagnostic Blue (Diagnose, Strong Evidence, Voltage)
    val BluePrimary = Color(0xFF0C4F8E)
    val BlueBright = Color(0xFF125EB8)
    val BlueContainer = Color(0xFFD0EAFC)
    val BlueBorder = Color(0xFFA4D4F7)
    val BlueSoftTile = Color(0xFFE6F2FD)
    val BlueChartLine = Color(0xFF0D47A1)
    val BlueBarLight = Color(0xFFB5DCFA)

    // Insights Purple (Insights, Current, Controlled Experiment)
    val PurplePrimary = Color(0xFF561D94)
    val PurpleBright = Color(0xFF631794)
    val PurpleContainer = Color(0xFFEADBFB)
    val PurpleBorder = Color(0xFFCFB0F7)
    val PurpleSoftTile = Color(0xFFF2E8FE)
    val PurpleDeepButton = Color(0xFF451285)

    // Amber / Orange (Charging, Temperature, Anomaly)
    val AmberPrimary = Color(0xFF7D4300)
    val AmberDarkText = Color(0xFF442400)
    val TemperatureOrange = Color(0xFFAD4B00)
    val AmberContainer = Color(0xFFFCE2BA)
    val AmberBorder = Color(0xFFF3C684)
    val AmberInnerCard = Color(0xFFFFF6E8)
    val AmberPill = Color(0xFFFADCA8)
    val AmberSoftTile = Color(0xFFFEF3D6)
    val AmberIconBox = Color(0xFF753E00)

    // Red / Alert (Anomaly Drain, High Temp, Delete, Unavailable X)
    val RedPrimary = Color(0xFFB43210)
    val RedBar = Color(0xFFC83727)
    val RedContainer = Color(0xFFFCE2E2)
    val RedBorder = Color(0xFFF3B8B8)

    // Neutral Gray (Settings, Doze Inactive, Possible)
    val GrayPrimary = Color(0xFF343940)
    val GrayContainer = Color(0xFFDCE1E8)
    val GrayBorder = Color(0xFFCBD2DC)
    val GraySoftTile = Color(0xFFEEF1F6)
}

internal object ForensicsDarkColors {
    val ScreenBackground = Color(0xFF10141A)
    val CardSurface = Color(0xFF1B212B)
    val SubtleSurface = Color(0xFF232B37)
    val SubtleSurfaceAlt = Color(0xFF2A3341)
    val BorderSubtle = Color(0xFF323D4D)
    val BorderStrong = Color(0xFF415062)
    val DividerColor = Color(0xFF2A3341)

    val TextPrimary = Color(0xFFE9EDF2)
    val TextSecondary = Color(0xFFB4BECD)
    val TextMuted = Color(0xFF8A94A6)

    // Forensic Green — mid-tone so it reads as text on dark cards
    val GreenPrimary = Color(0xFF34A853)
    val GreenGauge = Color(0xFF3D9E66)
    val GreenContainer = Color(0xFF1E3A2B)
    val GreenBorder = Color(0xFF2E5C40)
    val GreenTrack = Color(0xFF24402F)
    val GreenSoftTile = Color(0xFF1B2E22)

    // Diagnostic Blue
    val BluePrimary = Color(0xFF4A90D9)
    val BlueBright = Color(0xFF7FB6EC)
    val BlueContainer = Color(0xFF1B3049)
    val BlueBorder = Color(0xFF2C4A6B)
    val BlueSoftTile = Color(0xFF18293D)
    val BlueChartLine = Color(0xFF6FA8DC)
    val BlueBarLight = Color(0xFF2E4A66)

    // Insights Purple
    val PurplePrimary = Color(0xFFA06CD5)
    val PurpleBright = Color(0xFFC79BF5)
    val PurpleContainer = Color(0xFF2E2145)
    val PurpleBorder = Color(0xFF4A3480)
    val PurpleSoftTile = Color(0xFF281E3A)
    val PurpleDeepButton = Color(0xFF5A2E9E)

    // Amber / Orange
    val AmberPrimary = Color(0xFFE8A33D)
    val AmberDarkText = Color(0xFFFFD9A0)
    val TemperatureOrange = Color(0xFFF08A3C)
    val AmberContainer = Color(0xFF3A2A14)
    val AmberBorder = Color(0xFF5C451F)
    val AmberInnerCard = Color(0xFF2A2118)
    val AmberPill = Color(0xFF3D2F16)
    val AmberSoftTile = Color(0xFF2E2616)
    val AmberIconBox = Color(0xFF7A4E12)

    // Red / Alert
    val RedPrimary = Color(0xFFE57373)
    val RedBar = Color(0xFFD95D4A)
    val RedContainer = Color(0xFF3D1E1A)
    val RedBorder = Color(0xFF5C2E28)

    // Neutral Gray
    val GrayPrimary = Color(0xFFC7CDD6)
    val GrayContainer = Color(0xFF2A2F37)
    val GrayBorder = Color(0xFF3A414D)
    val GraySoftTile = Color(0xFF20252D)
}

object ForensicsPalette {
    // Every color resolves against the system theme at composition time, so
    // all existing call sites (screens, components, MainActivity) follow
    // dark mode with no changes at the call sites.
    val ScreenBackground: Color
        @Composable get() = if (isSystemInDarkTheme()) ForensicsDarkColors.ScreenBackground else ForensicsLightColors.ScreenBackground
    val CardSurface: Color
        @Composable get() = if (isSystemInDarkTheme()) ForensicsDarkColors.CardSurface else ForensicsLightColors.CardSurface
    val SubtleSurface: Color
        @Composable get() = if (isSystemInDarkTheme()) ForensicsDarkColors.SubtleSurface else ForensicsLightColors.SubtleSurface
    val SubtleSurfaceAlt: Color
        @Composable get() = if (isSystemInDarkTheme()) ForensicsDarkColors.SubtleSurfaceAlt else ForensicsLightColors.SubtleSurfaceAlt
    val BorderSubtle: Color
        @Composable get() = if (isSystemInDarkTheme()) ForensicsDarkColors.BorderSubtle else ForensicsLightColors.BorderSubtle
    val BorderStrong: Color
        @Composable get() = if (isSystemInDarkTheme()) ForensicsDarkColors.BorderStrong else ForensicsLightColors.BorderStrong
    val DividerColor: Color
        @Composable get() = if (isSystemInDarkTheme()) ForensicsDarkColors.DividerColor else ForensicsLightColors.DividerColor

    val TextPrimary: Color
        @Composable get() = if (isSystemInDarkTheme()) ForensicsDarkColors.TextPrimary else ForensicsLightColors.TextPrimary
    val TextSecondary: Color
        @Composable get() = if (isSystemInDarkTheme()) ForensicsDarkColors.TextSecondary else ForensicsLightColors.TextSecondary
    val TextMuted: Color
        @Composable get() = if (isSystemInDarkTheme()) ForensicsDarkColors.TextMuted else ForensicsLightColors.TextMuted

    val GreenPrimary: Color
        @Composable get() = if (isSystemInDarkTheme()) ForensicsDarkColors.GreenPrimary else ForensicsLightColors.GreenPrimary
    val GreenGauge: Color
        @Composable get() = if (isSystemInDarkTheme()) ForensicsDarkColors.GreenGauge else ForensicsLightColors.GreenGauge
    val GreenContainer: Color
        @Composable get() = if (isSystemInDarkTheme()) ForensicsDarkColors.GreenContainer else ForensicsLightColors.GreenContainer
    val GreenBorder: Color
        @Composable get() = if (isSystemInDarkTheme()) ForensicsDarkColors.GreenBorder else ForensicsLightColors.GreenBorder
    val GreenTrack: Color
        @Composable get() = if (isSystemInDarkTheme()) ForensicsDarkColors.GreenTrack else ForensicsLightColors.GreenTrack
    val GreenSoftTile: Color
        @Composable get() = if (isSystemInDarkTheme()) ForensicsDarkColors.GreenSoftTile else ForensicsLightColors.GreenSoftTile

    val BluePrimary: Color
        @Composable get() = if (isSystemInDarkTheme()) ForensicsDarkColors.BluePrimary else ForensicsLightColors.BluePrimary
    val BlueBright: Color
        @Composable get() = if (isSystemInDarkTheme()) ForensicsDarkColors.BlueBright else ForensicsLightColors.BlueBright
    val BlueContainer: Color
        @Composable get() = if (isSystemInDarkTheme()) ForensicsDarkColors.BlueContainer else ForensicsLightColors.BlueContainer
    val BlueBorder: Color
        @Composable get() = if (isSystemInDarkTheme()) ForensicsDarkColors.BlueBorder else ForensicsLightColors.BlueBorder
    val BlueSoftTile: Color
        @Composable get() = if (isSystemInDarkTheme()) ForensicsDarkColors.BlueSoftTile else ForensicsLightColors.BlueSoftTile
    val BlueChartLine: Color
        @Composable get() = if (isSystemInDarkTheme()) ForensicsDarkColors.BlueChartLine else ForensicsLightColors.BlueChartLine
    val BlueBarLight: Color
        @Composable get() = if (isSystemInDarkTheme()) ForensicsDarkColors.BlueBarLight else ForensicsLightColors.BlueBarLight

    val PurplePrimary: Color
        @Composable get() = if (isSystemInDarkTheme()) ForensicsDarkColors.PurplePrimary else ForensicsLightColors.PurplePrimary
    val PurpleBright: Color
        @Composable get() = if (isSystemInDarkTheme()) ForensicsDarkColors.PurpleBright else ForensicsLightColors.PurpleBright
    val PurpleContainer: Color
        @Composable get() = if (isSystemInDarkTheme()) ForensicsDarkColors.PurpleContainer else ForensicsLightColors.PurpleContainer
    val PurpleBorder: Color
        @Composable get() = if (isSystemInDarkTheme()) ForensicsDarkColors.PurpleBorder else ForensicsLightColors.PurpleBorder
    val PurpleSoftTile: Color
        @Composable get() = if (isSystemInDarkTheme()) ForensicsDarkColors.PurpleSoftTile else ForensicsLightColors.PurpleSoftTile
    val PurpleDeepButton: Color
        @Composable get() = if (isSystemInDarkTheme()) ForensicsDarkColors.PurpleDeepButton else ForensicsLightColors.PurpleDeepButton

    val AmberPrimary: Color
        @Composable get() = if (isSystemInDarkTheme()) ForensicsDarkColors.AmberPrimary else ForensicsLightColors.AmberPrimary
    val AmberDarkText: Color
        @Composable get() = if (isSystemInDarkTheme()) ForensicsDarkColors.AmberDarkText else ForensicsLightColors.AmberDarkText
    val TemperatureOrange: Color
        @Composable get() = if (isSystemInDarkTheme()) ForensicsDarkColors.TemperatureOrange else ForensicsLightColors.TemperatureOrange
    val AmberContainer: Color
        @Composable get() = if (isSystemInDarkTheme()) ForensicsDarkColors.AmberContainer else ForensicsLightColors.AmberContainer
    val AmberBorder: Color
        @Composable get() = if (isSystemInDarkTheme()) ForensicsDarkColors.AmberBorder else ForensicsLightColors.AmberBorder
    val AmberInnerCard: Color
        @Composable get() = if (isSystemInDarkTheme()) ForensicsDarkColors.AmberInnerCard else ForensicsLightColors.AmberInnerCard
    val AmberPill: Color
        @Composable get() = if (isSystemInDarkTheme()) ForensicsDarkColors.AmberPill else ForensicsLightColors.AmberPill
    val AmberSoftTile: Color
        @Composable get() = if (isSystemInDarkTheme()) ForensicsDarkColors.AmberSoftTile else ForensicsLightColors.AmberSoftTile
    val AmberIconBox: Color
        @Composable get() = if (isSystemInDarkTheme()) ForensicsDarkColors.AmberIconBox else ForensicsLightColors.AmberIconBox

    val RedPrimary: Color
        @Composable get() = if (isSystemInDarkTheme()) ForensicsDarkColors.RedPrimary else ForensicsLightColors.RedPrimary
    val RedBar: Color
        @Composable get() = if (isSystemInDarkTheme()) ForensicsDarkColors.RedBar else ForensicsLightColors.RedBar
    val RedContainer: Color
        @Composable get() = if (isSystemInDarkTheme()) ForensicsDarkColors.RedContainer else ForensicsLightColors.RedContainer
    val RedBorder: Color
        @Composable get() = if (isSystemInDarkTheme()) ForensicsDarkColors.RedBorder else ForensicsLightColors.RedBorder

    val GrayPrimary: Color
        @Composable get() = if (isSystemInDarkTheme()) ForensicsDarkColors.GrayPrimary else ForensicsLightColors.GrayPrimary
    val GrayContainer: Color
        @Composable get() = if (isSystemInDarkTheme()) ForensicsDarkColors.GrayContainer else ForensicsLightColors.GrayContainer
    val GrayBorder: Color
        @Composable get() = if (isSystemInDarkTheme()) ForensicsDarkColors.GrayBorder else ForensicsLightColors.GrayBorder
    val GraySoftTile: Color
        @Composable get() = if (isSystemInDarkTheme()) ForensicsDarkColors.GraySoftTile else ForensicsLightColors.GraySoftTile
}
