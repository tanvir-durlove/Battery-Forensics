package com.example.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

// 1.0.2: the palette resolves against the system theme, so the app follows
// dark mode instead of staying bright white at night. Light values are the
// original 1.0.1 palette, unchanged. Dark values keep the same accents at
// mid-tone saturation: light enough to read as text on dark cards, dark
// enough for the few places an accent is used as a background (e.g. the
// green status banner in MainActivity, which keeps hardcoded white text).
//
// The palette is plain state, NOT a CompositionLocal: CompositionLocal.current
// is a composable getter, so palette reads through it were composable
// invocations that broke compilation in the top-level Material3 schemes
// (Theme.kt) and inside every Canvas onDraw lambda. A `var colors` backed by
// mutableStateOf is plain: reads compile anywhere (composable bodies, Canvas
// lambdas, top-level vals), and reads inside composable bodies track the
// state, so a runtime theme change recomposes. MyApplicationTheme syncs the
// palette with `remember(darkTheme) { ForensicsPalette.setDark(darkTheme) }`.

internal interface ForensicsColors {
    val ScreenBackground: Color
    val CardSurface: Color
    val SubtleSurface: Color
    val SubtleSurfaceAlt: Color
    val BorderSubtle: Color
    val BorderStrong: Color
    val DividerColor: Color
    val TextPrimary: Color
    val TextSecondary: Color
    val TextMuted: Color
    val GreenPrimary: Color
    val GreenGauge: Color
    val GreenContainer: Color
    val GreenBorder: Color
    val GreenTrack: Color
    val GreenSoftTile: Color
    val BluePrimary: Color
    val BlueBright: Color
    val BlueContainer: Color
    val BlueBorder: Color
    val BlueSoftTile: Color
    val BlueChartLine: Color
    val BlueBarLight: Color
    val PurplePrimary: Color
    val PurpleBright: Color
    val PurpleContainer: Color
    val PurpleBorder: Color
    val PurpleSoftTile: Color
    val PurpleDeepButton: Color
    val AmberPrimary: Color
    val AmberDarkText: Color
    val TemperatureOrange: Color
    val AmberContainer: Color
    val AmberBorder: Color
    val AmberInnerCard: Color
    val AmberPill: Color
    val AmberSoftTile: Color
    val AmberIconBox: Color
    val RedPrimary: Color
    val RedBar: Color
    val RedContainer: Color
    val RedBorder: Color
    val GrayPrimary: Color
    val GrayContainer: Color
    val GrayBorder: Color
    val GraySoftTile: Color
}

internal object ForensicsLightColors : ForensicsColors {
    // Distinct cool slate-gray background so pure white cards pop with high contrast
    override val ScreenBackground = Color(0xFFEAEEF4)
    override val CardSurface = Color(0xFFFFFFFF)
    override val SubtleSurface = Color(0xFFF0F3F8)
    override val SubtleSurfaceAlt = Color(0xFFE3E8F0)
    override val BorderSubtle = Color(0xFFD8E0EB)
    override val BorderStrong = Color(0xFFC8D2E0)
    override val DividerColor = Color(0xFFE4E9F1)

    override val TextPrimary = Color(0xFF14181F)
    override val TextSecondary = Color(0xFF4E5663)
    override val TextMuted = Color(0xFF7B8492)

    // Forensic Green (Home, Normal, Good, Measured Health)
    override val GreenPrimary = Color(0xFF146336)
    override val GreenGauge = Color(0xFF196E3E)
    override val GreenContainer = Color(0xFFD5F2E3)
    override val GreenBorder = Color(0xFFA8E0C2)
    override val GreenTrack = Color(0xFFD2EDE0)
    override val GreenSoftTile = Color(0xFFE2F6EC)

    // Diagnostic Blue (Diagnose, Strong Evidence, Voltage)
    override val BluePrimary = Color(0xFF0C4F8E)
    override val BlueBright = Color(0xFF125EB8)
    override val BlueContainer = Color(0xFFD0EAFC)
    override val BlueBorder = Color(0xFFA4D4F7)
    override val BlueSoftTile = Color(0xFFE6F2FD)
    override val BlueChartLine = Color(0xFF0D47A1)
    override val BlueBarLight = Color(0xFFB5DCFA)

