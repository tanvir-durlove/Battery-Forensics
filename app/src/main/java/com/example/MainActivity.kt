package com.example

import android.app.Activity
import android.os.Build
import android.os.Bundle
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.example.data.BatteryTrackingReceiver
import com.example.ui.ForensicsViewModel
import com.example.ui.InsightsTimeframe
import com.example.ui.MainTab
import com.example.ui.components.ForensicsBottomBar
import com.example.ui.components.ForensicsGuideAndOnboardingModal
import com.example.ui.components.ForensicsSplashScreen
import com.example.ui.components.ForensicsTopAppBar
import com.example.ui.components.StatGuideTopic
import com.example.ui.components.StatInfoBottomSheet
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
    val activityEstimates by viewModel.activityEstimates.collectAsStateWithLifecycle()
    val appActivityInsights by viewModel.appActivityInsights.collectAsStateWithLifecycle()
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
    val isAiDoctorSessionUnlocked by viewModel.isAiDoctorSessionUnlocked.collectAsStateWithLifecycle()
    val isExportSessionUnlocked by viewModel.isExportSessionUnlocked.collectAsStateWithLifecycle()
    val isDeepBenchmarkUnlocked by viewModel.isDeepBenchmarkUnlocked.collectAsStateWithLifecycle()
    val videoAd1ShownCount by viewModel.videoAd1ShownCount.collectAsStateWithLifecycle()
    val selectedAiDoctorQuestion by viewModel.selectedAiDoctorQuestion.collectAsStateWithLifecycle()
    val generatedAiDoctorPrompt by viewModel.generatedAiDoctorPrompt.collectAsStateWithLifecycle()
    val statusBannerMessage by viewModel.statusBannerMessage.collectAsStateWithLifecycle()
    val hasSeenOnboarding by viewModel.hasSeenOnboarding.collectAsStateWithLifecycle()

    val activity = context as? Activity
    var showAiDoctorSheet by remember { mutableStateOf(false) }
    val isRobolectric = remember { Build.FINGERPRINT.lowercase().contains("robolectric") }
    var showSplashScreen by rememberSaveable { mutableStateOf(!isRobolectric) }
    var showGuideModal by rememberSaveable { mutableStateOf(false) }
    var guideModalInitialTab by rememberSaveable { mutableIntStateOf(0) }
    var selectedStatTopic by remember { mutableStateOf<StatGuideTopic?>(null) }

    fun handleAiDoctorClick() {
        viewModel.requestEnterAiDoctor(
            activity = activity,
            onOpenSheet = {
                showAiDoctorSheet = true
            }
        )
    }

    fun handleTriggerVideoAd1() {
        viewModel.requestVideoAd1DiagnosticCompletion(activity = activity)
    }

    fun handleTriggerVideoAd2() {
        viewModel.requestVideoAd2ChargingBenchmark(activity = activity)
    }

    fun handleUnlockExport() {
        viewModel.requestUnlockExportWithRewardAd(activity = activity)
    }

    // Event-driven system BroadcastReceiver listening for ACTION_BATTERY_CHANGED (zero continuous polling)
    DisposableEffect(context) {
        val receiver = BatteryTrackingReceiver { event ->
            viewModel.onBatteryBroadcastEvent(event)
        }
        BatteryTrackingReceiver.register(context, receiver)
        onDispose {
            BatteryTrackingReceiver.unregister(context, receiver)
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
                isAiDoctorUnlocked = isAiDoctorSessionUnlocked,
                onOpenAiDoctor = { handleAiDoctorClick() },
                onOpenGuide = {
                    guideModalInitialTab = 0
                    showGuideModal = true
                }
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
                            activityEstimates = activityEstimates,
                            capabilities = capabilities,
                            isHistoryEmpty = diagnosticSessions.isEmpty() && chargingSessions.isEmpty(),
                            onRefresh = { viewModel.refreshTelemetry() },
                            onNavigateToDiagnose = { viewModel.selectTab(MainTab.DIAGNOSE) },
                            onShowTopicGuide = { selectedStatTopic = it }
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
                            onCompleteTest = {
                                viewModel.completeAndSaveActiveTest()
                                handleTriggerVideoAd1()
                            },
                            onCancelTest = { viewModel.cancelActiveTest() },
                            onCreateExperiment = { t, h, c ->
                                viewModel.createControlledExperiment(t, h, c)
                                handleTriggerVideoAd1()
                            },
                            onAddTimelineAnnotation = { t, d -> viewModel.addTimelineAnnotation(t, d) },
                            onNavigateToAdbSettings = { viewModel.navigateToEnableAdbMode() },
                            onOpenAiDoctor = { handleAiDoctorClick() },
                            videoAd1ShownCount = videoAd1ShownCount,
                            onTriggerVideoAd1 = { handleTriggerVideoAd1() },
                            onShowTopicGuide = { selectedStatTopic = it }
                        )
                    }
                    MainTab.INSIGHTS -> {
                        val weeklyPts = viewModel.getDrainPointsForSessions(diagnosticSessions, InsightsTimeframe.WEEK)
                        val dailyPts = viewModel.getDrainPointsForSessions(diagnosticSessions, InsightsTimeframe.DAY)
                        val monthlyPts = viewModel.getDrainPointsForSessions(diagnosticSessions, InsightsTimeframe.MONTH)
                        InsightsScreen(
                            timeframe = insightsTimeframe,
                            onSelectTimeframe = { viewModel.selectInsightsTimeframe(it) },
                            weeklyPoints = weeklyPts,
                            dailyPoints = dailyPts,
                            monthlyPoints = monthlyPts,
                            appInsights = appActivityInsights,
                            onInvestigateAnomaly = { viewModel.investigateThursdayAnomaly() },
                            onShowTopicGuide = { selectedStatTopic = it }
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
                            },
                            isDeepBenchmarkUnlocked = isDeepBenchmarkUnlocked,
                            onTriggerVideoAd2 = { handleTriggerVideoAd2() },
                            onShowTopicGuide = { selectedStatTopic = it }
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
                            onUnlockExport = { handleUnlockExport() },
                            onDeleteAllData = { reseed -> viewModel.deleteAllUserData(reseed) },
                            onRunConsoleCommand = { cmd -> viewModel.runConsoleCommand(cmd) },
                            onPermissionsUpdated = { viewModel.refreshTelemetry() },
                            isExportUnlocked = isExportSessionUnlocked,
                            onOpenOnboardingTour = {
                                guideModalInitialTab = 0
                                showGuideModal = true
                            },
                            onShowTopicGuide = { selectedStatTopic = it }
                        )
                    }
                }
            }
        }
    }

    selectedStatTopic?.let { topic ->
        StatInfoBottomSheet(
            topic = topic,
            onOpenFullGuide = {
                selectedStatTopic = null
                viewModel.navigateToFaqGuide()
            },
            onDismiss = { selectedStatTopic = null }
        )
    }

    if (showGuideModal || (!showSplashScreen && !hasSeenOnboarding && !isRobolectric)) {
        ForensicsGuideAndOnboardingModal(
            initialTab = guideModalInitialTab,
            onSelectStartingLevel = { level ->
                if (level == 2) {
                    viewModel.setAdbModeEnabled(true)
                } else {
                    viewModel.setAdbModeEnabled(false)
                }
            },
            onNavigateToPermissions = {
                viewModel.markOnboardingCompleted()
                showGuideModal = false
                viewModel.navigateToPermissionsSettings()
            },
            onDismiss = {
                viewModel.markOnboardingCompleted()
                showGuideModal = false
            }
        )
    }

    if (showAiDoctorSheet && isAiDoctorSessionUnlocked) {
        AiBatteryDoctorSheet(
            selectedQuestion = selectedAiDoctorQuestion,
            generatedPrompt = generatedAiDoctorPrompt,
            onSelectQuestion = { viewModel.generateDynamicPromptForQuestion(it) },
            onPromptCopied = { viewModel.notifyPromptCopied() },
            onDismiss = { showAiDoctorSheet = false }
        )
    }

    if (showSplashScreen) {
        ForensicsSplashScreen(
            onSplashFinished = { showSplashScreen = false }
        )
    }
}
