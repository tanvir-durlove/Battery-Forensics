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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppActivityInsight
import com.example.data.DayDrainPoint
import com.example.ui.InsightsTimeframe
import com.example.ui.components.CardInfoIconButton
import com.example.ui.components.ClassificationBadge
import com.example.ui.components.ForensicsCard
import com.example.ui.components.InlineForensicsAdBannerCard
import com.example.ui.components.SegmentedPillSelector
import com.example.ui.components.StatGuideTopic
import com.example.ui.theme.ForensicsPalette
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun InsightsScreen(
    timeframe: InsightsTimeframe,
    onSelectTimeframe: (InsightsTimeframe) -> Unit,
    weeklyPoints: List<DayDrainPoint>,
    dailyPoints: List<DayDrainPoint>,
    monthlyPoints: List<DayDrainPoint>,
    appInsights: List<AppActivityInsight>,
    onInvestigateAnomaly: () -> Unit,
    onShowTopicGuide: (StatGuideTopic) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val activePoints = when (timeframe) {
        InsightsTimeframe.DAY -> dailyPoints
        InsightsTimeframe.WEEK -> weeklyPoints
        InsightsTimeframe.MONTH -> monthlyPoints
    }
    val anomalyPoint = activePoints.firstOrNull { it.isAnomaly } ?: weeklyPoints.firstOrNull { it.isAnomaly }
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
                text = "Daily trends, usage patterns, and unusual drain alerts.",
                fontSize = 13.sp,
                color = ForensicsPalette.TextSecondary
            )
        }

        // Top Banner: Anomaly if detected, OR "Building Baseline — Keep Using" on fresh install
        item {
            if (anomalyPoint != null) {
                val avgDrain = weeklyPoints.map { it.drainPercent }.average().roundToInt()
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
                                text = "Anomaly — ${anomalyPoint.dayFull}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = ForensicsPalette.AmberDarkText
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${anomalyPoint.drainPercent}% drain vs your recorded average of ${avgDrain}%.",
                                fontSize = 13.sp,
                                color = ForensicsPalette.AmberPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Investigate →",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = ForensicsPalette.AmberDarkText
                            )
                        }
                    }
                }
            } else {
                ForensicsCard(
                    containerColor = ForensicsPalette.BlueSoftTile,
                    onClick = onInvestigateAnomaly,
                    modifier = Modifier.testTag("investigate_anomaly_button")
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
                                text = "Building Your Battery Baseline",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = ForensicsPalette.BluePrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 8.dp)
                            )
                            ClassificationBadge(
                                text = "Data collection in progress",
                                containerColor = ForensicsPalette.BlueContainer,
                                contentColor = ForensicsPalette.BluePrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Keep using your phone normally — daily and weekly drain trends, overnight comparisons, and unusual drain alerts will appear here soon.",
                            fontSize = 13.sp,
                            color = ForensicsPalette.TextPrimary
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

        // Native Ad Card (Above the Fold — Top of Insights Tab)
        item {
            InlineForensicsAdBannerCard(
                placementLabel = "Battery Analytics",
                tagName = "insights_inline_ad_card"
            )
        }

        // Battery Drain Line Chart Card
        item {
            val chartSubtitle = when (timeframe) {
                InsightsTimeframe.DAY -> "Battery Drain — Today"
                InsightsTimeframe.WEEK -> "Battery Drain — 7 Days"
                InsightsTimeframe.MONTH -> "Battery Drain — 4 Weeks"
            }
            val avgHeadline = if (activePoints.isNotEmpty()) {
                "${activePoints.map { it.drainPercent }.average().roundToInt()}% avg"
            } else {
                "Collecting data..."
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
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = ForensicsPalette.TextPrimary
                            )
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            LegendDotRow(color = ForensicsPalette.BlueChartLine, label = "Usual")
                            LegendDotRow(color = ForensicsPalette.TemperatureOrange, label = "High Drain")
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    if (activePoints.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(ForensicsPalette.SubtleSurface)
                                .border(1.dp, ForensicsPalette.BorderSubtle, RoundedCornerShape(14.dp))
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Not enough history yet. Keep using your device and the drain trend curve will generate automatically.",
                                fontSize = 13.sp,
                                color = ForensicsPalette.TextSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        DrainTrendLineChart(points = activePoints)
                    }
                }
            }
        }

        // 3 Colored Metric Cards: AVG DRAIN | SCREEN AVG | IDLE DRAIN
        item {
            val avgDrainStr = if (activePoints.isNotEmpty()) {
                "${activePoints.map { it.drainPercent }.average().roundToInt()}%"
            } else "—"
            val screenAvgStr = if (activePoints.isNotEmpty()) {
                val hrs = activePoints.map { it.screenHours.toDouble() }.average()
                String.format(Locale.US, "%.1fh", hrs)
            } else "—"
            val idleAvgStr = if (activePoints.isNotEmpty()) {
                val idle = activePoints.map { it.idleDrainRate.toDouble() }.average()
                String.format(Locale.US, "%.1f%%", idle)
            } else "—"

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                InsightMetricBox(
                    label = "AVG DRAIN",
                    value = avgDrainStr,
                    sub = if (activePoints.isNotEmpty()) "per session" else "learning",
                    bgColor = ForensicsPalette.BlueContainer,
                    borderColor = ForensicsPalette.BlueBorder,
                    textColor = ForensicsPalette.BluePrimary,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
                InsightMetricBox(
                    label = "SCREEN TIME",
                    value = screenAvgStr,
                    sub = if (activePoints.isNotEmpty()) "per day" else "learning",
                    bgColor = ForensicsPalette.PurpleContainer,
                    borderColor = ForensicsPalette.PurpleBorder,
                    textColor = ForensicsPalette.PurplePrimary,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
                InsightMetricBox(
                    label = "STANDBY DRAIN",
                    value = idleAvgStr,
                    sub = if (activePoints.isNotEmpty()) "per hour" else "learning",
                    bgColor = ForensicsPalette.GreenContainer,
                    borderColor = ForensicsPalette.GreenBorder,
                    textColor = ForensicsPalette.GreenPrimary,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
            }
        }

        // BASELINE COMPARISON Card
        item {
            ForensicsCard {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "BASELINE COMPARISON",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp,
                            color = ForensicsPalette.TextSecondary,
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp)
                        )
                        CardInfoIconButton(
                            topicKey = "insights_trends",
                            onShowTopic = onShowTopicGuide
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    if (weeklyPoints.size < 2) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(ForensicsPalette.SubtleSurface)
                                .border(1.dp, ForensicsPalette.BorderSubtle, RoundedCornerShape(12.dp))
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Baseline comparison requires at least 2 recorded overnight or idle sessions. Keep using your device — comparison data will generate soon.",
                                fontSize = 13.sp,
                                color = ForensicsPalette.TextSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        val latest = weeklyPoints.last()
                        val priorPoints = weeklyPoints.dropLast(1).ifEmpty { weeklyPoints }
                        val minBaseline = priorPoints.minOf { it.drainPercent }
                        val maxBaseline = priorPoints.maxOf { it.drainPercent }
                        val baselineLabel = if (minBaseline == maxBaseline) {
                            "${minBaseline}%"
                        } else {
                            "${minBaseline}–${maxBaseline}%"
                        }
                        val boxShape = RoundedCornerShape(16.dp)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(IntrinsicSize.Max),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(boxShape)
                                    .background(ForensicsPalette.GreenSoftTile)
                                    .border(1.dp, ForensicsPalette.GreenBorder, boxShape)
                                    .padding(vertical = 18.dp, horizontal = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "USUAL DRAIN",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp,
                                    color = ForensicsPalette.GreenPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = baselineLabel,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ForensicsPalette.GreenPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
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
                                    .fillMaxHeight()
                                    .clip(boxShape)
                                    .background(ForensicsPalette.AmberContainer)
                                    .border(1.dp, ForensicsPalette.AmberBorder, boxShape)
                                    .padding(vertical = 18.dp, horizontal = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = latest.dayFull.uppercase(Locale.getDefault()),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp,
                                    color = ForensicsPalette.AmberPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "${latest.drainPercent}%",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (latest.isAnomaly) ForensicsPalette.RedPrimary else ForensicsPalette.TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Based on ${weeklyPoints.size} recorded sessions",
                            fontSize = 12.sp,
                            color = ForensicsPalette.TextSecondary
                        )
                    }
                }
            }
        }

        // APP ACTIVITY Card
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp)
                        ) {
                            Text(
                                text = "APP ACTIVITY",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp,
                                color = ForensicsPalette.TextSecondary
                            )
                            CardInfoIconButton(
                                topicKey = "insights_apps",
                                onShowTopic = onShowTopicGuide
                            )
                        }
                        Text(
                            text = "Activity indicators",
                            fontSize = 11.sp,
                            color = ForensicsPalette.TextMuted
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    if (appInsights.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(ForensicsPalette.SubtleSurface)
                                .border(1.dp, ForensicsPalette.BorderSubtle, RoundedCornerShape(12.dp))
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Keep using your phone — real app foreground time and background event counts will generate soon (ensure Usage Access is enabled in Settings → Permissions).",
                                fontSize = 13.sp,
                                color = ForensicsPalette.TextSecondary
                            )
                        }
                    } else {
                        appInsights.forEachIndexed { index, app ->
                            val (avatarBg, avatarBorder, avatarText) = when (index % 5) {
                                0 -> Triple(ForensicsPalette.PurpleSoftTile, ForensicsPalette.PurpleBorder, ForensicsPalette.PurplePrimary)
                                1 -> Triple(ForensicsPalette.BlueSoftTile, ForensicsPalette.BlueBorder, ForensicsPalette.BluePrimary)
                                2 -> Triple(ForensicsPalette.RedContainer, ForensicsPalette.RedBorder, ForensicsPalette.RedPrimary)
                                3 -> Triple(ForensicsPalette.AmberSoftTile, ForensicsPalette.AmberBorder, ForensicsPalette.AmberPrimary)
                                else -> Triple(ForensicsPalette.GreenSoftTile, ForensicsPalette.GreenBorder, ForensicsPalette.GreenPrimary)
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
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 10.dp)
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
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = app.appName,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = ForensicsPalette.TextPrimary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f, fill = false)
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
                                            text = "${app.foregroundDurationLabel} · ${app.backgroundEventsCount} events",
                                            fontSize = 12.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = ForensicsPalette.TextSecondary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
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
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Shows screen time and background activity (Android hides exact per-app battery %)",
                        fontSize = 11.sp,
                        color = ForensicsPalette.TextMuted
                    )
                }
            }
        }

        // TEMPERATURE TREND Card
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
                    if (weeklyPoints.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(ForensicsPalette.SubtleSurface)
                                .border(1.dp, ForensicsPalette.BorderSubtle, RoundedCornerShape(12.dp))
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Keep using your device — daily peak temperature trends will generate automatically as thermal readings are logged.",
                                fontSize = 13.sp,
                                color = ForensicsPalette.TextSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        TemperatureBarChart(points = weeklyPoints)
                        val peakPt = weeklyPoints.maxByOrNull { it.peakTempCelsius }
                        if (peakPt != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Peak observed: ${peakPt.peakTempCelsius}°C (${peakPt.dayFull})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (peakPt.peakTempCelsius >= 35f) ForensicsPalette.RedPrimary else ForensicsPalette.TextSecondary
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    selectedApp?.let { app ->
        AlertDialog(
            onDismissRequest = { selectedApp = null },
            title = { Text("${app.appName} — Activity Summary", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "App ID: ${app.packageName}",
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        color = ForensicsPalette.TextMuted
                    )
                    Text(
                        text = "• Screen-on time: ${app.foregroundDurationLabel}\n" +
                            "• Background activity: ${app.backgroundEventsCount} times\n" +
                            "• Estimated battery impact: ${app.impactLevel}",
                        fontSize = 13.sp,
                        color = ForensicsPalette.TextPrimary
                    )
                    if (app.updateCorrelationNote != null) {
                        Text(
                            text = "Recent Update Note: ${app.updateCorrelationNote}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ForensicsPalette.AmberPrimary
                        )
                    }
                    Text(
                        text = "Note: Android does not share exact per-app battery percentage with standard apps.",
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
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp,
            color = textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = sub,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = textColor.copy(alpha = 0.8f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
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
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(8.dp))
                            .background(ForensicsPalette.CardSurface)
                            .border(0.8.dp, ForensicsPalette.BorderSubtle, RoundedCornerShape(8.dp))
                    )
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
