package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ForensicsPalette
import kotlin.math.cos
import kotlin.math.sin

data class StatGuideTopic(
    val key: String,
    val title: String,
    val category: String,
    val plainMeaning: String,
    val healthyRange: String,
    val warningRange: String,
    val practicalTip: String
)

data class FaqEntry(
    val question: String,
    val answer: String,
    val category: String
)

object ForensicsGuideCatalog {
    val topics: Map<String, StatGuideTopic> = listOf(
        StatGuideTopic(
            key = "health_score",
            title = "Battery Health & Calibration",
            category = "Dashboard",
            plainMeaning = "What it did: Measured the charge stored in your battery and compared it against your phone's factory design rating.\n\nWhy it calibrates: For your first 3 full charges, the app stays in 'Calibrating' mode and averages your last 5–10 sessions so a single fluctuation never gives a fake or over-100% score.",
            healthyRange = "85% – 100% (Healthy battery with normal wear)",
            warningRange = "Below 80% (Noticeable aging; shorter daily battery life)",
            practicalTip = "Charge from below 30% up to 100% a few times so the app can lock in your true full capacity."
        ),
        StatGuideTopic(
            key = "capacity_cycles",
            title = "Capacity (mAh) & Cycle Count",
            category = "Dashboard",
            plainMeaning = "What it did: Calculated how much energy (mAh) your battery holds on a full charge (capped at Factory Capacity) and tracked your total charge cycles.\n\n• Custom Cycle Tracker: When a phone brand hides hardware cycles, the app adds up the mAh from your charging sessions and adds +1 cycle every time you complete an equivalent 100% charge.",
            healthyRange = "0 – 300 cycles (Peak battery lifespan)",
            warningRange = "500+ cycles (Batteries naturally lose 15–20% capacity after 500 cycles)",
            practicalTip = "Two half-charges (50% → 100% twice) equal 1 full cycle. Partial charges are gentler than deep 0% drains."
        ),
        StatGuideTopic(
            key = "live_readings",
            title = "Live Temperature, Voltage & Current",
            category = "Dashboard",
            plainMeaning = "What it did: Read the live sensors inside your battery pack:\n• Temperature (°C): Turns amber/red if your battery exceeds 38°C.\n• Voltage (V): Electrical pressure inside the cell.\n• Current (mA): Live electricity flowing in (+) while charging or flowing out (–) while unplugged.",
            healthyRange = "Temp: 20°C – 37°C · Voltage: 3.7V – 4.4V",
            warningRange = "Temp: 38°C+ (Running warm — slows charging & increases wear)",
            practicalTip = "If temperature exceeds 38°C, remove thick cases while fast-charging or let the phone cool down."
        ),
        StatGuideTopic(
            key = "drain_monitor",
            title = "Drain Rate Monitor (Active vs Standby)",
            category = "Dashboard",
            plainMeaning = "What it did: Tracked how fast your battery percentage drops per hour while unplugged, separating screen-on usage from screen-off sleep time:\n• Active Use: Drain speed while your screen is on.\n• Standby (Idle): Drain speed while your screen is off in your pocket or overnight.",
            healthyRange = "Active: 6% – 14%/hr · Standby: 0.5% – 1.5%/hr",
            warningRange = "Active: 18%+/hr · Standby: 2.5%+/hr (Background drain)",
            practicalTip = "If Standby Drain is above 2.5%/hr, open the 'Diagnose' tab to see what is keeping your phone awake."
        ),
        StatGuideTopic(
            key = "phone_status",
            title = "Live Phone Status & Sleep Mode",
            category = "Dashboard",
            plainMeaning = "What it did: Checked the hardware states that impact battery drain right now:\n• Sleep Mode (Doze): Whether Android has put background tasks into deep sleep.\n• Mobile Signal: Weak cell signal (1–2 bars) forces the radio amplifier to use up to 3× more power.",
            healthyRange = "Strong Wi-Fi or 4–5 signal bars; Sleep Mode active when idle",
            warningRange = "Weak signal (1–2 bars) or phone staying Awake with screen off",
            practicalTip = "In areas with weak 5G reception, switching to Wi-Fi or LTE can cut standby drain in half."
        ),
        StatGuideTopic(
            key = "time_estimates",
            title = "Estimated Time Left (By Activity)",
            category = "Dashboard",
            plainMeaning = "What it did: Combined your current battery percentage and capacity with real-world discharge rates to estimate how many hours remain for Screen On, Video Streaming, Voice Calls, or Standby.",
            healthyRange = "6h – 10h+ of active screen time on a full charge",
            warningRange = "Under 4h of screen time on a full charge (High drain or aged battery)",
            practicalTip = "Lowering screen brightness and using Wi-Fi instead of 5G can add 1.5+ hours of active screen time."
        ),
        StatGuideTopic(
            key = "phone_capabilities",
            title = "Phone Sensor Capabilities",
            category = "Dashboard",
            plainMeaning = "What it did: Checked which battery sensors your phone brand exposes vs. restricts. Green means direct hardware readings; Amber means smart estimation or permission needed.",
            healthyRange = "Direct sensor support + Usage Access enabled",
            warningRange = "Usage Access denied (prevents app-level background tracking)",
            practicalTip = "Go to Settings → Permissions to grant Usage Access if App Usage shows as limited."
        ),
        StatGuideTopic(
            key = "diagnose_drain",
            title = "Drain Detective & Evidence Breakdown",
            category = "Diagnose",
            plainMeaning = "What it did: Analyzed your recorded drain sessions to rank the Primary and Secondary reasons your battery dropped (such as screen-on usage, background app wakeups, or weak cellular signal).",
            healthyRange = "Overnight drain under 6%–8% over 8 hours (~0.7%–1.0%/hr)",
            warningRange = "Overnight drain above 12% over 8 hours (Flagged as Anomaly)",
            practicalTip = "Tap '+ Add Note' on the timeline when you game or use hotspot to see how it affects your curve."
        ),
        StatGuideTopic(
            key = "diagnose_tests",
            title = "Live Tests & Controlled Experiments",
            category = "Diagnose",
            plainMeaning = "What it did: Lets you run a timed benchmark (like a 30-min Idle Test or 5G vs LTE Test) or compare 'Before vs. After' drain when you change a setting or restrict an app.",
            healthyRange = "Idle Test drain under 1.5%/hr with screen off",
            warningRange = "Idle Test drain above 3.0%/hr indicates background wakeups",
            practicalTip = "Run an Overnight Test while sleeping (unplugged) to measure your phone's true baseline standby health."
        ),
        StatGuideTopic(
            key = "insights_trends",
            title = "Drain Trends & Usual Baseline",
            category = "Insights",
            plainMeaning = "What it did: Plotted your daily, weekly, and monthly battery drain and compared your latest session against your multi-day average ('Usual Drain'). Orange highlights unusual spikes.",
            healthyRange = "Daily drain within 10%–15% of your usual baseline",
            warningRange = "1.5× or higher than your usual overnight or daily average",
            practicalTip = "Switch between Day, Week, and Month views to check if a recent app update increased your daily drain."
        ),
        StatGuideTopic(
            key = "insights_apps",
            title = "App Activity & Screen Time",
            category = "Insights",
            plainMeaning = "What it did: Used Android's UsageEvents API to measure how long each app was on screen and how often it ran in the background, estimating its overall battery impact.",
            healthyRange = "Low background activity for apps you rarely use",
            warningRange = "High background events from social or shopping apps while idle",
            practicalTip = "Enable 'Usage Access' in Settings → Permissions so this list stays accurate."
        ),
        StatGuideTopic(
            key = "charging_speed",
            title = "Charging Speed (Watts) & 80% Slowdown",
            category = "Charging",
            plainMeaning = "What it did: Multiplied live Voltage × Current to measure real charging power in Watts (W) and tracked how your phone tapers power above 80% to protect the battery.",
            healthyRange = "15W – 45W+ fast charging (below 80%) · Smooth slowdown above 80%",
            warningRange = "Under 5W on a wall charger (check for a worn cable or lint in port)",
            practicalTip = "Save profiles for your home, car, and office chargers in the 'Chargers' tab to compare speed and heat."
        ),
        StatGuideTopic(
            key = "charging_health",
            title = "Charging Stress & Heat Check",
            category = "Charging",
            plainMeaning = "What it did: Measured how hard your charger is pushing your battery relative to its capacity (C-Rate stress) and how many degrees of safety margin remain below 40°C.",
            healthyRange = "Stress under 1.0C · Temperature under 38°C while charging",
            warningRange = "Temperature above 38°C–40°C (Phone will throttle charging speed)",
            practicalTip = "Avoid heavy gaming or placing your phone under a pillow while fast-charging."
        ),
        StatGuideTopic(
            key = "settings_permissions",
            title = "Why Permissions Matter",
            category = "Settings",
            plainMeaning = "What it did: Checked which system permissions are active. Battery info works automatically, while 'Usage Access' lets the app see which apps wake up your phone.",
            healthyRange = "Battery Info + Usage Access granted",
            warningRange = "Usage Access denied (hides per-app screen & background stats)",
            practicalTip = "All permission data stays 100% on your phone and is never uploaded anywhere."
        ),
        StatGuideTopic(
            key = "settings_alerts",
            title = "Smart Battery & Heat Alerts",
            category = "Settings",
            plainMeaning = "What it did: Configures when the app warns you about higher-than-usual drain, battery temperature above 38°C, slow charging, or capacity changes.",
            healthyRange = "Medium sensitivity (balanced for everyday use)",
            warningRange = "Alerts turned off (you might miss an overheating charger or runaway app)",
            practicalTip = "Keep 'Battery temperature is high' enabled so you're alerted whenever heat crosses 38°C."
        ),
        StatGuideTopic(
            key = "settings_adb",
            title = "Advanced Mode & Battery Impact",
            category = "Settings",
            plainMeaning = "What it did: Allows power users to inspect deep kernel wakelocks via ADB, export JSON/CSV reports, and verify that this app uses less than 0.8% battery per day.",
            healthyRange = "App daily battery use under 1% (Event-driven tracking)",
            warningRange = "N/A — zero continuous background polling",
            practicalTip = "You don't need ADB Mode for everyday battery health, drain speed, or charger tracking."
        )
    ).associateBy { it.key }

