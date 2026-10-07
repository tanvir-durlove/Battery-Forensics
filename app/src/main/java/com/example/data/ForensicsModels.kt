package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class DataClassification(val label: String) {
    MEASURED("Measured"),
    ESTIMATED("Estimated"),
    DEVICE_DEPENDENT("Device dependent"),
    LIMITED("Limited"),
    UNAVAILABLE("Unavailable"),
    ADB_DERIVED("ADB-derived diagnostic data"),
    DEMO("Demo")
}

enum class ConfidenceLevel(val label: String) {
    MEASURED("Measured"),
    STRONG_EVIDENCE("Strong Evidence"),
    LIKELY("Likely"),
    POSSIBLE("Possible"),
    INSUFFICIENT_DATA("Insufficient Data"),
    DEMO("Demo")
}

enum class ContributorRole(val label: String) {
    PRIMARY("Primary"),
    SECONDARY("Secondary"),
    FACTOR("Factor"),
    POSSIBLE("Possible"),
    NO_EVIDENCE("No Evidence Found"),
    INSUFFICIENT_DATA("Insufficient Data")
}

enum class DiagnosticAccessLevel(val title: String, val description: String) {
    STANDARD("Standard Mode", "Uses normal Android public APIs and permissions."),
    DEVICE_DEPENDENT("Device-Dependent Mode", "Uses OEM/Android version specific APIs where exposed."),
    ADB_ADVANCED("Advanced ADB Mode", "Optional user-enabled ADB diagnostics for wakelocks & batterystats.")
}

enum class CapabilityStatus {
    SUPPORTED,
    ESTIMATED_OR_LIMITED,
    UNAVAILABLE
}

data class CapabilityItem(
    val id: String,
    val name: String,
    val status: CapabilityStatus,
    val rightNote: String? = null,
    val detailExplanation: String
)

data class LiveTelemetrySnapshot(
    val deviceModel: String,
    val manufacturer: String,
    val androidVersion: String,
    val sdkInt: Int,
    val currentTimeFormatted: String,
    val batteryPercent: Int,
    val healthLabel: String,
    val drainRateText: String,
    val sinceChargeText: String,
    val estRemainingText: String,
    val awakeOffScreenText: String,
    val temperatureCelsius: Float,
    val temperatureStatus: String,
    val temperatureClassification: DataClassification,
    val voltageVolts: Float,
    val voltageStatus: String,
    val voltageClassification: DataClassification,
    val currentMilliAmps: Int,
    val currentStatus: String,
    val currentClassification: DataClassification,
    val healthScore: Int,
    val healthScoreClassification: DataClassification,
    val designCapacityMah: Int,
    val estimatedFullCapacityMah: Int,
    val cycleCount: Int?,
    val isCharging: Boolean,
    val chargingSource: String,
    val liveChargingWatts: Float?,
    val screenState: String,
    val dozeState: String,
    val wifiState: String,
    val mobileState: String,
    val bluetoothState: String,
    val locationState: String,
    val thermalStatusLabel: String,
    val usageAccessGranted: Boolean,
    val fineLocationGranted: Boolean,
    val phoneStateGranted: Boolean,
    val bluetoothScanGranted: Boolean,
    val notificationsGranted: Boolean,
    // 1 & 2. Calibration & Smoothed Capacity State
    val isHealthScoreCalibrating: Boolean = true,
    val calibrationSessionsCompleted: Int = 0,
    val calibrationTargetSessions: Int = 3,
    val healthCalibrationStatusText: String = "Learning your battery patterns",
    val smoothedCapacitySampleCount: Int = 0,
    // 3. Thermal Warning System (>= 38.0°C)
    val isThermalWarningActive: Boolean = false,
    val thermalAdvisoryTip: String? = null,
    // 4. Custom Cycle Count Fallback Tracker
    val isCycleCountHardwareMeasured: Boolean = false,
    val estimatedCycleCount: Int = 0,
    val cycleCountProgressPercent: Int = 0,
    val accumulatedChargeMah: Int = 0,
    // 5. Real-Time Battery Drain Rate Monitor (Active Use vs Standby)
    val activeDrainRatePerHr: Float = 0f,
    val standbyDrainRatePerHr: Float = 0f,
    val liveInstantDrainRatePerHr: Float = 0f,
    val activeDischargingMinutes: Int = 0,
    val standbyDischargingMinutes: Int = 0,
    val drainMonitorStatusText: String = "Monitoring active & standby discharge"
)

data class ActivityEstimateItem(
    val name: String,
    val percentage: Int,
    val colorCategory: String // "blue", "orange", "purple", "brown", "gray"
)

data class DrainContributor(
    val role: ContributorRole,
    val confidence: ConfidenceLevel,
    val title: String,
    val metricSubtitle: String,
    val detailedExplanation: String
)

