package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
import com.example.data.AppActivityInsight
import com.example.data.DayDrainPoint
import com.example.ui.InsightsTimeframe
import com.example.ui.components.ClassificationBadge
import com.example.ui.components.ForensicsCard
import com.example.ui.components.SegmentedPillSelector
import com.example.ui.theme.ForensicsPalette

@Composable
fun InsightsScreen(
    timeframe: InsightsTimeframe,
    onSelectTimeframe: (InsightsTimeframe) -> Unit,
    weeklyPoints: List<DayDrainPoint>,
    dailyPoints: List<DayDrainPoint>,
    monthlyPoints: List<DayDrainPoint>,
    appInsights: List<AppActivityInsight>,
    onInvestigateAnomaly: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activePoints = when (timeframe) {
        InsightsTimeframe.DAY -> dailyPoints
        InsightsTimeframe.WEEK -> weeklyPoints
        InsightsTimeframe.MONTH -> monthlyPoints
    }
    var selectedApp by remember { mutableStateOf<AppActivityInsight?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ForensicsPalette.ScreenBackground)
            .padding(horizontal = 16.dp)
            .testTag("insights_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Insights",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = ForensicsPalette.TextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Trends, patterns and anomaly detection.",
                fontSize = 13.sp,
                color = ForensicsPalette.TextSecondary
            )
        }

        // Anomaly — Thursday Banner Card (Screenshot 4)
        item {
            ForensicsCard(
                containerColor = ForensicsPalette.AmberContainer,
                onClick = onInvestigateAnomaly,
                modifier = Modifier.testTag("investigate_anomaly_button")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(ForensicsPalette.AmberIconBox)
                            .border(1.dp, ForensicsPalette.AmberDarkText, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.WarningAmber,
                            contentDescription = "Anomaly warning",
                            tint = ForensicsPalette.AmberPill,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Anomaly — Thursday",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = ForensicsPalette.AmberDarkText
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "47% drain vs your 7-day average of 23%. Instagram showed elevated background activity.",
                            fontSize = 13.sp,
                            color = ForensicsPalette.AmberPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Investigate →",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = ForensicsPalette.AmberDarkText,
                            modifier = Modifier.testTag("investigate_anomaly_button")
                        )
                    }
                }
            }
        }

        // Day / Week / Month Selector
        item {
            SegmentedPillSelector(
                options = listOf("Day", "Week", "Month"),
                selectedIndex = when (timeframe) {
                    InsightsTimeframe.DAY -> 0
                    InsightsTimeframe.WEEK -> 1
                    InsightsTimeframe.MONTH -> 2
                },
                onSelect = { idx ->
                    val tf = when (idx) {
                        0 -> InsightsTimeframe.DAY
                        1 -> InsightsTimeframe.WEEK
                        else -> InsightsTimeframe.MONTH
                    }
                    onSelectTimeframe(tf)
                },
                activeTextColor = ForensicsPalette.BluePrimary
            )
        }

        // Battery Drain — 7 Days Line Chart Card
        item {
            val chartSubtitle = when (timeframe) {
                InsightsTimeframe.DAY -> "Battery Drain — Today (4h blocks)"
                InsightsTimeframe.WEEK -> "Battery Drain — 7 Days"
                InsightsTimeframe.MONTH -> "Battery Drain — 4 Weeks"
            }
            val avgHeadline = when (timeframe) {
                InsightsTimeframe.DAY -> "21% today"
                InsightsTimeframe.WEEK -> "23% avg"
                InsightsTimeframe.MONTH -> "22.8% avg"
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
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Text(
                                text = chartSubtitle,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = ForensicsPalette.TextSecondary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = avgHeadline,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = ForensicsPalette.TextPrimary
                            )
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            LegendDotRow(color = ForensicsPalette.BlueChartLine, label = "Normal")
                            LegendDotRow(color = ForensicsPalette.TemperatureOrange, label = "Anomaly")
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    DrainTrendLineChart(points = activePoints)
                }
            }
        }

        // 3 Colored Metric Cards: AVG DRAIN | SCREEN AVG | IDLE DRAIN
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                InsightMetricBox(
                    label = "AVG DRAIN",
                    value = "23%",
                    sub = "per day",
                    bgColor = ForensicsPalette.BlueContainer,
                    borderColor = ForensicsPalette.BlueBorder,
                    textColor = ForensicsPalette.BluePrimary,
                    modifier = Modifier.weight(1f)
                )
                InsightMetricBox(
                    label = "SCREEN AVG",
                    value = "6h 12m",
                    sub = "per day",
                    bgColor = ForensicsPalette.PurpleContainer,
                    borderColor = ForensicsPalette.PurpleBorder,
                    textColor = ForensicsPalette.PurplePrimary,
                    modifier = Modifier.weight(1f)
                )
                InsightMetricBox(
                    label = "IDLE DRAIN",
                    value = "0.8%",
                    sub = "per hour",
                    bgColor = ForensicsPalette.GreenContainer,
                    borderColor = ForensicsPalette.GreenBorder,
                    textColor = ForensicsPalette.GreenPrimary,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // BASELINE COMPARISON Card (Screenshot 4)
        item {
            ForensicsCard {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "BASELINE COMPARISON",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                        color = ForensicsPalette.TextSecondary,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val boxShape = RoundedCornerShape(16.dp)
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(boxShape)
                                .background(ForensicsPalette.GreenSoftTile)
                                .border(1.dp, ForensicsPalette.GreenBorder, boxShape)
                                .padding(vertical = 18.dp, horizontal = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "NORMAL OVERNIGHT",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp,
                                color = ForensicsPalette.GreenPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "4–6%",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = ForensicsPalette.GreenPrimary
                            )
                        }
                        Text(
                            text = "VS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = ForensicsPalette.TextSecondary
                        )
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(boxShape)
                                .background(ForensicsPalette.AmberContainer)
                                .border(1.dp, ForensicsPalette.AmberBorder, boxShape)
                                .padding(vertical = 18.dp, horizontal = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "THURSDAY NIGHT",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp,
                                color = ForensicsPalette.AmberPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "14%",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = ForensicsPalette.RedPrimary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "2.3× above normal · Based on 14 recorded nights",
                        fontSize = 12.sp,
                        color = ForensicsPalette.TextSecondary
                    )
                }
            }
        }

        // APP ACTIVITY Card (Screenshot 4)
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
                            text = "APP ACTIVITY",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp,
                            color = ForensicsPalette.TextSecondary
                        )
                        Text(
                            text = "Activity indicators",
                            fontSize = 11.sp,
                            color = ForensicsPalette.TextMuted
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    appInsights.forEachIndexed { index, app ->
                        val (avatarBg, avatarBorder, avatarText) = when (app.initial) {
                            "I" -> Triple(ForensicsPalette.PurpleSoftTile, ForensicsPalette.PurpleBorder, ForensicsPalette.PurplePrimary)
                            "C" -> Triple(ForensicsPalette.BlueSoftTile, ForensicsPalette.BlueBorder, ForensicsPalette.BluePrimary)
                            "Y" -> Triple(ForensicsPalette.RedContainer, ForensicsPalette.RedBorder, ForensicsPalette.RedPrimary)
                            "M" -> Triple(ForensicsPalette.AmberSoftTile, ForensicsPalette.AmberBorder, ForensicsPalette.AmberPrimary)
                            "S" -> Triple(ForensicsPalette.GreenSoftTile, ForensicsPalette.GreenBorder, ForensicsPalette.GreenPrimary)
                            else -> Triple(ForensicsPalette.SubtleSurfaceAlt, ForensicsPalette.BorderStrong, ForensicsPalette.TextSecondary)
                        }
                        val (impactBg, impactColor) = when (app.impactLevel) {
                            "High" -> ForensicsPalette.AmberSoftTile to ForensicsPalette.AmberPrimary
                            "Med" -> ForensicsPalette.BlueSoftTile to ForensicsPalette.BluePrimary
                            else -> ForensicsPalette.GreenSoftTile to ForensicsPalette.GreenPrimary
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { selectedApp = app }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                val avatarShape = RoundedCornerShape(10.dp)
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(avatarShape)
                                        .background(avatarBg)
                                        .border(1.dp, avatarBorder, avatarShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = app.initial,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = avatarText
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = app.appName,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ForensicsPalette.TextPrimary
                                        )
                                        if (app.hasLocationBadge) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            ClassificationBadge(
                                                text = "Location",
                                                containerColor = ForensicsPalette.AmberSoftTile,
                                                contentColor = ForensicsPalette.AmberPrimary
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${app.foregroundDurationLabel} · ${app.backgroundEventsCount} bg events",
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = ForensicsPalette.TextSecondary
                                    )
                                }
                            }
                            ClassificationBadge(
                                text = app.impactLevel,
                                containerColor = impactBg,
                                contentColor = impactColor
                            )
                        }
                        if (index < appInsights.lastIndex) {
                            HorizontalDivider(color = ForensicsPalette.DividerColor, thickness = 0.8.dp)
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Battery attribution unavailable · bg = background events counted",
                        fontSize = 11.sp,
                        color = ForensicsPalette.TextMuted
                    )
                }
            }
        }

        // TEMPERATURE TREND Card (Screenshot 4)
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
                            text = "TEMPERATURE TREND",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp,
                            color = ForensicsPalette.TextSecondary
                        )
                        Text(
                            text = "7 DAYS",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold,
                            color = ForensicsPalette.TextSecondary
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    TemperatureBarChart(points = weeklyPoints)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Thu peak 33.6°C — above normal charging temperature range",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = ForensicsPalette.RedPrimary
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    selectedApp?.let { app ->
        AlertDialog(
            onDismissRequest = { selectedApp = null },
            title = { Text("${app.appName} — Forensic Activity", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Package: ${app.packageName}",
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        color = ForensicsPalette.TextMuted
                    )
                    Text(
                        text = "• Foreground / Screen-on Duration: ${app.foregroundDurationLabel}\n" +
                            "• Background Activity Events: ${app.backgroundEventsCount} events\n" +
                            "• Location Access Indicator: ${if (app.hasLocationBadge) "Elevated (Passive + Foreground)" else "None observed"}\n" +
                            "• Activity Impact Classification: ${app.impactLevel}",
                        fontSize = 13.sp,
                        color = ForensicsPalette.TextPrimary
                    )
                    if (app.updateCorrelationNote != null) {
                        Text(
                            text = "App Update Correlation: ${app.updateCorrelationNote}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ForensicsPalette.AmberPrimary
                        )
                    }
                    Text(
                        text = "Limitation: Exact per-app mAh battery attribution is unavailable on this device in Standard Mode.",
                        fontSize = 11.sp,
                        color = ForensicsPalette.TextMuted
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedApp = null }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
private fun LegendDotRow(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(9.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = ForensicsPalette.TextSecondary
        )
    }
}

@Composable
private fun DrainTrendLineChart(points: List<DayDrainPoint>) {
    if (points.isEmpty()) return
    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(155.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(ForensicsPalette.SubtleSurface)
                .border(1.dp, ForensicsPalette.BorderSubtle, RoundedCornerShape(14.dp))
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val maxVal = (points.maxOfOrNull { it.drainPercent } ?: 50).coerceAtLeast(50).toFloat()
                val minVal = 0f
                val chartHeight = size.height - 6.dp.toPx()
                val stepX = if (points.size > 1) size.width / (points.size - 1) else size.width
                val dashEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)

                // Horizontal reference grid lines (top, mid, bottom)
                listOf(0.10f, 0.52f, 0.94f).forEach { frac ->
                    val gy = chartHeight * frac
                    drawLine(
                        color = ForensicsPalette.BorderStrong,
                        start = Offset(0f, gy),
                        end = Offset(size.width, gy),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = dashEffect
                    )
                }

                val coordinates = points.mapIndexed { idx, pt ->
                    val x = idx * stepX
                    val normalizedY = ((pt.drainPercent - minVal) / (maxVal - minVal)).coerceIn(0.08f, 0.92f)
                    val y = chartHeight * (1f - normalizedY) + 4.dp.toPx()
                    Offset(x, y)
                }

                // Draw anomaly highlight column under any anomaly point (e.g. Thu)
                points.forEachIndexed { idx, pt ->
                    if (pt.isAnomaly) {
                        val center = coordinates[idx]
                        val barW = 30.dp.toPx()
                        drawRoundRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    ForensicsPalette.AmberPill.copy(alpha = 0.95f),
                                    ForensicsPalette.AmberSoftTile.copy(alpha = 0.55f)
                                ),
                                startY = center.y,
                                endY = chartHeight
                            ),
                            topLeft = Offset(center.x - barW / 2f, center.y),
                            size = Size(barW, chartHeight - center.y),
                            cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                        )
                        drawRoundRect(
                            color = ForensicsPalette.AmberBorder,
                            topLeft = Offset(center.x - barW / 2f, center.y),
                            size = Size(barW, chartHeight - center.y),
                            cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx()),
                            style = Stroke(width = 1.dp.toPx())
                        )
                    }
                }

                // Area path under curve
                val areaPath = Path().apply {
                    moveTo(coordinates.first().x, chartHeight)
                    coordinates.forEach { lineTo(it.x, it.y) }
                    lineTo(coordinates.last().x, chartHeight)
                    close()
                }
                drawPath(
                    path = areaPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            ForensicsPalette.BlueContainer.copy(alpha = 0.75f),
                            ForensicsPalette.BlueSoftTile.copy(alpha = 0.15f)
                        )
                    )
                )

                // Line path
                val linePath = Path().apply {
                    coordinates.forEachIndexed { idx, offset ->
                        if (idx == 0) moveTo(offset.x, offset.y) else lineTo(offset.x, offset.y)
                    }
                }
                drawPath(
                    path = linePath,
                    color = ForensicsPalette.BlueChartLine,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )

                // Data points with crisp white halos
                coordinates.forEachIndexed { idx, center ->
                    val isAnom = points[idx].isAnomaly
                    val radius = if (isAnom) 6.5.dp.toPx() else 4.5.dp.toPx()
                    drawCircle(
                        color = Color.White,
                        radius = radius + 2.5.dp.toPx(),
                        center = center
                    )
                    drawCircle(
                        color = if (isAnom) ForensicsPalette.TemperatureOrange else ForensicsPalette.BlueChartLine,
                        radius = radius,
                        center = center
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            points.forEach { pt ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = pt.dayShort,
                        fontSize = 11.sp,
                        fontWeight = if (pt.isAnomaly) FontWeight.Bold else FontWeight.Medium,
                        color = if (pt.isAnomaly) ForensicsPalette.TemperatureOrange else ForensicsPalette.TextSecondary
                    )
                    Text(
                        text = "${pt.drainPercent}%",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = if (pt.isAnomaly) FontWeight.Bold else FontWeight.Normal,
                        color = if (pt.isAnomaly) ForensicsPalette.RedPrimary else ForensicsPalette.TextMuted
                    )
                }
            }
        }
    }
}