    val faqs: List<FaqEntry> = listOf(
        FaqEntry(
            question = "How do I use this app for the best results?",
            answer = "1. Grant 'Usage Access' in Settings → Permissions so the app can see which apps run on screen and in the background.\n2. Use and charge your phone normally for 3 to 5 charge cycles so the Health Score finishes calibrating.\n3. Tap any small grey (i) icon next to any statistic to see what it did and whether your numbers are healthy.",
            category = "Getting Started"
        ),
        FaqEntry(
            question = "Why does my Battery Health Score say 'Calibrating'?",
            answer = "A single short charge can fluctuate due to temperature or background tasks. Instead of showing an inaccurate or over-100% number on day one, the app learns across your first 3 to 5 charge cycles and smooths the last 5–10 sessions so your health score is realistic and capped at 100%.",
            category = "Health & Capacity"
        ),
        FaqEntry(
            question = "What do mAh, mA, Voltage (V), and Watts (W) mean?",
            answer = "• mAh (Milliamp-hours): Battery bucket size — how much total charge your battery stores (e.g., 4855 mAh).\n• mA (Milliamps): Live flow speed — positive (+mA) when charging, negative (–mA) when draining.\n• V (Volts): Electrical pressure (usually 3.7V when low up to 4.4V when full).\n• W (Watts): Total charging power (Volts × Amps). A 25W charger fills your battery much faster than a 5W charger.",
            category = "Stats Explained"
        ),
        FaqEntry(
            question = "What is a normal battery drain rate (% per hour)?",
            answer = "• Screen On (Active Use): 6%/hr to 14%/hr is normal for web browsing, social media, and video. Heavy 3D gaming or camera use can reach 18%–25%/hr.\n• Screen Off (Standby / Idle): 0.5%/hr to 1.5%/hr is healthy. If standby drain exceeds 2.5%/hr, an app or weak cellular signal is keeping your phone awake.",
            category = "Drain & Standby"
        ),
        FaqEntry(
            question = "Why does my Temperature card turn Amber/Red above 38°C?",
            answer = "Lithium batteries are happiest between 20°C and 37°C. Above 38°C, chemical wear accelerates and Android may throttle CPU speed or slow down charging. The app highlights temperatures above 38°C so you know when to let your phone cool down.",
            category = "Temperature"
        ),
        FaqEntry(
            question = "How does the Cycle Count work if my phone hides hardware cycles?",
            answer = "Some phone brands report exact cycle counts from the battery chip, while others hide it. When hardware data is unavailable, our built-in tracker continuously measures the charge added (mAh) during your charging sessions and increments your custom cycle count every time you complete an equivalent 100% full charge.",
            category = "Health & Capacity"
        ),
        FaqEntry(
            question = "Why does charging slow down after 80%?",
            answer = "To prevent overheating and battery swelling, phones use fast constant current from 0% to 80%, then gradually step down the current ('taper') from 80% to 100%. Slow charging above 80% is a sign of a healthy charging system, not a broken charger.",
            category = "Charging"
        ),
        FaqEntry(
            question = "Does Battery Forensics cause battery drain or phone overheating?",
            answer = "No. Battery Forensics operates on a strict Zero-Footprint architecture:\n• Battery Impact: < 0.1% per 24 hours (passive OS broadcast listener only).\n• Background CPU: 0.0% (never runs continuous wake-lock loops or polling timers).\n• Thermal Footprint: Zero heat generated.\n• Hardware Safety: You can verify this anytime in Insights → Battery Forensics Self-Audit.",
            category = "Safety & Heat"
        ),
        FaqEntry(
            question = "How do I unlock exact per-app mAh & wakelocks with ADB?",
            answer = "Google restricts exact per-app mAh on Android 10–15 to prevent fingerprinting. You can grant BATTERY_STATS permission with a single one-time ADB command via PC or Shizuku/Wireless Debugging:\n\nadb shell pm grant com.nextgen.batteryforensics android.permission.BATTERY_STATS\n\nThis unlocks microscopic per-app mAh consumption, kernel wakelocks, and modem radio power without rooting your phone!",
            category = "Advanced & ADB"
        ),
        FaqEntry(
            question = "Does this app upload my personal data?",
            answer = "No. All tracking is event-driven and stores 100% of diagnostic logs within your phone's local private SQLite sandbox. No cloud servers, no account registration, and zero remote analytics.",
            category = "Privacy"
        )
    )
}

