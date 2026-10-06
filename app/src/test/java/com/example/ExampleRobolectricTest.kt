package com.example

import android.app.Application
import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.CapabilityStatus
import com.example.data.ChargerProfileEntity
import com.example.data.DeviceTelemetryScanner
import com.example.data.ExperimentEntity
import com.example.data.ForensicsDatabase
import com.example.data.ForensicsReportExporter
import com.example.data.ForensicsRepository
import com.example.ui.AlertSensitivity
import com.example.ui.ChargingSubTab
import com.example.ui.DiagnoseSubTab
import com.example.ui.ForensicsViewModel
import com.example.ui.InsightsTimeframe
import com.example.ui.MainTab
import com.example.ui.SettingsSection
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun `verify app_name telemetry scanner capabilities and ADB diagnostics`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Battery Forensics", appName)

        val scanner = DeviceTelemetryScanner(context)
        val snapshot = scanner.captureLiveTelemetry(adbModeEnabled = false)
        assertTrue(snapshot.batteryPercent in 0..100)
        assertTrue(snapshot.healthScore in 0..100)
        assertTrue(snapshot.estimatedFullCapacityMah >= 0)

        // Standard Mode capabilities (7 items)
        val stdCapabilities = scanner.scanCapabilities(snapshot, adbModeEnabled = false)
        assertEquals(7, stdCapabilities.size)
        val stdWakelockCap = stdCapabilities.first { it.id == "system_wakelocks" }
        assertEquals(CapabilityStatus.UNAVAILABLE, stdWakelockCap.status)
        assertEquals("ADB mode required", stdWakelockCap.rightNote)
        assertTrue(scanner.getAdbWakelockData(adbModeEnabled = false).isEmpty())

        // ADB Mode capabilities
        val adbCapabilities = scanner.scanCapabilities(snapshot, adbModeEnabled = true)
        val adbWakelockCap = adbCapabilities.first { it.id == "system_wakelocks" }
        assertEquals(CapabilityStatus.SUPPORTED, adbWakelockCap.status)
        assertEquals("ADB-derived", adbWakelockCap.rightNote)
        assertTrue(scanner.getAdbWakelockData(adbModeEnabled = true).isNotEmpty())

        // Console commands
        val helpOut = scanner.runDiagnosticConsoleCommand("help", snapshot, adbModeEnabled = false)
        assertTrue(helpOut.contains("dumpsys batterystats"))
        val blockedWakelockOut = scanner.runDiagnosticConsoleCommand("wakelocks", snapshot, adbModeEnabled = false)
        assertTrue(blockedWakelockOut.contains("Restricted in Standard Mode"))
        val activeWakelockOut = scanner.runDiagnosticConsoleCommand("wakelocks", snapshot, adbModeEnabled = true)
        assertTrue(activeWakelockOut.contains("ADB WAKELOCK TABLE"))
    }

    @Test
    fun `verify Room database seeding timeline annotations experiments and reset`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, ForensicsDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val repo = ForensicsRepository(db.forensicsDao())

        repo.ensureSeeded()

        val sessions = repo.diagnosticSessions.first()
        assertEquals(4, sessions.size)
        assertTrue(sessions.any { it.id == "last_night" && it.drainPercent == 14 })

        val charging = repo.chargingSessions.first()
        assertEquals(4, charging.size)

        val chargers = repo.chargerProfiles.first()
        assertEquals(3, chargers.size)

        // Add timeline annotation
        repo.addAnnotatedTimelineEvent(
            sessionId = "last_night",
            timeLabel = "6:30 AM",
            batteryPercent = 79,
            title = "User Note: Hotel Wi-Fi dropped",
            detail = "Switched to weak LTE"
        )
        val events = repo.allTimelineEvents.first()
        assertTrue(events.any { it.title.contains("Hotel Wi-Fi dropped") && it.isUserAnnotation })

        // Add charger profile
        repo.addChargerProfile(
            ChargerProfileEntity(
                name = "Anker 45W PPS",
                sessionsCount = 5,
                maxObservedWatts = 26.8f,
                avgPowerWatts = 21.4f,
                avgTempCelsius = 30.2f,
                colorTheme = "green"
            )
        )
        assertEquals(4, repo.chargerProfiles.first().size)

        // Add controlled experiment
        repo.addExperiment(
            ExperimentEntity(
                title = "Disable 5G Overnight",
                hypothesis = "Force LTE reduces standby drain",
                testCategory = "5G vs LTE Test",
                baselineDrainRate = 1.75f,
                experimentDrainRate = 0.65f,
                durationMinutes = 480,
                status = "Completed",
                confidenceLabel = "Strong Evidence",
                findingSummary = "Drain dropped 62%",
                integrityWarning = null,
                limitationsNote = "Measured on device",
                createdAt = System.currentTimeMillis()
            )
        )
        assertTrue(repo.experiments.first().any { it.title == "Disable 5G Overnight" })

        // Delete all data without reseeding
        repo.deleteAllDataAndReset(reseedAfterClear = false)
        assertTrue(repo.diagnosticSessions.first().isEmpty())
        assertTrue(repo.chargingSessions.first().isEmpty())

        // Reset with baseline reseed
        repo.deleteAllDataAndReset(reseedAfterClear = true)
        assertEquals(4, repo.diagnosticSessions.first().size)

        db.close()
    }

    @Test
    fun `verify PDF CSV and JSON forensic report exporter writes valid files`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, ForensicsDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val repo = ForensicsRepository(db.forensicsDao())
        repo.ensureSeeded()

        val scanner = DeviceTelemetryScanner(context)
        val snapshot = scanner.captureLiveTelemetry(adbModeEnabled = false)
        val capabilities = scanner.scanCapabilities(snapshot, adbModeEnabled = false)
        val sessions = repo.diagnosticSessions.first()
        val timeline = repo.allTimelineEvents.first()
        val charging = repo.chargingSessions.first()
        val chargers = repo.chargerProfiles.first()

        val exporter = ForensicsReportExporter(context)

        val pdfFile = exporter.exportPdfDiagnosticReport(
            snapshot = snapshot,
            session = sessions.first(),
            capabilities = capabilities,
            timeline = timeline,
            recommendations = emptyList()
        )
        assertTrue(pdfFile.exists() && pdfFile.length() > 50)

        val csvFile = exporter.exportCsvData(sessions, charging)
        assertTrue(csvFile.exists())
        val csvText = csvFile.readText()
        assertTrue(csvText.contains("RecordType,TitleOrDate"))
        assertTrue(csvText.contains("Last night"))

        val jsonFile = exporter.exportJsonData(snapshot, sessions, charging, chargers, adbEnabled = false)
        assertTrue(jsonFile.exists())
        val jsonText = jsonFile.readText()
        assertTrue(jsonText.contains("\"diagnosticSessions\""))
        assertTrue(jsonText.contains("\"chargerProfiles\""))

        db.close()
    }

    @Test
    fun `verify ForensicsViewModel navigation evidence chain and test runner flows`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = ForensicsViewModel(app)

        assertEquals(MainTab.HOME, vm.currentTab.value)

        // Investigate Thursday Anomaly jumps to Diagnose -> Show Me Why with evidence chain expanded
        vm.investigateThursdayAnomaly()
        assertEquals(MainTab.DIAGNOSE, vm.currentTab.value)
        assertEquals(DiagnoseSubTab.SHOW_ME_WHY, vm.diagnoseSubTab.value)
        assertEquals("last_night", vm.selectedSessionId.value)
        assertTrue(vm.showEvidenceChainExpanded.value)

        // Start and cancel diagnostic test
        vm.startDiagnosticTest("Idle Drain Test", "30 min", "Screen off baseline")
        assertNotNull(vm.activeTestRun.value)
        assertEquals("Idle Drain Test", vm.activeTestRun.value?.testName)
        vm.cancelActiveTest()
        assertEquals(null, vm.activeTestRun.value)

        // Settings & Alert sensitivity updates
        vm.setAlertSensitivity(AlertSensitivity.HIGH)
        assertEquals(AlertSensitivity.HIGH, vm.alertSensitivity.value)
        vm.setAdbModeEnabled(true)
        assertTrue(vm.adbModeEnabled.value)
        vm.setAdbModeEnabled(false)
        assertFalse(vm.adbModeEnabled.value)

        // Sub-tab selections
        vm.selectInsightsTimeframe(InsightsTimeframe.MONTH)
        assertEquals(InsightsTimeframe.MONTH, vm.insightsTimeframe.value)
        vm.selectChargingSubTab(ChargingSubTab.HEALTH)
        assertEquals(ChargingSubTab.HEALTH, vm.chargingSubTab.value)
        vm.selectSettingsSection(SettingsSection.PRIVACY)
        assertEquals(SettingsSection.PRIVACY, vm.settingsSection.value)
    }

    @Test
    fun `verify BatteryTrackingReceiver parses ACTION_BATTERY_CHANGED and tracks live charging and discharging`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val capturedEvents = mutableListOf<com.example.data.LiveBatteryBroadcastEvent>()
        val receiver = com.example.data.BatteryTrackingReceiver { event ->
            capturedEvents.add(event)
        }

        // Simulate a discharging ACTION_BATTERY_CHANGED broadcast at 80%
        val dischargeStartIntent = android.content.Intent(android.content.Intent.ACTION_BATTERY_CHANGED).apply {
            putExtra(android.os.BatteryManager.EXTRA_LEVEL, 80)
            putExtra(android.os.BatteryManager.EXTRA_SCALE, 100)
            putExtra(android.os.BatteryManager.EXTRA_STATUS, android.os.BatteryManager.BATTERY_STATUS_DISCHARGING)
            putExtra(android.os.BatteryManager.EXTRA_PLUGGED, 0)
            putExtra(android.os.BatteryManager.EXTRA_TEMPERATURE, 295)
            putExtra(android.os.BatteryManager.EXTRA_VOLTAGE, 4120)
        }
        receiver.onReceive(context, dischargeStartIntent)
        assertEquals(1, capturedEvents.size)
        assertEquals(80, capturedEvents.last().batteryPercent)
        assertFalse(capturedEvents.last().isCharging)

        // Simulate a subsequent discharging ACTION_BATTERY_CHANGED broadcast dropping to 76%
        val dischargeDropIntent = android.content.Intent(android.content.Intent.ACTION_BATTERY_CHANGED).apply {
            putExtra(android.os.BatteryManager.EXTRA_LEVEL, 76)
            putExtra(android.os.BatteryManager.EXTRA_SCALE, 100)
            putExtra(android.os.BatteryManager.EXTRA_STATUS, android.os.BatteryManager.BATTERY_STATUS_DISCHARGING)
            putExtra(android.os.BatteryManager.EXTRA_PLUGGED, 0)
            putExtra(android.os.BatteryManager.EXTRA_TEMPERATURE, 308)
            putExtra(android.os.BatteryManager.EXTRA_VOLTAGE, 4050)
        }
        receiver.onReceive(context, dischargeDropIntent)
        assertEquals(2, capturedEvents.size)
        assertEquals(76, capturedEvents.last().batteryPercent)

        // Verify ViewModel processes live broadcast events
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = ForensicsViewModel(app)
        vm.onBatteryBroadcastEvent(capturedEvents[0])
        vm.onBatteryBroadcastEvent(capturedEvents[1])
        assertTrue(vm.liveBroadcastEventsCount.value >= 2)

        // Reset singleton DB so subsequent UI tests verify clean fresh-install state
        val dao = ForensicsDatabase.getInstance(context).forensicsDao()
        dao.clearAllSessions()
        dao.clearAllChargingSessions()
        dao.clearAllTimelineEvents()
    }

    @Test
    fun `verify AiDoctorSessionManager and ForensicsViewModel Rewarded Ad callback toggles unlocked state`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        com.example.data.AiDoctorSessionManager.resetSession()
        val vm = ForensicsViewModel(app)

        // 1. Starts locked (false) for a new session
        assertFalse(com.example.data.AiDoctorSessionManager.isUnlocked.value)
        assertFalse(vm.isAiDoctorSessionUnlocked.value)

        // 2. Attempting to enter AI Doctor while locked triggers Rewarded Ad flow instead of opening sheet
        var sheetOpenedCount = 0
        var adTriggeredCount = 0
        vm.requestEnterAiDoctor(
            activity = null,
            onOpenSheet = { sheetOpenedCount += 1 },
            onShowInteractiveTestAd = { adTriggeredCount += 1 }
        )
        assertEquals(0, sheetOpenedCount)
        assertEquals(1, adTriggeredCount)
        assertFalse(vm.isAiDoctorSessionUnlocked.value)

        // 3. Verify Google Mobile Ads OnUserEarnedRewardListener callback toggles 'unlocked' state to true
        var verifiedRewardAmount = 0
        var verifiedRewardType = ""
        val adMobRewardListener = vm.rewardedAdManager.createEarnedRewardListener { amount, type ->
            verifiedRewardAmount = amount
            verifiedRewardType = type
            vm.onRewardAdEarnedCallback(amount, type)
        }
        val mockRewardItem = object : com.google.android.gms.ads.rewarded.RewardItem {
            override fun getAmount(): Int = 1
            override fun getType(): String = "ai_doctor_session"
        }
        adMobRewardListener.onUserEarnedReward(mockRewardItem)

        assertTrue(com.example.data.AiDoctorSessionManager.isUnlocked.value)
        assertTrue(vm.isAiDoctorSessionUnlocked.value)
        assertEquals(1, verifiedRewardAmount)
        assertEquals("ai_doctor_session", verifiedRewardType)

        // 4. Subsequent attempt to enter AI Doctor in the same session opens sheet immediately without ad
        vm.requestEnterAiDoctor(
            activity = null,
            onOpenSheet = { sheetOpenedCount += 1 },
            onShowInteractiveTestAd = { adTriggeredCount += 1 }
        )
        assertEquals(1, sheetOpenedCount)
        assertEquals(1, adTriggeredCount)

        // 5. Verify Video Ad #1 (Diagnostic Lab Interstitial Video), Video Ad #2 (Charging Benchmark), and Export Rewarded Ad #2
        assertEquals("ca-app-pub-7794111343358988/7732707201", com.example.data.RewardedAdManager.TEST_VIDEO_INTERSTITIAL_AD_UNIT_ID)
        assertEquals("ca-app-pub-7794111343358988/3306377034", com.example.data.RewardedAdManager.TEST_VIDEO_REWARDED_INTERSTITIAL_AD_UNIT_ID)
        assertEquals("ca-app-pub-7794111343358988/5962575944", com.example.data.RewardedAdManager.TEST_BANNER_AD_UNIT_ID)

        assertFalse(vm.isExportSessionUnlocked.value)
        assertTrue(vm.onExportRewardAdEarnedCallback())
        assertTrue(vm.isExportSessionUnlocked.value)

        assertFalse(vm.isDeepBenchmarkUnlocked.value)
        assertTrue(vm.onVideoAd2BenchmarkUnlockedCallback())
        assertTrue(vm.isDeepBenchmarkUnlocked.value)

        assertEquals(0, vm.videoAd1ShownCount.value)
        assertEquals(1, vm.onVideoAd1CompletedCallback())
        assertEquals(1, vm.videoAd1ShownCount.value)

        // Reset session unlock state for subsequent UI test
        vm.resetAiDoctorSessionUnlock()
        assertFalse(com.example.data.AiDoctorSessionManager.isUnlocked.value)
        assertFalse(vm.isAiDoctorSessionUnlocked.value)
        assertFalse(vm.isExportSessionUnlocked.value)
        assertFalse(vm.isDeepBenchmarkUnlocked.value)
    }

    @Test
    fun `verify full Compose UI Critical User Journey across all 5 tabs and AI Doctor`() {
        com.example.data.AiDoctorSessionManager.resetSession()
        composeTestRule.waitForIdle()

        // 1. Verify Home Dashboard Screen, top Native Ad card, & 'Data collection in progress' empty history state
        composeTestRule.onNodeWithTag("dashboard_screen").assertIsDisplayed()
        composeTestRule.onNodeWithText("Battery Forensics").assertIsDisplayed()
        composeTestRule.onNodeWithTag("data_collection_in_progress_card").assertIsDisplayed()
        composeTestRule.onNodeWithText("Data collection in progress").assertIsDisplayed()

        composeTestRule.onNodeWithTag("dashboard_screen").performScrollToIndex(3)
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("home_inline_ad_card").assertIsDisplayed()

        // Open Health Score transparent breakdown dialog on Dashboard
        composeTestRule.onNodeWithTag("dashboard_screen").performScrollToIndex(4)
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("health_score_card").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Close").performClick()
        composeTestRule.waitForIdle()

        // Verify new Real-Time Drain Rate Monitor card on Dashboard
        composeTestRule.onNodeWithTag("dashboard_screen").performScrollToIndex(5)
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("drain_rate_monitor_card").assertIsDisplayed()
        composeTestRule.onNodeWithText("DRAIN RATE MONITOR").assertIsDisplayed()

        // 2. Navigate to Diagnose (Drain Detective) Tab and verify 'Data collection in progress' card
        composeTestRule.onNodeWithTag("bottom_nav_diagnose").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("diagnose_screen").assertIsDisplayed()
        composeTestRule.onNodeWithText("Drain Detective").assertIsDisplayed()
        composeTestRule.onNodeWithTag("diagnose_inline_ad_card").assertIsDisplayed()
        composeTestRule.onNodeWithTag("diagnose_screen").performScrollToIndex(3)
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("diagnose_data_collection_card").assertIsDisplayed()

        // Switch to Tests & Experiments sub-tab
        composeTestRule.onNodeWithTag("diagnose_screen").performScrollToIndex(1)
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("segmented_option_tests_&_experiments").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Idle Test").assertIsDisplayed()

        // Switch back to Show Me Why sub-tab
        composeTestRule.onNodeWithTag("segmented_option_show_me_why").performClick()
        composeTestRule.waitForIdle()

        // 3. Navigate to Insights Tab and verify fresh-install baseline collection state & top Native Ad card
        composeTestRule.onNodeWithTag("bottom_nav_insights").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("insights_screen").assertIsDisplayed()
        composeTestRule.onNodeWithText("Building Your Battery Baseline").assertIsDisplayed()
        composeTestRule.onNodeWithText("Data collection in progress").assertIsDisplayed()
        composeTestRule.onNodeWithTag("insights_inline_ad_card").assertIsDisplayed()

        // Switch Insights timeframes
        composeTestRule.onNodeWithTag("segmented_option_day").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("segmented_option_week").performClick()
        composeTestRule.waitForIdle()

        // Verify Deep BATTERY_STATS ADB reminder card and copy action
        composeTestRule.onNodeWithTag("insights_screen").performScrollToIndex(6)
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("insights_adb_reminder_card").assertIsDisplayed()
        composeTestRule.onNodeWithTag("insights_copy_adb_cmd_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("insights_adb_guide_button").assertExists()

        // Verify Thermal & Battery Drain Culprits Card and Self-Audit Card
        composeTestRule.onNodeWithTag("insights_screen").performScrollToIndex(7)
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("insights_culprits_card").assertIsDisplayed()
        composeTestRule.onNodeWithText("OVERHEAT & DRAIN CULPRITS").assertIsDisplayed()

        composeTestRule.onNodeWithTag("insights_screen").performScrollToIndex(8)
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("insights_self_audit_card").assertIsDisplayed()
        composeTestRule.onNodeWithText("BATTERY FORENSICS SELF-AUDIT").assertIsDisplayed()
        composeTestRule.onNodeWithText("Verified: 0% Heat").assertIsDisplayed()

        // Scroll back up and click "Investigate ->" on Anomaly Card -> should navigate to Diagnose tab
        composeTestRule.onNodeWithTag("insights_screen").performScrollToIndex(1)
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("investigate_anomaly_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("diagnose_screen").assertIsDisplayed()

        // 4. Navigate to Charging Tab & switch sub-tabs (History, Chargers, Health)
        composeTestRule.onNodeWithTag("bottom_nav_charging").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("charging_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("charging_screen").performScrollToIndex(2)
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("segmented_option_chargers").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("segmented_option_health").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("segmented_option_history").performClick()
        composeTestRule.waitForIdle()

        // 5. Navigate to Settings Tab & verify FAQ & Battery Guide section
        composeTestRule.onNodeWithTag("bottom_nav_settings").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("settings_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("settings_nav_faq & battery guide").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("settings_screen").performScrollToIndex(2)
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("open_onboarding_tour_button").assertIsDisplayed()

        // Scroll to the Stat Dictionary and tap a statistic topic to open the Tap-to-Explain Bottom Sheet
        composeTestRule.onNodeWithTag("settings_screen").performScrollToIndex(3)
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("faq_stat_topic_health_score").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("stat_info_bottom_sheet").assertIsDisplayed()
        composeTestRule.onNodeWithText("Got it").performClick()
        composeTestRule.waitForIdle()

        // Open the 4-Screen SVG Onboarding from Settings and step through all 4 screens
        composeTestRule.onNodeWithTag("settings_screen").performScrollToIndex(2)
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("open_onboarding_tour_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("guide_and_onboarding_modal").assertIsDisplayed()
        composeTestRule.onNodeWithTag("svg_card_1").assertIsDisplayed()
        composeTestRule.onNodeWithTag("onboarding_continue_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("svg_card_2").assertIsDisplayed()
        composeTestRule.onNodeWithTag("onboarding_continue_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("svg_card_3").assertIsDisplayed()
        composeTestRule.onNodeWithTag("onboarding_continue_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("svg_card_4").assertIsDisplayed()
        composeTestRule.onNodeWithTag("onboarding_start_diagnostics_button").performClick()
        composeTestRule.waitForIdle()

        // Verify tapping Privacy in Settings opens the Google Play Privacy Policy screen directly
        composeTestRule.onNodeWithTag("settings_screen").performScrollToIndex(0)
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("settings_nav_privacy").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("privacy_policy_screen").assertIsDisplayed()
        composeTestRule.onNodeWithText("Privacy Policy").assertIsDisplayed()
        composeTestRule.onNodeWithTag("copy_privacy_policy_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("privacy_policy_back_button").performClick()
        composeTestRule.waitForIdle()

        // 6. Click Unlock AI Doctor in top bar -> unlocks directly without middle popup modal and opens AI Doctor sheet
        assertEquals("ca-app-pub-7794111343358988/5415781037", com.example.data.RewardedAdManager.TEST_REWARDED_AD_UNIT_ID)
        assertEquals("ca-app-pub-7794111343358988~5759501161", com.example.data.RewardedAdManager.TEST_ADMOB_APP_ID)
        assertFalse(com.example.data.AiDoctorSessionManager.isUnlocked.value)
        composeTestRule.onNodeWithTag("top_bar_ai_doctor_button").performClick()
        composeTestRule.waitForIdle()
        assertTrue(com.example.data.AiDoctorSessionManager.isUnlocked.value)
        composeTestRule.onNodeWithTag("ai_doctor_sheet").assertIsDisplayed()

        // Click a question to dynamically generate prompt without AI and verify Copy Prompt button
        composeTestRule.onNodeWithTag("ai_doctor_preset_0").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("unlocked_gemini_prompt_box").assertIsDisplayed()
        composeTestRule.onNodeWithTag("copy_gemini_prompt_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("copy_gemini_prompt_button").performClick()
        composeTestRule.waitForIdle()
    }

    @Test
    fun `verify capacity capping at design capacity and moving average smoothing plus calibration state`() {
        val scanner = com.example.data.DeviceTelemetryScanner(composeTestRule.activity)
        // Verify that an over-design raw coulomb reading (4996 mAh) is strictly capped at Design Capacity (4855 mAh)
        val (cappedMah, sampleCount1) = scanner.recordAndSmoothCapacityEstimate(
            rawEstimateMah = 4996,
            designCapacityMah = 4855,
            isCharging = true,
            batteryPercent = 80
        )
        assertEquals(4855, cappedMah)
        assertTrue(sampleCount1 >= 1)

        // Add a lower sample (4755 mAh) and verify moving average smoothing works and stays <= 4855 mAh
        val (smoothedMah, sampleCount2) = scanner.recordAndSmoothCapacityEstimate(
            rawEstimateMah = 4755,
            designCapacityMah = 4855,
            isCharging = true,
            batteryPercent = 85
        )
        assertTrue(smoothedMah in 4755..4855)
        assertEquals(2, sampleCount2)

        // Verify live telemetry starts in Calibration State on initial learning cycles
        val snap = scanner.captureLiveTelemetry()
        assertTrue(snap.isHealthScoreCalibrating)
        assertEquals(0, snap.healthScore)
        assertTrue(snap.healthCalibrationStatusText.contains("Learning your battery patterns"))
    }
}
