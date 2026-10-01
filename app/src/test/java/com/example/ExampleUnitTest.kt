package com.example

import com.example.data.AiBatteryDoctorClient
import com.example.data.ConfidenceLevel
import com.example.data.DataClassification
import com.example.data.DiagnosticSessionEntity
import com.example.data.LiveTelemetrySnapshot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    private val sampleSnapshot = LiveTelemetrySnapshot(
        deviceModel = "Pixel 8 Pro",
        manufacturer = "Google",
        androidVersion = "15",
        sdkInt = 35,
        currentTimeFormatted = "14:32",
        batteryPercent = 78,
        healthLabel = "Good",
        drainRateText = "3.2%/hr",
        sinceChargeText = "4h 23m",
        estRemainingText = "~18h",
        awakeOffScreenText = "38m",
        temperatureCelsius = 29.4f,
        temperatureStatus = "Normal range",
        temperatureClassification = DataClassification.MEASURED,
        voltageVolts = 4.12f,
        voltageStatus = "Nominal",
        voltageClassification = DataClassification.MEASURED,
        currentMilliAmps = -340,
        currentStatus = "Discharging",
        currentClassification = DataClassification.MEASURED,
        healthScore = 94,
        healthScoreClassification = DataClassification.ESTIMATED,
        designCapacityMah = 5000,
        estimatedFullCapacityMah = 4700,
        cycleCount = 142,
        isCharging = false,
        chargingSource = "Discharging",
        liveChargingWatts = null,
        screenState = "ON",
        dozeState = "Inactive",
        wifiState = "Connected",
        mobileState = "LTE · 3 bars",
        bluetoothState = "2 devices",
        locationState = "3 apps",
        thermalStatusLabel = "Nominal (None)",
        usageAccessGranted = true,
        fineLocationGranted = false,
        phoneStateGranted = true,
        bluetoothScanGranted = true,
        notificationsGranted = true
    )

    private val sampleSession = DiagnosticSessionEntity(
        id = "last_night",
        title = "Last night",
        timeWindow = "11 PM – 7 AM",
        durationHoursLabel = "8 hours",
        drainPercent = 14,
        isAnomaly = true,
        drainRatePerHr = 1.75f,
        normalDrainRatePerHr = 0.66f,
        startBatteryPercent = 92,
        endBatteryPercent = 78,
        multiplierVsNormal = 2.3f,
        overallConfidence = "Strong Evidence",
        primaryTitle = "Elevated Background Activity",
        primarySubtitle = "Instagram · 47 background events",
        primaryConfidence = "Strong Evidence",
        secondaryTitle = "Weak Cellular Signal",
        secondarySubtitle = "LTE 1–2 bars for 2h 31m",
        secondaryConfidence = "Likely",
        factorTitle = "Device Awake Time",
        factorSubtitle = "1h 18m awake while screen off",
        factorConfidence = "Measured",
        possibleTitle = "Background Location",
        possibleSubtitle = "Maps + 2 apps with background access",
        possibleConfidence = "Possible",
        screenOffDuration = "7h 42m",
        awakeDuration = "1h 18m",
        weakSignalDuration = "2h 31m",
        peakTempCelsius = 31.4f,
        integrityNote = null,
        timestamp = 1727700000000L
    )

    @Test
    fun `verify data classifications and confidence levels match specification`() {
        assertEquals("Measured", DataClassification.MEASURED.label)
        assertEquals("Estimated", DataClassification.ESTIMATED.label)
        assertEquals("Device dependent", DataClassification.DEVICE_DEPENDENT.label)
        assertEquals("Limited", DataClassification.LIMITED.label)
        assertEquals("Unavailable", DataClassification.UNAVAILABLE.label)

        assertEquals("Measured", ConfidenceLevel.MEASURED.label)
        assertEquals("Strong Evidence", ConfidenceLevel.STRONG_EVIDENCE.label)
        assertEquals("Likely", ConfidenceLevel.LIKELY.label)
        assertEquals("Possible", ConfidenceLevel.POSSIBLE.label)
        assertEquals("Insufficient Data", ConfidenceLevel.INSUFFICIENT_DATA.label)
    }

    @Test
    fun `AI Battery Doctor answers overnight drain with strong evidence and no gimmicks`() = runTest {
        val client = AiBatteryDoctorClient()
        val exchange = client.askForensicQuestion(
            question = "Why did my battery drop 14% last night?",
            snapshot = sampleSnapshot,
            selectedSession = sampleSession,
            adbEnabled = false,
            allowCloudAi = false
        )

        assertEquals(ConfidenceLevel.STRONG_EVIDENCE, exchange.confidence)
        assertFalse(exchange.isInsufficientData)
        assertTrue(exchange.conclusion.contains("14%"))
        assertTrue(exchange.primaryContributor.contains("Elevated Background Activity"))
        assertTrue(exchange.evidencePoints.isNotEmpty())
        // Anti-gimmick verification
        val combined = (exchange.conclusion + exchange.supportingData + exchange.dataLimitations).lowercase()
        assertFalse(combined.contains("ram booster"))
        assertFalse(combined.contains("cache cleaner"))
    }

    @Test
    fun `AI Battery Doctor triggers Insufficient Data when Fine Location is denied for 5G query`() = runTest {
        val client = AiBatteryDoctorClient()
        val deniedExchange = client.askForensicQuestion(
            question = "Is 5G associated with higher drain during my test?",
            snapshot = sampleSnapshot.copy(fineLocationGranted = false),
            selectedSession = sampleSession,
            adbEnabled = false,
            allowCloudAi = false
        )

        assertEquals(ConfidenceLevel.INSUFFICIENT_DATA, deniedExchange.confidence)
        assertTrue(deniedExchange.isInsufficientData)
        assertTrue(deniedExchange.dataLimitations.contains("Fine Location"))

        val grantedExchange = client.askForensicQuestion(
            question = "Is 5G associated with higher drain during my test?",
            snapshot = sampleSnapshot.copy(fineLocationGranted = true),
            selectedSession = sampleSession,
            adbEnabled = false,
            allowCloudAi = false
        )
        assertEquals(ConfidenceLevel.LIKELY, grantedExchange.confidence)
        assertFalse(grantedExchange.isInsufficientData)
    }

    @Test
    fun `AI Battery Doctor triggers Insufficient Data for restricted kernel CPU hardware queries`() = runTest {
        val client = AiBatteryDoctorClient()
        val exchange = client.askForensicQuestion(
            question = "Did a kernel CPU spike cause drain?",
            snapshot = sampleSnapshot,
            selectedSession = sampleSession,
            adbEnabled = false,
            allowCloudAi = false
        )

        assertEquals(ConfidenceLevel.INSUFFICIENT_DATA, exchange.confidence)
        assertTrue(exchange.isInsufficientData)
        assertTrue(exchange.conclusion.contains("Insufficient data"))
    }

    @Test
    fun `AI Battery Doctor answers thermal, charging, and health questions with transparent evidence`() = runTest {
        val client = AiBatteryDoctorClient()

        val thermal = client.askForensicQuestion(
            question = "Why is my phone getting hot?",
            snapshot = sampleSnapshot,
            selectedSession = sampleSession,
            adbEnabled = false,
            allowCloudAi = false
        )
        assertEquals(ConfidenceLevel.MEASURED, thermal.confidence)
        assertTrue(thermal.conclusion.contains("29.4°C"))

        val charging = client.askForensicQuestion(
            question = "Why is charging slower today?",
            snapshot = sampleSnapshot,
            selectedSession = sampleSession,
            adbEnabled = false,
            allowCloudAi = false
        )
        assertEquals(ConfidenceLevel.MEASURED, charging.confidence)
        assertTrue(charging.conclusion.contains("Unknown charger"))

        val health = client.askForensicQuestion(
            question = "Is my battery health changing?",
            snapshot = sampleSnapshot,
            selectedSession = sampleSession,
            adbEnabled = false,
            allowCloudAi = false
        )
        assertEquals(ConfidenceLevel.LIKELY, health.confidence)
        assertTrue(health.conclusion.contains("94/100"))
        assertTrue(health.dataLimitations.contains("Never presented as official manufacturer warranty health"))
    }
}