/**
 * Subtle, small grey (i) icon button placed beside headers and metrics across the app.
 */
@Composable
fun CardInfoIconButton(
    topicKey: String,
    onShowTopic: (StatGuideTopic) -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = ForensicsPalette.TextMuted
) {
    val topic = ForensicsGuideCatalog.topics[topicKey] ?: return
    Box(
        modifier = modifier
            .size(22.dp)
            .clip(CircleShape)
            .background(ForensicsPalette.SubtleSurfaceAlt)
            .border(1.dp, ForensicsPalette.BorderStrong, CircleShape)
            .clickable { onShowTopic(topic) }
            .testTag("info_button_$topicKey"),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "i",
            fontSize = 11.sp,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            color = tint,
            textAlign = TextAlign.Center,
            lineHeight = 11.sp
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatInfoBottomSheet(
    topic: StatGuideTopic,
    onOpenFullGuide: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ForensicsPalette.CardSurface,
        modifier = Modifier.testTag("stat_info_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ClassificationBadge(
                    text = "${topic.category} · What It Did",
                    containerColor = ForensicsPalette.BlueContainer,
                    contentColor = ForensicsPalette.BluePrimary
                )
                TextButton(onClick = onDismiss) {
                    Text("Got it", fontWeight = FontWeight.Bold, color = ForensicsPalette.BluePrimary)
                }
            }

            Text(
                text = topic.title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = ForensicsPalette.TextPrimary
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(ForensicsPalette.SubtleSurface)
                    .border(1.dp, ForensicsPalette.BorderSubtle, RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Text(
                    text = "HOW IT WORKS & WHAT IT MEASURED",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.7.sp,
                    color = ForensicsPalette.TextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = topic.plainMeaning,
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    color = ForensicsPalette.TextPrimary
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val boxShape = RoundedCornerShape(14.dp)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(boxShape)
                        .background(ForensicsPalette.GreenSoftTile)
                        .border(1.dp, ForensicsPalette.GreenBorder, boxShape)
                        .padding(12.dp)
                ) {
                    Text(
                        text = "✓ NORMAL / HEALTHY",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = ForensicsPalette.GreenPrimary
                    )
                    Spacer(modifier = Modifier.height(5.dp))
                    Text(
                        text = topic.healthyRange,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = ForensicsPalette.TextPrimary
                    )
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(boxShape)
                        .background(ForensicsPalette.AmberSoftTile)
                        .border(1.dp, ForensicsPalette.AmberBorder, boxShape)
                        .padding(12.dp)
                ) {
                    Text(
                        text = "⚠ WATCH OUT",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = ForensicsPalette.AmberDarkText
                    )
                    Spacer(modifier = Modifier.height(5.dp))
                    Text(
                        text = topic.warningRange,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = ForensicsPalette.TextPrimary
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(ForensicsPalette.BlueSoftTile)
                    .border(1.dp, ForensicsPalette.BlueBorder, RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Text(
                    text = "💡 HELPFUL TIP",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = ForensicsPalette.BluePrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = topic.practicalTip,
                    fontSize = 13.sp,
                    color = ForensicsPalette.TextPrimary
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = {
                        onDismiss()
                        onOpenFullGuide()
                    }
                ) {
                    Text(
                        text = "Open Full FAQ in Settings →",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = ForensicsPalette.BluePrimary
                    )
                }
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = ForensicsPalette.BluePrimary),
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Text("Close", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

private val OnboardingGold = Color(0xFFF7B318)
private val OnboardingChalkWhite = Color(0xFFF2F0E8)
private val OnboardingMutedText = Color(0xFF9BA5B2)
private val OnboardingDarkInk = Color(0xFF14181E)

/**
 * Full-Screen 4-Step Dark-Slate Textured SVG Onboarding Flow matching Screenshot_20261002_020131_Claude.jpg:
 * - Deep radial slate background (#28323E -> #1C232B -> #12161B) + fine organic grain/stipple noise texture
 * - Chalk-textured white vector strokes + glowing golden-amber (#F7B318) highlights, pills, and Next -> button
 */
@Composable
fun ForensicsGuideAndOnboardingModal(
    initialTab: Int = 0,
    onSelectStartingLevel: (Int) -> Unit = {},
    onNavigateToPermissions: () -> Unit = {},
    onDismiss: () -> Unit
) {
    var currentStep by rememberSaveable { mutableIntStateOf(initialTab.coerceIn(0, 3)) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF2B3542),
                        Color(0xFF1E2630),
                        Color(0xFF13181E)
                    )
                )
            )
            .testTag("guide_and_onboarding_modal"),
        contentAlignment = Alignment.TopCenter
    ) {
        // Full-screen fine film grain / stipple noise texture overlay matching the SVG filter
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawFineSlateGrainTexture()
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 520.dp)
                .windowInsetsPadding(WindowInsets.statusBars)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = 28.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Row: "Skip" in the top-right corner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentStep < 3) {
                    Text(
                        text = "Skip",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = OnboardingMutedText,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onDismiss() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("close_guide_modal_button")
                    )
                } else {
                    Spacer(
                        modifier = Modifier
                            .size(28.dp)
                            .clickable { onDismiss() }
                            .testTag("close_guide_modal_button")
                    )
                }
            }

            // Center Open-Canvas SVG Illustration + Headline + Subtitle
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                when (currentStep) {
                    0 -> OnboardingDarkSvg1ShowMeWhy()
                    1 -> OnboardingDarkSvg2EvidenceChain()
                    2 -> OnboardingDarkSvg3HonestData()
                    3 -> OnboardingDarkSvg4Private()
                }

                Spacer(modifier = Modifier.height(34.dp))

                val headline = when (currentStep) {
                    0 -> "Find out why your\nbattery drained"
                    1 -> "Every finding shows\nits evidence"
                    2 -> "Every number is\nhonestly labeled"
                    else -> "Your data stays\non your phone"
                }

                val subtext = when (currentStep) {
                    0 -> "Not just 82%. See what happened\novernight and what likely\ncontributed to the drain."
                    1 -> "Follow the chain from battery drop\nto contributor. Every conclusion\ncomes with a confidence level."
                    2 -> "Measured, estimated or unavailable.\nNo fake scores, no RAM boosters,\nno cache cleaners."
                    else -> "Stored on your device. No account,\nno uploads without your consent.\nPermissions are explained first."
                }

                Text(
                    text = headline,
                    fontSize = 30.sp,
                    lineHeight = 38.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnboardingChalkWhite,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = subtext,
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    color = OnboardingMutedText,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Bottom Section: 4-Dot Pagination (Gold Pill + Slate Dots) & Full-Width Gold Pill Button
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (step in 0..3) {
                        val isCurrent = step == currentStep
                        Box(
                            modifier = Modifier
                                .width(if (isCurrent) 28.dp else 8.dp)
                                .height(8.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .background(if (isCurrent) OnboardingGold else Color(0xFF566170))
                                .clickable { currentStep = step }
                                .testTag("onboarding_dot_$step")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (currentStep < 3) {
                            currentStep += 1
                        } else {
                            onSelectStartingLevel(0)
                            onDismiss()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = OnboardingGold,
                        contentColor = OnboardingDarkInk
                    ),
                    shape = RoundedCornerShape(999.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .testTag(if (currentStep == 3) "onboarding_start_diagnostics_button" else "onboarding_continue_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (currentStep == 3) "Get started" else "Next",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnboardingDarkInk
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        // Crisp vector right arrow matching the screenshot button ("Next ->")
                        Canvas(modifier = Modifier.size(18.dp)) {
                            val w = size.width
                            val h = size.height
                            val strokeW = 2.4.dp.toPx()
                            drawLine(
                                color = OnboardingDarkInk,
                                start = Offset(0f, h * 0.5f),
                                end = Offset(w * 0.88f, h * 0.5f),
                                strokeWidth = strokeW,
                                cap = StrokeCap.Round
                            )
                            val headPath = Path().apply {
                                moveTo(w * 0.52f, h * 0.18f)
                                lineTo(w * 0.92f, h * 0.5f)
                                lineTo(w * 0.52f, h * 0.82f)
                            }
                            drawPath(
                                path = headPath,
                                color = OnboardingDarkInk,
                                style = Stroke(
                                    width = strokeW,
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Draws the subtle SVG feTurbulence / grain noise texture across the dark slate canvas.
 */
private fun DrawScope.drawFineSlateGrainTexture() {
    val w = size.width
    val h = size.height
    var seed = 1337421
    val cols = 46
    val rows = 84
    val cellW = w / cols
    val cellH = h / rows

    for (r in 0 until rows) {
        for (c in 0 until cols) {
            seed = (seed * 1103515245 + 12345) and 0x7fffffff
            val dx = ((seed % 100) / 100f) * cellW
            seed = (seed * 1103515245 + 12345) and 0x7fffffff
            val dy = ((seed % 100) / 100f) * cellH
            val isLightSpeck = (seed and 1) == 0
            drawCircle(
                color = if (isLightSpeck) Color(0x0BFFFFFF) else Color(0x14000000),
                radius = 1.1.dp.toPx(),
                center = Offset(c * cellW + dx, r * cellH + dy)
            )
        }
    }
}

/**
 * Helper to draw a chalk-textured stroke (matches the organic hand-drawn/textured white stroke in the SVG).
 */
private fun DrawScope.drawChalkTexturedPath(
    path: Path,
    color: Color = OnboardingChalkWhite,
    strokeWidthPx: Float
) {
    // Base core stroke
    drawPath(
        path = path,
        color = color.copy(alpha = 0.88f),
        style = Stroke(
            width = strokeWidthPx,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )
    // Stippled chalk overlay along the path
    val measure = PathMeasure()
    measure.setPath(path, false)
    val len = measure.length
    var dist = 0f
    var s = 98765
    while (dist < len) {
        val pos = measure.getPosition(dist)
        if (pos != Offset.Unspecified) {
            s = (s * 1664525 + 1013904223) and 0x7fffffff
            val jitterX = (((s % 21) - 10) / 10f) * (strokeWidthPx * 0.36f)
            s = (s * 1664525 + 1013904223) and 0x7fffffff
            val jitterY = (((s % 21) - 10) / 10f) * (strokeWidthPx * 0.36f)
            drawCircle(
                color = Color(0xFF1C232B).copy(alpha = 0.48f),
                radius = strokeWidthPx * 0.19f,
                center = Offset(pos.x + jitterX, pos.y + jitterY)
            )
        }
        dist += strokeWidthPx * 0.42f
    }
}

/**
 * SVG 1: onboarding-1-show-me-why.svg (Matches Screenshot_20261002_020131_Claude.jpg)
 * - Open dark canvas (no card box)
 * - "BATTERY LEVEL" header
 * - Chalk-white initial curve -> dotted "your normal" line + glowing golden-amber steep drop
 * - Golden bullseye target ring on the steep drop connected to the golden "-14%" pill
 * - Chalk-white bottom axis with tick marks at 11 PM, 3 AM, 7 AM
 */
@Composable
private fun OnboardingDarkSvg1ShowMeWhy() {
    var highlightAnomaly by remember { mutableStateOf(true) }
    var animateTrigger by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { animateTrigger = true }

    val dropProgress by animateFloatAsState(
        targetValue = if (animateTrigger) 1f else 0f,
        animationSpec = tween(durationMillis = 750, easing = FastOutSlowInEasing),
        label = "svg1_drop"
    )
    val infiniteTransition = rememberInfiniteTransition(label = "bullseye_pulse")
    val ringPulse by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ring_scale"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { highlightAnomaly = !highlightAnomaly }
            .testTag("svg_card_1")
    ) {
        Text(
            text = "BATTERY LEVEL",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 1.8.sp,
            color = OnboardingMutedText
        )

        Spacer(modifier = Modifier.height(14.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(235.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Segment 1 (11 PM to ~1:20 AM): Chalk-textured white curve
                val seg1 = Path().apply {
                    moveTo(w * 0.02f, h * 0.08f)
                    cubicTo(
                        w * 0.14f, h * 0.09f,
                        w * 0.25f, h * 0.13f,
                        w * 0.36f, h * 0.19f
                    )
                }
                drawChalkTexturedPath(
                    path = seg1,
                    color = OnboardingChalkWhite,
                    strokeWidthPx = 4.6.dp.toPx()
                )

                // Dotted "your normal" line continuing gently from (0.36, 0.19) to (0.94, 0.43)
                val dotCount = 13
                for (i in 1..dotCount) {
                    val t = i / dotCount.toFloat()
                    val x = w * (0.36f + (0.93f - 0.36f) * t)
                    val y = h * (0.19f + (0.43f - 0.19f) * t)
                    drawCircle(
                        color = Color(0xFF8D97A4),
                        radius = 2.4.dp.toPx(),
                        center = Offset(x, y)
                    )
                }

                // Segment 2 (~1:20 AM to ~4:10 AM): Steep Golden-Amber Drop
                val goldenSlopePath = Path().apply {
                    moveTo(w * 0.36f, h * 0.19f)
                    cubicTo(
                        w * 0.43f, h * 0.23f,
                        w * 0.51f, h * 0.46f,
                        w * 0.61f, h * 0.67f
                    )
                }
                val slopeMeasure = PathMeasure()
                slopeMeasure.setPath(goldenSlopePath, false)
                val animatedGoldenPath = Path()
                slopeMeasure.getSegment(
                    startDistance = 0f,
                    stopDistance = slopeMeasure.length * dropProgress,
                    destination = animatedGoldenPath,
                    startWithMoveTo = true
                )

                // Segment 3 (~4:10 AM to 7 AM): Chalk-textured white tail curve
                val seg3 = Path().apply {
                    moveTo(w * 0.61f, h * 0.67f)
                    cubicTo(
                        w * 0.72f, h * 0.72f,
                        w * 0.84f, h * 0.75f,
                        w * 0.95f, h * 0.78f
                    )
                }
                drawChalkTexturedPath(
                    path = seg3,
                    color = OnboardingChalkWhite,
                    strokeWidthPx = 4.6.dp.toPx()
                )

                // Target Bullseye Ring on the Golden Slope + Pointer Line to "-14%" Pill
                val bullseyeCenter = Offset(w * 0.49f, h * 0.39f)
                if (highlightAnomaly) {
                    // Outer translucent golden-brown halo ring
                    drawCircle(
                        color = Color(0x38F7B318),
                        radius = 30.dp.toPx() * ringPulse,
                        center = bullseyeCenter,
                        style = Stroke(width = 6.dp.toPx())
                    )
                    // Inner crisp golden target ring
                    drawCircle(
                        color = OnboardingGold,
                        radius = 14.dp.toPx(),
                        center = bullseyeCenter,
                        style = Stroke(width = 2.6.dp.toPx())
                    )
                    // Diagonal pointer line from bullseye ring down-left to the "-14%" pill
                    drawLine(
                        color = OnboardingGold,
                        start = Offset(
                            bullseyeCenter.x - 10.dp.toPx(),
                            bullseyeCenter.y + 10.dp.toPx()
                        ),
                        end = Offset(w * 0.33f, h * 0.56f),
                        strokeWidth = 2.2.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }

                // Draw crisp golden slope on top
                drawPath(
                    path = animatedGoldenPath,
                    color = OnboardingGold,
                    style = Stroke(
                        width = 5.2.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )

                // Bottom Chalk-Textured Horizontal Axis Line + 3 Ticks (11 PM, 3 AM, 7 AM)
                val axisY = h * 0.94f
                val axisPath = Path().apply {
                    moveTo(w * 0.01f, axisY)
                    lineTo(w * 0.99f, axisY)
                }
                drawChalkTexturedPath(
                    path = axisPath,
                    color = OnboardingChalkWhite,
                    strokeWidthPx = 3.8.dp.toPx()
                )

                listOf(0.04f, 0.49f, 0.95f).forEach { xRatio ->
                    val tickPath = Path().apply {
                        moveTo(w * xRatio, axisY)
                        lineTo(w * xRatio, axisY + 9.dp.toPx())
                    }
                    drawChalkTexturedPath(
                        path = tickPath,
                        color = OnboardingChalkWhite,
                        strokeWidthPx = 3.2.dp.toPx()
                    )
                }
            }

            // "your normal" label near the dotted curve on the right
            Text(
                text = "your normal",
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = OnboardingMutedText,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 56.dp, end = 6.dp)
            )

            // Golden "-14%" Pill Badge connected to the bullseye pointer
            if (highlightAnomaly) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .offset(x = 52.dp, y = 18.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(OnboardingGold)
                        .padding(horizontal = 18.dp, vertical = 7.dp)
                ) {
                    Text(
                        text = "–14%",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnboardingDarkInk
                    )
                }
            }

            // "last night" golden label above the right end of the axis
            Text(
                text = "last night",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = OnboardingGold,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 22.dp, end = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Time Axis Labels: 11 PM | 3 AM | 7 AM
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "11 PM", fontSize = 13.sp, color = OnboardingMutedText)
            Text(text = "3 AM", fontSize = 13.sp, color = OnboardingMutedText)
            Text(text = "7 AM", fontSize = 13.sp, color = OnboardingMutedText)
        }
    }
}

/**
 * SVG 2: onboarding-2-evidence-chain.svg
 * Open dark-slate canvas with vertical evidence chain (1..4) and golden Primary Contributor pill.
 */
@Composable
private fun OnboardingDarkSvg2EvidenceChain() {
    var selectedStep by remember { mutableIntStateOf(3) }
    val steps = listOf(
        Triple("1", "Battery dropped", "–14%"),
        Triple("2", "Screen was off", "7h 42m"),
        Triple("3", "Device awake", "1h 18m"),
        Triple("4", "App X activity", "47 events")
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("svg_card_2")
    ) {
        Text(
            text = "EVIDENCE CHAIN",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 1.8.sp,
            color = OnboardingMutedText
        )

        Spacer(modifier = Modifier.height(18.dp))

        Box(modifier = Modifier.fillMaxWidth()) {
            // Vertical connecting line behind numbered nodes
            Canvas(
                modifier = Modifier
                    .padding(start = 15.dp, top = 18.dp)
                    .width(2.dp)
                    .height(175.dp)
            ) {
                drawLine(
                    color = Color(0xFF4A5565),
                    start = Offset(0f, 0f),
                    end = Offset(0f, size.height),
                    strokeWidth = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                steps.forEachIndexed { index, (num, label, value) ->
                    val isHighlighted = index == selectedStep || index == 0
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { selectedStep = index }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(if (index == selectedStep) OnboardingGold else Color(0xFF26303D))
                                    .border(
                                        width = 1.5.dp,
                                        color = if (index == selectedStep) OnboardingGold else Color(0xFF566375),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = num,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (index == selectedStep) OnboardingDarkInk else OnboardingChalkWhite
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = label,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Medium,
                                color = OnboardingChalkWhite
                            )
                        }

                        Text(
                            text = value,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isHighlighted) OnboardingGold else OnboardingChalkWhite
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        // Bottom Primary Contributor Callout Row matching SVG 2
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xFF232C38))
                .border(1.dp, Color(0xFF394656), RoundedCornerShape(18.dp))
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(OnboardingGold),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "!",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = OnboardingDarkInk
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "App X activity",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnboardingChalkWhite
                    )
                    Text(
                        text = "Primary contributor",
                        fontSize = 13.sp,
                        color = OnboardingMutedText
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(OnboardingGold)
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Likely",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnboardingDarkInk
                )
            }
        }
    }
}

/**
 * SVG 3: onboarding-3-honest-data.svg
 * Dark-slate layout showing Measured (green pill), Estimated (golden pill), Unavailable (muted label),
 * and NEVER IN THIS APP (RAM boost, Cache clean, Fake scores).
 */
@Composable
private fun OnboardingDarkSvg3HonestData() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("svg_card_3"),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Row 1: Battery temperature | 28.4 °C | Measured (Green pill)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF222B36))
                .border(1.dp, Color(0xFF344150), RoundedCornerShape(16.dp))
                .padding(horizontal = 18.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Battery temperature",
                    fontSize = 13.sp,
                    color = OnboardingMutedText
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "28.4 °C",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnboardingChalkWhite
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color(0xFF19382B))
                    .border(1.dp, Color(0xFF2E6F51), RoundedCornerShape(999.dp))
                    .padding(horizontal = 16.dp, vertical = 7.dp)
            ) {
                Text(
                    text = "Measured",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4ADE80)
                )
            }
        }

        // Row 2: Health score | 94 / 100 | Estimated (Golden pill)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF222B36))
                .border(1.dp, Color(0xFF344150), RoundedCornerShape(16.dp))
                .padding(horizontal = 18.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Health score",
                    fontSize = 13.sp,
                    color = OnboardingMutedText
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "94 / 100",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnboardingChalkWhite
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(OnboardingGold)
                    .padding(horizontal = 16.dp, vertical = 7.dp)
            ) {
                Text(
                    text = "Estimated",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnboardingDarkInk
                )
            }
        }

        // Row 3: Cycle count | Not exposed by OEM | Unavailable
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF222B36))
                .border(1.dp, Color(0xFF344150), RoundedCornerShape(16.dp))
                .padding(horizontal = 18.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Cycle count",
                    fontSize = 13.sp,
                    color = OnboardingMutedText
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Not exposed by OEM",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = OnboardingChalkWhite
                )
            }
            Text(
                text = "Unavailable",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = OnboardingMutedText
            )
        }

        // NEVER IN THIS APP section
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "NEVER IN THIS APP",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.8.sp,
                color = OnboardingMutedText
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("RAM boost", "Cache clean", "Fake scores").forEach { label ->
                    Text(
                        text = label,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = OnboardingMutedText,
                        textDecoration = TextDecoration.LineThrough
                    )
                }
            }
        }
    }
}