    // Insights Purple (Insights, Current, Controlled Experiment)
    override val PurplePrimary = Color(0xFF561D94)
    override val PurpleBright = Color(0xFF631794)
    override val PurpleContainer = Color(0xFFEADBFB)
    override val PurpleBorder = Color(0xFFCFB0F7)
    override val PurpleSoftTile = Color(0xFFF2E8FE)
    override val PurpleDeepButton = Color(0xFF451285)

    // Amber / Orange (Charging, Temperature, Anomaly)
    override val AmberPrimary = Color(0xFF7D4300)
    override val AmberDarkText = Color(0xFF442400)
    override val TemperatureOrange = Color(0xFFAD4B00)
    override val AmberContainer = Color(0xFFFCE2BA)
    override val AmberBorder = Color(0xFFF3C684)
    override val AmberInnerCard = Color(0xFFFFF6E8)
    override val AmberPill = Color(0xFFFADCA8)
    override val AmberSoftTile = Color(0xFFFEF3D6)
    override val AmberIconBox = Color(0xFF753E00)

    // Red / Alert (Anomaly Drain, High Temp, Delete, Unavailable X)
    override val RedPrimary = Color(0xFFB43210)
    override val RedBar = Color(0xFFC83727)
    override val RedContainer = Color(0xFFFCE2E2)
    override val RedBorder = Color(0xFFF3B8B8)

    // Neutral Gray (Settings, Doze Inactive, Possible)
    override val GrayPrimary = Color(0xFF343940)
    override val GrayContainer = Color(0xFFDCE1E8)
    override val GrayBorder = Color(0xFFCBD2DC)
    override val GraySoftTile = Color(0xFFEEF1F6)
}

internal object ForensicsDarkColors : ForensicsColors {
    override val ScreenBackground = Color(0xFF10141A)
    override val CardSurface = Color(0xFF1B212B)
    override val SubtleSurface = Color(0xFF232B37)
    override val SubtleSurfaceAlt = Color(0xFF2A3341)
    override val BorderSubtle = Color(0xFF323D4D)
    override val BorderStrong = Color(0xFF415062)
    override val DividerColor = Color(0xFF2A3341)

    override val TextPrimary = Color(0xFFE9EDF2)
    override val TextSecondary = Color(0xFFB4BECD)
    override val TextMuted = Color(0xFF8A94A6)

    // Forensic Green — mid-tone so it reads as text on dark cards
    override val GreenPrimary = Color(0xFF34A853)
    override val GreenGauge = Color(0xFF3D9E66)
    override val GreenContainer = Color(0xFF1E3A2B)
    override val GreenBorder = Color(0xFF2E5C40)
    override val GreenTrack = Color(0xFF24402F)
    override val GreenSoftTile = Color(0xFF1B2E22)

    // Diagnostic Blue
    override val BluePrimary = Color(0xFF4A90D9)
    override val BlueBright = Color(0xFF7FB6EC)
    override val BlueContainer = Color(0xFF1B3049)
    override val BlueBorder = Color(0xFF2C4A6B)
    override val BlueSoftTile = Color(0xFF18293D)
    override val BlueChartLine = Color(0xFF6FA8DC)
    override val BlueBarLight = Color(0xFF2E4A66)

    // Insights Purple
    override val PurplePrimary = Color(0xFFA06CD5)
    override val PurpleBright = Color(0xFFC79BF5)
    override val PurpleContainer = Color(0xFF2E2145)
    override val PurpleBorder = Color(0xFF4A3480)
    override val PurpleSoftTile = Color(0xFF281E3A)
    override val PurpleDeepButton = Color(0xFF5A2E9E)

