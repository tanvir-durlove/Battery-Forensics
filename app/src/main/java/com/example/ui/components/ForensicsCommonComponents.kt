package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.outlined.BatteryStd
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.R
import com.example.data.RewardedAdManager
import com.example.ui.MainTab
import com.example.ui.theme.ForensicsPalette
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

@Composable
fun ForensicsTopAppBar(
    currentTab: MainTab,
    batteryPercent: Int,
    isAiDoctorUnlocked: Boolean = false,
    onOpenAiDoctor: () -> Unit,
    onOpenGuide: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = ForensicsPalette.CardSurface,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF12161B))
                            .border(1.dp, Color(0xFF2A3441), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_launcher_foreground),
                            contentDescription = "Battery Forensics Logo",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Battery Forensics",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = ForensicsPalette.TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val pillShape = RoundedCornerShape(999.dp)
                    // Quick access chip for AI Battery Doctor
                    Row(
                        modifier = Modifier
                            .clip(pillShape)
                            .background(ForensicsPalette.PurpleSoftTile)
                            .border(1.dp, ForensicsPalette.PurpleBorder, pillShape)
                            .clickable { onOpenAiDoctor() }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                            .testTag("top_bar_ai_doctor_button"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Psychology,
                            contentDescription = "AI Battery Doctor",
                            tint = ForensicsPalette.PurplePrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isAiDoctorUnlocked) "AI Doctor" else "Ask AI",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ForensicsPalette.PurplePrimary,
                            maxLines = 1,
                            softWrap = false
                        )
                    }

                    // Live Battery Icon + Percentage inside a subtle pill
                    Row(
                        modifier = Modifier
                            .clip(pillShape)
                            .background(ForensicsPalette.GreenSoftTile)
                            .border(1.dp, ForensicsPalette.GreenBorder, pillShape)
                            .padding(horizontal = 9.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MiniBatteryIndicator(percent = batteryPercent)
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "$batteryPercent%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = ForensicsPalette.GreenPrimary,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
            HorizontalDivider(color = ForensicsPalette.BorderSubtle, thickness = 1.dp)
        }
    }
}

@Composable
fun MiniBatteryIndicator(
    percent: Int,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(width = 25.dp, height = 13.dp)) {
        val bodyWidth = size.width - 3.dp.toPx()
        val bodyHeight = size.height
        val strokePx = 1.5.dp.toPx()

        // Outer battery outline
        drawRoundRect(
            color = ForensicsPalette.GreenPrimary,
            topLeft = Offset.Zero,
            size = Size(bodyWidth, bodyHeight),
            cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx()),
            style = Stroke(width = strokePx)
        )

        // Positive terminal nub
        val nubHeight = bodyHeight * 0.46f
        drawRoundRect(
            color = ForensicsPalette.GreenPrimary,
            topLeft = Offset(bodyWidth + 1.dp.toPx(), (bodyHeight - nubHeight) / 2f),
            size = Size(2.2.dp.toPx(), nubHeight),
            cornerRadius = CornerRadius(1.dp.toPx(), 1.dp.toPx())
        )

        // Inner fill bar
        val pad = 2.4.dp.toPx()
        val maxFillW = (bodyWidth - pad * 2).coerceAtLeast(0f)
        val fillW = maxFillW * (percent.coerceIn(0, 100) / 100f)
        drawRoundRect(
            color = ForensicsPalette.GreenPrimary,
            topLeft = Offset(pad, pad),
            size = Size(fillW, (bodyHeight - pad * 2).coerceAtLeast(0f)),
            cornerRadius = CornerRadius(1.5.dp.toPx(), 1.5.dp.toPx())
        )
    }
}

