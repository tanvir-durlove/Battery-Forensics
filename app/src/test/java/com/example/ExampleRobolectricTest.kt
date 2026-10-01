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
        assertTrue(snapshot.estimatedFullCapacityMah > 0)

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
        assertTrue(activeWakelockOut.contains("PowerManagerService.WakeLocks"))
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
    fun `verify full Compose UI Critical User Journey across all 5 tabs and AI Doctor`() {
        composeTestRule.waitForIdle()

        // 1. Verify Home Dashboard Screen is displayed
        composeTestRule.onNodeWithTag("dashboard_screen").assertIsDisplayed()
        composeTestRule.onNodeWithText("Battery Forensics").assertIsDisplayed()

        // Open Health Score transparent breakdown dialog on Dashboard
        composeTestRule.onNodeWithTag("dashboard_screen").performScrollToIndex(2)
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("health_score_card").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Close").performClick()
        composeTestRule.waitForIdle()

        // 2. Navigate to Diagnose (Drain Detective) Tab
        composeTestRule.onNodeWithTag("bottom_nav_diagnose").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("diagnose_screen").assertIsDisplayed()
        composeTestRule.onNodeWithText("Drain Detective").assertIsDisplayed()

        // Switch to Tests & Experiments sub-tab
        composeTestRule.onNodeWithTag("segmented_option_tests_&_experiments").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Idle Test").assertIsDisplayed()

        // Switch back to Show Me Why sub-tab
        composeTestRule.onNodeWithTag("segmented_option_show_me_why").performClick()
        composeTestRule.waitForIdle()

        // 3. Navigate to Insights Tab and trigger Thursday Anomaly Investigation
        composeTestRule.onNodeWithTag("bottom_nav_insights").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("insights_screen").assertIsDisplayed()
        composeTestRule.onNodeWithText("Anomaly — Thursday").assertIsDisplayed()

        // Switch Insights timeframes
        composeTestRule.onNodeWithTag("segmented_option_day").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("segmented_option_week").performClick()
        composeTestRule.waitForIdle()

        // Click "Investigate ->" on Anomaly Card -> should navigate to Diagnose tab
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

        // 5. Navigate to Settings Tab
        composeTestRule.onNodeWithTag("bottom_nav_settings").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("settings_screen").assertIsDisplayed()

        // 6. Open AI Battery Doctor sheet from top bar and ask a preset question
        composeTestRule.onNodeWithTag("top_bar_ai_doctor_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("ai_doctor_sheet").assertIsDisplayed()
        composeTestRule.onNodeWithTag("ai_doctor_preset_0").performClick()
        composeTestRule.waitForIdle()
    }
}