    // Amber / Orange
    override val AmberPrimary = Color(0xFFE8A33D)
    override val AmberDarkText = Color(0xFFFFD9A0)
    override val TemperatureOrange = Color(0xFFF08A3C)
    override val AmberContainer = Color(0xFF3A2A14)
    override val AmberBorder = Color(0xFF5C451F)
    override val AmberInnerCard = Color(0xFF2A2118)
    override val AmberPill = Color(0xFF3D2F16)
    override val AmberSoftTile = Color(0xFF2E2616)
    override val AmberIconBox = Color(0xFF7A4E12)

    // Red / Alert
    override val RedPrimary = Color(0xFFE57373)
    override val RedBar = Color(0xFFD95D4A)
    override val RedContainer = Color(0xFF3D1E1A)
    override val RedBorder = Color(0xFF5C2E28)

    // Neutral Gray
    override val GrayPrimary = Color(0xFFC7CDD6)
    override val GrayContainer = Color(0xFF2A2F37)
    override val GrayBorder = Color(0xFF3A414D)
    override val GraySoftTile = Color(0xFF20252D)
}

object ForensicsPalette {
    // Backed by snapshot state: plain reads everywhere, recomposition on change.
    internal var colors: ForensicsColors by mutableStateOf(ForensicsLightColors)

    fun setDark(dark: Boolean) {
        colors = if (dark) ForensicsDarkColors else ForensicsLightColors
    }

    val ScreenBackground: Color get() = colors.ScreenBackground
    val CardSurface: Color get() = colors.CardSurface
    val SubtleSurface: Color get() = colors.SubtleSurface
    val SubtleSurfaceAlt: Color get() = colors.SubtleSurfaceAlt
    val BorderSubtle: Color get() = colors.BorderSubtle
    val BorderStrong: Color get() = colors.BorderStrong
    val DividerColor: Color get() = colors.DividerColor
    val TextPrimary: Color get() = colors.TextPrimary
    val TextSecondary: Color get() = colors.TextSecondary
    val TextMuted: Color get() = colors.TextMuted
    val GreenPrimary: Color get() = colors.GreenPrimary
    val GreenGauge: Color get() = colors.GreenGauge
    val GreenContainer: Color get() = colors.GreenContainer
    val GreenBorder: Color get() = colors.GreenBorder
    val GreenTrack: Color get() = colors.GreenTrack
    val GreenSoftTile: Color get() = colors.GreenSoftTile
    val BluePrimary: Color get() = colors.BluePrimary
    val BlueBright: Color get() = colors.BlueBright
    val BlueContainer: Color get() = colors.BlueContainer
    val BlueBorder: Color get() = colors.BlueBorder
    val BlueSoftTile: Color get() = colors.BlueSoftTile
    val BlueChartLine: Color get() = colors.BlueChartLine
    val BlueBarLight: Color get() = colors.BlueBarLight
    val PurplePrimary: Color get() = colors.PurplePrimary
    val PurpleBright: Color get() = colors.PurpleBright
    val PurpleContainer: Color get() = colors.PurpleContainer
    val PurpleBorder: Color get() = colors.PurpleBorder
    val PurpleSoftTile: Color get() = colors.PurpleSoftTile
    val PurpleDeepButton: Color get() = colors.PurpleDeepButton
    val AmberPrimary: Color get() = colors.AmberPrimary
    val AmberDarkText: Color get() = colors.AmberDarkText
    val TemperatureOrange: Color get() = colors.TemperatureOrange
    val AmberContainer: Color get() = colors.AmberContainer
    val AmberBorder: Color get() = colors.AmberBorder
    val AmberInnerCard: Color get() = colors.AmberInnerCard
    val AmberPill: Color get() = colors.AmberPill
    val AmberSoftTile: Color get() = colors.AmberSoftTile
    val AmberIconBox: Color get() = colors.AmberIconBox
    val RedPrimary: Color get() = colors.RedPrimary
    val RedBar: Color get() = colors.RedBar
    val RedContainer: Color get() = colors.RedContainer
    val RedBorder: Color get() = colors.RedBorder
    val GrayPrimary: Color get() = colors.GrayPrimary
    val GrayContainer: Color get() = colors.GrayContainer
    val GrayBorder: Color get() = colors.GrayBorder
    val GraySoftTile: Color get() = colors.GraySoftTile
}
