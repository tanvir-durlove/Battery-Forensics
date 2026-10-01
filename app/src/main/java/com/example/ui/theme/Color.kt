package com.example.ui.theme

import androidx.compose.ui.graphics.Color

object ForensicsPalette {
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
