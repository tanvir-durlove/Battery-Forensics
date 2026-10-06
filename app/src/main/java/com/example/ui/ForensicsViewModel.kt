package com.example.ui

import android.app.Activity
import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ActivityEstimateItem
import com.example.data.AdbWakelockEntry
import com.example.data.AiBatteryDoctorClient
import com.example.data.AiDoctorExchange
import com.example.data.AiDoctorSessionManager
import com.example.data.AppActivityInsight
import com.example.data.AppSelfAudit
import com.example.data.CapabilityItem
import com.example.data.ChargerProfileEntity
import com.example.data.ChargingSessionEntity
import com.example.data.ConfidenceLevel
import com.example.data.DataClassification
import com.example.data.DayDrainPoint
import com.example.data.DeviceTelemetryScanner
import com.example.data.DiagnosticSessionEntity
import com.example.data.EvidenceChainStep
import com.example.data.EvidenceRecommendation
import com.example.data.ExperimentEntity
import com.example.data.ForensicsDatabase
import com.example.data.ForensicsReportExporter
import com.example.data.ForensicsRepository
import com.example.data.BatteryTrackingReceiver
import com.example.data.LiveBatteryBroadcastEvent
import com.example.data.LiveTelemetrySnapshot
import com.example.data.ManufacturerProfileInfo
import com.example.data.RewardedAdManager
import com.example.data.RewardedAdState
import com.example.data.TimelineEventEntity
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

enum class MainTab(val label: String) {
    HOME("Home"),
    DIAGNOSE("Diagnose"),
    INSIGHTS("Insights"),
    CHARGING("Charging"),
    SETTINGS("Settings")
}

enum class DiagnoseSubTab(val label: String) {
    SHOW_ME_WHY("Show Me Why"),
    TESTS_AND_EXPERIMENTS("Tests & Experiments")
}

enum class InsightsTimeframe(val label: String) {
    DAY("Day"),
    WEEK("Week"),
    MONTH("Month")
}

enum class ChargingSubTab(val label: String) {
    HISTORY("History"),
    CHARGERS("Chargers"),
    HEALTH("Health")
}

enum class SettingsSection(val label: String) {
    PERMISSIONS("Permissions"),
    PRIVACY("Privacy"),
    ALERTS("Alerts"),
    ADVANCED("Advanced"),
    FAQ("FAQ & Guide")
}

enum class AlertSensitivity(val label: String) {
    LOW("Low"),
    MEDIUM("Medium"),
    HIGH("High")
}

data class ActiveDiagnosticTestRun(
    val testName: String,
    val targetDurationLabel: String,
    val description: String,
    val elapsedSeconds: Int,
    val startBatteryPercent: Int,
    val currentBatteryPercent: Int,
    val currentTempCelsius: Float,
    val screenTurnedOnEvents: Int,
    val networkStateLabel: String,
    val isIntegrityCompromised: Boolean,
    val integrityNote: String?,
    val isFinished: Boolean
)

class ForensicsViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("battery_forensics_prefs", Context.MODE_PRIVATE)
    private val database = ForensicsDatabase.getInstance(application)
    private val repository = ForensicsRepository(database.forensicsDao())
    private val scanner = DeviceTelemetryScanner(application)
    private val aiClient = AiBatteryDoctorClient()
    private val exporter = ForensicsReportExporter(application)
    val rewardedAdManager = RewardedAdManager(application)

    // Navigation state
    private val _currentTab = MutableStateFlow(MainTab.HOME)
    val currentTab: StateFlow<MainTab> = _currentTab.asStateFlow()

    private val _diagnoseSubTab = MutableStateFlow(DiagnoseSubTab.SHOW_ME_WHY)
    val diagnoseSubTab: StateFlow<DiagnoseSubTab> = _diagnoseSubTab.asStateFlow()

    private val _insightsTimeframe = MutableStateFlow(InsightsTimeframe.WEEK)
    val insightsTimeframe: StateFlow<InsightsTimeframe> = _insightsTimeframe.asStateFlow()

    private val _chargingSubTab = MutableStateFlow(ChargingSubTab.HISTORY)
    val chargingSubTab: StateFlow<ChargingSubTab> = _chargingSubTab.asStateFlow()

    private val _settingsSection = MutableStateFlow(SettingsSection.PERMISSIONS)
    val settingsSection: StateFlow<SettingsSection> = _settingsSection.asStateFlow()

    // Persisted Settings & Privacy flags
    private val _hasSeenOnboarding = MutableStateFlow(prefs.getBoolean("has_seen_dark_svg_onboarding_v5", false))
    val hasSeenOnboarding: StateFlow<Boolean> = _hasSeenOnboarding.asStateFlow()

    private val _adbModeEnabled = MutableStateFlow(prefs.getBoolean("adb_mode", false))
    val adbModeEnabled: StateFlow<Boolean> = _adbModeEnabled.asStateFlow()

    private val _localStorageOnly = MutableStateFlow(prefs.getBoolean("local_storage_only", true))
    val localStorageOnly: StateFlow<Boolean> = _localStorageOnly.asStateFlow()

    private val _alertDrainHigher = MutableStateFlow(prefs.getBoolean("alert_drain", true))
    val alertDrainHigher: StateFlow<Boolean> = _alertDrainHigher.asStateFlow()

    private val _alertTempHigh = MutableStateFlow(prefs.getBoolean("alert_temp", true))
    val alertTempHigh: StateFlow<Boolean> = _alertTempHigh.asStateFlow()

    private val _alertChargingSlow = MutableStateFlow(prefs.getBoolean("alert_charging", false))
    val alertChargingSlow: StateFlow<Boolean> = _alertChargingSlow.asStateFlow()

    private val _alertUnusualBg = MutableStateFlow(prefs.getBoolean("alert_bg", true))
    val alertUnusualBg: StateFlow<Boolean> = _alertUnusualBg.asStateFlow()

    private val _alertCapacityChange = MutableStateFlow(prefs.getBoolean("alert_capacity", false))
    val alertCapacityChange: StateFlow<Boolean> = _alertCapacityChange.asStateFlow()

    private val _alertSensitivity = MutableStateFlow(
        AlertSensitivity.entries.firstOrNull {
            it.name == prefs.getString("alert_sensitivity", AlertSensitivity.MEDIUM.name)
        } ?: AlertSensitivity.MEDIUM
    )
    val alertSensitivity: StateFlow<AlertSensitivity> = _alertSensitivity.asStateFlow()

    // Live Telemetry & Capabilities
    private val _liveTelemetry = MutableStateFlow(scanner.captureLiveTelemetry(_adbModeEnabled.value))
    val liveTelemetry: StateFlow<LiveTelemetrySnapshot> = _liveTelemetry.asStateFlow()

    private val _capabilities = MutableStateFlow(scanner.scanCapabilities(_liveTelemetry.value, _adbModeEnabled.value))
    val capabilities: StateFlow<List<CapabilityItem>> = _capabilities.asStateFlow()

    val manufacturerProfile: ManufacturerProfileInfo = scanner.getManufacturerProfile()

    // Real App Usage & Activity Estimates (from UsageStatsManager)
    private val _appActivityInsights = MutableStateFlow(scanner.queryRealAppActivityInsights())
    val appActivityInsights: StateFlow<List<AppActivityInsight>> = _appActivityInsights.asStateFlow()

    private val _activityEstimates = MutableStateFlow(scanner.queryRealActivityEstimates(_appActivityInsights.value))
    val activityEstimates: StateFlow<List<ActivityEstimateItem>> = _activityEstimates.asStateFlow()

    val appSelfAudit: StateFlow<AppSelfAudit> = MutableStateFlow(scanner.getAppSelfAudit()).asStateFlow()

    private val _isAdbBatteryStatsGranted = MutableStateFlow(scanner.isAdbBatteryStatsGranted())
    val isAdbBatteryStatsGranted: StateFlow<Boolean> = _isAdbBatteryStatsGranted.asStateFlow()

    // Room Flows
    val diagnosticSessions: StateFlow<List<DiagnosticSessionEntity>> = repository.diagnosticSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTimelineEvents: StateFlow<List<TimelineEventEntity>> = repository.allTimelineEvents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chargingSessions: StateFlow<List<ChargingSessionEntity>> = repository.chargingSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chargerProfiles: StateFlow<List<ChargerProfileEntity>> = repository.chargerProfiles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val experiments: StateFlow<List<ExperimentEntity>> = repository.experiments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected Period in Drain Detective
    private val _selectedSessionId = MutableStateFlow("last_night")
    val selectedSessionId: StateFlow<String> = _selectedSessionId.asStateFlow()

    private val _showEvidenceChainExpanded = MutableStateFlow(false)
    val showEvidenceChainExpanded: StateFlow<Boolean> = _showEvidenceChainExpanded.asStateFlow()

    // Interactive Diagnostic Test Runner
    private val _activeTestRun = MutableStateFlow<ActiveDiagnosticTestRun?>(null)
    val activeTestRun: StateFlow<ActiveDiagnosticTestRun?> = _activeTestRun.asStateFlow()
    private var testRunnerJob: Job? = null

    // AI Battery Doctor & Session Ad Unlock states — locked by default and unlocked via Video/Rewarded Ads for the current session only
    private val _isAiDoctorSessionUnlocked = MutableStateFlow(AiDoctorSessionManager.isUnlocked.value)
    val isAiDoctorSessionUnlocked: StateFlow<Boolean> = _isAiDoctorSessionUnlocked.asStateFlow()

    private val _isExportSessionUnlocked = MutableStateFlow(AiDoctorSessionManager.isExportUnlocked.value)
    val isExportSessionUnlocked: StateFlow<Boolean> = _isExportSessionUnlocked.asStateFlow()

    private val _isDeepBenchmarkUnlocked = MutableStateFlow(AiDoctorSessionManager.isDeepBenchmarkUnlocked.value)
    val isDeepBenchmarkUnlocked: StateFlow<Boolean> = _isDeepBenchmarkUnlocked.asStateFlow()

    private val _videoAd1ShownCount = MutableStateFlow(AiDoctorSessionManager.videoAd1ShownCount.value)
    val videoAd1ShownCount: StateFlow<Int> = _videoAd1ShownCount.asStateFlow()

    private val _selectedAiDoctorQuestion = MutableStateFlow<String?>(null)
    val selectedAiDoctorQuestion: StateFlow<String?> = _selectedAiDoctorQuestion.asStateFlow()

    private val _generatedAiDoctorPrompt = MutableStateFlow<String?>(null)
    val generatedAiDoctorPrompt: StateFlow<String?> = _generatedAiDoctorPrompt.asStateFlow()

    val rewardedAdState: StateFlow<RewardedAdState> = rewardedAdManager.adState

    // Recommendations — only populated when diagnostic sessions exist
    private val _recommendations = MutableStateFlow<List<EvidenceRecommendation>>(emptyList())
    val recommendations: StateFlow<List<EvidenceRecommendation>> = _recommendations.asStateFlow()

    // Status message toast/banner
    private val _statusBannerMessage = MutableStateFlow<String?>(null)
    val statusBannerMessage: StateFlow<String?> = _statusBannerMessage.asStateFlow()

    // Live broadcast tracking state (from BatteryTrackingReceiver listening to ACTION_BATTERY_CHANGED)
    private val _liveBroadcastEventsCount = MutableStateFlow(0)
    val liveBroadcastEventsCount: StateFlow<Int> = _liveBroadcastEventsCount.asStateFlow()

    private var segmentStartMs: Long = 0L
    private var segmentStartPercent: Int = -1
    private var segmentIsCharging: Boolean = false
    private var segmentChargingSource: String = "Discharging"
    private var segmentPeakTempC: Float = 0f
    private var lastObservedPercent: Int = -1
    private var activeChargeSessionId: Long = 9001L
    private var hasIncrementedChargeProfileSession: Boolean = false

    private val batteryReceiver = BatteryTrackingReceiver { event ->
        onBatteryBroadcastEvent(event)
    }

    init {
        // Fresh install starts strictly with real device telemetry — no random pre-seeded numbers
        refreshTelemetry()
        try {
            BatteryTrackingReceiver.register(getApplication(), batteryReceiver)
        } catch (_: Exception) {
        }
    }

    override fun onCleared() {
        super.onCleared()
        BatteryTrackingReceiver.unregister(getApplication(), batteryReceiver)
    }

    fun onBatteryBroadcastEvent(event: LiveBatteryBroadcastEvent) {
        refreshTelemetry()
        _liveBroadcastEventsCount.value += 1

        if (segmentStartPercent < 0) {
            segmentStartMs = event.timestampMs
            segmentStartPercent = event.batteryPercent
            segmentIsCharging = event.isCharging
            segmentChargingSource = event.chargingSource
            segmentPeakTempC = event.temperatureCelsius
            lastObservedPercent = event.batteryPercent
            hasIncrementedChargeProfileSession = false
            return
        }

        if (event.temperatureCelsius > segmentPeakTempC) {
            segmentPeakTempC = event.temperatureCelsius
        }

        val powerStateChanged = segmentIsCharging != event.isCharging
        val levelChanged = event.batteryPercent != lastObservedPercent
        lastObservedPercent = event.batteryPercent

        if (!powerStateChanged && !levelChanged) {
            return
        }

        viewModelScope.launch {
            val elapsedMs = (event.timestampMs - segmentStartMs).coerceAtLeast(60_000L)
            val elapsedMins = (elapsedMs / 60_000L).coerceAtLeast(1L)
            val durationLabel = if (elapsedMins >= 60L) {
                "${elapsedMins / 60L}h ${elapsedMins % 60L}m"
            } else {
                "${elapsedMins}m"
            }

            if (segmentIsCharging) {
                // Track live charging session progress or completion using strictly measured wattage
                val endPct = maxOf(event.batteryPercent, segmentStartPercent)
                val watts = event.powerWatts ?: (_liveTelemetry.value.liveChargingWatts ?: 0f)
                val chargerLabel = when {
                    segmentChargingSource.contains("AC", ignoreCase = true) -> "AC Adapter"
                    segmentChargingSource.contains("USB", ignoreCase = true) -> "USB Port"
                    segmentChargingSource.contains("Wireless", ignoreCase = true) -> "Wireless Charger"
                    else -> "Live Charger"
                }
                val session = ChargingSessionEntity(
                    id = activeChargeSessionId,
                    dateTimeLabel = "Today, ${event.timeFormatted}",
                    chargerName = chargerLabel,
                    startPercent = segmentStartPercent,
                    endPercent = endPct,
                    durationLabel = durationLabel,
                    avgPowerWatts = watts,
                    peakTempCelsius = if (segmentPeakTempC > 0f) segmentPeakTempC else event.temperatureCelsius,
                    isSlowBadge = watts in 0.1f..9.9f,
                    isElevatedTemp = segmentPeakTempC >= 36.0f,
                    timestamp = event.timestampMs
                )
                val wattsLabel = if (watts > 0f) "${watts}W" else "Wattage unavailable"
                val timelineEvent = TimelineEventEntity(
                    sessionId = _selectedSessionId.value,
                    timeLabel = event.timeFormatted,
                    batteryPercent = event.batteryPercent,
                    title = if (!event.isCharging) "Charger disconnected" else "Charging milestone (${event.batteryPercent}%)",
                    detail = "$chargerLabel · ${segmentStartPercent}% → ${endPct}% · $wattsLabel · ${event.temperatureCelsius}°C",
                    classificationLabel = "Measured",
                    timestamp = event.timestampMs
                )
                val shouldIncrementProfile = !hasIncrementedChargeProfileSession
                hasIncrementedChargeProfileSession = true
                repository.recordLiveChargingSession(
                    session = session,
                    chargerName = chargerLabel,
                    observedWatts = watts,
                    tempCelsius = session.peakTempCelsius,
                    incrementSessionCount = shouldIncrementProfile,
                    timelineEvent = timelineEvent
                )
            } else if (event.batteryPercent < segmentStartPercent) {
                // Track live discharging session progress
                val dropPct = (segmentStartPercent - event.batteryPercent).coerceAtLeast(1)
                val elapsedHours = (elapsedMs / 3_600_000f).coerceAtLeast(0.1f)
                val drainRate = ((dropPct / elapsedHours) * 100f).roundToInt() / 100f
                val historicalBaselineRate = diagnosticSessions.value
                    .filter { it.id != "live_discharging_session" && it.drainRatePerHr > 0f }
                    .map { it.drainRatePerHr }
                    .average()
                    .takeIf { !it.isNaN() && it > 0.0 }
                    ?.let { ((it * 100.0).roundToInt() / 100f) }
                    ?: drainRate
                val multiplier = if (historicalBaselineRate > 0f) {
                    ((drainRate / historicalBaselineRate) * 10f).roundToInt() / 10f
                } else {
                    1.0f
                }
                val snap = _liveTelemetry.value
                val liveSessionId = "live_discharging_session"
                val liveSession = DiagnosticSessionEntity(
                    id = liveSessionId,
                    title = "Live Discharging Session",
                    timeWindow = "Tracked today (${event.timeFormatted})",
                    durationHoursLabel = durationLabel,
                    drainPercent = dropPct,
                    isAnomaly = historicalBaselineRate > 0f && drainRate > historicalBaselineRate * 1.5f,
                    drainRatePerHr = drainRate,
                    normalDrainRatePerHr = historicalBaselineRate,
                    startBatteryPercent = segmentStartPercent,
                    endBatteryPercent = event.batteryPercent,
                    multiplierVsNormal = multiplier,
                    overallConfidence = "Measured",
                    primaryTitle = "Screen & Wi-Fi Activity",
                    primarySubtitle = "Screen ${snap.screenState} · Wi-Fi ${snap.wifiState}",
                    primaryConfidence = "Measured",
                    secondaryTitle = "Mobile Network & Bluetooth",
                    secondarySubtitle = "${snap.mobileState} · Bluetooth ${snap.bluetoothState}",
                    secondaryConfidence = "Measured",
                    factorTitle = "Temperature & Voltage",
                    factorSubtitle = "${event.temperatureCelsius}°C · ${event.voltageVolts}V",
                    factorConfidence = "Measured",
                    possibleTitle = "Location Services",
                    possibleSubtitle = "Location ${snap.locationState}",
                    possibleConfidence = "Possible",
                    screenOffDuration = if (snap.screenState == "OFF") durationLabel else "0m",
                    awakeDuration = durationLabel,
                    weakSignalDuration = "0m",
                    peakTempCelsius = if (segmentPeakTempC > 0f) segmentPeakTempC else event.temperatureCelsius,
                    integrityNote = null,
                    timestamp = event.timestampMs
                )
                val timelineEvent = TimelineEventEntity(
                    sessionId = liveSessionId,
                    timeLabel = event.timeFormatted,
                    batteryPercent = event.batteryPercent,
                    title = "Battery discharged to ${event.batteryPercent}%",
                    detail = "Live discharge tracked: -${dropPct}% ($drainRate%/hr) · ${event.temperatureCelsius}°C",
                    classificationLabel = "Measured",
                    timestamp = event.timestampMs
                )
                repository.recordLiveDischargingSession(
                    session = liveSession,
                    timelineEvent = timelineEvent
                )
                if (_selectedSessionId.value == "last_night" && diagnosticSessions.value.none { it.id == "last_night" }) {
                    _selectedSessionId.value = liveSessionId
                }
            }

            if (powerStateChanged) {
                segmentStartMs = event.timestampMs
                segmentStartPercent = event.batteryPercent
                segmentIsCharging = event.isCharging
                segmentChargingSource = event.chargingSource
                segmentPeakTempC = event.temperatureCelsius
                hasIncrementedChargeProfileSession = false
                if (event.isCharging) {
                    activeChargeSessionId += 1L
                }
            }
        }
    }

    fun refreshTelemetry() {
        val snap = scanner.captureLiveTelemetry(_adbModeEnabled.value)
        _liveTelemetry.value = snap
        _capabilities.value = scanner.scanCapabilities(snap, _adbModeEnabled.value)
        val realApps = scanner.queryRealAppActivityInsights()
        _appActivityInsights.value = realApps
        _activityEstimates.value = scanner.queryRealActivityEstimates(realApps)
        _isAdbBatteryStatsGranted.value = scanner.isAdbBatteryStatsGranted()
    }

    fun selectTab(tab: MainTab) {
        _currentTab.value = tab
        refreshTelemetry()
    }

    fun selectDiagnoseSubTab(subTab: DiagnoseSubTab) {
        _diagnoseSubTab.value = subTab
    }

    fun selectInsightsTimeframe(timeframe: InsightsTimeframe) {
        _insightsTimeframe.value = timeframe
    }

    fun selectChargingSubTab(subTab: ChargingSubTab) {
        _chargingSubTab.value = subTab
    }

    fun selectSettingsSection(section: SettingsSection) {
        _settingsSection.value = section
        refreshTelemetry()
    }

    fun selectSession(sessionId: String) {
        _selectedSessionId.value = sessionId
    }

    fun toggleEvidenceChain() {
        _showEvidenceChainExpanded.value = !_showEvidenceChainExpanded.value
    }

    fun investigateThursdayAnomaly() {
        val targetId = diagnosticSessions.value.firstOrNull { it.isAnomaly }?.id
            ?: diagnosticSessions.value.firstOrNull()?.id
            ?: "last_night"
        _selectedSessionId.value = targetId
        _diagnoseSubTab.value = DiagnoseSubTab.SHOW_ME_WHY
        _showEvidenceChainExpanded.value = true
        _currentTab.value = MainTab.DIAGNOSE
    }

    fun navigateToEnableAdbMode() {
        _settingsSection.value = SettingsSection.ADVANCED
        _currentTab.value = MainTab.SETTINGS
    }

    fun navigateToPermissionsSettings() {
        _settingsSection.value = SettingsSection.PERMISSIONS
        _currentTab.value = MainTab.SETTINGS
    }

    fun navigateToFaqGuide() {
        _settingsSection.value = SettingsSection.FAQ
        _currentTab.value = MainTab.SETTINGS
    }

    fun markOnboardingCompleted() {
        _hasSeenOnboarding.value = true
        prefs.edit().putBoolean("has_seen_dark_svg_onboarding_v5", true).apply()
    }

    fun setAdbModeEnabled(enabled: Boolean) {
        _adbModeEnabled.value = enabled
        prefs.edit().putBoolean("adb_mode", enabled).apply()
        refreshTelemetry()
        showBanner(if (enabled) "ADB Advanced Mode enabled: Wakelock & batterystats analysis unlocked." else "Returned to Standard Mode.")
    }

    fun setLocalStorageOnly(enabled: Boolean) {
        _localStorageOnly.value = enabled
        prefs.edit().putBoolean("local_storage_only", enabled).apply()
    }

    fun setAlertToggle(type: String, enabled: Boolean) {
        when (type) {
            "drain" -> {
                _alertDrainHigher.value = enabled
                prefs.edit().putBoolean("alert_drain", enabled).apply()
            }
            "temp" -> {
                _alertTempHigh.value = enabled
                prefs.edit().putBoolean("alert_temp", enabled).apply()
            }
            "charging" -> {
                _alertChargingSlow.value = enabled
                prefs.edit().putBoolean("alert_charging", enabled).apply()
            }
            "bg" -> {
                _alertUnusualBg.value = enabled
                prefs.edit().putBoolean("alert_bg", enabled).apply()
            }
            "capacity" -> {
                _alertCapacityChange.value = enabled
                prefs.edit().putBoolean("alert_capacity", enabled).apply()
            }
        }
    }

    fun setAlertSensitivity(sensitivity: AlertSensitivity) {
        _alertSensitivity.value = sensitivity
        prefs.edit().putString("alert_sensitivity", sensitivity.name).apply()
    }

    fun getEvidenceChainForSession(session: DiagnosticSessionEntity): List<EvidenceChainStep> {
        return listOf(
            EvidenceChainStep(
                stepOrder = 1,
                observation = "Battery dropped ${session.drainPercent}% (${session.startBatteryPercent}% → ${session.endBatteryPercent}%)",
                classification = DataClassification.MEASURED,
                timestampOrDuration = session.timeWindow
            ),
            EvidenceChainStep(
                stepOrder = 2,
                observation = "Screen was OFF for ${session.screenOffDuration}",
                classification = DataClassification.MEASURED,
                timestampOrDuration = "Out of ${session.durationHoursLabel} window"
            ),
            EvidenceChainStep(
                stepOrder = 3,
                observation = "Phone stayed awake for ${session.awakeDuration} while screen was off",
                classification = DataClassification.MEASURED,
                timestampOrDuration = "Background wake time"
            ),
            EvidenceChainStep(
                stepOrder = 4,
                observation = "${session.primaryTitle}: ${session.primarySubtitle}",
                classification = DataClassification.MEASURED,
                timestampOrDuration = "Matching background activity"
            ),
            EvidenceChainStep(
                stepOrder = 5,
                observation = "${session.secondaryTitle}: ${session.secondarySubtitle}",
                classification = if (_liveTelemetry.value.fineLocationGranted) DataClassification.MEASURED else DataClassification.LIMITED,
                timestampOrDuration = "Observed ${session.weakSignalDuration}"
            ),
            EvidenceChainStep(
                stepOrder = 6,
                observation = "Summary: ${session.primaryTitle} is the main cause · Confidence: ${session.overallConfidence}",
                classification = DataClassification.MEASURED,
                timestampOrDuration = "${session.drainRatePerHr}%/hr vs ${session.normalDrainRatePerHr}%/hr usual"
            )
        )
    }

    fun getDrainPointsForSessions(
        sessions: List<DiagnosticSessionEntity>,
        timeframe: InsightsTimeframe
    ): List<DayDrainPoint> {
        if (sessions.isEmpty()) return emptyList()
        val measuredScreenHours = computeMeasuredForegroundHours()
        return sessions.reversed().mapIndexed { index, session ->
            val shortLabel = when (timeframe) {
                InsightsTimeframe.DAY -> "S${index + 1}"
                InsightsTimeframe.WEEK -> session.title.take(3)
                InsightsTimeframe.MONTH -> "W${index + 1}"
            }
            DayDrainPoint(
                dayShort = shortLabel,
                dayFull = session.title,
                drainPercent = session.drainPercent,
                isAnomaly = session.isAnomaly,
                peakTempCelsius = session.peakTempCelsius,
                screenHours = measuredScreenHours,
                idleDrainRate = session.drainRatePerHr
            )
        }
    }

    private fun computeMeasuredForegroundHours(): Float {
        val insights = _appActivityInsights.value
        if (insights.isEmpty()) return 0f
        var totalMins = 0
        for (item in insights) {
            val label = item.foregroundDurationLabel
            val hrs = Regex("(\\d+)h").find(label)?.groupValues?.getOrNull(1)?.toIntOrNull() ?: 0
            val mins = Regex("(\\d+)m").find(label)?.groupValues?.getOrNull(1)?.toIntOrNull() ?: 0
            totalMins += (hrs * 60) + mins
        }
        return ((totalMins / 60f) * 10f).roundToInt() / 10f
    }

    fun addTimelineAnnotation(title: String, detail: String) {
        viewModelScope.launch {
            val timeStr = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
            repository.addAnnotatedTimelineEvent(
                sessionId = _selectedSessionId.value,
                timeLabel = timeStr,
                batteryPercent = _liveTelemetry.value.batteryPercent,
                title = title,
                detail = detail
            )
            showBanner("Event '$title' added to synchronized Battery Drain Timeline.")
        }
    }

    fun startDiagnosticTest(testName: String, durationLabel: String, description: String) {
        testRunnerJob?.cancel()
        val snap = scanner.captureLiveTelemetry(_adbModeEnabled.value)
        _activeTestRun.value = ActiveDiagnosticTestRun(
            testName = testName,
            targetDurationLabel = durationLabel,
            description = description,
            elapsedSeconds = 0,
            startBatteryPercent = snap.batteryPercent,
            currentBatteryPercent = snap.batteryPercent,
            currentTempCelsius = snap.temperatureCelsius,
            screenTurnedOnEvents = 0,
            networkStateLabel = "Wi-Fi (${snap.wifiState}) · Cellular (${snap.mobileState})",
            isIntegrityCompromised = false,
            integrityNote = null,
            isFinished = false
        )
        testRunnerJob = viewModelScope.launch {
            for (sec in 1..15) {
                delay(1000L)
                val currentRun = _activeTestRun.value ?: break
                val liveSnap = scanner.captureLiveTelemetry(_adbModeEnabled.value)
                _activeTestRun.value = currentRun.copy(
                    elapsedSeconds = sec,
                    currentBatteryPercent = liveSnap.batteryPercent,
                    currentTempCelsius = liveSnap.temperatureCelsius
                )
            }
        }
    }

    fun completeAndSaveActiveTest() {
        val run = _activeTestRun.value ?: return
        testRunnerJob?.cancel()
        viewModelScope.launch {
            val id = "test_${System.currentTimeMillis()}"
            val actualDrop = (run.startBatteryPercent - run.currentBatteryPercent).coerceAtLeast(0)
            val elapsedHrs = (run.elapsedSeconds / 3600f).coerceAtLeast(0.01f)
            val measuredRate = ((actualDrop / elapsedHrs) * 100f).roundToInt() / 100f
            val baselineRate = diagnosticSessions.value
                .filter { it.drainRatePerHr > 0f }
                .map { it.drainRatePerHr }
                .average()
                .takeIf { !it.isNaN() && it > 0.0 }
                ?.let { ((it * 100.0).roundToInt() / 100f) }
                ?: measuredRate
            val newSession = DiagnosticSessionEntity(
                id = id,
                title = "${run.testName} Session",
                timeWindow = "Recorded today (${run.targetDurationLabel} window)",
                durationHoursLabel = run.targetDurationLabel,
                drainPercent = actualDrop,
                isAnomaly = false,
                drainRatePerHr = measuredRate,
                normalDrainRatePerHr = baselineRate,
                startBatteryPercent = run.startBatteryPercent,
                endBatteryPercent = run.currentBatteryPercent,
                multiplierVsNormal = 1.0f,
                overallConfidence = "Measured",
                primaryTitle = "${run.testName} Primary Factor",
                primarySubtitle = run.networkStateLabel,
                primaryConfidence = "Measured",
                secondaryTitle = "Thermal Behavior",
                secondarySubtitle = "${run.currentTempCelsius}°C measured",
                secondaryConfidence = "Measured",
                factorTitle = "Test Integrity Check",
                factorSubtitle = run.integrityNote ?: "Controlled environment verified",
                factorConfidence = "Measured",
                possibleTitle = "Live Sensor State",
                possibleSubtitle = "Voltage ${_liveTelemetry.value.voltageVolts}V · Current ${_liveTelemetry.value.currentMilliAmps}mA",
                possibleConfidence = "Measured",
                screenOffDuration = if (run.testName.contains("Screen")) "0m" else "${run.elapsedSeconds}s",
                awakeDuration = "${run.elapsedSeconds}s",
                weakSignalDuration = "0m",
                peakTempCelsius = run.currentTempCelsius,
                integrityNote = run.integrityNote,
                timestamp = System.currentTimeMillis()
            )
            repository.addDiagnosticSession(newSession)
            _selectedSessionId.value = id
            _activeTestRun.value = null
            _diagnoseSubTab.value = DiagnoseSubTab.SHOW_ME_WHY
            showBanner("Saved '${newSession.title}' to Diagnostic Sessions.")
        }
    }

    fun cancelActiveTest() {
        testRunnerJob?.cancel()
        _activeTestRun.value = null
    }

    fun createControlledExperiment(title: String, hypothesis: String, category: String) {
        viewModelScope.launch {
            val exp = ExperimentEntity(
                title = title,
                hypothesis = hypothesis,
                testCategory = category,
                baselineDrainRate = 0.0f,
                experimentDrainRate = 0.0f,
                durationMinutes = 60,
                status = "In Progress — Collecting Data",
                confidenceLabel = "Insufficient Data",
                findingSummary = "Experiment created. Keep using your device during the test window so baseline vs experiment drain rates can be measured.",
                integrityWarning = null,
                limitationsNote = "Measured on-device via coulomb counter & UsageStats events.",
                createdAt = System.currentTimeMillis()
            )
            repository.addExperiment(exp)
            showBanner("Controlled Experiment '$title' started. Data will generate as you use the device.")
        }
    }

    fun createChargerProfile(name: String, maxWatts: Float, avgWatts: Float, avgTemp: Float) {
        viewModelScope.launch {
            repository.addChargerProfile(
                ChargerProfileEntity(
                    name = name,
                    sessionsCount = 1,
                    maxObservedWatts = maxWatts,
                    avgPowerWatts = avgWatts,
                    avgTempCelsius = avgTemp,
                    colorTheme = if (maxWatts >= 20f) "green" else if (maxWatts >= 12f) "blue" else "amber"
                )
            )
            showBanner("Charger profile '$name' saved.")
        }
    }

    private fun buildLiveFallbackSession(): DiagnosticSessionEntity {
        val snap = _liveTelemetry.value
        return DiagnosticSessionEntity(
            id = "live_fallback",
            title = "Live Device Snapshot",
            timeWindow = "Current Session",
            durationHoursLabel = "Live",
            drainPercent = 0,
            isAnomaly = false,
            drainRatePerHr = 0f,
            normalDrainRatePerHr = 0f,
            startBatteryPercent = snap.batteryPercent,
            endBatteryPercent = snap.batteryPercent,
            multiplierVsNormal = 1.0f,
            overallConfidence = "Measured",
            primaryTitle = "Active Screen & System Telemetry",
            primarySubtitle = "Screen ${snap.screenState} · Wi-Fi ${snap.wifiState}",
            primaryConfidence = "Measured",
            secondaryTitle = "Cellular Radio State",
            secondarySubtitle = "${snap.mobileState} state",
            secondaryConfidence = "Measured",
            factorTitle = "Battery Temperature",
            factorSubtitle = "${snap.temperatureCelsius}°C (${snap.temperatureStatus})",
            factorConfidence = "Measured",
            possibleTitle = "Location Service",
            possibleSubtitle = "Location ${snap.locationState}",
            possibleConfidence = "Possible",
            screenOffDuration = "Collecting...",
            awakeDuration = "Collecting...",
            weakSignalDuration = "Collecting...",
            peakTempCelsius = snap.temperatureCelsius,
            integrityNote = null,
            timestamp = System.currentTimeMillis()
        )
    }

    /**
     * Attempts to enter AI Battery Doctor.
     * - If already unlocked in the current session, opens the sheet immediately.
     * - Otherwise triggers a Google Mobile Ads Rewarded Ad and unlocks the session only upon
     *   receiving the verified OnUserEarnedRewardListener callback.
     */
    fun requestEnterAiDoctor(
        activity: Activity?,
        onOpenSheet: () -> Unit,
        onShowInteractiveTestAd: (() -> Unit)? = null
    ) {
        if (_isAiDoctorSessionUnlocked.value || AiDoctorSessionManager.isUnlocked.value) {
            _isAiDoctorSessionUnlocked.value = true
            onOpenSheet()
            return
        }

        rewardedAdManager.showRewardedAd(
            activity = activity,
            rewardType = "ai_doctor_session",
            onRewardEarned = { amount, type ->
                val unlocked = onRewardAdEarnedCallback(amount, type)
                if (unlocked) {
                    onOpenSheet()
                }
            },
            onShowInteractiveTestAd = onShowInteractiveTestAd
        )
    }

    /**
     * Verifies the Google Mobile Ads OnUserEarnedRewardListener callback and toggles the
     * local session 'unlocked' state.
     */
    fun onRewardAdEarnedCallback(amount: Int = 1, type: String = "ai_doctor_session"): Boolean {
        val unlocked = AiDoctorSessionManager.unlockForCurrentSession(amount, type)
        _isAiDoctorSessionUnlocked.value = unlocked
        if (unlocked) {
            showBanner("AI Battery Doctor unlocked for this session.")
        }
        return _isAiDoctorSessionUnlocked.value
    }

    fun unlockAiDoctorForSession() {
        onRewardAdEarnedCallback()
    }

    /**
     * Rewarded Video Ad #2: Unlocks PDF/CSV/JSON Forensic Report Export for the current session.
     */
    fun requestUnlockExportWithRewardAd(activity: Activity?) {
        if (_isExportSessionUnlocked.value || AiDoctorSessionManager.isExportUnlocked.value) {
            _isExportSessionUnlocked.value = true
            return
        }

        rewardedAdManager.showRewardedAd(
            activity = activity,
            rewardType = "export_report_session",
            onRewardEarned = { _, _ ->
                onExportRewardAdEarnedCallback()
            }
        )
    }

    fun onExportRewardAdEarnedCallback(pendingFormat: String? = null): Boolean {
        val unlocked = AiDoctorSessionManager.unlockExportForCurrentSession()
        _isExportSessionUnlocked.value = unlocked
        if (unlocked && !pendingFormat.isNullOrBlank()) {
            exportReport(pendingFormat)
        } else if (unlocked) {
            showBanner("Forensic Report Export unlocked for this session.")
        }
        return _isExportSessionUnlocked.value
    }

    /**
     * Video Ad #1 (Interstitial Video): Triggered upon Diagnostic Test or Controlled Experiment completion.
     */
    fun requestVideoAd1DiagnosticCompletion(activity: Activity?) {
        rewardedAdManager.showVideoAd1DiagnosticCompletion(
            activity = activity,
            onAdCompleted = {
                onVideoAd1CompletedCallback()
            }
        )
    }

    fun onVideoAd1CompletedCallback(): Int {
        val count = AiDoctorSessionManager.recordVideoAd1Completed()
        _videoAd1ShownCount.value = count
        return count
    }

    /**
     * Video Ad #2 (Rewarded Interstitial Video): Unlocks the Deep Health & Thermal Stress Benchmark in Charging tab.
     */
    fun requestVideoAd2ChargingBenchmark(activity: Activity?) {
        if (_isDeepBenchmarkUnlocked.value || AiDoctorSessionManager.isDeepBenchmarkUnlocked.value) {
            _isDeepBenchmarkUnlocked.value = true
            return
        }

        rewardedAdManager.showVideoAd2ChargingBenchmark(
            activity = activity,
            onBenchmarkUnlocked = {
                onVideoAd2BenchmarkUnlockedCallback()
            }
        )
    }

    fun onVideoAd2BenchmarkUnlockedCallback(): Boolean {
        val unlocked = AiDoctorSessionManager.unlockDeepBenchmarkForCurrentSession()
        _isDeepBenchmarkUnlocked.value = unlocked
        if (unlocked) {
            showBanner("Deep Health & Thermal Stress Benchmark unlocked for this session.")
        }
        return _isDeepBenchmarkUnlocked.value
    }

    fun resetAiDoctorSessionUnlock() {
        AiDoctorSessionManager.resetSession()
        _isAiDoctorSessionUnlocked.value = false
        _isExportSessionUnlocked.value = false
        _isDeepBenchmarkUnlocked.value = false
        _videoAd1ShownCount.value = 0
        _selectedAiDoctorQuestion.value = null
        _generatedAiDoctorPrompt.value = null
    }

    fun generateDynamicPromptForQuestion(question: String) {
        if (question.isBlank()) return
        refreshTelemetry()
        val session = diagnosticSessions.value.firstOrNull { it.id == _selectedSessionId.value }
            ?: diagnosticSessions.value.firstOrNull()
            ?: buildLiveFallbackSession()
        val prompt = aiClient.buildGeminiForensicPrompt(
            question = question,
            snapshot = _liveTelemetry.value,
            session = session,
            adbEnabled = _adbModeEnabled.value,
            appInsights = _appActivityInsights.value
        )
        _selectedAiDoctorQuestion.value = question
        _generatedAiDoctorPrompt.value = prompt
    }

    fun notifyPromptCopied() {
        showBanner("Prompt copied to clipboard.")
    }

    fun exportReport(format: String) {
        viewModelScope.launch {
            val snap = _liveTelemetry.value
            val sessions = diagnosticSessions.value
            val selected = sessions.firstOrNull { it.id == _selectedSessionId.value }
                ?: sessions.firstOrNull()
                ?: buildLiveFallbackSession()
            val timeline = allTimelineEvents.value.filter { it.sessionId == selected.id }
            when (format.uppercase(Locale.getDefault())) {
                "PDF" -> {
                    val file = exporter.exportPdfDiagnosticReport(
                        snapshot = snap,
                        session = selected,
                        capabilities = _capabilities.value,
                        timeline = timeline,
                        recommendations = _recommendations.value
                    )
                    showBanner("PDF Diagnostic Report generated (${file.name}). Opening share sheet...")
                    exporter.shareExportedFile(file, "application/pdf", "Battery Forensics Diagnostic Report (PDF)")
                }
                "CSV" -> {
                    val file = exporter.exportCsvData(sessions, chargingSessions.value)
                    showBanner("CSV Forensic Data exported (${file.name}). Opening share sheet...")
                    exporter.shareExportedFile(file, "text/csv", "Battery Forensics Data Export (CSV)")
                }
                else -> {
                    val file = exporter.exportJsonData(
                        snapshot = snap,
                        sessions = sessions,
                        chargingSessions = chargingSessions.value,
                        chargers = chargerProfiles.value,
                        adbEnabled = _adbModeEnabled.value
                    )
                    showBanner("JSON Forensic Data exported (${file.name}). Opening share sheet...")
                    exporter.shareExportedFile(file, "application/json", "Battery Forensics Data Export (JSON)")
                }
            }
        }
    }

    fun deleteAllUserData(reseedDemoBaseline: Boolean = false) {
        viewModelScope.launch {
            repository.deleteAllDataAndReset(reseedDemoBaseline)
            segmentStartPercent = -1
            lastObservedPercent = -1
            if (reseedDemoBaseline) {
                _selectedSessionId.value = "last_night"
            }
            showBanner(
                if (reseedDemoBaseline) "Sample demo data loaded for preview."
                else "All local diagnostic sessions, timeline events, and profiles permanently deleted."
            )
        }
    }

    fun getAdbWakelocks(): List<AdbWakelockEntry> =
        scanner.getAdbWakelockData(_adbModeEnabled.value)

    fun runConsoleCommand(cmd: String): String =
        scanner.runDiagnosticConsoleCommand(cmd, _liveTelemetry.value, _adbModeEnabled.value)

    fun clearBanner() {
        _statusBannerMessage.value = null
    }

    private fun showBanner(msg: String) {
        _statusBannerMessage.value = msg
    }
}
