package com.example.data

import java.util.Locale

/**
 * Generates structured, evidence-first forensic telemetry prompts that the user can unlock
 * via a Rewarded Ad and copy-paste into their own Google Gemini (or preferred AI) app.
 *
 * By packaging only factual on-device telemetry and letting the user run the prompt in their
 * own external AI app, the developer incurs zero LLM API costs and zero AI diagnostic liability.
 */
class AiBatteryDoctorClient {

    fun askForensicQuestion(
        question: String,
        snapshot: LiveTelemetrySnapshot,
        selectedSession: DiagnosticSessionEntity,
        adbEnabled: Boolean,
        allowCloudAi: Boolean = false,
        appInsights: List<AppActivityInsight> = emptyList(),
        unlockedByReward: Boolean = false
    ): AiDoctorExchange {
        val promptText = buildGeminiForensicPrompt(
            question = question,
            snapshot = snapshot,
            session = selectedSession,
            adbEnabled = adbEnabled,
            appInsights = appInsights
        )
        val localSummary = evaluateWithLocalEvidenceEngine(
            question = question,
            snapshot = snapshot,
            session = selectedSession,
            adbEnabled = adbEnabled
        )
        return localSummary.copy(
            id = "prompt_${System.currentTimeMillis()}_${question.hashCode()}",
            geminiPromptText = promptText,
            isUnlockedByRewardAd = unlockedByReward
        )
    }

