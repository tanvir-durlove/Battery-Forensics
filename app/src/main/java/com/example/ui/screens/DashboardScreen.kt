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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ActivityEstimateItem
import com.example.data.CapabilityItem
import com.example.data.LiveTelemetrySnapshot
import com.example.ui.components.CapabilityStatusGraphicBadge
import com.example.ui.components.CardInfoIconButton
import com.example.ui.components.ClassificationBadge
import com.example.ui.components.ForensicsCard
import com.example.ui.components.InlineForensicsAdBannerCard
import com.example.ui.components.StatGuideTopic
import com.example.ui.theme.ForensicsPalette
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun DashboardScreen(
    snapshot: LiveTelemetrySnapshot,
    activityEstimates: List<ActivityEstimateItem>,
    capabilities: List<CapabilityItem>,
    isHistoryEmpty: Boolean = false,
    onRefresh: () -> Unit,
    onNavigateToDiagnose: () -> Unit,
    onShowTopicGuide: (StatGuideTopic) -> Unit = {},
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
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 10.dp)
                ) {
                    Text(
                        text = snapshot.deviceModel,
                        fontSize = 23.sp,
                        fontWeight = FontWeight.Bold,
                        color = ForensicsPalette.TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Android ${snapshot.androidVersion} · Live monitoring active",
                        fontSize = 13.sp,
                        color = ForensicsPalette.TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
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
                        color = ForensicsPalette.TextSecondary,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }

        // Data collection in progress card when battery history is empty
        if (isHistoryEmpty) {
            item {
                ForensicsCard(
                    containerColor = ForensicsPalette.BlueSoftTile,
                    modifier = Modifier.testTag("data_collection_in_progress_card")
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
                                text = "Data collection in progress",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = ForensicsPalette.BluePrimary,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 8.dp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            ClassificationBadge(
                                text = "Live Tracking",
                                containerColor = ForensicsPalette.BlueContainer,
                                contentColor = ForensicsPalette.BluePrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "We're learning how your battery behaves as you charge and use your phone. Detailed history, drain speed, and daily summaries will appear here automatically.",
                            fontSize = 13.sp,
                            color = ForensicsPalette.TextPrimary
                        )
                    }
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
                            label = "Time left",
                            modifier = Modifier.weight(1f)
                        )
                        VerticalDivider(
                            color = ForensicsPalette.BorderSubtle,
                            thickness = 1.dp,
                            modifier = Modifier.fillMaxHeight()
                        )
                        GaugeStatColumn(
                            value = snapshot.awakeOffScreenText,
                            label = "Background active",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Native Ad Card (Above the Fold — Top of Home Dashboard)
        item {
            InlineForensicsAdBannerCard(
                placementLabel = "Battery Care",
                tagName = "home_inline_ad_card"
            )
        }

        // 2x2 Metric Grid Cards (Temperature, Voltage, Current, Health Score)
        item {
            val isWarm = snapshot.temperatureCelsius >= 38.0f || snapshot.isThermalWarningActive
            val isHot = snapshot.temperatureCelsius >= 40.0f
            val tempBadgeBg = when {
                isHot -> ForensicsPalette.RedContainer
                isWarm -> ForensicsPalette.AmberPill
                else -> ForensicsPalette.GreenContainer
            }
            val tempAccentColor = when {
                isHot -> ForensicsPalette.RedPrimary
                isWarm -> ForensicsPalette.TemperatureOrange
                else -> ForensicsPalette.GreenPrimary
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "LIVE BATTERY READINGS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                        color = ForensicsPalette.TextSecondary,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                    )
                    CardInfoIconButton(
                        topicKey = "live_readings",
                        onShowTopic = onShowTopicGuide
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Max),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricQuadCard(
                        title = "Temperature",
                        badgeText = if (isWarm) "Warm" else "Live",
                        badgeBg = tempBadgeBg,
                        badgeTextColor = tempAccentColor,
                        mainValue = "${snapshot.temperatureCelsius}",
                        unit = " °C",
                        valueColor = tempAccentColor,
                        subtitle = snapshot.temperatureStatus,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .testTag("temperature_metric_card")
                    )
                    MetricQuadCard(
                        title = "Voltage",
                        badgeText = "Live",
                        badgeBg = ForensicsPalette.BlueContainer,
                        badgeTextColor = ForensicsPalette.BluePrimary,
                        mainValue = "${snapshot.voltageVolts}",
                        unit = " V",
                        valueColor = ForensicsPalette.BlueBright,
                        subtitle = snapshot.voltageStatus,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Max),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricQuadCard(
                        title = "Power Flow",
                        badgeText = if (snapshot.currentMilliAmps != 0) "Live" else "Auto",
                        badgeBg = ForensicsPalette.PurpleContainer,
                        badgeTextColor = ForensicsPalette.PurplePrimary,
                        mainValue = if (snapshot.currentMilliAmps != 0) "${snapshot.currentMilliAmps}" else "—",
                        unit = " mA",
                        valueColor = ForensicsPalette.PurpleBright,
                        subtitle = snapshot.currentStatus,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                    val showCalibratedScore = !snapshot.isHealthScoreCalibrating && snapshot.healthScore > 0
                    MetricQuadCard(
                        title = "Health Score",
                        badgeText = if (showCalibratedScore) "Ready" else "Learning",
                        badgeBg = ForensicsPalette.GreenContainer,
                        badgeTextColor = ForensicsPalette.GreenPrimary,
                        mainValue = if (showCalibratedScore) "${snapshot.healthScore.coerceAtMost(100)}" else "Calibrating...",
                        unit = if (showCalibratedScore) " /100" else "",
                        valueFontSize = if (showCalibratedScore) 26 else 18,
                        valueColor = ForensicsPalette.GreenPrimary,
                        subtitle = if (showCalibratedScore) {
                            "Based on ${snapshot.smoothedCapacitySampleCount} charges"
                        } else {
                            snapshot.healthCalibrationStatusText
                        },
                        onClick = { showHealthModal = true },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .testTag("health_score_card")
                    )
                }

                // Dynamic Thermal Alert Banner when temperature >= 38.0°C
                if (isWarm) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isHot) ForensicsPalette.RedContainer else ForensicsPalette.AmberContainer)
                            .border(
                                1.dp,
                                if (isHot) ForensicsPalette.RedBorder else ForensicsPalette.AmberBorder,
                                RoundedCornerShape(12.dp)
                            )
                            .padding(14.dp)
                            .testTag("thermal_warning_banner")
                    ) {
                        Text(
                            text = snapshot.thermalAdvisoryTip
                                ?: "Your device is running warm, which may temporarily affect battery performance and measurement accuracy.",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isHot) ForensicsPalette.RedPrimary else ForensicsPalette.AmberPrimary
                        )
                    }
                }
            }
        }

        // REAL-TIME BATTERY DRAIN RATE MONITOR Card (Discharging Analytics: Active Use vs Standby)
        item {
            ForensicsCard(
                modifier = Modifier.testTag("drain_rate_monitor_card")
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
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 10.dp)
                        ) {
                            Text(
                                text = "DRAIN RATE MONITOR",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp,
                                color = ForensicsPalette.TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = snapshot.drainMonitorStatusText,
                                fontSize = 12.sp,
                                color = ForensicsPalette.TextMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            CardInfoIconButton(
                                topicKey = "drain_monitor",
                                onShowTopic = onShowTopicGuide
                            )
                            ClassificationBadge(
                                text = if (snapshot.isCharging) "Paused" else "Active",
                                containerColor = if (snapshot.isCharging) ForensicsPalette.AmberContainer else ForensicsPalette.GreenContainer,
                                contentColor = if (snapshot.isCharging) ForensicsPalette.AmberPrimary else ForensicsPalette.GreenPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Max),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val boxShape = RoundedCornerShape(12.dp)
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(boxShape)
                                .background(ForensicsPalette.BlueSoftTile)
                                .border(1.dp, ForensicsPalette.BlueBorder, boxShape)
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "SCREEN ON USE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = ForensicsPalette.BluePrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (snapshot.activeDrainRatePerHr > 0f) {
                                    String.format(java.util.Locale.US, "%.1f%% / hr", snapshot.activeDrainRatePerHr)
                                } else {
                                    "Measuring..."
                                },
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = ForensicsPalette.TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Active (${snapshot.activeDischargingMinutes}m)",
                                fontSize = 11.sp,
                                color = ForensicsPalette.TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(boxShape)
                                .background(ForensicsPalette.PurpleSoftTile)
                                .border(1.dp, ForensicsPalette.PurpleBorder, boxShape)
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "SCREEN OFF STANDBY",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = ForensicsPalette.PurplePrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (snapshot.standbyDrainRatePerHr > 0f) {
                                    String.format(java.util.Locale.US, "%.1f%% / hr", snapshot.standbyDrainRatePerHr)
                                } else {
                                    "Measuring..."
                                },
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = ForensicsPalette.TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Locked (${snapshot.standbyDischargingMinutes}m)",
                                fontSize = 11.sp,
                                color = ForensicsPalette.TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = if (snapshot.standbyDrainRatePerHr > 1.5f) {
                            "High standby drain noticed — background apps or weak signal are using battery while your screen is locked."
                        } else {
                            "Compares how fast your battery drains while you're using the screen vs. when your phone is locked."
                        },
                        fontSize = 11.sp,
                        color = ForensicsPalette.TextSecondary
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "LIVE PHONE STATUS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp,
                            color = ForensicsPalette.TextSecondary,
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp)
                        )
                        CardInfoIconButton(
                            topicKey = "phone_status",
                            onShowTopic = onShowTopicGuide
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Max),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        SystemStateTile(
                            label = "SCREEN",
                            value = snapshot.screenState,
                            bgColor = ForensicsPalette.BlueSoftTile,
                            borderColor = ForensicsPalette.BlueBorder,
                            textColor = ForensicsPalette.BlueBright,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                        SystemStateTile(
                            label = "SLEEP MODE",
                            value = snapshot.dozeState,
                            bgColor = ForensicsPalette.GraySoftTile,
                            borderColor = ForensicsPalette.GrayBorder,
                            textColor = ForensicsPalette.TextSecondary,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                        SystemStateTile(
                            label = "WI-FI",
                            value = snapshot.wifiState,
                            bgColor = ForensicsPalette.GreenSoftTile,
                            borderColor = ForensicsPalette.GreenBorder,
                            textColor = ForensicsPalette.GreenPrimary,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Max),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        SystemStateTile(
                            label = "MOBILE",
                            value = snapshot.mobileState,
                            bgColor = ForensicsPalette.PurpleSoftTile,
                            borderColor = ForensicsPalette.PurpleBorder,
                            textColor = ForensicsPalette.PurpleBright,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                        SystemStateTile(
                            label = "BLUETOOTH",
                            value = snapshot.bluetoothState,
                            bgColor = ForensicsPalette.BlueSoftTile,
                            borderColor = ForensicsPalette.BlueBorder,
                            textColor = ForensicsPalette.BlueBright,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                        SystemStateTile(
                            label = "LOCATION",
                            value = snapshot.locationState,
                            bgColor = ForensicsPalette.AmberSoftTile,
                            borderColor = ForensicsPalette.AmberBorder,
                            textColor = ForensicsPalette.AmberPrimary,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
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
                            text = "APP & SCREEN USAGE",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp,
                            color = ForensicsPalette.TextSecondary,
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        CardInfoIconButton(
                            topicKey = "insights_apps",
                            onShowTopic = onShowTopicGuide
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    if (activityEstimates.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(ForensicsPalette.SubtleSurface)
                                .border(1.dp, ForensicsPalette.BorderSubtle, RoundedCornerShape(12.dp))
                                .padding(14.dp)
                        ) {
                            Text(
                                text = if (snapshot.usageAccessGranted) {
                                    "Keep using your phone — app usage shares will appear here automatically as you use your apps."
                                } else {
                                    "Grant Usage Access in Settings → Permissions to see which apps use the most screen and background time."
                                },
                                fontSize = 13.sp,
                                color = ForensicsPalette.TextSecondary
                            )
                        }
                    } else {
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
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier
                                        .weight(1.35f)
                                        .padding(end = 8.dp)
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
                                    maxLines = 1,
                                    softWrap = false,
                                    modifier = Modifier.width(44.dp)
                                )
                            }
                            if (index < activityEstimates.lastIndex) {
                                Spacer(modifier = Modifier.height(2.dp))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Based on screen time and background app checks",
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "WHAT YOUR PHONE SUPPORTS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp,
                            color = ForensicsPalette.TextSecondary,
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp)
                        )
                        CardInfoIconButton(
                            topicKey = "phone_capabilities",
                            onShowTopic = onShowTopicGuide
                        )
                    }
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
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 8.dp)
                            ) {
                                CapabilityStatusGraphicBadge(status = cap.status)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = cap.name,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = ForensicsPalette.TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            if (cap.rightNote != null) {
                                Text(
                                    text = cap.rightNote,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = ForensicsPalette.TextSecondary,
                                    maxLines = 1,
                                    softWrap = false
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
        val isWarm = snapshot.temperatureCelsius >= 38.0f || snapshot.isThermalWarningActive
        val cappedEstMah = if (snapshot.designCapacityMah > 0 && snapshot.estimatedFullCapacityMah > 0) {
            snapshot.estimatedFullCapacityMah.coerceAtMost(snapshot.designCapacityMah)
        } else {
            snapshot.estimatedFullCapacityMah
        }
        val cycleLine = if (snapshot.isCycleCountHardwareMeasured) {
            "${snapshot.estimatedCycleCount} full charges (Reported by phone)"
        } else {
            "${snapshot.estimatedCycleCount} full charges (${snapshot.cycleCountProgressPercent}% toward next cycle · ${snapshot.accumulatedChargeMah} mAh charged)"
        }

        AlertDialog(
            onDismissRequest = { showHealthModal = false },
            title = {
                Text(
                    text = "Battery Health & Score Details",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (!snapshot.isHealthScoreCalibrating && snapshot.healthScore > 0) {
                            "Battery Health Score: ${snapshot.healthScore.coerceAtMost(100)} / 100"
                        } else {
                            "Health Score: Calibrating... — ${snapshot.healthCalibrationStatusText}"
                        },
                        fontWeight = FontWeight.SemiBold,
                        color = ForensicsPalette.GreenPrimary
                    )
                    Text(
                        text = "• Factory Capacity: ${if (snapshot.designCapacityMah > 0) "${snapshot.designCapacityMah} mAh" else "Waiting for first charge"}\n" +
                            "• Usable Capacity: ${if (cappedEstMah > 0) "~$cappedEstMah mAh (averaged over ${snapshot.smoothedCapacitySampleCount} charges)" else "Still learning — charge your phone normally"}\n" +
                            "• Charge Cycles: $cycleLine\n" +
                            "• Temperature: ${snapshot.temperatureStatus} (${snapshot.temperatureCelsius}°C live)\n" +
                            "• Battery Condition: ${snapshot.healthLabel}",
                        fontSize = 13.sp,
                        color = ForensicsPalette.TextSecondary
                    )
                    if (isWarm) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(ForensicsPalette.AmberContainer)
                                .border(1.dp, ForensicsPalette.AmberBorder, RoundedCornerShape(10.dp))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = "Your device is running warm, which may temporarily affect battery performance and measurement accuracy.",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = ForensicsPalette.AmberPrimary
                            )
                        }
                    } else {
                        Text(
                            text = "Temperature is normal (< 38.0°C) — ideal for accurate battery health readings.",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = ForensicsPalette.GreenPrimary
                        )
                    }
                    HorizontalDivider(color = ForensicsPalette.DividerColor)
                    Text(
                        text = "How this works: Your score compares your battery's usable capacity against its original factory capacity across your last 5–10 charges (capped at 100%). Needs 3 full charges to finish learning.",
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
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = ForensicsPalette.TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = ForensicsPalette.TextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
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
    valueFontSize: Int = 26,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    ForensicsCard(
        modifier = modifier,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
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
                    color = ForensicsPalette.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 6.dp)
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
                            fontSize = valueFontSize.sp,
                            fontWeight = FontWeight.Bold,
                            color = valueColor
                        )
                    ) {
                        append(mainValue)
                    }
                    withStyle(
                        SpanStyle(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ForensicsPalette.TextSecondary
                        )
                    ) {
                        append(unit)
                    }
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = ForensicsPalette.TextMuted,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
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
            .padding(vertical = 12.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.4.sp,
            color = textColor.copy(alpha = 0.85f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}