data class EvidenceChainStep(
    val stepOrder: Int,
    val observation: String,
    val classification: DataClassification,
    val timestampOrDuration: String
)

data class EvidenceRecommendation(
    val id: String,
    val title: String,
    val whyRecommended: String,
    val targetContributor: String,
    val beforeDrainPercent: Int,
    val afterDrainPercent: Int?,
    val verificationConfidence: ConfidenceLevel
)

@Entity(tableName = "diagnostic_sessions")
data class DiagnosticSessionEntity(
    @PrimaryKey val id: String,
    val title: String,
    val timeWindow: String,
    val durationHoursLabel: String,
    val drainPercent: Int,
    val isAnomaly: Boolean,
    val drainRatePerHr: Float,
    val normalDrainRatePerHr: Float,
    val startBatteryPercent: Int,
    val endBatteryPercent: Int,
    val multiplierVsNormal: Float,
    val overallConfidence: String,
    val primaryTitle: String,
    val primarySubtitle: String,
    val primaryConfidence: String,
    val secondaryTitle: String,
    val secondarySubtitle: String,
    val secondaryConfidence: String,
    val factorTitle: String,
    val factorSubtitle: String,
    val factorConfidence: String,
    val possibleTitle: String,
    val possibleSubtitle: String,
    val possibleConfidence: String,
    val screenOffDuration: String,
    val awakeDuration: String,
    val weakSignalDuration: String,
    val peakTempCelsius: Float,
    val integrityNote: String?,
    val timestamp: Long
)

@Entity(tableName = "timeline_events")
data class TimelineEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: String,
    val timeLabel: String,
    val batteryPercent: Int,
    val title: String,
    val detail: String,
    val classificationLabel: String,
    val isUserAnnotation: Boolean = false,
    val timestamp: Long
)

@Entity(tableName = "charging_sessions")
data class ChargingSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateTimeLabel: String,
    val chargerName: String,
    val startPercent: Int,
    val endPercent: Int,
    val durationLabel: String,
    val avgPowerWatts: Float,
    val peakTempCelsius: Float,
    val isSlowBadge: Boolean = false,
    val isElevatedTemp: Boolean = false,
    val timestamp: Long
)

@Entity(tableName = "charger_profiles")
data class ChargerProfileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val sessionsCount: Int,
    val maxObservedWatts: Float,
    val avgPowerWatts: Float,
    val avgTempCelsius: Float,
    val colorTheme: String // "green", "blue", "amber"
)

@Entity(tableName = "controlled_experiments")
data class ExperimentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val hypothesis: String,
    val testCategory: String,
    val baselineDrainRate: Float,
    val experimentDrainRate: Float,
    val durationMinutes: Int,
    val status: String, // "Completed", "Running"
    val confidenceLabel: String,
    val findingSummary: String,
    val integrityWarning: String?,
    val limitationsNote: String,
    val createdAt: Long
)

data class AppActivityInsight(
    val initial: String,
    val appName: String,
    val packageName: String,
    val hasLocationBadge: Boolean,
    val foregroundDurationLabel: String,
    val backgroundEventsCount: Int,
    val impactLevel: String, // "High", "Med", "Low"
    val isRecentlyUpdated: Boolean = false,
    val updateCorrelationNote: String? = null,
    val culpritType: String? = null, // "Thermal Overheat", "Background Vampire", "Screen Drainer"
    val thermalCorrelationNote: String? = null,
    val estimatedDrainPct: Float = 0f
)

data class AppActivityInsightsResult(
    val items: List<AppActivityInsight> = emptyList(),
    val needsUsagePermission: Boolean = false
)

data class AppSelfAudit(
    val appName: String = "Battery Forensics",
    val packageName: String = "com.nextgen.batteryforensics",
    val batteryImpactEstimate: String = "< 0.1% / 24h",
    val activeWakelocksCount: Int = 0,
    val backgroundCpuUsage: String = "0.0%",
    val thermalContribution: String = "Zero Heat (Passive OS Listener)",
    val ramUsageMb: Int = 26,
    val isSafeVerdict: Boolean = true,
    val verdictSummary: String = "Verified: Battery Forensics did not cause observed battery drain or thermal overheating."
)

data class DayDrainPoint(
    val dayShort: String,
    val dayFull: String,
    val drainPercent: Int,
    val isAnomaly: Boolean,
    val peakTempCelsius: Float,
    val screenHours: Float,
    val idleDrainRate: Float
)

data class AiDoctorExchange(
    val id: String = "prompt_${System.currentTimeMillis()}",
    val question: String,
    val geminiPromptText: String = "",
    val isUnlockedByRewardAd: Boolean = false,
    val conclusion: String,
    val evidencePoints: List<String>,
    val supportingData: String,
    val primaryContributor: String,
    val confidence: ConfidenceLevel,
    val dataLimitations: String,
    val isInsufficientData: Boolean = false
)