    fun buildGeminiForensicPrompt(
        question: String,
        snapshot: LiveTelemetrySnapshot,
        session: DiagnosticSessionEntity,
        adbEnabled: Boolean,
        appInsights: List<AppActivityInsight> = emptyList()
    ): String {
        val healthScoreDisplay = if (snapshot.healthScore > 0) {
            "${snapshot.healthScore}/100 (~${snapshot.estimatedFullCapacityMah} mAh / ${snapshot.designCapacityMah} mAh design, Estimated)"
        } else {
            "Calibrating (awaiting full coulomb-counter charge cycle)"
        }

        val currentDisplay = if (snapshot.currentMilliAmps != 0) {
            "${snapshot.currentMilliAmps} mA (${snapshot.currentStatus})"
        } else {
            "Waiting for hardware gauge sample"
        }

        val appsSection = if (appInsights.isNotEmpty()) {
            appInsights.take(6).joinToString("\n") { app ->
                "  - ${app.appName} (${app.packageName}): ${app.foregroundDurationLabel} foreground · ${app.backgroundEventsCount} activity events · Impact: ${app.impactLevel}"
            }
        } else if (snapshot.usageAccessGranted) {
            "  - Collecting app usage events (keep using device to populate top apps)"
        } else {
            "  - Usage Access permission not yet granted (per-app foreground/background counts unavailable)"
        }

        val sessionSection = if (session.id == "live_fallback" && session.drainPercent == 0) {
            """
            • Session Status: Data collection in progress (Live Device Snapshot at ${snapshot.batteryPercent}%)
            • Primary Signal: ${session.primaryTitle} (${session.primarySubtitle})
            • Secondary Signal: ${session.secondaryTitle} (${session.secondarySubtitle})
            • Thermal State: ${session.factorTitle} (${session.factorSubtitle})
            """.trimIndent()
        } else {
            """
            • Period: ${session.title} (${session.timeWindow})
            • Battery Change: ${session.startBatteryPercent}% → ${session.endBatteryPercent}% (-${session.drainPercent}% total · ${session.drainRatePerHr}%/hr vs ${session.normalDrainRatePerHr}%/hr baseline)
            • Screen-Off Duration: ${session.screenOffDuration} | Off-Screen Awake: ${session.awakeDuration}
            • Peak Session Temperature: ${session.peakTempCelsius}°C
            • Ranked Contributors:
              - Primary: ${session.primaryTitle} (${session.primarySubtitle}) [${session.primaryConfidence}]
              - Secondary: ${session.secondaryTitle} (${session.secondarySubtitle}) [${session.secondaryConfidence}]
              - Factor: ${session.factorTitle} (${session.factorSubtitle}) [${session.factorConfidence}]
            """.trimIndent()
        }

        val limitationsList = buildString {
            append("• Exact per-app mAh consumption is not exposed by Android public APIs in Standard Mode.\n")
            if (!adbEnabled) {
                append("• System-wide partial/kernel wakelock tables require ADB Advanced Mode (currently Disabled).\n")
            } else {
                append("• ADB Advanced Mode is Enabled (wakelock inspection active).\n")
            }
            if (!snapshot.fineLocationGranted) {
                append("• Fine Location permission is Denied (granular cellular RSRP dBm signal strength unavailable).")
            } else {
                append("• Fine Location permission is Granted (cellular signal strength correlation active).")
            }
        }

        return """
=== BATTERY FORENSICS — TELEMETRY PROMPT FOR GEMINI ===
ROLE: Act as a Senior Android Battery & Power Systems Forensics Engineer.
MY QUESTION: "$question"

STRICT ANALYSIS RULES:
1. Base your diagnosis ONLY on the Measured and Estimated device telemetry below. Do NOT invent missing metrics.
2. Explicitly separate [Confirmed Measured Facts] from [Likely Possibilities] and [Insufficient Data].
3. Do NOT suggest placebo fixes such as RAM boosters, task killers, or clearing app caches.

[1. DEVICE & OS PROFILE]
• Device: ${snapshot.manufacturer} ${snapshot.deviceModel}
• Android Version: Android ${snapshot.androidVersion} (API ${snapshot.sdkInt})
• Diagnostic Access Mode: ${if (adbEnabled) "ADB Advanced Mode" else "Standard Mode (Public Android APIs)"}

[2. LIVE BATTERY & SENSOR TELEMETRY (MEASURED)]
• Battery Level: ${snapshot.batteryPercent}% (${snapshot.chargingSource})
• System Battery Health: ${snapshot.healthLabel}
• Live Temperature: ${snapshot.temperatureCelsius}°C (${snapshot.temperatureStatus} · ${snapshot.thermalStatusLabel})
• Live Voltage: ${snapshot.voltageVolts} V (${snapshot.voltageStatus})
• Instantaneous Current: $currentDisplay
• Live Rate: ${snapshot.drainRateText} | Est. Remaining: ${snapshot.estRemainingText}
• App-Estimated Health Score: $healthScoreDisplay
• Cycle Count: ${snapshot.cycleCount?.let { "$it cycles (Measured)" } ?: "Not exposed by OEM"}

[3. SYSTEM & RADIO STATE (MEASURED)]
• Screen: ${snapshot.screenState} | Doze Mode: ${snapshot.dozeState}
• Wi-Fi: ${snapshot.wifiState} | Mobile Data: ${snapshot.mobileState}
• Bluetooth: ${snapshot.bluetoothState} | Location: ${snapshot.locationState}
• Off-Screen Awake Activity: ${snapshot.awakeOffScreenText}

[4. RECORDED DIAGNOSTIC SESSION]
$sessionSection

[5. APP ACTIVITY INDICATORS (ANDROID USAGESTATSMANAGER)]
$appsSection

[6. KNOWN DATA LIMITATIONS]
$limitationsList

PLEASE PROVIDE:
1. Direct Answer & Most Likely Root Cause (with Confidence Level: High / Medium / Low)
2. Step-by-Step Evidence Chain citing the exact numbers above
3. 2–3 Practical, Evidence-Based Android Settings I can test on my ${snapshot.manufacturer} device
=======================================================
        """.trimIndent()
    }

