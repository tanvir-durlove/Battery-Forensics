package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ForensicsPalette

/**
 * Standard, Google Play Developer Policy-compliant Privacy Policy screen
 * tailored for Battery Forensics (com.nextgen.batteryforensics).
 */
@Composable
fun PrivacyPolicyScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var expandedSection by remember { mutableIntStateOf(0) }

    BackHandler {
        onNavigateBack()
    }

    val fullPolicyMarkdown = remember {
        """
# Privacy Policy — Battery Forensics
**Effective Date:** October 4, 2026
**Application:** Battery Forensics (Package: com.nextgen.batteryforensics)
**Developer:** NextGen Tool (Contact: nextgentoolbd@gmail.com)

---

### 1. Overview & Data Philosophy
Battery Forensics is an on-device diagnostic and analytical tool designed to provide transparent, evidence-based battery health metrics and discharge rate forensics.
**Core Privacy Commitment:** All battery telemetry, charging benchmarks, and diagnostic logs remain exclusively on your device. We do not operate remote tracking servers, sell your information, or perform clandestine background monitoring.

---

### 2. Information We Process
#### A. On-Device Diagnostic Data (Stored Locally Only):
* Battery level percentage, temperature (°C), live voltage (mV), and instantaneous current (mA).
* Full design capacity (mAh) and estimated health calibration ratings.
* Charging logs: Wattage curves, charging session durations, and thermal thresholds.
* Aggregated app screen-time and background wake-up frequencies (derived via Android UsageStats API).
* Storage: All diagnostic records are persisted solely within a private, sandboxed SQLite/Room database on your device.

#### B. Information We NEVER Collect:
* Names, email addresses, phone numbers, or user account credentials.
* Keystrokes, text inputs, messages, photos, or files.
* Physical GPS coordinates or real-time location tracking.

---

### 3. Android Permissions & Prominent Disclosures
In compliance with Google Play Developer Program policies, below is the exact rationale for every permission declared in our manifest:
* **PACKAGE_USAGE_STATS (Usage Access):** 
  *Purpose:* Allows the app to correlate device battery discharge with foreground application activity.
  *Scope:* Operates strictly on-device. Personal app data and message contents are never accessed or transmitted.
* **ACCESS_NETWORK_STATE & ACCESS_WIFI_STATE:**
  *Purpose:* Radio hardware state detection. Weak cellular reception and active data radios significantly increase battery consumption.
* **BLUETOOTH_SCAN (usesPermissionFlags="neverForLocation") & BLUETOOTH_CONNECT:**
  *Purpose:* Detects connected Bluetooth peripherals (e.g., audio headsets or smartwatches) to account for accessory power draw. Explicitly configured never to derive user physical location.
* **READ_PHONE_STATE:**
  *Purpose:* Observes cellular radio connectivity status to detect standby battery drain caused by cellular modem searching. No call logs, phone numbers, or audio are accessed.
* **POST_NOTIFICATIONS:**
  *Purpose:* Delivers user-configured system alerts (e.g., 38°C battery overheating warnings or slow charger detection).

---

### 4. Third-Party Services (Google AdMob)
The application integrates Google Mobile Ads SDK (AdMob) to display optional rewarded ads for unlocking advanced forensic reports.
AdMob may collect and process device identifiers (such as Google Advertising ID / GAID), coarse location from IP address, and interaction telemetry according to Google's Privacy Policy:
* https://policies.google.com/privacy
* https://support.google.com/admob/answer/6128543
No battery telemetry, usage statistics, or local diagnostic logs are ever shared with or transmitted to Google AdMob.

---

### 5. User Control & Data Retention
You retain complete, sovereign control over your data:
* **Export Data:** You may export your entire diagnostic log as human-readable CSV or JSON at any time.
* **Instant Erasure:** You can permanently purge all stored telemetry in 1 tap under *Settings → Privacy → Erase All Diagnostic Data*.
* **App Uninstallation:** Removing the application completely deletes all private app databases from your device storage.

---

### 6. Children's Privacy (COPPA)
Battery Forensics is a general-utility diagnostics tool that does not knowingly collect personal information from children under the age of 13.

---

### 7. Changes & Contact
We may periodically update this policy to reflect new Android OS capabilities. Any revisions will be reflected in-app with an updated Effective Date.
For inquiries, please contact: nextgentoolbd@gmail.com
        """.trimIndent()
    }

    fun copyPolicyToClipboard() {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        val clip = ClipData.newPlainText("Battery Forensics Privacy Policy", fullPolicyMarkdown)
        clipboard?.setPrimaryClip(clip)
        Toast.makeText(context, "Privacy Policy copied to clipboard!", Toast.LENGTH_SHORT).show()
    }

    fun sharePolicy() {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, fullPolicyMarkdown)
            putExtra(Intent.EXTRA_TITLE, "Battery Forensics Privacy Policy")
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share Privacy Policy")
        context.startActivity(shareIntent)
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag("privacy_policy_screen"),
        color = ForensicsPalette.ScreenBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
                .windowInsetsPadding(WindowInsets.navigationBars)
        ) {
            // Header Bar
            Surface(
                color = ForensicsPalette.CardSurface,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(ForensicsPalette.SubtleSurfaceAlt)
                                .clickable { onNavigateBack() }
                                .testTag("privacy_policy_back_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "←",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = ForensicsPalette.TextPrimary
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Privacy Policy",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = ForensicsPalette.TextPrimary
                            )
                            Text(
                                text = "Google Play Compliant · Version 1.0",
                                fontSize = 12.sp,
                                color = ForensicsPalette.TextSecondary
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { copyPolicyToClipboard() },
                            shape = RoundedCornerShape(999.dp),
                            modifier = Modifier.testTag("copy_privacy_policy_button")
                        ) {
                            Text(
                                text = "Copy",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = ForensicsPalette.BluePrimary
                            )
                        }
                        Button(
                            onClick = { sharePolicy() },
                            colors = ButtonDefaults.buttonColors(containerColor = ForensicsPalette.GreenPrimary),
                            shape = RoundedCornerShape(999.dp),
                            modifier = Modifier.testTag("share_privacy_policy_button")
                        ) {
                            Text(
                                text = "Share",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // Scrollable Content
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(6.dp))
                    // Summary Banner
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = ForensicsPalette.GreenSoftTile),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, ForensicsPalette.GreenBorder, RoundedCornerShape(16.dp))
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(ForensicsPalette.GreenPrimary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "✓",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Private by Architecture",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ForensicsPalette.GreenPrimary
                                    )
                                    Text(
                                        text = "Zero remote analytics. 100% on-device storage.",
                                        fontSize = 12.sp,
                                        color = ForensicsPalette.TextSecondary
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Battery Forensics is built on the principle of evidence, not tracking. Your hardware statistics, battery records, and usage logs are stored strictly inside your phone's sandboxed storage and are never uploaded or sold.",
                                fontSize = 13.sp,
                                lineHeight = 19.sp,
                                color = ForensicsPalette.TextPrimary
                            )
                        }
                    }
                }

                item {
                    // Google Play Data Safety Quick Checklist
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = ForensicsPalette.CardSurface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, ForensicsPalette.BorderSubtle, RoundedCornerShape(16.dp))
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                text = "GOOGLE PLAY DATA SAFETY SUMMARY",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = ForensicsPalette.TextSecondary
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            PolicyPointRow(
                                title = "Data Collection",
                                detail = "No personal, financial, biometric, or location data collected.",
                                isPositive = true
                            )
                            PolicyPointRow(
                                title = "Data Sharing",
                                detail = "No diagnostic or battery data shared with third parties or brokers.",
                                isPositive = true
                            )
                            PolicyPointRow(
                                title = "Data Retention & Erasure",
                                detail = "Full user control: Export or permanently purge your data in 1 tap.",
                                isPositive = true
                            )
                            PolicyPointRow(
                                title = "Security Practices",
                                detail = "Private app sandbox with no remote database endpoints.",
                                isPositive = true
                            )
                        }
                    }
                }

                // Expandable Policy Sections
                val policySections = listOf(
                    PolicySectionItem(
                        id = 0,
                        title = "1. Information We Process & Store",
                        summary = "Battery telemetry and charging history stored in a local SQLite database.",
                        body = "Battery Forensics measures raw system metrics including battery percentage, temperature (°C), live voltage (mV), charge current (mA), design capacity (mAh), and charging duration.\n\nAll diagnostic records are persisted exclusively in a private, sandboxed local Room database (ForensicsDatabase.db). The app does not collect personal identifiers, phone numbers, contact books, documents, photos, or keystrokes."
                    ),
                    PolicySectionItem(
                        id = 1,
                        title = "2. Android Permissions & Purpose Disclosures",
                        summary = "Detailed breakdown of why sensitive permissions are declared.",
                        body = "• PACKAGE_USAGE_STATS (Usage Access): Used strictly to attribute battery drop to active foreground applications. Never reads personal content or message threads.\n\n• ACCESS_NETWORK_STATE & ACCESS_WIFI_STATE: Monitors Wi-Fi and mobile radio states, as active radio search significantly impacts standby discharge.\n\n• BLUETOOTH_SCAN (neverForLocation) & BLUETOOTH_CONNECT: Detects connected Bluetooth accessories (e.g. headphones, smartwatches) without tracking physical location coordinates.\n\n• READ_PHONE_STATE: Observes cellular modem state (e.g. searching vs connected) to explain standby drain. Does not access call logs or phone numbers.\n\n• POST_NOTIFICATIONS: Used strictly for user-enabled hardware threshold alerts (such as the 38°C battery overheating guard)."
                    ),
                    PolicySectionItem(
                        id = 2,
                        title = "3. Third-Party Services (Google AdMob)",
                        summary = "How Google Mobile Ads handles ad delivery for optional rewarded unlocks.",
                        body = "The application integrates the Google Mobile Ads SDK (AdMob) to display optional rewarded ads for unlocking AI Doctor prompts and report exports.\n\nGoogle AdMob may collect and process device identifiers (such as the Google Advertising ID) and IP addresses to serve ads and prevent fraud in compliance with Google's Privacy Policy (https://policies.google.com/privacy). No battery diagnostics or usage patterns are ever transmitted to AdMob."
                    ),
                    PolicySectionItem(
                        id = 3,
                        title = "4. Data Erasure & User Rights (GDPR & CCPA)",
                        summary = "How you can export, inspect, or permanently delete your records.",
                        body = "Under GDPR, CCPA, and Google Play policies, you have absolute ownership of your data:\n\n• Export: Download all records as JSON or CSV from Settings → Privacy.\n\n• Instant Erasure: Tap 'Delete All Stored Data' under Settings → Privacy to immediately drop and reinitialize the local database.\n\n• Uninstallation: Deleting the app removes all database files from your device."
                    ),
                    PolicySectionItem(
                        id = 4,
                        title = "5. Children's Privacy (COPPA)",
                        summary = "Protection for users under the age of 13.",
                        body = "Battery Forensics is a technical diagnostic utility and does not knowingly solicit or collect personal information from children under 13. If you believe a child has provided us with personal information, please contact us for immediate remediation."
                    ),
                    PolicySectionItem(
                        id = 5,
                        title = "6. Developer Contact & Revisions",
                        summary = "How to reach the developer regarding privacy concerns.",
                        body = "If you have questions or feedback concerning this Privacy Policy, please contact:\n\nDeveloper: NextGen Tool\nEmail: nextgentoolbd@gmail.com\nPackage: com.nextgen.batteryforensics\nEffective Date: October 4, 2026"
                    )
                )

                items(policySections.size) { index ->
                    val sec = policySections[index]
                    val isExpanded = expandedSection == sec.id

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isExpanded) ForensicsPalette.BlueSoftTile else ForensicsPalette.CardSurface
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 1.dp,
                                color = if (isExpanded) ForensicsPalette.BlueBorder else ForensicsPalette.BorderSubtle,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable {
                                expandedSection = if (isExpanded) -1 else sec.id
                            }
                            .testTag("policy_section_${sec.id}")
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = sec.title,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ForensicsPalette.TextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = sec.summary,
                                        fontSize = 12.sp,
                                        color = ForensicsPalette.TextSecondary
                                    )
                                }
                                Text(
                                    text = if (isExpanded) "▲" else "▼",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ForensicsPalette.BluePrimary,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }

                            AnimatedVisibility(visible = isExpanded) {
                                Column {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    HorizontalDivider(color = ForensicsPalette.BorderSubtle)
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = sec.body,
                                        fontSize = 13.sp,
                                        lineHeight = 20.sp,
                                        color = ForensicsPalette.TextPrimary
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    // Bottom Action & Acknowledgment
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Button(
                            onClick = onNavigateBack,
                            colors = ButtonDefaults.buttonColors(containerColor = ForensicsPalette.BluePrimary),
                            shape = RoundedCornerShape(999.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("privacy_policy_done_button")
                        ) {
                            Text(
                                text = "Done & Return to App",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Package: com.nextgen.batteryforensics · All rights reserved",
                            fontSize = 11.sp,
                            color = ForensicsPalette.TextMuted
                        )
                    }
                }
            }
        }
    }
}

private data class PolicySectionItem(
    val id: Int,
    val title: String,
    val summary: String,
    val body: String
)

@Composable
private fun PolicyPointRow(
    title: String,
    detail: String,
    isPositive: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 2.dp)
                .size(18.dp)
                .clip(CircleShape)
                .background(if (isPositive) ForensicsPalette.GreenContainer else ForensicsPalette.AmberContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isPositive) "✓" else "!",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isPositive) ForensicsPalette.GreenPrimary else ForensicsPalette.AmberDarkText
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = ForensicsPalette.TextPrimary
            )
            Text(
                text = detail,
                fontSize = 12.sp,
                color = ForensicsPalette.TextSecondary,
                lineHeight = 16.sp
            )
        }
    }
}