/**
 * SVG 4: onboarding-4-private.svg
 * Open dark-slate canvas with central phone + golden lock badge, "No uploads", "No account",
 * "Stays on this device", and "Export or delete your data anytime".
 */
@Composable
private fun OnboardingDarkSvg4Private() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("svg_card_4"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val center = Offset(w * 0.5f, h * 0.46f)

                // Concentric dashed protection rings around the phone
                drawCircle(
                    color = Color(0x33F7B318),
                    radius = 68.dp.toPx(),
                    center = center,
                    style = Stroke(
                        width = 1.6.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )
                )

                // Chalk-textured phone outline in the center
                val phoneW = 68.dp.toPx()
                val phoneH = 116.dp.toPx()
                val phoneLeft = center.x - phoneW / 2f
                val phoneTop = center.y - phoneH / 2f
                val phonePath = Path().apply {
                    addRoundRect(
                        androidx.compose.ui.geometry.RoundRect(
                            left = phoneLeft,
                            top = phoneTop,
                            right = phoneLeft + phoneW,
                            bottom = phoneTop + phoneH,
                            cornerRadius = CornerRadius(14.dp.toPx(), 14.dp.toPx())
                        )
                    )
                }
                drawChalkTexturedPath(
                    path = phonePath,
                    color = OnboardingChalkWhite,
                    strokeWidthPx = 3.6.dp.toPx()
                )

                // Blocked outward arrows on left & right ("No uploads" / "No account")
                val leftX = w * 0.18f
                val rightX = w * 0.82f
                drawCircle(
                    color = Color(0xFF394656),
                    radius = 16.dp.toPx(),
                    center = Offset(leftX, center.y - 18.dp.toPx()),
                    style = Stroke(width = 2.dp.toPx())
                )
                drawCircle(
                    color = Color(0xFF394656),
                    radius = 16.dp.toPx(),
                    center = Offset(rightX, center.y - 18.dp.toPx()),
                    style = Stroke(width = 2.dp.toPx())
                )

                // Diagonal slash across left & right circles
                val r = 11.dp.toPx() * cos(Math.PI / 4).toFloat()
                val rs = 11.dp.toPx() * sin(Math.PI / 4).toFloat()
                drawLine(
                    color = OnboardingGold,
                    start = Offset(leftX - r, center.y - 18.dp.toPx() - rs),
                    end = Offset(leftX + r, center.y - 18.dp.toPx() + rs),
                    strokeWidth = 2.2.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = OnboardingGold,
                    start = Offset(rightX - r, center.y - 18.dp.toPx() - rs),
                    end = Offset(rightX + r, center.y - 18.dp.toPx() + rs),
                    strokeWidth = 2.2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            // Golden rounded-square lock badge overlapping top of the phone silhouette
            Box(
                modifier = Modifier
                    .offset(y = (-24).dp)
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(OnboardingGold),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(22.dp)) {
                    val w = size.width
                    val h = size.height
                    drawRoundRect(
                        color = OnboardingDarkInk,
                        topLeft = Offset(w * 0.2f, h * 0.45f),
                        size = Size(w * 0.6f, h * 0.45f),
                        cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                    )
                    drawArc(
                        color = OnboardingDarkInk,
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = false,
                        topLeft = Offset(w * 0.3f, h * 0.15f),
                        size = Size(w * 0.4f, h * 0.5f),
                        style = Stroke(width = 2.4.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }

            // Left label: "No uploads"
            Text(
                text = "No uploads",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = OnboardingMutedText,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(top = 28.dp, start = 4.dp)
            )

            // Right label: "No account"
            Text(
                text = "No account",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = OnboardingMutedText,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(top = 28.dp, end = 4.dp)
            )

            // Bottom center label under phone: "Stays on this device"
            Text(
                text = "Stays on this device",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = OnboardingGold,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Export or delete your data anytime",
            fontSize = 13.sp,
            fontWeight = FontWeight.Normal,
            color = OnboardingMutedText
        )
    }
}

@Composable
fun FaqAccordionList(
    expandedIndex: Int,
    onSelectIndex: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        ForensicsGuideCatalog.faqs.forEachIndexed { idx, faq ->
            val isExpanded = expandedIndex == idx
            ForensicsCard(
                containerColor = if (isExpanded) ForensicsPalette.BlueSoftTile else ForensicsPalette.CardSurface,
                onClick = { onSelectIndex(idx) },
                modifier = Modifier.testTag("faq_item_$idx")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp)
                        ) {
                            ClassificationBadge(
                                text = faq.category,
                                containerColor = if (isExpanded) ForensicsPalette.BlueContainer else ForensicsPalette.SubtleSurfaceAlt,
                                contentColor = if (isExpanded) ForensicsPalette.BluePrimary else ForensicsPalette.TextSecondary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = faq.question,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = ForensicsPalette.TextPrimary
                            )
                        }
                        Text(
                            text = if (isExpanded) "▲" else "▼",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = ForensicsPalette.BluePrimary
                        )
                    }
                    AnimatedVisibility(visible = isExpanded) {
                        Column {
                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = ForensicsPalette.BorderSubtle)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = faq.answer,
                                fontSize = 13.sp,
                                lineHeight = 19.sp,
                                color = ForensicsPalette.TextPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}
