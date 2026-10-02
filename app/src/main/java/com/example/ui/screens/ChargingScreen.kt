package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ChargerProfileEntity
import com.example.data.ChargingSessionEntity
import com.example.data.LiveTelemetrySnapshot
import com.example.ui.ChargingSubTab
import com.example.ui.components.ChargerPlugGraphicIcon
import com.example.ui.components.ClassificationBadge
import com.example.ui.components.ForensicsCard
import com.example.ui.components.SegmentedPillSelector
import com.example.ui.theme.ForensicsPalette

@Composable
fun ChargingScreen(
    snapshot: LiveTelemetrySnapshot,
    subTab: ChargingSubTab,
    onSelectSubTab: (ChargingSubTab) -> Unit,
    chargingSessions: List<ChargingSessionEntity>,
    chargerProfiles: List<ChargerProfileEntity>,
    onAddChargerProfile: (String, Float, Float, Float) -> Unit,
    isDeepBenchmarkUnlocked: Boolean = false,
    onTriggerVideoAd2: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showAddChargerDialog by remember { mutableStateOf(false) }
    var showCompareChargersDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ForensicsPalette.ScreenBackground)
            .padding(horizontal = 16.dp)
            .testTag("charging_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Charging",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = ForensicsPalette.TextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Sessions, charger profiles, and health.",
                fontSize = 13.sp,
                color = ForensicsPalette.TextSecondary
            )
        }

        // Top Charging Status Card (Screenshots 5, 6, 7)
        item {
            val latestSession = chargingSessions.firstOrNull()
            val lastTimeText = when {
                snapshot.isCharging -> "Live session active"
                latestSession != null -> "Last: ${latestSession.dateTimeLabel}"
                else -> "No session recorded yet"
            }
            val avgPowerText = when {
                snapshot.liveChargingWatts != null -> String.format(java.util.Locale.US, "%.1fW", snapshot.liveChargingWatts)
                latestSession != null -> "${latestSession.avgPowerWatts}W"
                else -> "—"
            }
            val peakTempText = when {
                latestSession != null -> "${latestSession.peakTempCelsius}°C"
                snapshot.temperatureCelsius > 0f -> "${snapshot.temperatureCelsius}°C"
                else -> "—"
            }
            val lastDurText = when {
                snapshot.isCharging -> "Charging"
                latestSession != null -> latestSession.durationLabel
                else -> "—"
            }

            ForensicsCard {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "STATUS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.7.sp,
                                color = ForensicsPalette.TextSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = snapshot.chargingSource,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (snapshot.isCharging) ForensicsPalette.GreenPrimary else ForensicsPalette.TextPrimary
                            )
                        }
                        Text(
                            text = lastTimeText,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium,
                            color = ForensicsPalette.TextSecondary
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = ForensicsPalette.DividerColor, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ChargingTopStatColumn(
                            label = if (snapshot.isCharging) "Live power" else "Last avg power",
                            value = avgPowerText,
                            modifier = Modifier.weight(1f)
                        )
                        VerticalDivider(
                            color = ForensicsPalette.BorderSubtle,
                            thickness = 1.dp,
                            modifier = Modifier.fillMaxHeight()
                        )
                        ChargingTopStatColumn(
                            label = if (latestSession != null) "Peak temp" else "Live temp",
                            value = peakTempText,
                            modifier = Modifier.weight(1f)
                        )
                        VerticalDivider(
                            color = ForensicsPalette.BorderSubtle,
                            thickness = 1.dp,
                            modifier = Modifier.fillMaxHeight()
                        )
                        ChargingTopStatColumn(
                            label = "Last duration",
                            value = lastDurText,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // 3-Tab Segmented Control: History | Chargers | Health
        item {
            SegmentedPillSelector(
                options = listOf("History", "Chargers", "Health"),
                selectedIndex = when (subTab) {
                    ChargingSubTab.HISTORY -> 0
                    ChargingSubTab.CHARGERS -> 1
                    ChargingSubTab.HEALTH -> 2
                },
                onSelect = { idx ->
                    val target = when (idx) {
                        0 -> ChargingSubTab.HISTORY
                        1 -> ChargingSubTab.CHARGERS
                        else -> ChargingSubTab.HEALTH
                    }
                    onSelectSubTab(target)
                },
                activeTextColor = ForensicsPalette.AmberPrimary
            )
        }

        when (subTab) {
            ChargingSubTab.HISTORY -> {
                if (chargingSessions.isEmpty()) {
                    item {
                        ForensicsCard(containerColor = ForensicsPalette.GreenSoftTile) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Data collection in progress",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ForensicsPalette.GreenPrimary
                                    )
                                    ClassificationBadge(
                                        text = "Collecting History",
                                        containerColor = ForensicsPalette.GreenContainer,
                                        contentColor = ForensicsPalette.GreenPrimary
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Keep using your device and plug in your charger — charging curves, wattage profiles, and session history will generate automatically as charging sessions are recorded.",
                                    fontSize = 13.sp,
                                    color = ForensicsPalette.TextPrimary
                                )
                            }
                        }
                    }
                } else {
                    val latest = chargingSessions.first()
                    item {
                        ForensicsCard {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Latest · ${latest.dateTimeLabel}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = ForensicsPalette.TextSecondary
                                    )
                                    ClassificationBadge(
                                        text = "Estimated",
                                        containerColor = ForensicsPalette.GreenContainer,
                                        contentColor = ForensicsPalette.GreenPrimary
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (latest.avgPowerWatts > 0f) {
                                        "${latest.avgPowerWatts}W average (${latest.startPercent}% → ${latest.endPercent}%)"
                                    } else {
                                        "${latest.startPercent}% → ${latest.endPercent}% (${latest.durationLabel})"
                                    },
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ForensicsPalette.GreenPrimary
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                ChargingCurveChart(session = latest)
                            }
                        }
                    }

                    items(chargingSessions.size) { index ->
                        val session = chargingSessions[index]
                        ChargingSessionCard(session = session)
                    }
                }

                item { Spacer(modifier = Modifier.height(20.dp)) }
            }

            ChargingSubTab.CHARGERS -> {
                // Action Row for Compare Chargers & Add Profile
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val chipShape = RoundedCornerShape(8.dp)
                        if (chargerProfiles.size >= 2) {
                            Text(
                                text = "Compare Chargers ⇄",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = ForensicsPalette.BluePrimary,
                                modifier = Modifier
                                    .clip(chipShape)
                                    .background(ForensicsPalette.BlueSoftTile)
                                    .border(1.dp, ForensicsPalette.BlueBorder, chipShape)
                                    .clickable { showCompareChargersDialog = true }
                                    .padding(horizontal = 12.dp, vertical = 7.dp)
                            )
                        } else {
                            Spacer(modifier = Modifier.width(1.dp))
                        }
                        Text(
                            text = "+ New Charger Profile",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = ForensicsPalette.GreenPrimary,
                            modifier = Modifier
                                .clip(chipShape)
                                .background(ForensicsPalette.GreenSoftTile)
                                .border(1.dp, ForensicsPalette.GreenBorder, chipShape)
                                .clickable { showAddChargerDialog = true }
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                                .testTag("add_charger_profile_button")
                        )
                    }
                }

                if (chargerProfiles.isEmpty()) {
                    item {
                        ForensicsCard {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp)
                            ) {
                                Text(
                                    text = "No Charger Profiles Recorded Yet",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ForensicsPalette.TextPrimary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Keep using your phone and charge with your adapter to build charger fingerprints, or tap '+ New Charger Profile' above to save a charger.",
                                    fontSize = 13.sp,
                                    color = ForensicsPalette.TextSecondary
                                )
                            }
                        }
                    }
                } else {
                    items(chargerProfiles.size) { idx ->
                        ChargerProfileCard(profile = chargerProfiles[idx])
                    }
                }

                item { Spacer(modifier = Modifier.height(20.dp)) }
            }

            ChargingSubTab.HEALTH -> {
                val hasSessions = chargingSessions.isNotEmpty()
                // Deep Health & Thermal Stress Benchmark (Unlocked via Video Ad #2)
                item {
                    ForensicsCard(
                        containerColor = if (isDeepBenchmarkUnlocked) ForensicsPalette.GreenSoftTile else ForensicsPalette.PurpleContainer
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ClassificationBadge(
                                    text = if (isDeepBenchmarkUnlocked) "UNLOCKED" else "DEEP BENCHMARK",
                                    containerColor = if (isDeepBenchmarkUnlocked) ForensicsPalette.GreenContainer else ForensicsPalette.PurpleSoftTile,
                                    contentColor = if (isDeepBenchmarkUnlocked) ForensicsPalette.GreenPrimary else ForensicsPalette.PurplePrimary
                                )
                                Text(
                                    text = if (isDeepBenchmarkUnlocked) "Active" else "Session Locked",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDeepBenchmarkUnlocked) ForensicsPalette.GreenPrimary else ForensicsPalette.PurplePrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Deep C-Rate, Voltage Sag & Thermal Benchmark",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = ForensicsPalette.TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            if (!isDeepBenchmarkUnlocked) {
                                Text(
                                    text = "Unlock live C-rate charge stress, terminal voltage sag, and thermal throttle headroom metrics for this session.",
                                    fontSize = 12.sp,
                                    color = ForensicsPalette.TextSecondary
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = onTriggerVideoAd2,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("video_ad_2_charging_button")
                                ) {
                                    Text(
                                        text = "Unlock Benchmark",
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            } else {
                                val cRate = if (snapshot.designCapacityMah > 0) {
                                    kotlin.math.abs(snapshot.currentMilliAmps).toFloat() / snapshot.designCapacityMah.toFloat()
                                } else 0f
                                Text(
                                    text = String.format(
                                        java.util.Locale.US,
                                        "• Live C-Rate Stress: %.2fC (%d mA / %d mAh design)\n• Live Terminal Voltage: %.3f V (%s)\n• Thermal Headroom: %.1f°C below 40.0°C throttle threshold",
                                        cRate,
                                        snapshot.currentMilliAmps,
                                        snapshot.designCapacityMah,
                                        snapshot.voltageVolts,
                                        snapshot.currentStatus,
                                        (40.0f - snapshot.temperatureCelsius).coerceAtLeast(0f)
                                    ),
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = ForensicsPalette.TextPrimary,
                                    modifier = Modifier.testTag("unlocked_deep_benchmark_box")
                                )
                            }
                        }
                    }
                }
                item {
                    ChargingHealthItemCard(
                        accentColor = ForensicsPalette.GreenPrimary,
                        title = "Charging Speed",
                        badgeText = if (hasSessions || snapshot.isCharging) "Measured" else "Collecting",
                        badgeBg = ForensicsPalette.GreenContainer,
                        badgeTextColor = ForensicsPalette.GreenPrimary,
                        description = when {
                            snapshot.liveChargingWatts != null -> String.format(java.util.Locale.US, "Live charging at %.1fW (%s).", snapshot.liveChargingWatts, snapshot.chargingSource)
                            hasSessions -> "Avg ${chargingSessions.first().avgPowerWatts}W in latest recorded session."
                            else -> "Plug in your device to measure charging wattage and speed stability."
                        }
                    )
                }
                item {
                    val tempHigh = snapshot.temperatureCelsius >= 36.0f
                    ChargingHealthItemCard(
                        accentColor = if (tempHigh) ForensicsPalette.RedPrimary else ForensicsPalette.GreenPrimary,
                        title = "Temperature",
                        badgeText = snapshot.temperatureStatus,
                        badgeBg = if (tempHigh) ForensicsPalette.RedContainer else ForensicsPalette.GreenContainer,
                        badgeTextColor = if (tempHigh) ForensicsPalette.RedPrimary else ForensicsPalette.GreenPrimary,
                        description = "Current thermistor reading: ${snapshot.temperatureCelsius}°C (${snapshot.thermalStatusLabel})."
                    )
                }
                item {
                    ChargingHealthItemCard(
                        accentColor = ForensicsPalette.GreenPrimary,
                        title = "Interruptions",
                        badgeText = if (hasSessions) "None detected" else "Monitoring",
                        badgeBg = ForensicsPalette.GreenContainer,
                        badgeTextColor = ForensicsPalette.GreenPrimary,
                        description = if (hasSessions) {
                            "No unexpected interruptions across ${chargingSessions.size} recorded sessions."
                        } else {
                            "Keep using your device — charging interruption checks will generate during your next charge."
                        }
                    )
                }
                item {
                    val hasTaperCrossing = chargingSessions.any { it.startPercent < 80 && it.endPercent >= 80 }
                    ChargingHealthItemCard(
                        accentColor = ForensicsPalette.GreenPrimary,
                        title = "Curve Shape",
                        badgeText = when {
                            hasTaperCrossing -> "Measured"
                            hasSessions -> "Partial Cycle"
                            else -> "Calibrating"
                        },
                        badgeBg = ForensicsPalette.GreenContainer,
                        badgeTextColor = ForensicsPalette.GreenPrimary,
                        description = when {
                            hasTaperCrossing -> {
                                val s = chargingSessions.first { it.startPercent < 80 && it.endPercent >= 80 }
                                "Measured charge progression (${s.startPercent}% → ${s.endPercent}% in ${s.durationLabel}) across the 80% CC/CV threshold."
                            }
                            hasSessions -> {
                                val s = chargingSessions.first()
                                "Latest recorded charge (${s.startPercent}% → ${s.endPercent}%) did not cross the 80%–100% CV taper window."
                            }
                            else -> "Requires a charging session crossing 80% → 100% to evaluate constant-current / constant-voltage taper."
                        }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Hardware failure cannot be diagnosed without sufficient evidence.",
                        fontSize = 12.sp,
                        color = ForensicsPalette.TextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }

    if (showAddChargerDialog) {
        val liveWattsStr = snapshot.liveChargingWatts?.let { String.format(java.util.Locale.US, "%.1f", it) } ?: ""
        val liveTempStr = if (snapshot.temperatureCelsius > 0f) {
            String.format(java.util.Locale.US, "%.1f", snapshot.temperatureCelsius)
        } else ""
        var name by remember {
            mutableStateOf(
                if (snapshot.isCharging) snapshot.chargingSource else ""
            )
        }
        var maxW by remember { mutableStateOf(liveWattsStr) }
        var avgW by remember { mutableStateOf(liveWattsStr) }
        var temp by remember { mutableStateOf(liveTempStr) }

        AlertDialog(
            onDismissRequest = { showAddChargerDialog = false },
            title = { Text("Add Charger Profile", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Charger / Cable Label") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = maxW,
                        onValueChange = { maxW = it },
                        label = { Text("Max Observed Power (W)") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = avgW,
                        onValueChange = { avgW = it },
                        label = { Text("Average Power (W)") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = temp,
                        onValueChange = { temp = it },
                        label = { Text("Average Temperature (°C)") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val parsedMax = maxW.toFloatOrNull() ?: (snapshot.liveChargingWatts ?: 0f)
                    val parsedAvg = avgW.toFloatOrNull() ?: parsedMax
                    val parsedTemp = temp.toFloatOrNull() ?: snapshot.temperatureCelsius
                    onAddChargerProfile(
                        name.ifBlank { "Charger (${snapshot.chargingSource})" },
                        parsedMax,
                        parsedAvg,
                        parsedTemp
                    )
                    showAddChargerDialog = false
                }) {
                    Text("Save Profile")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddChargerDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showCompareChargersDialog && chargerProfiles.size >= 2) {
        val c1 = chargerProfiles[0]
        val c2 = chargerProfiles.getOrElse(2) { chargerProfiles[1] }
        AlertDialog(
            onDismissRequest = { showCompareChargersDialog = false },
            title = { Text("${c1.name} vs ${c2.name}", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "All comparisons are based strictly on recorded voltage × current measurements:",
                        fontSize = 12.sp,
                        color = ForensicsPalette.TextSecondary
                    )
                    Text("• Max Power: ${c1.maxObservedWatts}W vs ${c2.maxObservedWatts}W", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Text("• Avg Power: ${c1.avgPowerWatts}W vs ${c2.avgPowerWatts}W", fontSize = 13.sp)
                    Text("• Avg Temp: ${c1.avgTempCelsius}°C vs ${c2.avgTempCelsius}°C", fontSize = 13.sp)
                    Text("• Recorded Sessions: ${c1.sessionsCount} vs ${c2.sessionsCount}", fontSize = 13.sp)
                    Text(
                        text = "Observation: ${c2.name} delivered lower wattage (${c2.avgPowerWatts}W) while running +${String.format("%.1f", c2.avgTempCelsius - c1.avgTempCelsius)}°C warmer.",
                        fontSize = 12.sp,
                        color = ForensicsPalette.AmberPrimary
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showCompareChargersDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
private fun ChargingTopStatColumn(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = ForensicsPalette.TextSecondary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = ForensicsPalette.TextPrimary
        )
    }
}

@Composable
private fun ChargingCurveChart(session: ChargingSessionEntity) {
    val startPct = session.startPercent.coerceIn(0, 100)
    val endPct = session.endPercent.coerceIn(startPct, 100)
    val crossedTaper = startPct < 80 && endPct > 80
    val taperFractionX = if (crossedTaper && endPct > startPct) {
        ((80f - startPct) / (endPct - startPct).toFloat()).coerceIn(0.1f, 0.9f)
    } else null

    // Parse real minutes from session.durationLabel (e.g., "1h 20m" or "15m")
    val hrs = Regex("(\\d+)h").find(session.durationLabel)?.groupValues?.getOrNull(1)?.toIntOrNull() ?: 0
    val mins = Regex("(\\d+)m").find(session.durationLabel)?.groupValues?.getOrNull(1)?.toIntOrNull() ?: 0
    val totalMins = ((hrs * 60) + mins).coerceAtLeast(1)
    val xLabels = (0..4).map { step -> "${(totalMins * step) / 4}m" }

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(142.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(ForensicsPalette.SubtleSurface)
                .border(1.dp, ForensicsPalette.BorderSubtle, RoundedCornerShape(14.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height - 6.dp.toPx()
                val dashGrid = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)

                // Horizontal grid lines for 100%, 50%, 0%
                listOf(0.06f, 0.50f, 0.94f).forEach { frac ->
                    drawLine(
                        color = ForensicsPalette.BorderStrong,
                        start = Offset(0f, h * frac),
                        end = Offset(w, h * frac),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = dashGrid
                    )
                }

                fun pctToY(pct: Float): Float {
                    val normalized = (pct / 100f).coerceIn(0f, 1f)
                    return h * (0.94f - normalized * 0.88f)
                }

                val pts = (0..4).map { idx ->
                    val frac = idx / 4f
                    val pctAtStep = startPct + (endPct - startPct) * frac
                    Offset(w * frac, pctToY(pctAtStep))
                }

                val area = Path().apply {
                    moveTo(pts.first().x, h)
                    pts.forEach { lineTo(it.x, it.y) }
                    lineTo(pts.last().x, h)
                    close()
                }
                drawPath(
                    path = area,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            ForensicsPalette.GreenGauge.copy(alpha = 0.35f),
                            ForensicsPalette.GreenSoftTile.copy(alpha = 0.08f)
                        )
                    )
                )

                val curve = Path().apply {
                    pts.forEachIndexed { idx, offset ->
                        if (idx == 0) moveTo(offset.x, offset.y) else lineTo(offset.x, offset.y)
                    }
                }
                drawPath(
                    path = curve,
                    color = ForensicsPalette.GreenGauge,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )

                if (taperFractionX != null) {
                    val taperX = w * taperFractionX
                    drawLine(
                        color = ForensicsPalette.AmberPrimary,
                        start = Offset(taperX, pctToY(80f)),
                        end = Offset(taperX, h),
                        strokeWidth = 1.8.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 7f), 0f)
                    )
                }

                pts.forEach { pt ->
                    val r = 3.8.dp.toPx()
                    drawCircle(color = Color.White, radius = r + 2.dp.toPx(), center = pt)
                    drawCircle(
                        color = ForensicsPalette.GreenGauge,
                        radius = r,
                        center = pt
                    )
                }
            }

            // Y-axis labels
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(bottom = 8.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text("100%", fontSize = 10.sp, fontWeight = FontWeight.Medium, color = ForensicsPalette.TextSecondary)
                Text("50%", fontSize = 10.sp, fontWeight = FontWeight.Medium, color = ForensicsPalette.TextSecondary)
                Text("0%", fontSize = 10.sp, fontWeight = FontWeight.Medium, color = ForensicsPalette.TextSecondary)
            }

            if (taperFractionX != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 6.dp, end = 34.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(ForensicsPalette.AmberPill)
                        .border(1.dp, ForensicsPalette.AmberBorder, RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "80% threshold",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = ForensicsPalette.AmberPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            xLabels.forEach { label ->
                Text(
                    text = label,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium,
                    color = ForensicsPalette.TextSecondary
                )
            }
        }
    }
}

@Composable
private fun ChargingSessionCard(session: ChargingSessionEntity) {
    val cardBg = if (session.isSlowBadge) ForensicsPalette.AmberInnerCard else ForensicsPalette.CardSurface
    val tempColor = if (session.isElevatedTemp) ForensicsPalette.RedPrimary else ForensicsPalette.TextPrimary

    ForensicsCard(containerColor = cardBg) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = session.dateTimeLabel,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = ForensicsPalette.TextPrimary
                    )
                    if (session.isSlowBadge) {
                        Spacer(modifier = Modifier.width(8.dp))
                        ClassificationBadge(
                            text = "Slow",
                            containerColor = ForensicsPalette.AmberPill,
                            contentColor = ForensicsPalette.AmberPrimary
                        )
                    }
                }
                Text(
                    text = "${session.startPercent}% → ${session.endPercent}%",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = ForensicsPalette.TextPrimary
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = session.chargerName,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    color = ForensicsPalette.TextSecondary
                )
                Text(
                    text = session.durationLabel,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    color = ForensicsPalette.TextSecondary
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(28.dp)) {
                Column {
                    Text(
                        text = "AVG POWER",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        color = ForensicsPalette.TextSecondary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${session.avgPowerWatts}W",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = ForensicsPalette.TextPrimary
                    )
                }
                Column {
                    Text(
                        text = "PEAK TEMP",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        color = ForensicsPalette.TextSecondary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${session.peakTempCelsius}°C",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = tempColor
                    )
                }
            }
        }
    }
}

@Composable
private fun ChargerProfileCard(profile: ChargerProfileEntity) {
    val (accentColor, softBg, softBorder) = when (profile.colorTheme) {
        "green" -> Triple(ForensicsPalette.GreenPrimary, ForensicsPalette.GreenSoftTile, ForensicsPalette.GreenBorder)
        "blue" -> Triple(ForensicsPalette.BlueBright, ForensicsPalette.BlueSoftTile, ForensicsPalette.BlueBorder)
        else -> Triple(ForensicsPalette.AmberPrimary, ForensicsPalette.AmberContainer, ForensicsPalette.AmberBorder)
    }

    ForensicsCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ChargerPlugGraphicIcon(
                        accentColor = accentColor,
                        containerColor = softBg
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = profile.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = ForensicsPalette.TextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${profile.sessionsCount} sessions",
                            fontSize = 12.sp,
                            color = ForensicsPalette.TextSecondary
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${profile.maxObservedWatts}W",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                    Text(
                        text = "max observed",
                        fontSize = 11.sp,
                        color = ForensicsPalette.TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            // Relative wattage bar (scaled to 30W max) with bordered track
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape)
                    .background(ForensicsPalette.SubtleSurfaceAlt)
                    .border(0.8.dp, ForensicsPalette.BorderStrong, CircleShape)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth((profile.maxObservedWatts / 30f).coerceIn(0.15f, 1f))
                        .height(8.dp)
                        .clip(CircleShape)
                        .background(accentColor)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val subShape = RoundedCornerShape(12.dp)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(subShape)
                        .background(softBg)
                        .border(1.dp, softBorder, subShape)
                        .padding(12.dp)
                ) {
                    Text(
                        text = "AVG POWER",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        color = accentColor
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${profile.avgPowerWatts}W",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(subShape)
                        .background(ForensicsPalette.SubtleSurface)
                        .border(1.dp, ForensicsPalette.BorderStrong, subShape)
                        .padding(12.dp)
                ) {
                    Text(
                        text = "AVG TEMP",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        color = ForensicsPalette.TextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${profile.avgTempCelsius}°C",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = ForensicsPalette.TextPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun ChargingHealthItemCard(
    accentColor: Color,
    title: String,
    badgeText: String,
    badgeBg: Color,
    badgeTextColor: Color,
    description: String
) {
    ForensicsCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .fillMaxHeight()
                    .background(accentColor)
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = ForensicsPalette.TextPrimary
                    )
                    ClassificationBadge(
                        text = badgeText,
                        containerColor = badgeBg,
                        contentColor = badgeTextColor
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = description,
                    fontSize = 13.sp,
                    color = ForensicsPalette.TextSecondary
                )
            }
        }
    }
}
