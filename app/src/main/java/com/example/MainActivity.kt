package com.example

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.ForensicsViewModel
import com.example.ui.MainTab
import com.example.ui.components.ForensicsBottomBar
import com.example.ui.components.ForensicsTopAppBar
import com.example.ui.screens.AiBatteryDoctorSheet
import com.example.ui.screens.ChargingScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DiagnoseScreen
import com.example.ui.screens.InsightsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.ForensicsPalette
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                BatteryForensicsApp()
            }
        }
    }
}

@Composable
fun BatteryForensicsApp(
    viewModel: ForensicsViewModel = viewModel()
) {
    val context = LocalContext.current
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val diagnoseSubTab by viewModel.diagnoseSubTab.collectAsStateWithLifecycle()
    val insightsTimeframe by viewModel.insightsTimeframe.collectAsStateWithLifecycle()
    val chargingSubTab by viewModel.chargingSubTab.collectAsStateWithLifecycle()
    val settingsSection by viewModel.settingsSection.collectAsStateWithLifecycle()

    val liveTelemetry by viewModel.liveTelemetry.collectAsStateWithLifecycle()
    val capabilities by viewModel.capabilities.collectAsStateWithLifecycle()
    val diagnosticSessions by viewModel.diagnosticSessions.collectAsStateWithLifecycle()
    val selectedSessionId by viewModel.selectedSessionId.collectAsStateWithLifecycle()
    val showEvidenceChain by viewModel.showEvidenceChainExpanded.collectAsStateWithLifecycle()
    val allTimelineEvents by viewModel.allTimelineEvents.collectAsStateWithLifecycle()
    val recommendations by viewModel.recommendations.collectAsStateWithLifecycle()
    val experiments by viewModel.experiments.collectAsStateWithLifecycle()
    val chargingSessions by viewModel.chargingSessions.collectAsStateWithLifecycle()
    val chargerProfiles by viewModel.chargerProfiles.collectAsStateWithLifecycle()

    val adbModeEnabled by viewModel.adbModeEnabled.collectAsStateWithLifecycle()
    val localStorageOnly by viewModel.localStorageOnly.collectAsStateWithLifecycle()
    val alertDrainHigher by viewModel.alertDrainHigher.collectAsStateWithLifecycle()
    val alertTempHigh by viewModel.alertTempHigh.collectAsStateWithLifecycle()
    val alertChargingSlow by viewModel.alertChargingSlow.collectAsStateWithLifecycle()
    val alertUnusualBg by viewModel.alertUnusualBg.collectAsStateWithLifecycle()
    val alertCapacityChange by viewModel.alertCapacityChange.collectAsStateWithLifecycle()
    val alertSensitivity by viewModel.alertSensitivity.collectAsStateWithLifecycle()

    val activeTestRun by viewModel.activeTestRun.collectAsStateWithLifecycle()
    val aiDoctorHistory by viewModel.aiDoctorHistory.collectAsStateWithLifecycle()
    val aiDoctorLoading by viewModel.aiDoctorLoading.collectAsStateWithLifecycle()
    val statusBannerMessage by viewModel.statusBannerMessage.collectAsStateWithLifecycle()

    var showAiDoctorSheet by remember { mutableStateOf(false) }

    // Event-driven system BroadcastReceiver (zero continuous polling)
    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                viewModel.refreshTelemetry()
            }
        }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_BATTERY_CHANGED)
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
        }
        ContextCompat.registerReceiver(
            context,
            receiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        onDispose {
            try {
                context.unregisterReceiver(receiver)
            } catch (_: Exception) {
            }
        }
    }

    // BackHandler on secondary tabs returns to Home tab
    BackHandler(enabled = currentTab != MainTab.HOME) {
        viewModel.selectTab(MainTab.HOME)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = ForensicsPalette.ScreenBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            ForensicsTopAppBar(
                currentTab = currentTab,
                batteryPercent = liveTelemetry.batteryPercent,
                onOpenAiDoctor = { showAiDoctorSheet = true }
            )
        },
        bottomBar = {
            ForensicsBottomBar(
                currentTab = currentTab,
                onSelectTab = { viewModel.selectTab(it) }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 640.dp)
            ) {
                AnimatedVisibility(visible = statusBannerMessage != null) {
                    statusBannerMessage?.let { msg ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(ForensicsPalette.GreenPrimary)
                                .clickable { viewModel.clearBanner() }
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = msg,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "✕",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                when (currentTab) {
                    MainTab.HOME -> {
                        DashboardScreen(
                            snapshot = liveTelemetry,
                            activityEstimates = viewModel.activityEstimates,
                            capabilities = capabilities,
                            onRefresh = { viewModel.refreshTelemetry() },
                            onNavigateToDiagnose = { viewModel.selectTab(MainTab.DIAGNOSE) }
                        )
                    }
                    MainTab.DIAGNOSE -> {
                        val selectedSession = diagnosticSessions.firstOrNull { it.id == selectedSessionId }
                            ?: diagnosticSessions.firstOrNull()
                        val evidenceSteps = if (selectedSession != null) {
                            viewModel.getEvidenceChainForSession(selectedSession)
                        } else emptyList()
                        val sessionTimeline = allTimelineEvents.filter { it.sessionId == selectedSessionId }
                            .ifEmpty { allTimelineEvents }

                        DiagnoseScreen(
                            subTab = diagnoseSubTab,
                            onSelectSubTab = { viewModel.selectDiagnoseSubTab(it) },
                            sessions = diagnosticSessions,
                            selectedSessionId = selectedSessionId,
                            onSelectSession = { viewModel.selectSession(it) },
                            showEvidenceChain = showEvidenceChain,
                            onToggleEvidenceChain = { viewModel.toggleEvidenceChain() },
                            evidenceSteps = evidenceSteps,
                            timelineEvents = sessionTimeline,
                            recommendations = recommendations,
                            experiments = experiments,
                            adbModeEnabled = adbModeEnabled,
                            adbWakelocks = viewModel.getAdbWakelocks(),
                            activeTestRun = activeTestRun,
                            onStartTest = { name, dur, desc -> viewModel.startDiagnosticTest(name, dur, desc) },
                            onCompleteTest = { viewModel.completeAndSaveActiveTest() },
                            onCancelTest = { viewModel.cancelActiveTest() },
                            onCreateExperiment = { t, h, c -> viewModel.createControlledExperiment(t, h, c) },
                            onAddTimelineAnnotation = { t, d -> viewModel.addTimelineAnnotation(t, d) },
                            onNavigateToAdbSettings = { viewModel.navigateToEnableAdbMode() },
                            onOpenAiDoctor = { showAiDoctorSheet = true }
                        )
                    }
                    MainTab.INSIGHTS -> {
                        InsightsScreen(
                            timeframe = insightsTimeframe,
                            onSelectTimeframe = { viewModel.selectInsightsTimeframe(it) },
                            weeklyPoints = viewModel.weeklyDrainPoints,
                            dailyPoints = viewModel.dailyDrainPoints,
                            monthlyPoints = viewModel.monthlyDrainPoints,
                            appInsights = viewModel.appActivityInsights,
                            onInvestigateAnomaly = { viewModel.investigateThursdayAnomaly() }
                        )
                    }
                    MainTab.CHARGING -> {
                        ChargingScreen(
                            snapshot = liveTelemetry,
                            subTab = chargingSubTab,
                            onSelectSubTab = { viewModel.selectChargingSubTab(it) },
                            chargingSessions = chargingSessions,
                            chargerProfiles = chargerProfiles,
                            onAddChargerProfile = { name, maxW, avgW, temp ->
                                viewModel.createChargerProfile(name, maxW, avgW, temp)
                            }
                        )
                    }
                    MainTab.SETTINGS -> {
                        SettingsScreen(
                            section = settingsSection,
                            onSelectSection = { viewModel.selectSettingsSection(it) },
                            snapshot = liveTelemetry,
                            manufacturerProfile = viewModel.manufacturerProfile,
                            adbModeEnabled = adbModeEnabled,
                            onToggleAdbMode = { viewModel.setAdbModeEnabled(it) },
                            localStorageOnly = localStorageOnly,
                            onToggleLocalStorage = { viewModel.setLocalStorageOnly(it) },
                            alertDrainHigher = alertDrainHigher,
                            alertTempHigh = alertTempHigh,
                            alertChargingSlow = alertChargingSlow,
                            alertUnusualBg = alertUnusualBg,
                            alertCapacityChange = alertCapacityChange,
                            onToggleAlert = { type, enabled -> viewModel.setAlertToggle(type, enabled) },
                            alertSensitivity = alertSensitivity,
                            onSelectSensitivity = { viewModel.setAlertSensitivity(it) },
                            onExportReport = { fmt -> viewModel.exportReport(fmt) },
                            onDeleteAllData = { reseed -> viewModel.deleteAllUserData(reseed) },
                            onRunConsoleCommand = { cmd -> viewModel.runConsoleCommand(cmd) },
                            onPermissionsUpdated = { viewModel.refreshTelemetry() }
                        )
                    }
                }
            }
        }
    }

    if (showAiDoctorSheet) {
        AiBatteryDoctorSheet(
            history = aiDoctorHistory,
            isLoading = aiDoctorLoading,
            onAskQuestion = { viewModel.askAiBatteryDoctor(it) },
            onDismiss = { showAiDoctorSheet = false }
        )
    }
}
