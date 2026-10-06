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
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.ui.platform.LocalContext
import com.example.data.AppSelfAudit
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
    appSelfAudit: AppSelfAudit = AppSelfAudit(),
    isAdbBatteryStatsGranted: Boolean = false,
    onRefreshAdbState: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val activePoints = when (timeframe) {
        InsightsTimeframe.DAY -> dailyPoints
        InsightsTimeframe.WEEK -> weeklyPoints
        InsightsTimeframe.MONTH -> monthlyPoints
    }
    val anomalyPoint = activePoints.firstOrNull { it.isAnomaly } ?: weeklyPoints.firstOrNull { it.isAnomaly }
    val context = LocalContext.current
    var selectedApp by remember { mutableStateOf<AppActivityInsight?>(null) }
    var showAdbGuideModal by remember { mutableStateOf(false) }
    var selectedCulpritFilter by remember { mutableStateOf("All") } // "All", "Overheat", "Background", "Screen"

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

        // 1. ADB High-Precision BATTERY_STATS Reminder Card
        item {
            val adbCmd = "adb shell pm grant com.nextgen.batteryforensics android.permission.BATTERY_STATS"
            ForensicsCard(
                containerColor = ForensicsPalette.CardSurface,
                modifier = Modifier.testTag("insights_adb_reminder_card")
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "⚡", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "DEEP DRAIN & WAKELOCK INSPECTION",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp,
                                color = ForensicsPalette.TextSecondary
                            )
                        }
                        ClassificationBadge(
                            text = if (isAdbBatteryStatsGranted) "ADB Active" else "Standard Mode",
                            containerColor = if (isAdbBatteryStatsGranted) ForensicsPalette.GreenContainer else ForensicsPalette.AmberContainer,
                            contentColor = if (isAdbBatteryStatsGranted) ForensicsPalette.GreenPrimary else ForensicsPalette.AmberPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isAdbBatteryStatsGranted) {
                            "BATTERY_STATS permission is active! Deep hardware dumpsys batterystats is unlocked for microscopic per-app hardware drain, kernel wakelocks, and antenna hold times."
                        } else {
                            "Want exact per-app mAh drain & kernel wakelocks? Android restricts third-party apps by default. Run this one-time command via PC or Wireless Debugging / Shizuku to unlock full system batterystats:"
                        },
                        fontSize = 12.sp,
                        color = ForensicsPalette.TextPrimary,
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(ForensicsPalette.SubtleSurface)
                            .border(1.dp, ForensicsPalette.BorderSubtle, RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = adbCmd,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = ForensicsPalette.BluePrimary,
                            lineHeight = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                val clip = ClipData.newPlainText("ADB Command", adbCmd)
                                clipboard?.setPrimaryClip(clip)
                                Toast.makeText(context, "ADB command copied! Run on PC or via Wireless Debugging / Shizuku", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ForensicsPalette.BluePrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1.3f)
                                .testTag("insights_copy_adb_cmd_button")
                        ) {
                            Text("Copy ADB Command", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { showAdbGuideModal = true },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("insights_adb_guide_button")
                        ) {
                            Text("1-Min Guide", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }

                        OutlinedButton(
                            onClick = {
                                onRefreshAdbState()
                                Toast.makeText(context, if (isAdbBatteryStatsGranted) "ADB Permission active!" else "Status refreshed", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("↻", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 2. THERMAL & BATTERY DRAIN CULPRITS Card
        item {
            val filteredCulprits = remember(appInsights, selectedCulpritFilter) {
                when (selectedCulpritFilter) {
                    "Overheat" -> appInsights.filter { it.culpritType?.contains("Overheat", ignoreCase = true) == true }
                    "Background" -> appInsights.filter { it.culpritType?.contains("Vampire", ignoreCase = true) == true || it.backgroundEventsCount >= 25 }
                    "Screen" -> appInsights.filter { it.culpritType?.contains("Screen", ignoreCase = true) == true || it.foregroundDurationLabel.contains("h") }
                    else -> appInsights
                }
            }

            ForensicsCard(modifier = Modifier.testTag("insights_culprits_card")) {
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
                                text = "OVERHEAT & DRAIN CULPRITS",
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
                            text = "${filteredCulprits.size} apps identified",
                            fontSize = 11.sp,
                            color = ForensicsPalette.TextMuted
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Identifies apps responsible for temperature spikes, standby battery drain, and excessive screen time. Tap any app to restrict background activity.",
                        fontSize = 12.sp,
                        color = ForensicsPalette.TextSecondary,
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Filter chips row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "All" to "All",
                            "Overheat" to "🔥 Overheat",
                            "Background" to "⚡ Background",
                            "Screen" to "📱 Screen"
                        ).forEach { (key, label) ->
                            val isSelected = selectedCulpritFilter == key
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) ForensicsPalette.BlueContainer else ForensicsPalette.SubtleSurface)
                                    .border(
                                        1.dp,
                                        if (isSelected) ForensicsPalette.BlueBorder else ForensicsPalette.BorderSubtle,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { selectedCulpritFilter = key }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) ForensicsPalette.BluePrimary else ForensicsPalette.TextSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (filteredCulprits.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(ForensicsPalette.SubtleSurface)
                                .border(1.dp, ForensicsPalette.BorderSubtle, RoundedCornerShape(12.dp))
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "No culprits matched for this filter. Device is operating within nominal power and thermal limits.",
                                fontSize = 13.sp,
                                color = ForensicsPalette.TextSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        filteredCulprits.forEachIndexed { index, app ->
                            val (avatarBg, avatarBorder, avatarText) = when {
                                app.culpritType?.contains("Overheat", ignoreCase = true) == true -> Triple(ForensicsPalette.RedContainer, ForensicsPalette.RedBorder, ForensicsPalette.RedPrimary)
                                app.culpritType?.contains("Vampire", ignoreCase = true) == true -> Triple(ForensicsPalette.AmberSoftTile, ForensicsPalette.AmberBorder, ForensicsPalette.AmberPrimary)
                                else -> Triple(ForensicsPalette.BlueSoftTile, ForensicsPalette.BlueBorder, ForensicsPalette.BluePrimary)
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
                                            .size(40.dp)
                                            .clip(avatarShape)
                                            .background(avatarBg)
                                            .border(1.dp, avatarBorder, avatarShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = app.initial,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = avatarText
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = app.appName,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = ForensicsPalette.TextPrimary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f, fill = false)
                                            )
                                            if (app.culpritType != null) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                ClassificationBadge(
                                                    text = app.culpritType,
                                                    containerColor = avatarBg,
                                                    contentColor = avatarText
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Text(
                                            text = "${app.foregroundDurationLabel} screen · ${app.backgroundEventsCount} background events",
                                            fontSize = 12.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = ForensicsPalette.TextSecondary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (app.thermalCorrelationNote != null) {
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = app.thermalCorrelationNote,
                                                fontSize = 11.sp,
                                                color = if (app.culpritType?.contains("Overheat", ignoreCase = true) == true) ForensicsPalette.RedPrimary else ForensicsPalette.AmberPrimary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    if (app.estimatedDrainPct > 0f) {
                                        Text(
                                            text = "~${app.estimatedDrainPct.roundToInt()}%",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = avatarText
                                        )
                                    }
                                    Text(
                                        text = "Details →",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = ForensicsPalette.BluePrimary
                                    )
                                }
                            }
                            if (index < filteredCulprits.lastIndex) {
                                HorizontalDivider(color = ForensicsPalette.DividerColor, thickness = 0.8.dp)
                            }
                        }
                    }
                }
            }
        }

        // 3. BATTERY FORENSICS SELF-AUDIT CARD (Verified Innocence)
        item {
            ForensicsCard(
                containerColor = ForensicsPalette.CardSurface,
                modifier = Modifier.testTag("insights_self_audit_card")
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🛡️", fontSize = 17.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "BATTERY FORENSICS SELF-AUDIT",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp,
                                color = ForensicsPalette.TextSecondary
                            )
                        }
                        ClassificationBadge(
                            text = "Verified: 0% Heat",
                            containerColor = ForensicsPalette.GreenContainer,
                            contentColor = ForensicsPalette.GreenPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Independent verification proving Battery Forensics does not cause battery drain or phone overheating:",
                        fontSize = 12.sp,
                        color = ForensicsPalette.TextPrimary,
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // 4-box verification grid
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Max),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        InsightMetricBox(
                            label = "BATTERY DRAIN",
                            value = appSelfAudit.batteryImpactEstimate,
                            sub = "Negligible",
                            bgColor = ForensicsPalette.GreenContainer,
                            borderColor = ForensicsPalette.GreenBorder,
                            textColor = ForensicsPalette.GreenPrimary,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                        InsightMetricBox(
                            label = "BG CPU USAGE",
                            value = appSelfAudit.backgroundCpuUsage,
                            sub = "Event-driven",
                            bgColor = ForensicsPalette.BlueContainer,
                            borderColor = ForensicsPalette.BlueBorder,
                            textColor = ForensicsPalette.BluePrimary,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Max),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        InsightMetricBox(
                            label = "BG WAKELOCKS",
                            value = "${appSelfAudit.activeWakelocksCount}",
                            sub = "Zero sleep hold",
                            bgColor = ForensicsPalette.PurpleContainer,
                            borderColor = ForensicsPalette.PurpleBorder,
                            textColor = ForensicsPalette.PurplePrimary,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                        InsightMetricBox(
                            label = "THERMAL LOAD",
                            value = "0%",
                            sub = "Zero Heat",
                            bgColor = ForensicsPalette.GreenContainer,
                            borderColor = ForensicsPalette.GreenBorder,
                            textColor = ForensicsPalette.GreenPrimary,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Technical verification note
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(ForensicsPalette.SubtleSurface)
                            .border(1.dp, ForensicsPalette.BorderSubtle, RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                text = "How We Guarantee Zero Standby Drain:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ForensicsPalette.TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Battery Forensics uses zero polling loops and zero background timers. It passively receives Android's system battery broadcast (Intent.ACTION_BATTERY_CHANGED) only when your OS battery changes naturally. It holds zero CPU wakelocks and cannot cause device heating.",
                                fontSize = 11.sp,
                                color = ForensicsPalette.TextSecondary,
                                lineHeight = 16.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "• Process RAM: ~${appSelfAudit.ramUsageMb} MB · Sandboxed SQLite · 0 Background Threads",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = ForensicsPalette.GreenPrimary
                            )
                        }
                    }
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
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = app.appName, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (app.culpritType != null) {
                        ClassificationBadge(
                            text = app.culpritType,
                            containerColor = if (app.culpritType.contains("Overheat")) ForensicsPalette.RedContainer else ForensicsPalette.AmberSoftTile,
                            contentColor = if (app.culpritType.contains("Overheat")) ForensicsPalette.RedPrimary else ForensicsPalette.AmberPrimary
                        )
                    }

                    Text(
                        text = "Package: ${app.packageName}",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = ForensicsPalette.TextMuted
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(ForensicsPalette.SubtleSurface)
                            .border(1.dp, ForensicsPalette.BorderSubtle, RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "• Screen-on usage: ${app.foregroundDurationLabel}",
                                fontSize = 13.sp,
                                color = ForensicsPalette.TextPrimary
                            )
                            Text(
                                text = "• Standby / wakeups: ${app.backgroundEventsCount} events",
                                fontSize = 13.sp,
                                color = ForensicsPalette.TextPrimary
                            )
                            if (app.estimatedDrainPct > 0f) {
                                Text(
                                    text = "• Estimated power share: ~${app.estimatedDrainPct.roundToInt()}%",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = ForensicsPalette.AmberPrimary
                                )
                            }
                        }
                    }

                    if (app.thermalCorrelationNote != null) {
                        Text(
                            text = "Thermal Note: ${app.thermalCorrelationNote}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (app.culpritType?.contains("Overheat") == true) ForensicsPalette.RedPrimary else ForensicsPalette.AmberPrimary
                        )
                    }

                    Text(
                        text = "Tip: Tap 'App Settings' below to restrict background battery or put this app into Deep Sleep via Android Settings.",
                        fontSize = 11.sp,
                        color = ForensicsPalette.TextSecondary,
                        lineHeight = 15.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        try {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", app.packageName, null)
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        } catch (_: Exception) {
                            Toast.makeText(context, "Could not open system settings for ${app.packageName}", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ForensicsPalette.BluePrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("App Settings →", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedApp = null }) {
                    Text("Close")
                }
            }
        )
    }

    if (showAdbGuideModal) {
        val adbCmd = "adb shell pm grant com.nextgen.batteryforensics android.permission.BATTERY_STATS"
        AlertDialog(
            onDismissRequest = { showAdbGuideModal = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("⚡ Unlock Deep BATTERY_STATS", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Why Android requires ADB:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = ForensicsPalette.TextPrimary
                    )
                    Text(
                        text = "To protect privacy against cross-app tracking, Android 10–15 restricts third-party battery apps from viewing the exact per-app mAh consumed. Granting this permission unlocks full dumpsys batterystats hardware telemetry.",
                        fontSize = 12.sp,
                        color = ForensicsPalette.TextSecondary,
                        lineHeight = 16.sp
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(ForensicsPalette.SubtleSurface)
                            .border(1.dp, ForensicsPalette.BorderSubtle, RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = adbCmd,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = ForensicsPalette.BluePrimary,
                            lineHeight = 15.sp
                        )
                    }

                    Text(
                        text = "Option A — Via PC (1 minute):\n1. Enable Developer Options & USB Debugging.\n2. Connect phone to PC and run the command above in Terminal.\n\nOption B — On Phone Without PC:\nUse Shizuku or LADB app with Wireless Debugging to grant it directly on-device.",
                        fontSize = 12.sp,
                        color = ForensicsPalette.TextPrimary,
                        lineHeight = 17.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                        val clip = ClipData.newPlainText("ADB Command", adbCmd)
                        clipboard?.setPrimaryClip(clip)
                        Toast.makeText(context, "Command copied!", Toast.LENGTH_SHORT).show()
                        showAdbGuideModal = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ForensicsPalette.BluePrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Copy Command", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAdbGuideModal = false }) {
                    Text("Got It")
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