@Composable
fun ForensicsBottomBar(
    currentTab: MainTab,
    onSelectTab: (MainTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = ForensicsPalette.CardSurface,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
        ) {
            HorizontalDivider(color = ForensicsPalette.BorderSubtle, thickness = 1.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MainTab.entries.forEach { tab ->
                    val isSelected = tab == currentTab
                    val (activePillBg, activeBorder, activeColor) = when (tab) {
                        MainTab.HOME -> Triple(
                            ForensicsPalette.GreenContainer,
                            ForensicsPalette.GreenBorder,
                            ForensicsPalette.GreenPrimary
                        )
                        MainTab.DIAGNOSE -> Triple(
                            ForensicsPalette.BlueContainer,
                            ForensicsPalette.BlueBorder,
                            ForensicsPalette.BluePrimary
                        )
                        MainTab.INSIGHTS -> Triple(
                            ForensicsPalette.PurpleContainer,
                            ForensicsPalette.PurpleBorder,
                            ForensicsPalette.PurplePrimary
                        )
                        MainTab.CHARGING -> Triple(
                            ForensicsPalette.AmberContainer,
                            ForensicsPalette.AmberBorder,
                            ForensicsPalette.AmberPrimary
                        )
                        MainTab.SETTINGS -> Triple(
                            ForensicsPalette.GrayContainer,
                            ForensicsPalette.GrayBorder,
                            ForensicsPalette.GrayPrimary
                        )
                    }

                    val icon = when (tab) {
                        MainTab.HOME -> if (isSelected) Icons.Filled.Home else Icons.Outlined.Home
                        MainTab.DIAGNOSE -> if (isSelected) Icons.Filled.Search else Icons.Outlined.Search
                        MainTab.INSIGHTS -> Icons.AutoMirrored.Filled.TrendingUp
                        MainTab.CHARGING -> if (isSelected) Icons.Filled.BatteryChargingFull else Icons.Outlined.BatteryStd
                        MainTab.SETTINGS -> if (isSelected) Icons.Filled.Settings else Icons.Outlined.Settings
                    }

                    val pillColor by animateColorAsState(
                        targetValue = if (isSelected) activePillBg else Color.Transparent,
                        label = "navPillColor"
                    )
                    val contentColor by animateColorAsState(
                        targetValue = if (isSelected) activeColor else ForensicsPalette.TextSecondary,
                        label = "navContentColor"
                    )

                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onSelectTab(tab) }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                            .testTag("bottom_nav_${tab.name.lowercase()}"),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .width(56.dp)
                                .height(30.dp)
                                .clip(CircleShape)
                                .background(pillColor)
                                .then(
                                    if (isSelected) Modifier.border(1.dp, activeBorder, CircleShape)
                                    else Modifier
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = tab.label,
                                tint = contentColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = tab.label,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = contentColor,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ClassificationBadge(
    text: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    val borderTint = contentColor.copy(alpha = 0.24f)
    val pillShape = RoundedCornerShape(999.dp)
    Box(
        modifier = modifier
            .clip(pillShape)
            .background(containerColor)
            .border(1.dp, borderTint, pillShape)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = contentColor,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun SegmentedPillSelector(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    activeTextColor: Color = ForensicsPalette.BluePrimary,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(ForensicsPalette.SubtleSurfaceAlt)
            .border(1.dp, ForensicsPalette.BorderStrong, RoundedCornerShape(14.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        options.forEachIndexed { index, label ->
            val isSelected = index == selectedIndex
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp)
                    .then(
                        if (isSelected) {
                            Modifier
                                .shadow(2.dp, RoundedCornerShape(10.dp))
                                .clip(RoundedCornerShape(10.dp))
                                .background(ForensicsPalette.CardSurface)
                                .border(1.dp, ForensicsPalette.BorderSubtle, RoundedCornerShape(10.dp))
                        } else {
                            Modifier.clip(RoundedCornerShape(10.dp))
                        }
                    )
                    .clickable { onSelect(index) }
                    .padding(horizontal = 6.dp)
                    .testTag("segmented_option_${label.lowercase().replace(" ", "_")}"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                    color = if (isSelected) activeTextColor else ForensicsPalette.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun ForensicsCard(
    modifier: Modifier = Modifier,
    containerColor: Color = ForensicsPalette.CardSurface,
    borderColor: Color? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(20.dp)
    // Automatically choose a crisp contrasting border for every card surface so it never blends with background
    val resolvedBorderColor = borderColor ?: when (containerColor) {
        ForensicsPalette.AmberContainer, ForensicsPalette.AmberInnerCard -> ForensicsPalette.AmberBorder
        ForensicsPalette.GreenContainer, ForensicsPalette.GreenSoftTile -> ForensicsPalette.GreenBorder
        ForensicsPalette.BlueContainer, ForensicsPalette.BlueSoftTile -> ForensicsPalette.BlueBorder
        ForensicsPalette.PurpleContainer, ForensicsPalette.PurpleSoftTile -> ForensicsPalette.PurpleBorder
        ForensicsPalette.RedContainer -> ForensicsPalette.RedBorder
        ForensicsPalette.SubtleSurface, ForensicsPalette.SubtleSurfaceAlt -> ForensicsPalette.BorderStrong
        else -> ForensicsPalette.BorderSubtle
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clip(shape).clickable { onClick() } else Modifier),
        shape = shape,
        color = containerColor,
        border = BorderStroke(1.dp, resolvedBorderColor),
        tonalElevation = 0.dp,
        shadowElevation = 2.dp
    ) {
        content()
    }
}

@Composable
fun InlineForensicsAdBannerCard(
    placementLabel: String,
    tagName: String,
    modifier: Modifier = Modifier
) {
    ForensicsCard(
        modifier = modifier.testTag(tagName),
        containerColor = ForensicsPalette.CardSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ClassificationBadge(
                        text = "Ad",
                        containerColor = ForensicsPalette.SubtleSurfaceAlt,
                        contentColor = ForensicsPalette.TextSecondary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = placementLabel,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ForensicsPalette.TextSecondary
                    )
                }
                Text(
                    text = "Sponsored",
                    fontSize = 10.sp,
                    color = ForensicsPalette.TextMuted
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (!RewardedAdManager.isRobolectricTest()) {
                AndroidView(
                    modifier = Modifier.fillMaxWidth(),
                    factory = { ctx ->
                        AdView(ctx).apply {
                            setAdSize(AdSize.BANNER)
                            adUnitId = RewardedAdManager.BANNER_AD_UNIT_ID
                            adListener = object : com.google.android.gms.ads.AdListener() {
                                override fun onAdFailedToLoad(loadAdError: com.google.android.gms.ads.LoadAdError) {
                                    if (loadAdError.code == com.google.android.gms.ads.AdRequest.ERROR_CODE_NO_FILL) {
                                        post {
                                            try {
                                                adUnitId = RewardedAdManager.GOOGLE_SAMPLE_BANNER_ID
                                                loadAd(AdRequest.Builder().build())
                                            } catch (_: Throwable) {}
                                        }
                                    }
                                }
                            }
                            loadAd(AdRequest.Builder().build())
                        }
                    }
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(ForensicsPalette.SubtleSurface),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Google AdMob Active Banner",
                        fontSize = 12.sp,
                        color = ForensicsPalette.TextSecondary
                    )
                }
            }
        }
    }
}