@Composable
private fun InsightMetricBox(
    label: String,
    value: String,
    sub: String,
    bgColor: Color,
    borderColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier = modifier
            .clip(shape)
            .background(bgColor)
            .border(1.dp, borderColor, shape)
            .padding(vertical = 14.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp,
            color = textColor
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = sub,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = textColor.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun TemperatureBarChart(points: List<DayDrainPoint>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(112.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(ForensicsPalette.SubtleSurface)
            .border(1.dp, ForensicsPalette.BorderSubtle, RoundedCornerShape(14.dp))
            .padding(horizontal = 10.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        points.forEach { pt ->
            val isPeak = pt.isAnomaly || pt.peakTempCelsius >= 33.0f
            // Normalize temperature height between 26°C and 35°C so bars have clear visual proportion
            val normFraction = ((pt.peakTempCelsius - 26.5f) / 8.0f).coerceIn(0.24f, 1.0f)
            val barColor = if (isPeak) ForensicsPalette.RedBar else ForensicsPalette.BlueBarLight
            val barBorder = if (isPeak) ForensicsPalette.RedPrimary else ForensicsPalette.BlueBorder

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                Text(
                    text = "${pt.peakTempCelsius}°",
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = if (isPeak) FontWeight.Bold else FontWeight.Medium,
                    color = if (isPeak) ForensicsPalette.RedPrimary else ForensicsPalette.TextSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    // Full-height subtle track behind bar
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(8.dp))
                            .background(ForensicsPalette.CardSurface)
                            .border(0.8.dp, ForensicsPalette.BorderSubtle, RoundedCornerShape(8.dp))
                    )
                    // Actual temperature fill bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(normFraction)
                            .clip(RoundedCornerShape(8.dp))
                            .background(barColor)
                            .border(1.dp, barBorder, RoundedCornerShape(8.dp))
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = pt.dayShort.first().toString(),
                    fontSize = 11.sp,
                    fontWeight = if (isPeak) FontWeight.Bold else FontWeight.SemiBold,
                    color = if (isPeak) ForensicsPalette.RedPrimary else ForensicsPalette.TextSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
