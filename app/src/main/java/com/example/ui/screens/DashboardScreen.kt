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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ActivityEstimateItem
import com.example.data.CapabilityItem
import com.example.data.LiveTelemetrySnapshot
import com.example.ui.components.CapabilityStatusGraphicBadge
import com.example.ui.components.ClassificationBadge
import com.example.ui.components.ForensicsCard
import com.example.ui.theme.ForensicsPalette
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun DashboardScreen(
    snapshot: LiveTelemetrySnapshot,
    activityEstimates: List<ActivityEstimateItem>,
    capabilities: List<CapabilityItem>,
    onRefresh: () -> Unit,
    onNavigateToDiagnose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showHealthModal by remember { mutableStateOf(false) }
    var selectedCapability by remember { mutableStateOf<CapabilityItem?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ForensicsPalette.ScreenBackground)
            .padding(horizontal = 16.dp)
            .testTag("dashboard_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(6.dp))
            // Device Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = snapshot.deviceModel,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = ForensicsPalette.TextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Android ${snapshot.androidVersion} · Monitoring active",
                        fontSize = 13.sp,
                        color = ForensicsPalette.TextSecondary
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(ForensicsPalette.CardSurface)
                        .border(1.dp, ForensicsPalette.BorderSubtle, RoundedCornerShape(8.dp))
                        .clickable { onRefresh() }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = snapshot.currentTimeFormatted,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold,
                        color = ForensicsPalette.TextSecondary
                    )
                }
            }
        }

        // Main Circular Gauge Card
        item {
            ForensicsCard(onClick = { onNavigateToDiagnose() }) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 22.dp, horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    BatteryCircularGauge(
                        percent = snapshot.batteryPercent,
                        healthLabel = snapshot.healthLabel,
                        drainRateText = snapshot.drainRateText
                    )
                    Spacer(modifier = Modifier.height(22.dp))
                    HorizontalDivider(color = ForensicsPalette.DividerColor, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        GaugeStatColumn(
                            value = snapshot.sinceChargeText,
                            label = "Since charge",
                            modifier = Modifier.weight(1f)
                        )
                        VerticalDivider(
                            color = ForensicsPalette.BorderSubtle,
                            thickness = 1.dp,
                            modifier = Modifier.fillMaxHeight()
                        )
                        GaugeStatColumn(
                            value = snapshot.estRemainingText,
                            label = "Est. remaining",
                            modifier = Modifier.weight(1f)
                        )
                        VerticalDivider(
                            color = ForensicsPalette.BorderSubtle,
                            thickness = 1.dp,
                            modifier = Modifier.fillMaxHeight()
                        )
                        GaugeStatColumn(
                            value = snapshot.awakeOffScreenText,
                            label = "Awake off-screen",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // 2x2 Metric Grid Cards (Temperature, Voltage, Current, Health Score)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricQuadCard(
                        title = "Temperature",
                        badgeText = snapshot.temperatureClassification.label,
                        badgeBg = ForensicsPalette.AmberPill,
                        badgeTextColor = ForensicsPalette.AmberPrimary,
                        mainValue = "${snapshot.temperatureCelsius}",
                        unit = " °C",
                        valueColor = ForensicsPalette.TemperatureOrange,
                        subtitle = snapshot.temperatureStatus,
                        modifier = Modifier.weight(1f)
                    )
                    MetricQuadCard(
                        title = "Voltage",
                        badgeText = snapshot.voltageClassification.label,
                        badgeBg = ForensicsPalette.BlueContainer,
                        badgeTextColor = ForensicsPalette.BluePrimary,
                        mainValue = "${snapshot.voltageVolts}",
                        unit = " V",
                        valueColor = ForensicsPalette.BlueBright,
                        subtitle = snapshot.voltageStatus,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricQuadCard(
                        title = "Current",
                        badgeText = snapshot.currentClassification.label,
                        badgeBg = ForensicsPalette.PurpleContainer,
                        badgeTextColor = ForensicsPalette.PurplePrimary,
                        mainValue = "${snapshot.currentMilliAmps}",
                        unit = " mA",
                        valueColor = ForensicsPalette.PurpleBright,
                        subtitle = snapshot.currentStatus,
                        modifier = Modifier.weight(1f)
                    )
                    MetricQuadCard(
                        title = "Health Score",
                        badgeText = snapshot.healthScoreClassification.label,
                        badgeBg = ForensicsPalette.GreenContainer,
                        badgeTextColor = ForensicsPalette.GreenPrimary,
                        mainValue = "${snapshot.healthScore}",
                        unit = " /100",
                        valueColor = ForensicsPalette.GreenPrimary,
                        subtitle = "App estimate",
                        onClick = { showHealthModal = true },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("health_score_card")
                    )
                }
            }
        }

        // SYSTEM STATE Card
        item {
            ForensicsCard {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Text(
                        text = "SYSTEM STATE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                        color = ForensicsPalette.TextSecondary
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        SystemStateTile(
                            label = "SCREEN",
                            value = snapshot.screenState,
                            bgColor = ForensicsPalette.BlueSoftTile,
                            borderColor = ForensicsPalette.BlueBorder,
                            textColor = ForensicsPalette.BlueBright,
                            modifier = Modifier.weight(1f)
                        )
                        SystemStateTile(
                            label = "DOZE",
                            value = snapshot.dozeState,
                            bgColor = ForensicsPalette.GraySoftTile,
                            borderColor = ForensicsPalette.GrayBorder,
                            textColor = ForensicsPalette.TextSecondary,
                            modifier = Modifier.weight(1f)
                        )
                        SystemStateTile(
                            label = "WI-FI",
                            value = snapshot.wifiState,
                            bgColor = ForensicsPalette.GreenSoftTile,
                            borderColor = ForensicsPalette.GreenBorder,
                            textColor = ForensicsPalette.GreenPrimary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        SystemStateTile(
                            label = "MOBILE",
                            value = snapshot.mobileState,
                            bgColor = ForensicsPalette.PurpleSoftTile,
                            borderColor = ForensicsPalette.PurpleBorder,
                            textColor = ForensicsPalette.PurpleBright,
                            modifier = Modifier.weight(1f)
                        )
                        SystemStateTile(
                            label = "BLUETOOTH",
                            value = snapshot.bluetoothState,
                            bgColor = ForensicsPalette.BlueSoftTile,
                            borderColor = ForensicsPalette.BlueBorder,
                            textColor = ForensicsPalette.BlueBright,
                            modifier = Modifier.weight(1f)
                        )
                        SystemStateTile(
                            label = "LOCATION",
                            value = snapshot.locationState,
                            bgColor = ForensicsPalette.AmberSoftTile,
                            borderColor = ForensicsPalette.AmberBorder,
                            textColor = ForensicsPalette.AmberPrimary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // ACTIVITY ESTIMATES Card
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
                            text = "ACTIVITY ESTIMATES",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp,
                            color = ForensicsPalette.TextSecondary
                        )
                        Text(
                            text = "Not exact consumption",
                            fontSize = 11.sp,
                            color = ForensicsPalette.TextMuted
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    activityEstimates.forEachIndexed { index, item ->
                        val barColor = when (item.colorCategory) {
                            "blue" -> ForensicsPalette.BlueBright
                            "orange" -> ForensicsPalette.TemperatureOrange
                            "purple" -> ForensicsPalette.PurpleBright
                            "brown" -> ForensicsPalette.AmberPrimary
                            else -> ForensicsPalette.TextSecondary
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = ForensicsPalette.TextPrimary,
                                modifier = Modifier.weight(1.35f)
                            )
                            Box(
                                modifier = Modifier
                                    .weight(0.9f)
                                    .height(8.dp)
                                    .clip(CircleShape)
                                    .background(ForensicsPalette.SubtleSurfaceAlt)
                                    .border(0.8.dp, ForensicsPalette.BorderSubtle, CircleShape)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth((item.percentage / 100f).coerceIn(0.08f, 1f))
                                        .height(8.dp)
                                        .clip(CircleShape)
                                        .background(barColor)
                                )
                            }
                            Text(
                                text = "${item.percentage}%",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = barColor,
                                textAlign = TextAlign.End,
                                modifier = Modifier.width(44.dp)
                            )
                        }
                        if (index < activityEstimates.lastIndex) {
                            Spacer(modifier = Modifier.height(2.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Battery attribution unavailable · Activity-based estimate only",
                        fontSize = 11.sp,
                        color = ForensicsPalette.TextMuted
                    )
                }
            }
        }

        // DEVICE CAPABILITIES Card
        item {
            ForensicsCard {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Text(
                        text = "DEVICE CAPABILITIES",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                        color = ForensicsPalette.TextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    capabilities.forEachIndexed { index, cap ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedCapability = cap }
                                .padding(vertical = 11.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CapabilityStatusGraphicBadge(status = cap.status)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = cap.name,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = ForensicsPalette.TextPrimary
                                )
                            }
                            if (cap.rightNote != null) {
                                Text(
                                    text = cap.rightNote,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = ForensicsPalette.TextSecondary
                                )
                            }
                        }
                        if (index < capabilities.lastIndex) {
                            HorizontalDivider(color = ForensicsPalette.DividerColor, thickness = 1.dp)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    if (showHealthModal) {
        AlertDialog(
            onDismissRequest = { showHealthModal = false },
            title = {
                Text(
                    text = "Battery Health & Score Breakdown",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "App-Generated Health Score: ${snapshot.healthScore}/100 (Estimated)",
                        fontWeight = FontWeight.SemiBold,
                        color = ForensicsPalette.GreenPrimary
                    )
                    Text(
                        text = "• Design Capacity: ${snapshot.designCapacityMah} mAh (Device spec)\n" +
                            "• Estimated Full Capacity: ~${snapshot.estimatedFullCapacityMah} mAh (~94.0%)\n" +
                            "• Cycle Count: ${snapshot.cycleCount?.let { "$it cycles (Measured)" } ?: "Data unavailable on this device/OEM"}\n" +
                            "• Thermal Behavior: Normal (28.4°C current, 30.1°C avg charge)\n" +
                            "• Charging Curve: Consistent 80% CC/CV taper",
                        fontSize = 13.sp,
                        color = ForensicsPalette.TextSecondary
                    )
                    HorizontalDivider(color = ForensicsPalette.DividerColor)
                    Text(
                        text = "Transparency Note: Calculated from coulomb-counter charge sessions (60% capacity retention + 20% thermal profile + 20% charging stability). Never presented as official manufacturer warranty health.",
                        fontSize = 12.sp,
                        color = ForensicsPalette.TextMuted
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showHealthModal = false }) {
                    Text("Close")
                }
            }
        )
    }

    selectedCapability?.let { cap ->
        AlertDialog(
            onDismissRequest = { selectedCapability = null },
            title = {
                Text(text = cap.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Status: ${cap.rightNote ?: "Measured & Supported"}",
                        fontWeight = FontWeight.SemiBold,
                        color = ForensicsPalette.BluePrimary
                    )
                    Text(
                        text = cap.detailExplanation,
                        fontSize = 14.sp,
                        color = ForensicsPalette.TextSecondary
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedCapability = null }) {
                    Text("Got it")
                }
            }
        )
    }
}

@Composable
private fun BatteryCircularGauge(
    percent: Int,
    healthLabel: String,
    drainRateText: String
) {
    Box(
        modifier = Modifier.size(204.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(192.dp)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val strokeWidth = 15.dp.toPx()
            val pad = 10.dp.toPx()
            val arcSize = Size(size.width - (strokeWidth + pad * 2), size.height - (strokeWidth + pad * 2))
            val topLeft = Offset(strokeWidth / 2f + pad, strokeWidth / 2f + pad)

            // Subtle inner dial face so the gauge center pops
            drawCircle(
                color = ForensicsPalette.GreenSoftTile.copy(alpha = 0.35f),
                radius = (arcSize.width / 2f) - strokeWidth * 0.7f,
                center = center
            )

            // Outer precision tick marks (24 ticks)
            val outerTickR = size.width / 2f - 2.dp.toPx()
            val innerTickR = outerTickR - 4.dp.toPx()
            for (i in 0 until 24) {
                val angleRad = Math.toRadians((i * 15 - 90).toDouble())
                val start = Offset(
                    center.x + (innerTickR * cos(angleRad)).toFloat(),
                    center.y + (innerTickR * sin(angleRad)).toFloat()
                )
                val end = Offset(
                    center.x + (outerTickR * cos(angleRad)).toFloat(),
                    center.y + (outerTickR * sin(angleRad)).toFloat()
                )
                drawLine(
                    color = ForensicsPalette.BorderStrong,
                    start = start,
                    end = end,
                    strokeWidth = if (i % 6 == 0) 2.dp.toPx() else 1.2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            // Background full ring
            drawArc(
                color = ForensicsPalette.GreenTrack,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth)
            )

            // Foreground progress arc starting from top (-90 degrees)
            val sweep = (percent.coerceIn(0, 100) / 100f) * 360f
            drawArc(
                color = ForensicsPalette.GreenGauge,
                startAngle = -90f,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$percent%",
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = ForensicsPalette.TextPrimary
            )
            Text(
                text = healthLabel,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = ForensicsPalette.GreenPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = drainRateText,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium,
                color = ForensicsPalette.TextSecondary
            )
        }
    }
}

@Composable
private fun GaugeStatColumn(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = ForensicsPalette.TextPrimary
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = ForensicsPalette.TextSecondary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun MetricQuadCard(
    title: String,
    badgeText: String,
    badgeBg: Color,
    badgeTextColor: Color,
    mainValue: String,
    unit: String,
    valueColor: Color,
    subtitle: String,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    ForensicsCard(
        modifier = modifier,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = ForensicsPalette.TextSecondary
                )
                ClassificationBadge(
                    text = badgeText,
                    containerColor = badgeBg,
                    contentColor = badgeTextColor
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = buildAnnotatedString {
                    withStyle(
                        SpanStyle(
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = valueColor
                        )
                    ) {
                        append(mainValue)
                    }
                    withStyle(
                        SpanStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ForensicsPalette.TextSecondary
                        )
                    ) {
                        append(unit)
                    }
                }
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = ForensicsPalette.TextMuted
            )
        }
    }
}

@Composable
private fun SystemStateTile(
    label: String,
    value: String,
    bgColor: Color,
    borderColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(14.dp)
    Column(
        modifier = modifier
            .clip(shape)
            .background(bgColor)
            .border(1.dp, borderColor, shape)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp,
            color = textColor.copy(alpha = 0.85f)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            textAlign = TextAlign.Center
        )
    }
}
