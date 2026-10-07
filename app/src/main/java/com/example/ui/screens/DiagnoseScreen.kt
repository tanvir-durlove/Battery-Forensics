package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AdbWakelockEntry
import com.example.data.AppActivityInsight
import com.example.data.DiagnosticSessionEntity
import com.example.data.EvidenceChainStep
import com.example.data.EvidenceRecommendation
import com.example.data.ExperimentEntity
import com.example.data.TimelineEventEntity
import com.example.ui.ActiveDiagnosticTestRun
import com.example.ui.DiagnoseSubTab
import com.example.ui.components.CardInfoIconButton
import com.example.ui.components.ClassificationBadge
import com.example.ui.components.DiagnosticTestGraphicIcon
import com.example.ui.components.ForensicsCard
import com.example.ui.components.InlineForensicsAdBannerCard
import com.example.ui.components.SegmentedPillSelector
import com.example.ui.components.StatGuideTopic
import com.example.ui.components.UsagePermissionCard
import com.example.ui.theme.ForensicsPalette

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DiagnoseScreen(
    subTab: DiagnoseSubTab,
    onSelectSubTab: (DiagnoseSubTab) -> Unit,
    sessions: List<DiagnosticSessionEntity>,
    selectedSessionId: String,
    onSelectSession: (String) -> Unit,
    showEvidenceChain: Boolean,
    onToggleEvidenceChain: () -> Unit,
    evidenceSteps: List<EvidenceChainStep>,
    timelineEvents: List<TimelineEventEntity>,
    recommendations: List<EvidenceRecommendation>,
    experiments: List<ExperimentEntity>,
    adbModeEnabled: Boolean,
    adbWakelocks: List<AdbWakelockEntry>,
    activeTestRun: ActiveDiagnosticTestRun?,
    onStartTest: (String, String, String) -> Unit,
    onCompleteTest: () -> Unit,
    onCancelTest: () -> Unit,
    onCreateExperiment: (String, String, String) -> Unit,
    onAddTimelineAnnotation: (String, String) -> Unit,
    onNavigateToAdbSettings: () -> Unit,
    onOpenAiDoctor: () -> Unit,
    videoAd1ShownCount: Int = 0,
    onTriggerVideoAd1: () -> Unit = {},
    onShowTopicGuide: (StatGuideTopic) -> Unit = {},
    needsUsagePermission: Boolean = false,
    isDemoData: Boolean = false,
    appInsights: List<AppActivityInsight> = emptyList(),
    sampleAppInsights: List<AppActivityInsight> = emptyList(),
    onOpenUsageSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val selectedSession = sessions.firstOrNull { it.id == selectedSessionId } ?: sessions.firstOrNull()
    var showAnnotateDialog by remember { mutableStateOf(false) }
    var showNewExperimentDialog by remember { mutableStateOf(false) }
    var showComparisonDialog by remember { mutableStateOf(false) }
    var isPreviewingSampleData by rememberSaveable { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ForensicsPalette.ScreenBackground)
            .padding(horizontal = 16.dp)
            .testTag("diagnose_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(6.dp))
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
                        text = "Drain Detective",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = ForensicsPalette.TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "See clear reasons why your battery drained.",
                        fontSize = 13.sp,
                        color = ForensicsPalette.TextSecondary
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(ForensicsPalette.BlueContainer)
                        .border(1.dp, ForensicsPalette.BlueBorder, RoundedCornerShape(999.dp))
                        .clickable { onOpenAiDoctor() }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                        .testTag("diagnose_ask_ai_button")
                ) {
                    Text(
                        text = "Ask Why →",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = ForensicsPalette.BluePrimary,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }

        item {
            SegmentedPillSelector(
                options = listOf("Show Me Why", "Tests & Experiments"),
                selectedIndex = if (subTab == DiagnoseSubTab.SHOW_ME_WHY) 0 else 1,
                onSelect = { idx ->
                    onSelectSubTab(
                        if (idx == 0) DiagnoseSubTab.SHOW_ME_WHY else DiagnoseSubTab.TESTS_AND_EXPERIMENTS
                    )
                },
                activeTextColor = ForensicsPalette.BluePrimary
            )
        }

        // Native Ad Card (Above the Fold — Top of Diagnose Tab)
        item {
            InlineForensicsAdBannerCard(
                placementLabel = "Hardware Diagnostics",
                tagName = "diagnose_inline_ad_card"
            )
        }

        if (subTab == DiagnoseSubTab.SHOW_ME_WHY) {
            if (selectedSession == null) {
                item {
                    ForensicsCard(
                        containerColor = ForensicsPalette.BlueSoftTile,
                        modifier = Modifier.testTag("diagnose_data_collection_card")
                    ) {
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
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ForensicsPalette.BluePrimary,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 8.dp)
                                )
                                ClassificationBadge(
                                    text = "Calibrating",
                                    containerColor = ForensicsPalette.BlueContainer,
                                    contentColor = ForensicsPalette.BluePrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            CardInfoIconButton(
                                topicKey = "diagnose_drain",
                                onShowTopic = onShowTopicGuide
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Keep using your phone normally — live charging and battery drain are being tracked. Detailed drain breakdowns and top battery users will appear here once enough history is recorded.",
                                fontSize = 13.sp,
                                color = ForensicsPalette.TextPrimary
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = { onSelectSubTab(DiagnoseSubTab.TESTS_AND_EXPERIMENTS) },
                                colors = ButtonDefaults.buttonColors(containerColor = ForensicsPalette.BluePrimary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "Run a Live Diagnostic Test Now →",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                item {
                    ForensicsCard(containerColor = ForensicsPalette.SubtleSurface) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp)
                        ) {
                            if (!adbModeEnabled) {
                                Text(
                                    text = "Standard Mode Limitations",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ForensicsPalette.TextPrimary
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                LimitationBullet("Exact per-app battery % — hidden by Android")
                                Spacer(modifier = Modifier.height(6.dp))
                                LimitationBullet("Background wakeups — PC connection (ADB) needed")
                                Spacer(modifier = Modifier.height(6.dp))
                                LimitationBullet("Deep sleep time — hidden by phone brand")
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Enable ADB Mode →",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ForensicsPalette.BluePrimary,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable { onNavigateToAdbSettings() }
                                        .padding(vertical = 2.dp)
                                        .testTag("enable_adb_mode_link")
                                )
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "ADB-Derived Diagnostic Data",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ForensicsPalette.GreenPrimary,
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(end = 8.dp)
                                    )
                                    ClassificationBadge(
                                        text = "ADB Mode Active",
                                        containerColor = ForensicsPalette.GreenContainer,
                                        contentColor = ForensicsPalette.GreenPrimary
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                adbWakelocks.forEach { wl ->
                                    Column(modifier = Modifier.padding(vertical = 5.dp)) {
                                        Text(
                                            text = wl.tag,
                                            fontSize = 12.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
                                            color = ForensicsPalette.TextPrimary
                                        )
                                        Text(
                                            text = "${wl.type} · ${wl.ownerAppOrProcess} · ${wl.totalDuration}",
                                            fontSize = 11.sp,
                                            color = ForensicsPalette.TextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }
            } else {
            // SELECT PERIOD Card (Screenshot 2)
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
                                    text = "SELECT PERIOD",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp,
                                    color = ForensicsPalette.TextSecondary
                                )
                                CardInfoIconButton(
                                    topicKey = "diagnose_drain",
                                    onShowTopic = onShowTopicGuide
                                )
                            }
                            Text(
                                text = "Compare Periods ⇄",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = ForensicsPalette.BluePrimary,
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(ForensicsPalette.BlueSoftTile)
                                    .border(1.dp, ForensicsPalette.BlueBorder, RoundedCornerShape(999.dp))
                                    .clickable { showComparisonDialog = true }
                                    .padding(horizontal = 12.dp, vertical = 5.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            sessions.forEach { session ->
                                val isSelected = session.id == selectedSession.id
                                val rowBg = if (isSelected) ForensicsPalette.BlueContainer else ForensicsPalette.SubtleSurface
                                val rowBorder = if (isSelected) ForensicsPalette.BlueBorder else ForensicsPalette.BorderStrong
                                val titleColor = if (isSelected) ForensicsPalette.BluePrimary else ForensicsPalette.TextPrimary
                                val drainColor = if (session.isAnomaly) ForensicsPalette.RedPrimary else ForensicsPalette.TextPrimary
                                val rowShape = RoundedCornerShape(14.dp)

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(rowShape)
                                        .background(rowBg)
                                        .border(1.dp, rowBorder, rowShape)
                                        .clickable { onSelectSession(session.id) }
                                        .padding(horizontal = 16.dp, vertical = 13.dp)
                                        .testTag("period_item_${session.id}"),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(end = 10.dp)
                                    ) {
                                        Text(
                                            text = session.title,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = titleColor,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = session.timeWindow,
                                            fontSize = 12.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = ForensicsPalette.TextSecondary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        val isDemoSession = isDemoData || session.overallConfidence.contains("Demo", ignoreCase = true) || session.id in listOf("last_night", "tuesday_night", "monday_night", "sunday_night")
                                        if (isDemoSession) {
                                            ClassificationBadge(
                                                text = "Demo",
                                                containerColor = ForensicsPalette.AmberPill,
                                                contentColor = ForensicsPalette.AmberPrimary
                                            )
                                        }
                                        if (session.isAnomaly) {
                                            ClassificationBadge(
                                                text = "Anomaly",
                                                containerColor = ForensicsPalette.AmberPill,
                                                contentColor = ForensicsPalette.AmberPrimary
                                            )
                                        }
                                        Text(
                                            text = "${session.drainPercent}%",
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = drainColor
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Summary Banner Card (Screenshot 2: Last night · 8 hours | 14% drained)
            item {
                val cardBg = if (selectedSession.isAnomaly) ForensicsPalette.AmberContainer else ForensicsPalette.GreenSoftTile
                val innerBg = if (selectedSession.isAnomaly) ForensicsPalette.AmberInnerCard else ForensicsPalette.CardSurface
                val innerBorder = if (selectedSession.isAnomaly) ForensicsPalette.AmberBorder else ForensicsPalette.GreenBorder

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
                            Text(
                                text = "${selectedSession.title} · ${selectedSession.durationHoursLabel}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = ForensicsPalette.AmberDarkText,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 8.dp)
                            )
                            val isSelectedDemo = isDemoData || selectedSession.id in listOf("last_night", "tuesday_night", "monday_night", "sunday_night") || selectedSession.overallConfidence.contains("Demo", ignoreCase = true)
                            val badgeLabel = if (isSelectedDemo) "Demo" else selectedSession.overallConfidence
                            ClassificationBadge(
                                text = badgeLabel,
                                containerColor = if (isSelectedDemo) ForensicsPalette.AmberPill else ForensicsPalette.BlueContainer,
                                contentColor = if (isSelectedDemo) ForensicsPalette.AmberPrimary else ForensicsPalette.BluePrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${selectedSession.drainPercent}% drained",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = ForensicsPalette.TextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${selectedSession.multiplierVsNormal}× your normal overnight average",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = ForensicsPalette.AmberDarkText
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(IntrinsicSize.Max),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            val subShape = RoundedCornerShape(14.dp)
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(subShape)
                                    .background(innerBg)
                                    .border(1.dp, innerBorder, subShape)
                                    .padding(14.dp)
                            ) {
                                Text(
                                    text = "DRAIN SPEED",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.6.sp,
                                    color = ForensicsPalette.AmberPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${selectedSession.drainRatePerHr}%/hr",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ForensicsPalette.TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = "Usual: ${selectedSession.normalDrainRatePerHr}%/hr",
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
                                    .clip(subShape)
                                    .background(innerBg)
                                    .border(1.dp, innerBorder, subShape)
                                    .padding(14.dp)
                            ) {
                                Text(
                                    text = "BATTERY USED",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.6.sp,
                                    color = ForensicsPalette.AmberPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${selectedSession.startBatteryPercent}%→${selectedSession.endBatteryPercent}%",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ForensicsPalette.TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = "${selectedSession.drainPercent}% total drop",
                                    fontSize = 11.sp,
                                    color = ForensicsPalette.TextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            // 4 Contributor Cards (Primary, Secondary, Factor, Possible)
            item {
                ContributorFindingCard(
                    roleLabel = "Primary",
                    roleBg = ForensicsPalette.PurpleContainer,
                    roleTextColor = ForensicsPalette.PurplePrimary,
                    confidenceLabel = selectedSession.primaryConfidence,
                    confidenceBg = ForensicsPalette.BlueContainer,
                    confidenceTextColor = ForensicsPalette.BluePrimary,
                    title = selectedSession.primaryTitle,
                    subtitle = selectedSession.primarySubtitle
                )
            }
            item {
                ContributorFindingCard(
                    roleLabel = "Secondary",
                    roleBg = ForensicsPalette.AmberPill,
                    roleTextColor = ForensicsPalette.AmberPrimary,
                    confidenceLabel = selectedSession.secondaryConfidence,
                    confidenceBg = ForensicsPalette.PurpleContainer,
                    confidenceTextColor = ForensicsPalette.PurplePrimary,
                    title = selectedSession.secondaryTitle,
                    subtitle = selectedSession.secondarySubtitle
                )
            }
            item {
                ContributorFindingCard(
                    roleLabel = "Factor",
                    roleBg = ForensicsPalette.BlueContainer,
                    roleTextColor = ForensicsPalette.BluePrimary,
                    confidenceLabel = selectedSession.factorConfidence,
                    confidenceBg = ForensicsPalette.BlueContainer,
                    confidenceTextColor = ForensicsPalette.BluePrimary,
                    title = selectedSession.factorTitle,
                    subtitle = selectedSession.factorSubtitle
                )
            }
            item {
                ContributorFindingCard(
                    roleLabel = "Possible",
                    roleBg = ForensicsPalette.SubtleSurfaceAlt,
                    roleTextColor = ForensicsPalette.TextSecondary,
                    confidenceLabel = selectedSession.possibleConfidence,
                    confidenceBg = ForensicsPalette.AmberPill,
                    confidenceTextColor = ForensicsPalette.AmberPrimary,
                    title = selectedSession.possibleTitle,
                    subtitle = selectedSession.possibleSubtitle
                )
            }

            // App Activity & Attribution Section
            item {
                if (needsUsagePermission && !isPreviewingSampleData) {
                    UsagePermissionCard(
                        onGrantPermission = onOpenUsageSettings,
                        onPreviewSampleData = { isPreviewingSampleData = true }
                    )
                } else {
                    val effectiveApps = if (needsUsagePermission && isPreviewingSampleData) sampleAppInsights else appInsights
                    ForensicsCard(modifier = Modifier.testTag("diagnose_app_activity_card")) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp)
                        ) {
                            if (needsUsagePermission && isPreviewingSampleData) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(ForensicsPalette.AmberContainer)
                                        .border(1.dp, ForensicsPalette.AmberBorder, RoundedCornerShape(10.dp))
                                        .padding(horizontal = 12.dp, vertical = 9.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f).padding(end = 8.dp)
                                        ) {
                                            ClassificationBadge(
                                                text = "SAMPLE",
                                                containerColor = ForensicsPalette.AmberPill,
                                                contentColor = ForensicsPalette.AmberPrimary
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Sample data — not measured on this device.",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = ForensicsPalette.AmberDarkText
                                            )
                                        }
                                        Text(
                                            text = "Grant Access →",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ForensicsPalette.BluePrimary,
                                            modifier = Modifier.clickable { onOpenUsageSettings() }
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(14.dp))
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "APP ACTIVITY & ATTRIBUTION",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp,
                                    color = ForensicsPalette.TextSecondary
                                )
                                Text(
                                    text = "${effectiveApps.size} apps detected",
                                    fontSize = 11.sp,
                                    color = ForensicsPalette.TextMuted
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Foreground screen duration and background wakeups mapped directly to measured drain windows.",
                                fontSize = 12.sp,
                                color = ForensicsPalette.TextSecondary,
                                lineHeight = 17.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            if (effectiveApps.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(ForensicsPalette.SubtleSurface)
                                        .border(1.dp, ForensicsPalette.BorderSubtle, RoundedCornerShape(12.dp))
                                        .padding(14.dp)
                                ) {
                                    Text(
                                        text = "No abnormal foreground compute or background wake events detected.",
                                        fontSize = 13.sp,
                                        color = ForensicsPalette.TextSecondary
                                    )
                                }
                            } else {
                                effectiveApps.forEachIndexed { idx, app ->
                                    val (avatarBg, avatarBorder, avatarText) = when {
                                        app.culpritType?.contains("Overheat", ignoreCase = true) == true -> Triple(ForensicsPalette.RedContainer, ForensicsPalette.RedBorder, ForensicsPalette.RedPrimary)
                                        app.culpritType?.contains("Vampire", ignoreCase = true) == true -> Triple(ForensicsPalette.AmberSoftTile, ForensicsPalette.AmberBorder, ForensicsPalette.AmberPrimary)
                                        else -> Triple(ForensicsPalette.BlueSoftTile, ForensicsPalette.BlueBorder, ForensicsPalette.BluePrimary)
                                    }
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f).padding(end = 8.dp)
                                        ) {
                                            val shape = RoundedCornerShape(8.dp)
                                            Box(
                                                modifier = Modifier
                                                    .size(34.dp)
                                                    .clip(shape)
                                                    .background(avatarBg)
                                                    .border(1.dp, avatarBorder, shape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = app.initial,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = avatarText
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = app.appName,
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = ForensicsPalette.TextPrimary,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                    if (needsUsagePermission && isPreviewingSampleData) {
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        ClassificationBadge(
                                                            text = "SAMPLE",
                                                            containerColor = ForensicsPalette.AmberPill,
                                                            contentColor = ForensicsPalette.AmberPrimary
                                                        )
                                                    }
                                                }
                                                Text(
                                                    text = "${app.foregroundDurationLabel} screen · ${app.backgroundEventsCount} bg wakeups",
                                                    fontSize = 11.sp,
                                                    fontFamily = FontFamily.Monospace,
                                                    color = ForensicsPalette.TextSecondary
                                                )
                                            }
                                        }
                                        if (app.culpritType != null) {
                                            ClassificationBadge(
                                                text = app.culpritType,
                                                containerColor = avatarBg,
                                                contentColor = avatarText
                                            )
                                        }
                                    }
                                    if (idx < effectiveApps.lastIndex) {
                                        HorizontalDivider(color = ForensicsPalette.DividerColor, thickness = 0.8.dp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // View Evidence Chain Button (Screenshot 2)
            item {
                val btnShape = RoundedCornerShape(16.dp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(btnShape)
                        .background(ForensicsPalette.BlueContainer)
                        .border(1.dp, ForensicsPalette.BlueBorder, btnShape)
                        .clickable { onToggleEvidenceChain() }
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                        .testTag("view_evidence_chain_button"),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (showEvidenceChain) "Hide Evidence Chain & Timeline" else "View Evidence Chain",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = ForensicsPalette.BluePrimary
                    )
                    Text(
                        text = if (showEvidenceChain) "↑" else "↓",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = ForensicsPalette.BluePrimary
                    )
                }
            }

            // Expanded Evidence Chain + Synchronized Timeline + Recommendations & Before/After Verification
            item {
                AnimatedVisibility(visible = showEvidenceChain) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        // 1. Step-by-Step Evidence Chain Card
                        ForensicsCard {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp)
                            ) {
                                Text(
                                    text = "STEP-BY-STEP DRAIN BREAKDOWN",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp,
                                    color = ForensicsPalette.BluePrimary
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                evidenceSteps.forEachIndexed { idx, step ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .weight(1f)
                                                .padding(end = 8.dp)
                                        ) {
                                            Text(
                                                text = step.observation,
                                                fontSize = 14.sp,
                                                fontWeight = if (idx == evidenceSteps.lastIndex) FontWeight.Bold else FontWeight.Medium,
                                                color = if (idx == evidenceSteps.lastIndex) ForensicsPalette.GreenPrimary else ForensicsPalette.TextPrimary
                                            )
                                            Text(
                                                text = step.timestampOrDuration,
                                                fontSize = 11.sp,
                                                fontFamily = FontFamily.Monospace,
                                                color = ForensicsPalette.TextMuted
                                            )
                                        }
                                        ClassificationBadge(
                                            text = step.classification.label,
                                            containerColor = ForensicsPalette.SubtleSurfaceAlt,
                                            contentColor = ForensicsPalette.TextSecondary
                                        )
                                    }
                                    if (idx < evidenceSteps.lastIndex) {
                                        Text(
                                            text = "↓",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ForensicsPalette.BluePrimary,
                                            modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // 2. Synchronized Battery Drain Timeline + Manual Event Annotation
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
                                        text = "BATTERY DRAIN TIMELINE",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.8.sp,
                                        color = ForensicsPalette.TextSecondary,
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(end = 8.dp)
                                    )
                                    Text(
                                        text = "+ Add Note",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ForensicsPalette.BluePrimary,
                                        maxLines = 1,
                                        softWrap = false,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(999.dp))
                                            .background(ForensicsPalette.BlueSoftTile)
                                            .border(1.dp, ForensicsPalette.BlueBorder, RoundedCornerShape(999.dp))
                                            .clickable { showAnnotateDialog = true }
                                            .padding(horizontal = 12.dp, vertical = 5.dp)
                                            .testTag("annotate_timeline_button")
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                timelineEvents.forEachIndexed { idx, ev ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 6.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Text(
                                            text = ev.timeLabel,
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.SemiBold,
                                            color = ForensicsPalette.BluePrimary,
                                            modifier = Modifier.width(68.dp)
                                        )
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "${ev.title} (${ev.batteryPercent}%)",
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = ForensicsPalette.TextPrimary
                                                )
                                                if (ev.isUserAnnotation) {
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    ClassificationBadge(
                                                        text = "Annotated",
                                                        containerColor = ForensicsPalette.PurpleContainer,
                                                        contentColor = ForensicsPalette.PurplePrimary
                                                    )
                                                }
                                            }
                                            Text(
                                                text = ev.detail,
                                                fontSize = 12.sp,
                                                color = ForensicsPalette.TextSecondary
                                            )
                                        }
                                    }
                                    if (idx < timelineEvents.lastIndex) {
                                        HorizontalDivider(color = ForensicsPalette.DividerColor, thickness = 0.8.dp)
                                    }
                                }
                            }
                        }

                        // 3. Evidence-Based Recommendations & Before/After Verification
                        ForensicsCard {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp)
                            ) {
                                Text(
                                    text = "WAYS TO SAVE BATTERY",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp,
                                    color = ForensicsPalette.GreenPrimary
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                recommendations.forEachIndexed { index, rec ->
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = rec.title,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = ForensicsPalette.TextPrimary,
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .padding(end = 8.dp)
                                            )
                                            ClassificationBadge(
                                                text = rec.verificationConfidence.label,
                                                containerColor = ForensicsPalette.GreenContainer,
                                                contentColor = ForensicsPalette.GreenPrimary
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Why: ${rec.whyRecommended}",
                                            fontSize = 12.sp,
                                            color = ForensicsPalette.TextSecondary
                                        )
                                        if (rec.afterDrainPercent != null) {
                                            Spacer(modifier = Modifier.height(6.dp))
                                            val verShape = RoundedCornerShape(10.dp)
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(verShape)
                                                    .background(ForensicsPalette.GreenSoftTile)
                                                    .border(1.dp, ForensicsPalette.GreenBorder, verShape)
                                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = "Before: ${rec.beforeDrainPercent}% → After: ${rec.afterDrainPercent}%",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = ForensicsPalette.GreenPrimary
                                                )
                                                Text(
                                                    text = "-${rec.beforeDrainPercent - rec.afterDrainPercent}% verified",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = ForensicsPalette.GreenPrimary
                                                )
                                            }
                                        }
                                    }
                                    if (index < recommendations.lastIndex) {
                                        HorizontalDivider(color = ForensicsPalette.DividerColor)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Standard Mode Limitations Card (or ADB Wakelock Table when ADB Mode is active)
            item {
                ForensicsCard(containerColor = ForensicsPalette.SubtleSurface) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        if (!adbModeEnabled) {
                            Text(
                                text = "Standard Mode Limitations",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = ForensicsPalette.TextPrimary
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            LimitationBullet("Exact per-app battery % — hidden by Android")
                            Spacer(modifier = Modifier.height(6.dp))
                            LimitationBullet("Background wakeups — PC connection (ADB) needed")
                            Spacer(modifier = Modifier.height(6.dp))
                            LimitationBullet("Deep sleep time — hidden by phone brand")
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Enable ADB Mode →",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = ForensicsPalette.BluePrimary,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { onNavigateToAdbSettings() }
                                    .padding(vertical = 2.dp)
                                    .testTag("enable_adb_mode_link")
                            )
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "ADB-Derived Diagnostic Data",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ForensicsPalette.GreenPrimary,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 8.dp)
                                )
                                ClassificationBadge(
                                    text = "ADB Mode Active",
                                    containerColor = ForensicsPalette.GreenContainer,
                                    contentColor = ForensicsPalette.GreenPrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            adbWakelocks.forEach { wl ->
                                Column(modifier = Modifier.padding(vertical = 5.dp)) {
                                    Text(
                                        text = wl.tag,
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        color = ForensicsPalette.TextPrimary
                                    )
                                    Text(
                                        text = "${wl.type} · ${wl.ownerAppOrProcess} · ${wl.totalDuration} (${wl.count}x)",
                                        fontSize = 11.sp,
                                        color = ForensicsPalette.TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
            }
        } else {
            // Sub-tab 2: Tests & Experiments (Screenshot 3)
            if (activeTestRun != null) {
                item {
                    ForensicsCard(containerColor = ForensicsPalette.BlueSoftTile) {
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
                                    text = "LIVE TEST: ${activeTestRun.testName.uppercase()}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ForensicsPalette.BluePrimary,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 8.dp)
                                )
                                ClassificationBadge(
                                    text = "Testing (${activeTestRun.elapsedSeconds}s)",
                                    containerColor = ForensicsPalette.BlueContainer,
                                    contentColor = ForensicsPalette.BluePrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { (activeTestRun.elapsedSeconds / 15f).coerceIn(0.05f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(CircleShape),
                                color = ForensicsPalette.BluePrimary,
                                trackColor = ForensicsPalette.CardSurface
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Start Battery: ${activeTestRun.startBatteryPercent}% · Temp: ${activeTestRun.currentTempCelsius}°C · ${activeTestRun.networkStateLabel}",
                                fontSize = 12.sp,
                                color = ForensicsPalette.TextSecondary
                            )
                            if (activeTestRun.integrityNote != null) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "⚠ ${activeTestRun.integrityNote}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = ForensicsPalette.AmberPrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Button(
                                    onClick = onCompleteTest,
                                    colors = ButtonDefaults.buttonColors(containerColor = ForensicsPalette.BluePrimary)
                                ) {
                                    Text("Finish & Save Session")
                                }
                                TextButton(onClick = onCancelTest) {
                                    Text("Cancel")
                                }
                            }
                        }
                    }
                }
            }

            item {
                DiagnosticTestCard(
                    testGraphicType = "idle",
                    iconBg = ForensicsPalette.BlueContainer,
                    iconAccent = ForensicsPalette.BluePrimary,
                    title = "Idle Test",
                    durationLabel = "~30 min",
                    description = "Screen-off drain measurement",
                    buttonBg = ForensicsPalette.BlueContainer,
                    buttonBorder = ForensicsPalette.BlueBorder,
                    buttonTextColor = ForensicsPalette.BluePrimary,
                    onStart = { onStartTest("Idle Test", "~30 min", "Screen-off drain measurement") }
                )
            }
            item {
                DiagnosticTestCard(
                    testGraphicType = "overnight",
                    iconBg = ForensicsPalette.PurpleContainer,
                    iconAccent = ForensicsPalette.PurplePrimary,
                    title = "Overnight Test",
                    durationLabel = "~8 hr",
                    description = "Full sleep drain analysis",
                    buttonBg = ForensicsPalette.PurpleContainer,
                    buttonBorder = ForensicsPalette.PurpleBorder,
                    buttonTextColor = ForensicsPalette.PurplePrimary,
                    onStart = { onStartTest("Overnight Test", "~8 hr", "Full sleep drain analysis") }
                )
            }
            item {
                DiagnosticTestCard(
                    testGraphicType = "screen",
                    iconBg = ForensicsPalette.GreenContainer,
                    iconAccent = ForensicsPalette.GreenPrimary,
                    title = "Screen Test",
                    durationLabel = "~15 min",
                    description = "Drain at different brightness levels",
                    buttonBg = ForensicsPalette.GreenContainer,
                    buttonBorder = ForensicsPalette.GreenBorder,
                    buttonTextColor = ForensicsPalette.GreenPrimary,
                    onStart = { onStartTest("Screen Test", "~15 min", "Drain at different brightness levels") }
                )
            }
            item {
                DiagnosticTestCard(
                    testGraphicType = "network",
                    iconBg = ForensicsPalette.AmberContainer,
                    iconAccent = ForensicsPalette.AmberPrimary,
                    title = "Network Test",
                    durationLabel = "~20 min",
                    description = "Wi-Fi vs cellular drain comparison",
                    buttonBg = ForensicsPalette.AmberContainer,
                    buttonBorder = ForensicsPalette.AmberBorder,
                    buttonTextColor = ForensicsPalette.AmberPrimary,
                    onStart = { onStartTest("Network Test", "~20 min", "Wi-Fi vs cellular drain comparison") }
                )
            }
            item {
                DiagnosticTestCard(
                    testGraphicType = "5g_lte",
                    iconBg = ForensicsPalette.AmberSoftTile,
                    iconAccent = ForensicsPalette.TemperatureOrange,
                    title = "5G vs LTE Test",
                    durationLabel = "~30 min",
                    description = "Network mode battery comparison",
                    buttonBg = ForensicsPalette.AmberContainer,
                    buttonBorder = ForensicsPalette.AmberBorder,
                    buttonTextColor = ForensicsPalette.AmberPrimary,
                    onStart = { onStartTest("5G vs LTE Test", "~30 min", "Network mode battery comparison") }
                )
            }

            // Controlled Experiment Mode Card (Screenshot 3)
            item {
                ForensicsCard(containerColor = ForensicsPalette.PurpleContainer) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Controlled Experiment Mode",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = ForensicsPalette.PurpleDeepButton
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        CardInfoIconButton(
                            topicKey = "diagnose_tests",
                            onShowTopic = onShowTopicGuide
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Test a simple question: \"Is this app or setting draining my battery?\" Compare before and after.",
                            fontSize = 13.sp,
                            color = ForensicsPalette.PurplePrimary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(ForensicsPalette.PurpleDeepButton)
                                .clickable { showNewExperimentDialog = true }
                                .padding(horizontal = 24.dp, vertical = 12.dp)
                                .testTag("new_experiment_button")
                        ) {
                            Text(
                                text = "New Experiment",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }

            // Recorded Controlled Experiments
            if (experiments.isNotEmpty()) {
                item {
                    ForensicsCard {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp)
                        ) {
                            Text(
                                text = "RECORDED EXPERIMENTS",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp,
                                color = ForensicsPalette.TextSecondary
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            experiments.forEachIndexed { idx, exp ->
                                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = exp.title,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ForensicsPalette.TextPrimary,
                                            modifier = Modifier
                                                .weight(1f)
                                                .padding(end = 8.dp)
                                        )
                                        ClassificationBadge(
                                            text = exp.confidenceLabel,
                                            containerColor = ForensicsPalette.BlueContainer,
                                            contentColor = ForensicsPalette.BluePrimary
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = exp.findingSummary,
                                        fontSize = 13.sp,
                                        color = ForensicsPalette.TextSecondary
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = "Limitations: ${exp.limitationsNote}",
                                        fontSize = 11.sp,
                                        color = ForensicsPalette.TextMuted
                                    )
                                }
                                if (idx < experiments.lastIndex) {
                                    HorizontalDivider(color = ForensicsPalette.DividerColor)
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }

    if (showAnnotateDialog) {
        val presets = listOf(
            "Started gaming",
            "Connected charger",
            "Switched to 5G",
            "Installed app",
            "Updated app",
            "Turned on hotspot",
            "Started navigation"
        )
        var selectedPreset by remember { mutableStateOf(presets.first()) }
        var customNote by remember { mutableStateOf("User manual timeline annotation") }

        AlertDialog(
            onDismissRequest = { showAnnotateDialog = false },
            title = { Text("Annotate Timeline Event", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Select an event to synchronize with the Battery Drain Timeline:", fontSize = 13.sp)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        presets.forEach { preset ->
                            val active = preset == selectedPreset
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = if (active) ForensicsPalette.BlueContainer else ForensicsPalette.SubtleSurface,
                                modifier = Modifier.clickable { selectedPreset = preset }
                            ) {
                                Text(
                                    text = preset,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (active) ForensicsPalette.BluePrimary else ForensicsPalette.TextSecondary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                    OutlinedTextField(
                        value = customNote,
                        onValueChange = { customNote = it },
                        label = { Text("Context / Note") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    onAddTimelineAnnotation(selectedPreset, customNote)
                    showAnnotateDialog = false
                }) {
                    Text("Add to Timeline")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAnnotateDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showNewExperimentDialog) {
        var title by remember { mutableStateOf("Test 5G vs LTE Overnight Drain") }
        var hypothesis by remember { mutableStateOf("Does locking network to LTE reduce overnight standby drain in weak 5G coverage?") }

        AlertDialog(
            onDismissRequest = { showNewExperimentDialog = false },
            title = { Text("New Controlled Experiment", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Establishes baseline (0.66%–1.75%/hr), monitors test window, checks test integrity, and assigns evidence confidence.",
                        fontSize = 12.sp,
                        color = ForensicsPalette.TextSecondary
                    )
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Experiment Title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = hypothesis,
                        onValueChange = { hypothesis = it },
                        label = { Text("Hypothesis to Test") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    onCreateExperiment(title, hypothesis, "Controlled Hypothesis")
                    showNewExperimentDialog = false
                }) {
                    Text("Run & Compare")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewExperimentDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showComparisonDialog) {
        AlertDialog(
            onDismissRequest = { showComparisonDialog = false },
            title = { Text("Normal Night vs Problem Night", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Side-by-side forensic comparison (Tuesday Night vs Last Night):",
                        fontSize = 13.sp,
                        color = ForensicsPalette.TextSecondary
                    )
                    ComparisonMetricRow("Total Drain", "5% (Normal)", "14% (Anomaly)")
                    ComparisonMetricRow("Drain / Hour", "0.63%/hr", "1.75%/hr")
                    ComparisonMetricRow("Screen Off", "7h 56m", "7h 42m")
                    ComparisonMetricRow("Awake Off-Screen", "19m", "1h 18m")
                    ComparisonMetricRow("Top App Events", "System (6)", "Instagram (47)")
                    ComparisonMetricRow("Cellular Signal", "Wi-Fi (-54dBm)", "LTE 1–2 bars (2h 31m)")
                    ComparisonMetricRow("Peak Temp", "26.8°C", "31.4°C")
                }
            },
            confirmButton = {
                TextButton(onClick = { showComparisonDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
private fun ComparisonMetricRow(metric: String, normalVal: String, problemVal: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = metric, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
        Text(text = normalVal, fontSize = 12.sp, color = ForensicsPalette.GreenPrimary, modifier = Modifier.weight(1f))
        Text(text = problemVal, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ForensicsPalette.RedPrimary, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun ContributorFindingCard(
    roleLabel: String,
    roleBg: Color,
    roleTextColor: Color,
    confidenceLabel: String,
    confidenceBg: Color,
    confidenceTextColor: Color,
    title: String,
    subtitle: String
) {
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
                ClassificationBadge(
                    text = roleLabel,
                    containerColor = roleBg,
                    contentColor = roleTextColor
                )
                ClassificationBadge(
                    text = confidenceLabel,
                    containerColor = confidenceBg,
                    contentColor = confidenceTextColor
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = ForensicsPalette.TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                color = ForensicsPalette.TextSecondary
            )
        }
    }
}

@Composable
private fun LimitationBullet(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(5.dp)
                .clip(CircleShape)
                .background(ForensicsPalette.TextSecondary)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            fontSize = 13.sp,
            color = ForensicsPalette.TextSecondary
        )
    }
}

@Composable
private fun DiagnosticTestCard(
    testGraphicType: String,
    iconBg: Color,
    iconAccent: Color,
    title: String,
    durationLabel: String,
    description: String,
    buttonBg: Color,
    buttonBorder: Color,
    buttonTextColor: Color,
    onStart: () -> Unit
) {
    ForensicsCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp)
            ) {
                DiagnosticTestGraphicIcon(
                    testType = testGraphicType,
                    containerColor = iconBg,
                    accentColor = iconAccent
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = ForensicsPalette.TextPrimary
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = description,
                        fontSize = 13.sp,
                        color = ForensicsPalette.TextSecondary
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = durationLabel,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium,
                    color = ForensicsPalette.TextSecondary,
                    maxLines = 1,
                    softWrap = false
                )
                Spacer(modifier = Modifier.height(6.dp))
                val btnShape = RoundedCornerShape(999.dp)
                Box(
                    modifier = Modifier
                        .clip(btnShape)
                        .background(buttonBg)
                        .border(1.dp, buttonBorder, btnShape)
                        .clickable { onStart() }
                        .padding(horizontal = 18.dp, vertical = 8.dp)
                        .testTag("start_test_${title.lowercase().replace(" ", "_")}")
                ) {
                    Text(
                        text = "Start",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = buttonTextColor,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}
