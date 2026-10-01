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
                            text = "Last: Today 8:14 AM",
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
                            label = "Last avg power",
                            value = "18W",
                            modifier = Modifier.weight(1f)
                        )
                        VerticalDivider(
                            color = ForensicsPalette.BorderSubtle,
                            thickness = 1.dp,
                            modifier = Modifier.fillMaxHeight()
                        )
                        ChargingTopStatColumn(
                            label = "Peak temp",
                            value = "31.2°C",
                            modifier = Modifier.weight(1f)
                        )
                        VerticalDivider(
                            color = ForensicsPalette.BorderSubtle,
                            thickness = 1.dp,
                            modifier = Modifier.fillMaxHeight()
                        )
                        ChargingTopStatColumn(
                            label = "Last duration",
                            value = "1h 42m",
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
                // Latest · Today 8:14 AM Charging Curve Card (Screenshot 5)
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
                                    text = "Latest · Today 8:14 AM",
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
                                text = "18.4W average",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = ForensicsPalette.GreenPrimary
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            ChargingCurveChart()
                        }
                    }
                }

                // Session History Cards (Screenshot 5)
                items(chargingSessions.size) { index ->
                    val session = chargingSessions[index]
                    ChargingSessionCard(session = session)
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

                // Charger Profile Cards (Screenshot 6)
                items(chargerProfiles.size) { idx ->
                    ChargerProfileCard(profile = chargerProfiles[idx])
                }

                item { Spacer(modifier = Modifier.height(20.dp)) }
            }

            ChargingSubTab.HEALTH -> {
                // 4 Charging Health Cards with left colored vertical bar (Screenshot 7)
                item {
                    ChargingHealthItemCard(
                        accentColor = ForensicsPalette.GreenPrimary,
                        title = "Charging Speed",
                        badgeText = "Normal",
                        badgeBg = ForensicsPalette.GreenContainer,
                        badgeTextColor = ForensicsPalette.GreenPrimary,
                        description = "Avg 19.2W with Pixel 30W · Consistent with fast-charge profile."
                    )
                }
                item {
                    ChargingHealthItemCard(
                        accentColor = ForensicsPalette.RedPrimary,
                        title = "Temperature",
                        badgeText = "Elevated on Mon",
                        badgeBg = ForensicsPalette.RedContainer,
                        badgeTextColor = ForensicsPalette.RedPrimary,
                        description = "32.1°C peak with unknown charger — within safe range but above 30.2°C average."
                    )
                }
                item {
                    ChargingHealthItemCard(
                        accentColor = ForensicsPalette.GreenPrimary,
                        title = "Interruptions",
                        badgeText = "None detected",
                        badgeBg = ForensicsPalette.GreenContainer,
                        badgeTextColor = ForensicsPalette.GreenPrimary,
                        description = "No unexpected interruptions in the last 28 sessions."
                    )
                }
                item {
                    ChargingHealthItemCard(
                        accentColor = ForensicsPalette.GreenPrimary,
                        title = "Curve Shape",
                        badgeText = "Normal",
                        badgeBg = ForensicsPalette.GreenContainer,
                        badgeTextColor = ForensicsPalette.GreenPrimary,
                        description = "Expected 80% taper observed consistently. No unexpected shape changes."
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
        var name by remember { mutableStateOf("Car USB-C PD 25W") }
        var maxW by remember { mutableStateOf("21.5") }
        var avgW by remember { mutableStateOf("17.8") }
        var temp by remember { mutableStateOf("30.6") }

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
                    onAddChargerProfile(
                        name.ifBlank { "Custom Charger" },
                        maxW.toFloatOrNull() ?: 18.0f,
                        avgW.toFloatOrNull() ?: 15.0f,
                        temp.toFloatOrNull() ?: 30.0f
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
private fun ChargingCurveChart() {
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

                // Curve points from 25% at 0m -> 52% at 20m -> 72% at 40m -> 88% at 64m -> 100% at 80m
                val pts = listOf(
                    Offset(0f, h * 0.72f),
                    Offset(w * 0.25f, h * 0.45f),
                    Offset(w * 0.52f, h * 0.28f),
                    Offset(w * 0.76f, h * 0.14f),
                    Offset(w, h * 0.05f)
                )

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

                // Dashed vertical 80% taper marker
                val taperX = w * 0.76f
                drawLine(
                    color = ForensicsPalette.AmberPrimary,
                    start = Offset(taperX, h * 0.05f),
                    end = Offset(taperX, h),
                    strokeWidth = 1.8.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 7f), 0f)
                )

                // Milestone dots along curve
                pts.forEachIndexed { idx, pt ->
                    val isTaper = idx == 3
                    val r = if (isTaper) 5.5.dp.toPx() else 3.8.dp.toPx()
                    drawCircle(color = Color.White, radius = r + 2.dp.toPx(), center = pt)
                    drawCircle(
                        color = if (isTaper) ForensicsPalette.AmberPrimary else ForensicsPalette.GreenGauge,
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

            // 80% Taper badge near dashed line
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
                    text = "80% taper",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = ForensicsPalette.AmberPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf("0m", "20m", "40m", "60m", "80m").forEach { label ->
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