    private fun evaluateWithLocalEvidenceEngine(
        question: String,
        snapshot: LiveTelemetrySnapshot,
        session: DiagnosticSessionEntity,
        adbEnabled: Boolean
    ): AiDoctorExchange {
        val q = question.lowercase(Locale.getDefault())
        return when {
            q.contains("5g") || q.contains("lte") || q.contains("signal") || q.contains("network") -> {
                if (!snapshot.fineLocationGranted) {
                    AiDoctorExchange(
                        question = question,
                        conclusion = "On-Device Telemetry Summary: Cellular state is ${snapshot.mobileState} and Wi-Fi is ${snapshot.wifiState}. Unlock and copy the Gemini prompt below for a full radio power breakdown.",
                        evidencePoints = listOf(
                            "Live network state: Wi-Fi ${snapshot.wifiState} · Mobile ${snapshot.mobileState}.",
                            "Fine Location permission is currently Denied, limiting cell-tower dBm logging.",
                            "Session '${session.title}' recorded ${session.secondaryTitle}: ${session.secondarySubtitle}."
                        ),
                        supportingData = "Live battery: ${snapshot.batteryPercent}% · Voltage: ${snapshot.voltageVolts}V · Temp: ${snapshot.temperatureCelsius}°C",
                        primaryContributor = "Cellular & Network Radio State",
                        confidence = ConfidenceLevel.INSUFFICIENT_DATA,
                        dataLimitations = "Fine Location permission is needed for granular cellular dBm signal strength.",
                        isInsufficientData = true
                    )
                } else {
                    AiDoctorExchange(
                        question = question,
                        conclusion = "On-Device Telemetry Summary: Mobile network (${snapshot.mobileState}) and Wi-Fi (${snapshot.wifiState}) metrics packaged for external Gemini analysis.",
                        evidencePoints = listOf(
                            "Live network state: Wi-Fi ${snapshot.wifiState} · Mobile ${snapshot.mobileState}.",
                            "Session '${session.title}' logged ${session.drainPercent}% drain (${session.startBatteryPercent}% → ${session.endBatteryPercent}%).",
                            "Secondary factor: ${session.secondaryTitle} (${session.secondarySubtitle})."
                        ),
                        supportingData = "Measured battery level: ${snapshot.batteryPercent}% · Rate: ${snapshot.drainRateText}",
                        primaryContributor = "${session.secondaryTitle} (${session.secondarySubtitle})",
                        confidence = ConfidenceLevel.LIKELY,
                        dataLimitations = "Android Standard Mode does not expose exact modem mAh attribution."
                    )
                }
            }
            q.contains("hot") || q.contains("temp") || q.contains("warm") || q.contains("heat") -> {
                AiDoctorExchange(
                    question = question,
                    conclusion = "On-Device Telemetry Summary: Current battery thermistor reading is ${snapshot.temperatureCelsius}°C (${snapshot.temperatureStatus}).",
                    evidencePoints = listOf(
                        "Live thermistor reading: ${snapshot.temperatureCelsius}°C (${snapshot.thermalStatusLabel}).",
                        "Session peak temperature: ${session.peakTempCelsius}°C during '${session.title}'.",
                        "Charging state: ${snapshot.chargingSource} at ${snapshot.voltageVolts}V."
                    ),
                    supportingData = "Thermal status: ${snapshot.thermalStatusLabel} · Normal operating range: 24°C–34°C.",
                    primaryContributor = "Thermal & Charging Load (${snapshot.temperatureCelsius}°C)",
                    confidence = ConfidenceLevel.MEASURED,
                    dataLimitations = "Only battery thermistor temperature is exposed by Android public APIs; per-core CPU temperatures are restricted."
                )
            }
            q.contains("slow") || q.contains("charg") || q.contains("watt") || q.contains("cable") -> {
                AiDoctorExchange(
                    question = question,
                    conclusion = "On-Device Telemetry Summary: Current power state is '${snapshot.chargingSource}' (${snapshot.liveChargingWatts?.let { "${it}W live" } ?: "${snapshot.voltageVolts}V / ${snapshot.currentMilliAmps}mA"}). An Unknown charger or lower-wattage adapter can reduce charging speed compared to a high-wattage PD adapter.",
                    evidencePoints = listOf(
                        "Live voltage: ${snapshot.voltageVolts}V (${snapshot.voltageStatus}).",
                        "Instantaneous current: ${snapshot.currentMilliAmps}mA (${snapshot.currentStatus}).",
                        "Battery temperature during session: ${snapshot.temperatureCelsius}°C."
                    ),
                    supportingData = "Power source: ${snapshot.chargingSource} · Health label: ${snapshot.healthLabel}",
                    primaryContributor = "Charger Power Delivery & CC/CV Taper",
                    confidence = ConfidenceLevel.MEASURED,
                    dataLimitations = "Wattage is derived from Voltage (${snapshot.voltageVolts}V) × Current (${snapshot.currentMilliAmps}mA)."
                )
            }
            q.contains("health") || q.contains("capacity") || q.contains("degrad") || q.contains("cycle") -> {
                AiDoctorExchange(
                    question = question,
                    conclusion = if (snapshot.healthScore > 0) {
                        "On-Device Telemetry Summary: App-estimated Health Score is ${snapshot.healthScore}/100 (~${snapshot.estimatedFullCapacityMah} mAh vs ${snapshot.designCapacityMah} mAh design)."
                    } else {
                        "On-Device Telemetry Summary: OS reports '${snapshot.healthLabel}' health. Capacity score is calibrating as charge cycles are recorded."
                    },
                    evidencePoints = listOf(
                        "Android OS health register: ${snapshot.healthLabel} (Measured).",
                        "Cycle count register: ${snapshot.cycleCount?.let { "$it cycles" } ?: "Not exposed by OEM"}.",
                        "Live voltage & temperature: ${snapshot.voltageVolts}V at ${snapshot.temperatureCelsius}°C."
                    ),
                    supportingData = "Health status: ${snapshot.healthLabel} · Design capacity: ${if (snapshot.designCapacityMah > 0) "${snapshot.designCapacityMah} mAh" else "Calibrating"}",
                    primaryContributor = "Lithium-Ion Cycle & Thermal Aging",
                    confidence = ConfidenceLevel.LIKELY,
                    dataLimitations = "App-generated capacity estimate based on coulomb-counter charge sessions. Never presented as official manufacturer warranty health."
                )
            }
            q.contains("kernel") || q.contains("cpu") || q.contains("voltage regulator") || q.contains("hardware defect") -> {
                AiDoctorExchange(
                    question = question,
                    conclusion = "Insufficient data to identify the cause. Android restricts kernel-level CPU frequency residency and hardware rail telemetry in Standard Mode.",
                    evidencePoints = listOf(
                        "Per-process CPU frequency states and kernel wakelocks are blocked by Android SELinux sandbox.",
                        "Hardware failure cannot be diagnosed without long-term coulomb and impedance evidence."
                    ),
                    supportingData = "Current mode: ${if (adbEnabled) "ADB Mode (Partial wakelocks visible, hardware rail telemetry unavailable)" else "Standard Mode"}.",
                    primaryContributor = "Insufficient Data",
                    confidence = ConfidenceLevel.INSUFFICIENT_DATA,
                    dataLimitations = "Missing: Kernel wakelock table & per-rail power telemetry. Enable ADB Advanced Mode or run an Overnight Test to gather wake correlation data.",
                    isInsufficientData = true
                )
            }
            else -> {
                AiDoctorExchange(
                    question = question,
                    conclusion = "On-Device Telemetry Summary: During ${session.title} (${session.timeWindow}), battery changed by ${session.drainPercent}% (${session.startBatteryPercent}% → ${session.endBatteryPercent}%). Live sensors: ${snapshot.batteryPercent}%, ${snapshot.temperatureCelsius}°C, ${snapshot.voltageVolts}V.",
                    evidencePoints = listOf(
                        "Session '${session.title}': ${session.startBatteryPercent}% → ${session.endBatteryPercent}% (-${session.drainPercent}%).",
                        "Primary signal: ${session.primaryTitle} (${session.primarySubtitle}).",
                        "System state: Screen ${snapshot.screenState} · Wi-Fi ${snapshot.wifiState} · Mobile ${snapshot.mobileState}."
                    ),
                    supportingData = "Drain rate: ${session.drainRatePerHr}%/hr · Peak temp: ${session.peakTempCelsius}°C",
                    primaryContributor = "${session.primaryTitle} (${session.primarySubtitle})",
                    confidence = ConfidenceLevel.STRONG_EVIDENCE,
                    dataLimitations = if (adbEnabled) {
                        "ADB Mode active: Wakelock inspection enabled. Exact per-app mAh remains estimated."
                    } else {
                        "Standard Mode: Exact per-app mAh and system wakelocks require ADB Mode."
                    }
                )
            }
        }
    }
}
