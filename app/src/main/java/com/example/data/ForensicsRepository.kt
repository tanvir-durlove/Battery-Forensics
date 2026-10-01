package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class ForensicsRepository(private val dao: ForensicsDao) {

    val diagnosticSessions: Flow<List<DiagnosticSessionEntity>> = dao.getAllDiagnosticSessions()
    val allTimelineEvents: Flow<List<TimelineEventEntity>> = dao.getAllTimelineEvents()
    val chargingSessions: Flow<List<ChargingSessionEntity>> = dao.getAllChargingSessions()
    val chargerProfiles: Flow<List<ChargerProfileEntity>> = dao.getAllChargerProfiles()
    val experiments: Flow<List<ExperimentEntity>> = dao.getAllExperiments()

    fun getTimelineForSession(sessionId: String): Flow<List<TimelineEventEntity>> =
        dao.getTimelineEventsForSession(sessionId)

    suspend fun ensureSeeded() {
        val currentSessions = dao.getAllDiagnosticSessions().first()
        if (currentSessions.isEmpty()) {
            seedDefaultData()
        }
    }

    suspend fun seedDefaultData() {
        val now = System.currentTimeMillis()
        val sessions = listOf(
            DiagnosticSessionEntity(
                id = "last_night",
                title = "Last night",
                timeWindow = "11 PM – 7 AM",
                durationHoursLabel = "8 hours",
                drainPercent = 14,
                isAnomaly = true,
                drainRatePerHr = 1.75f,
                normalDrainRatePerHr = 0.66f,
                startBatteryPercent = 67,
                endBatteryPercent = 53,
                multiplierVsNormal = 2.3f,
                overallConfidence = "Strong Evidence",
                primaryTitle = "Elevated Background Activity",
                primarySubtitle = "Instagram · 47 background events",
                primaryConfidence = "Strong Evidence",
                secondaryTitle = "Weak Cellular Signal",
                secondarySubtitle = "LTE · 1–2 bars for 2h 31m",
                secondaryConfidence = "Likely",
                factorTitle = "Unexpected Wake Events",
                factorSubtitle = "1h 18m awake while screen off",
                factorConfidence = "Strong Evidence",
                possibleTitle = "Background Location Access",
                possibleSubtitle = "3 apps with background location",
                possibleConfidence = "Possible",
                screenOffDuration = "7h 42m",
                awakeDuration = "1h 18m",
                weakSignalDuration = "2h 31m",
                peakTempCelsius = 31.4f,
                integrityNote = null,
                timestamp = now - 3_600_000L
            ),
            DiagnosticSessionEntity(
                id = "tuesday_night",
                title = "Tuesday night",
                timeWindow = "11 PM – 7 AM",
                durationHoursLabel = "8 hours",
                drainPercent = 5,
                isAnomaly = false,
                drainRatePerHr = 0.63f,
                normalDrainRatePerHr = 0.66f,
                startBatteryPercent = 78,
                endBatteryPercent = 73,
                multiplierVsNormal = 0.95f,
                overallConfidence = "Measured",
                primaryTitle = "Normal Doze Maintenance",
                primarySubtitle = "System idle · 6 maintenance windows",
                primaryConfidence = "Measured",
                secondaryTitle = "Stable Wi-Fi Connection",
                secondarySubtitle = "Wi-Fi 5GHz · -54 dBm throughout night",
                secondaryConfidence = "Measured",
                factorTitle = "Minimal Off-Screen Awake Time",
                factorSubtitle = "19m awake while screen off",
                factorConfidence = "Measured",
                possibleTitle = "Scheduled Cloud Backup",
                possibleSubtitle = "1 brief sync window at 2:15 AM",
                possibleConfidence = "Likely",
                screenOffDuration = "7h 56m",
                awakeDuration = "19m",
                weakSignalDuration = "0m",
                peakTempCelsius = 26.8f,
                integrityNote = null,
                timestamp = now - 86_400_000L
            ),
            DiagnosticSessionEntity(
                id = "monday_night",
                title = "Monday night",
                timeWindow = "10 PM – 6 AM",
                durationHoursLabel = "8 hours",
                drainPercent = 6,
                isAnomaly = false,
                drainRatePerHr = 0.75f,
                normalDrainRatePerHr = 0.66f,
                startBatteryPercent = 84,
                endBatteryPercent = 78,
                multiplierVsNormal = 1.1f,
                overallConfidence = "Measured",
                primaryTitle = "Routine Messaging Sync",
                primarySubtitle = "WhatsApp · 14 background events",
                primaryConfidence = "Likely",
                secondaryTitle = "Cellular Standby",
                secondarySubtitle = "LTE · 3–4 bars steady",
                secondaryConfidence = "Measured",
                factorTitle = "Off-Screen Awake Time",
                factorSubtitle = "26m awake while screen off",
                factorConfidence = "Measured",
                possibleTitle = "Location Geofence Check",
                possibleSubtitle = "Maps · 2 passive location checks",
                possibleConfidence = "Possible",
                screenOffDuration = "7h 50m",
                awakeDuration = "26m",
                weakSignalDuration = "12m",
                peakTempCelsius = 27.2f,
                integrityNote = null,
                timestamp = now - 172_800_000L
            ),
            DiagnosticSessionEntity(
                id = "sunday_night",
                title = "Sunday night",
                timeWindow = "11 PM – 7:30 AM",
                durationHoursLabel = "8.5 hours",
                drainPercent = 4,
                isAnomaly = false,
                drainRatePerHr = 0.47f,
                normalDrainRatePerHr = 0.66f,
                startBatteryPercent = 91,
                endBatteryPercent = 87,
                multiplierVsNormal = 0.71f,
                overallConfidence = "Measured",
                primaryTitle = "Deep Doze Efficiency",
                primarySubtitle = "94% screen-off idle residency",
                primaryConfidence = "Measured",
                secondaryTitle = "Strong Wi-Fi Signal",
                secondarySubtitle = "No cellular fallback handoffs",
                secondaryConfidence = "Measured",
                factorTitle = "Minimal Wake Events",
                factorSubtitle = "12m awake while screen off",
                factorConfidence = "Measured",
                possibleTitle = "No Abnormal App Activity",
                possibleSubtitle = "All apps within baseline standby buckets",
                possibleConfidence = "Measured",
                screenOffDuration = "8h 26m",
                awakeDuration = "12m",
                weakSignalDuration = "0m",
                peakTempCelsius = 26.1f,
                integrityNote = null,
                timestamp = now - 259_200_000L
            )
        )
        dao.insertDiagnosticSessions(sessions)

        val timeline = listOf(
            TimelineEventEntity(
                sessionId = "last_night",
                timeLabel = "11:00 PM",
                batteryPercent = 67,
                title = "Phone locked",
                detail = "Screen turned OFF · Light Doze scheduled",
                classificationLabel = "Measured",
                timestamp = now - 28_800_000L
            ),
            TimelineEventEntity(
                sessionId = "last_night",
                timeLabel = "11:21 PM",
                batteryPercent = 66,
                title = "App activity detected",
                detail = "Instagram · 18 background sync events after v312.0 update",
                classificationLabel = "Measured",
                timestamp = now - 27_500_000L
            ),
            TimelineEventEntity(
                sessionId = "last_night",
                timeLabel = "12:03 AM",
                batteryPercent = 64,
                title = "Network activity",
                detail = "Wi-Fi idle → Cellular LTE fallback burst",
                classificationLabel = "Measured",
                timestamp = now - 25_000_000L
            ),
            TimelineEventEntity(
                sessionId = "last_night",
                timeLabel = "1:17 AM",
                batteryPercent = 61,
                title = "Wake event",
                detail = "Device exited Doze for 29m (Instagram + GMS job)",
                classificationLabel = "Measured",
                timestamp = now - 20_500_000L
            ),
            TimelineEventEntity(
                sessionId = "last_night",
                timeLabel = "2:45 AM",
                batteryPercent = 58,
                title = "Weak cellular signal",
                detail = "LTE dropped to 1–2 bars for 2h 31m",
                classificationLabel = "Measured",
                timestamp = now - 15_300_000L
            ),
            TimelineEventEntity(
                sessionId = "last_night",
                timeLabel = "4:20 AM",
                batteryPercent = 55,
                title = "Temperature increased",
                detail = "Battery rose from 27.6°C to 31.4°C during background sync",
                classificationLabel = "Measured",
                timestamp = now - 9_600_000L
            ),
            TimelineEventEntity(
                sessionId = "last_night",
                timeLabel = "7:00 AM",
                batteryPercent = 53,
                title = "Phone unlocked",
                detail = "Overnight window complete · Total drain: 14% (1.75%/hr)",
                classificationLabel = "Measured",
                timestamp = now - 3_600_000L
            )
        )
        dao.insertTimelineEvents(timeline)

        val chargingSessions = listOf(
            ChargingSessionEntity(
                dateTimeLabel = "Today, 8:14 AM",
                chargerName = "Pixel 30W",
                startPercent = 53,
                endPercent = 100,
                durationLabel = "1h 42m",
                avgPowerWatts = 18.4f,
                peakTempCelsius = 31.2f,
                isSlowBadge = false,
                isElevatedTemp = false,
                timestamp = now - 32_000_000L
            ),
            ChargingSessionEntity(
                dateTimeLabel = "Yesterday, 11:30 PM",
                chargerName = "Pixel 30W",
                startPercent = 22,
                endPercent = 89,
                durationLabel = "1h 18m",
                avgPowerWatts = 21.1f,
                peakTempCelsius = 29.8f,
                isSlowBadge = false,
                isElevatedTemp = false,
                timestamp = now - 90_000_000L
            ),
            ChargingSessionEntity(
                dateTimeLabel = "Mon, 9:00 AM",
                chargerName = "Unknown charger",
                startPercent = 45,
                endPercent = 100,
                durationLabel = "1h 57m",
                avgPowerWatts = 14.2f,
                peakTempCelsius = 32.1f,
                isSlowBadge = true,
                isElevatedTemp = true,
                timestamp = now - 190_000_000L
            ),
            ChargingSessionEntity(
                dateTimeLabel = "Sun, 7:45 PM",
                chargerName = "Pixel 30W",
                startPercent = 18,
                endPercent = 82,
                durationLabel = "58m",
                avgPowerWatts = 23.0f,
                peakTempCelsius = 30.4f,
                isSlowBadge = false,
                isElevatedTemp = false,
                timestamp = now - 270_000_000L
            )
        )
        dao.insertChargingSessions(chargingSessions)

        val chargers = listOf(
            ChargerProfileEntity(
                name = "Pixel 30W",
                sessionsCount = 28,
                maxObservedWatts = 23.4f,
                avgPowerWatts = 19.2f,
                avgTempCelsius = 30.1f,
                colorTheme = "green"
            ),
            ChargerProfileEntity(
                name = "USB-C Desk",
                sessionsCount = 12,
                maxObservedWatts = 14.1f,
                avgPowerWatts = 11.8f,
                avgTempCelsius = 29.4f,
                colorTheme = "blue"
            ),
            ChargerProfileEntity(
                name = "Unknown Charger",
                sessionsCount = 3,
                maxObservedWatts = 8.2f,
                avgPowerWatts = 7.1f,
                avgTempCelsius = 32.8f,
                colorTheme = "amber"
            )
        )
        dao.insertChargerProfiles(chargers)

        val defaultExperiments = listOf(
            ExperimentEntity(
                title = "Instagram Background Restriction Test",
                hypothesis = "Does restricting Instagram background usage reduce overnight battery drain?",
                testCategory = "Overnight App Hypothesis",
                baselineDrainRate = 1.75f,
                experimentDrainRate = 0.72f,
                durationMinutes = 480,
                status = "Completed",
                confidenceLabel = "Strong Evidence",
                findingSummary = "Overnight drain dropped from 14% (1.75%/hr) to 5.8% (0.72%/hr) when Instagram background usage was set to Restricted.",
                integrityWarning = null,
                limitationsNote = "Cellular signal was slightly stronger (3 bars vs 1–2 bars) during the verification night.",
                createdAt = now - 48_000_000L
            )
        )
        dao.insertExperiments(defaultExperiments)
    }

    suspend fun addAnnotatedTimelineEvent(
        sessionId: String,
        timeLabel: String,
        batteryPercent: Int,
        title: String,
        detail: String
    ) {
        dao.insertTimelineEvent(
            TimelineEventEntity(
                sessionId = sessionId,
                timeLabel = timeLabel,
                batteryPercent = batteryPercent,
                title = title,
                detail = detail,
                classificationLabel = "User Annotated",
                isUserAnnotation = true,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun addDiagnosticSession(session: DiagnosticSessionEntity) {
        dao.insertDiagnosticSession(session)
    }

    suspend fun addChargerProfile(profile: ChargerProfileEntity) {
        dao.insertChargerProfile(profile)
    }

    suspend fun addExperiment(experiment: ExperimentEntity) {
        dao.insertExperiment(experiment)
    }

    suspend fun deleteAllDataAndReset(reseedAfterClear: Boolean = false) {
        dao.clearAllSessions()
        dao.clearAllTimelineEvents()
        dao.clearAllChargingSessions()
        dao.clearAllChargerProfiles()
        dao.clearAllExperiments()
        if (reseedAfterClear) {
            seedDefaultData()
        }
    }
}
