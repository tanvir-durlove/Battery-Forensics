package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CapabilityStatus
import com.example.data.LiveTelemetrySnapshot
import com.example.data.ManufacturerProfileInfo
import com.example.ui.AlertSensitivity
import com.example.ui.SettingsSection
import com.example.ui.components.CapabilityStatusGraphicBadge
import com.example.ui.components.CardInfoIconButton
import com.example.ui.components.ClassificationBadge
import com.example.ui.components.FaqAccordionList
import com.example.ui.components.ForensicsCard
import com.example.ui.components.ForensicsGuideCatalog
import com.example.ui.components.SettingsCategoryGraphicIcon
import com.example.ui.components.StatGuideTopic
import com.example.ui.theme.ForensicsPalette

@Composable
fun SettingsScreen(
    section: SettingsSection,
    onSelectSection: (SettingsSection) -> Unit,
    snapshot: LiveTelemetrySnapshot,
    manufacturerProfile: ManufacturerProfileInfo,
    adbModeEnabled: Boolean,
    onToggleAdbMode: (Boolean) -> Unit,
    localStorageOnly: Boolean,
    onToggleLocalStorage: (Boolean) -> Unit,
    alertDrainHigher: Boolean,
    alertTempHigh: Boolean,
    alertChargingSlow: Boolean,
    alertUnusualBg: Boolean,
    alertCapacityChange: Boolean,
    onToggleAlert: (String, Boolean) -> Unit,
    alertSensitivity: AlertSensitivity,
    onSelectSensitivity: (AlertSensitivity) -> Unit,
    onExportReport: (String) -> Unit,
    onUnlockExport: () -> Unit = {},
    onDeleteAllData: (Boolean) -> Unit,
    onRunConsoleCommand: (String) -> String,
    onPermissionsUpdated: () -> Unit,
    isExportUnlocked: Boolean = false,
    onOpenOnboardingTour: () -> Unit = {},
    onOpenPrivacyPolicy: () -> Unit = {},
    onShowTopicGuide: (StatGuideTopic) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showDeleteConfirmModal by remember { mutableStateOf(false) }
    var activeDeveloperTool by remember { mutableStateOf<String?>(null) }
    var expandedFaqIndex by remember { mutableIntStateOf(0) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        onPermissionsUpdated()
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ForensicsPalette.ScreenBackground)
            .padding(horizontal = 16.dp)
            .testTag("settings_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Settings",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = ForensicsPalette.TextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Permissions, privacy, alerts, advanced, and FAQ guide.",
                fontSize = 13.sp,
                color = ForensicsPalette.TextSecondary
            )
        }

        // Sub-Navigation Grid (Permissions, Privacy, Alerts, Advanced + Full-Width FAQ & Battery Guide)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SettingsNavTile(
                        categoryKey = "permissions",
                        label = "Permissions",
                        isSelected = section == SettingsSection.PERMISSIONS,
                        activeBg = ForensicsPalette.BlueContainer,
                        activeBorder = ForensicsPalette.BlueBorder,
                        activeTextColor = ForensicsPalette.BluePrimary,
                        iconAccent = ForensicsPalette.BluePrimary,
                        iconBg = ForensicsPalette.BlueSoftTile,
                        onClick = { onSelectSection(SettingsSection.PERMISSIONS) },
                        modifier = Modifier.weight(1f)
                    )
                    SettingsNavTile(
                        categoryKey = "privacy",
                        label = "Privacy",
                        isSelected = section == SettingsSection.PRIVACY,
                        activeBg = ForensicsPalette.PurpleContainer,
                        activeBorder = ForensicsPalette.PurpleBorder,
                        activeTextColor = ForensicsPalette.PurplePrimary,
                        iconAccent = ForensicsPalette.PurplePrimary,
                        iconBg = ForensicsPalette.PurpleSoftTile,
                        onClick = {
                            onSelectSection(SettingsSection.PRIVACY)
                            onOpenPrivacyPolicy()
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SettingsNavTile(
                        categoryKey = "alerts",
                        label = "Alerts",
                        isSelected = section == SettingsSection.ALERTS,
                        activeBg = ForensicsPalette.AmberContainer,
                        activeBorder = ForensicsPalette.AmberBorder,
                        activeTextColor = ForensicsPalette.AmberPrimary,
                        iconAccent = ForensicsPalette.AmberPrimary,
                        iconBg = ForensicsPalette.AmberSoftTile,
                        onClick = { onSelectSection(SettingsSection.ALERTS) },
                        modifier = Modifier.weight(1f)
                    )
                    SettingsNavTile(
                        categoryKey = "advanced",
                        label = "Advanced",
                        isSelected = section == SettingsSection.ADVANCED,
                        activeBg = ForensicsPalette.GreenContainer,
                        activeBorder = ForensicsPalette.GreenBorder,
                        activeTextColor = ForensicsPalette.GreenPrimary,
                        iconAccent = ForensicsPalette.GreenPrimary,
                        iconBg = ForensicsPalette.GreenSoftTile,
                        onClick = { onSelectSection(SettingsSection.ADVANCED) },
                        modifier = Modifier.weight(1f)
                    )
                }
                SettingsNavTile(
                    categoryKey = "faq",
                    label = "FAQ & Battery Guide",
                    isSelected = section == SettingsSection.FAQ,
                    activeBg = ForensicsPalette.BlueContainer,
                    activeBorder = ForensicsPalette.BlueBorder,
                    activeTextColor = ForensicsPalette.BluePrimary,
                    iconAccent = ForensicsPalette.BluePrimary,
                    iconBg = ForensicsPalette.BlueSoftTile,
                    onClick = { onSelectSection(SettingsSection.FAQ) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        when (section) {
            SettingsSection.PERMISSIONS -> {
                // Required Card (Screenshot 8)
                item {
                    ForensicsCard {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    ClassificationBadge(
                                        text = "Required",
                                        containerColor = ForensicsPalette.BlueContainer,
                                        contentColor = ForensicsPalette.BluePrimary
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Core functionality",
                                        fontSize = 12.sp,
                                        color = ForensicsPalette.TextSecondary
                                    )
                                }
                                CardInfoIconButton(
                                    topicKey = "settings_permissions",
                                    onShowTopic = onShowTopicGuide
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            PermissionItemRow(
                                title = "Battery Information",
                                description = "Level, temperature, voltage, current, charging state",
                                isGranted = true,
                                deniedConsequence = null,
                                onGrantClick = {}
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = ForensicsPalette.DividerColor)
                            Spacer(modifier = Modifier.height(12.dp))
                            PermissionItemRow(
                                title = "Usage Access",
                                description = "App usage duration and foreground/background events",
                                isGranted = snapshot.usageAccessGranted,
                                deniedConsequence = "Per-app activity & background event counts unavailable",
                                onGrantClick = {
                                    try {
                                        context.startActivity(
                                            Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            }
                                        )
                                    } catch (_: Exception) {
                                    }
                                }
                            )
                        }
                    }
                }

                // Optional Card (Screenshot 8)
                item {
                    ForensicsCard {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                ClassificationBadge(
                                    text = "Optional",
                                    containerColor = ForensicsPalette.PurpleContainer,
                                    contentColor = ForensicsPalette.PurplePrimary
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Improve diagnostics",
                                    fontSize = 12.sp,
                                    color = ForensicsPalette.TextSecondary
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            PermissionItemRow(
                                title = "Network State",
                                description = "Wi-Fi and cellular connection state",
                                isGranted = true,
                                deniedConsequence = null,
                                onGrantClick = {}
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = ForensicsPalette.DividerColor)
                            Spacer(modifier = Modifier.height(12.dp))
                            PermissionItemRow(
                                title = "Wi-Fi State",
                                description = "Wi-Fi connection details",
                                isGranted = true,
                                deniedConsequence = null,
                                onGrantClick = {}
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = ForensicsPalette.DividerColor)
                            Spacer(modifier = Modifier.height(12.dp))
                            PermissionItemRow(
                                title = "Fine Location",
                                description = "Cell tower and GPS signal strength",
                                isGranted = snapshot.fineLocationGranted,
                                deniedConsequence = "Signal strength data unavailable",
                                onGrantClick = {
                                    permissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                }
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = ForensicsPalette.DividerColor)
                            Spacer(modifier = Modifier.height(12.dp))
                            PermissionItemRow(
                                title = "Phone State",
                                description = "Cellular network type (5G / LTE)",
                                isGranted = snapshot.phoneStateGranted,
                                deniedConsequence = "5G vs LTE network generation unavailable",
                                onGrantClick = {
                                    permissionLauncher.launch(arrayOf(Manifest.permission.READ_PHONE_STATE))
                                }
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = ForensicsPalette.DividerColor)
                            Spacer(modifier = Modifier.height(12.dp))
                            PermissionItemRow(
                                title = "Bluetooth Scan",
                                description = "Bluetooth device count and activity",
                                isGranted = snapshot.bluetoothScanGranted && snapshot.fineLocationGranted,
                                deniedConsequence = "Bluetooth analysis unavailable",
                                onGrantClick = {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                        permissionLauncher.launch(
                                            arrayOf(
                                                Manifest.permission.BLUETOOTH_SCAN,
                                                Manifest.permission.BLUETOOTH_CONNECT
                                            )
                                        )
                                    } else {
                                        onPermissionsUpdated()
                                    }
                                }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            SettingsSection.PRIVACY -> {
                // Privacy Guarantees Card (Screenshot 9)
                item {
                    ForensicsCard {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Local storage only",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ForensicsPalette.TextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "All data stored on-device. No cloud uploads.",
                                        fontSize = 12.sp,
                                        color = ForensicsPalette.TextSecondary
                                    )
                                }
                                Switch(
                                    checked = localStorageOnly,
                                    onCheckedChange = onToggleLocalStorage,
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = ForensicsPalette.GreenPrimary
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = ForensicsPalette.DividerColor)
                            Spacer(modifier = Modifier.height(14.dp))
                            PrivacyAlwaysOnRow(
                                title = "No account required",
                                subtitle = "This app never requires sign-in."
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            HorizontalDivider(color = ForensicsPalette.DividerColor)
                            Spacer(modifier = Modifier.height(14.dp))
                            PrivacyAlwaysOnRow(
                                title = "No diagnostic uploads",
                                subtitle = "Data is never sent without explicit consent."
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            HorizontalDivider(color = ForensicsPalette.DividerColor)
                            Spacer(modifier = Modifier.height(14.dp))
                            PrivacyAlwaysOnRow(
                                title = "No hidden tracking",
                                subtitle = "App never tracks your personal activity."
                            )
                        }
                    }
                }

                // DATA MANAGEMENT Card (Screenshot 9)
                item {
                    ForensicsCard {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp)
                        ) {
                            Text(
                                text = "DATA MANAGEMENT",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp,
                                color = ForensicsPalette.TextSecondary
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            val btnShape = RoundedCornerShape(14.dp)
                            if (!isExportUnlocked) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(btnShape)
                                        .background(ForensicsPalette.SubtleSurface)
                                        .border(1.dp, ForensicsPalette.BorderStrong, btnShape)
                                        .clickable { onUnlockExport() }
                                        .padding(horizontal = 16.dp, vertical = 16.dp)
                                        .testTag("export_all_data_button"),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Export all data",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ForensicsPalette.TextPrimary
                                        )
                                        Text(
                                            text = "JSON · CSV · PDF",
                                            fontSize = 12.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Medium,
                                            color = ForensicsPalette.TextSecondary
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(50))
                                            .background(ForensicsPalette.GreenPrimary)
                                            .padding(horizontal = 16.dp, vertical = 8.dp)
                                    ) {
                                        Text(
                                            text = "Unlock",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            } else {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(btnShape)
                                        .background(ForensicsPalette.SubtleSurface)
                                        .border(1.dp, ForensicsPalette.BorderStrong, btnShape)
                                        .padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Export all data",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ForensicsPalette.TextPrimary
                                        )
                                        ClassificationBadge(
                                            text = "Unlocked",
                                            containerColor = ForensicsPalette.GreenContainer,
                                            contentColor = ForensicsPalette.GreenPrimary
                                        )
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = { onExportReport("PDF") },
                                            colors = ButtonDefaults.buttonColors(containerColor = ForensicsPalette.GreenPrimary),
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("export_pdf_button")
                                        ) {
                                            Text("PDF", fontWeight = FontWeight.Bold)
                                        }
                                        Button(
                                            onClick = { onExportReport("CSV") },
                                            colors = ButtonDefaults.buttonColors(containerColor = ForensicsPalette.BluePrimary),
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("export_csv_button")
                                        ) {
                                            Text("CSV", fontWeight = FontWeight.Bold)
                                        }
                                        Button(
                                            onClick = { onExportReport("JSON") },
                                            colors = ButtonDefaults.buttonColors(containerColor = ForensicsPalette.PurplePrimary),
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("export_json_button")
                                        ) {
                                            Text("JSON", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(btnShape)
                                    .background(ForensicsPalette.RedContainer)
                                    .border(1.dp, ForensicsPalette.RedBorder, btnShape)
                                    .clickable { showDeleteConfirmModal = true }
                                    .padding(horizontal = 16.dp, vertical = 16.dp)
                                    .testTag("delete_all_data_button"),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Delete all data",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ForensicsPalette.RedPrimary
                                )
                                Text(
                                    text = "Irreversible",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = ForensicsPalette.RedPrimary
                                )
                            }
                        }
                    }
                }

                // "This app never collects" Card (Screenshot 9)
                item {
                    ForensicsCard(containerColor = ForensicsPalette.SubtleSurface) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp)
                        ) {
                            Text(
                                text = "This app never collects",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = ForensicsPalette.TextPrimary
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            listOf(
                                "Messages or call content",
                                "Photos or files",
                                "Contacts",
                                "Passwords",
                                "Personal communications"
                            ).forEach { itemText ->
                                Row(
                                    modifier = Modifier.padding(vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CapabilityStatusGraphicBadge(status = CapabilityStatus.SUPPORTED)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = itemText,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = ForensicsPalette.TextSecondary
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Official Privacy Policy Card (Google Play Compliance)
                item {
                    ForensicsCard(containerColor = ForensicsPalette.CardSurface) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Google Play Privacy Policy",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ForensicsPalette.TextPrimary
                                )
                                ClassificationBadge(
                                    text = "Official",
                                    containerColor = ForensicsPalette.BlueContainer,
                                    contentColor = ForensicsPalette.BluePrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Review our comprehensive, Play Store-compliant privacy disclosures covering permissions, local SQLite data storage, and Google AdMob.",
                                fontSize = 12.sp,
                                color = ForensicsPalette.TextSecondary,
                                lineHeight = 17.sp
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = onOpenPrivacyPolicy,
                                colors = ButtonDefaults.buttonColors(containerColor = ForensicsPalette.BluePrimary),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("open_privacy_policy_button")
                            ) {
                                Text(
                                    text = "View Full Privacy Policy →",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            SettingsSection.ALERTS -> {
                // 5 Alert Toggles Card (Screenshot 10)
                item {
                    ForensicsCard {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp)
                        ) {
                            AlertToggleRow(
                                label = "Drain higher than normal",
                                checked = alertDrainHigher,
                                onCheckedChange = { onToggleAlert("drain", it) }
                            )
                            HorizontalDivider(color = ForensicsPalette.DividerColor)
                            AlertToggleRow(
                                label = "Temperature unusually high",
                                checked = alertTempHigh,
                                onCheckedChange = { onToggleAlert("temp", it) }
                            )
                            HorizontalDivider(color = ForensicsPalette.DividerColor)
                            AlertToggleRow(
                                label = "Charging speed lower than normal",
                                checked = alertChargingSlow,
                                onCheckedChange = { onToggleAlert("charging", it) }
                            )
                            HorizontalDivider(color = ForensicsPalette.DividerColor)
                            AlertToggleRow(
                                label = "Unusual background activity",
                                checked = alertUnusualBg,
                                onCheckedChange = { onToggleAlert("bg", it) }
                            )
                            HorizontalDivider(color = ForensicsPalette.DividerColor)
                            AlertToggleRow(
                                label = "Estimated capacity change",
                                checked = alertCapacityChange,
                                onCheckedChange = { onToggleAlert("capacity", it) }
                            )
                        }
                    }
                }

                // SENSITIVITY Card (Screenshot 10)
                item {
                    ForensicsCard {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp)
                        ) {
                            Text(
                                text = "SENSITIVITY",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp,
                                color = ForensicsPalette.TextSecondary
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                val sensShape = RoundedCornerShape(12.dp)
                                AlertSensitivity.entries.forEach { sens ->
                                    val selected = sens == alertSensitivity
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(44.dp)
                                            .clip(sensShape)
                                            .background(
                                                if (selected) ForensicsPalette.AmberContainer
                                                else ForensicsPalette.SubtleSurface
                                            )
                                            .border(
                                                width = 1.dp,
                                                color = if (selected) ForensicsPalette.AmberBorder else ForensicsPalette.BorderStrong,
                                                shape = sensShape
                                            )
                                            .clickable { onSelectSensitivity(sens) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = sens.label,
                                            fontSize = 14.sp,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                                            color = if (selected) ForensicsPalette.AmberDarkText else ForensicsPalette.TextSecondary
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "Alerts only notify you when real battery changes are detected.",
                                fontSize = 12.sp,
                                color = ForensicsPalette.TextSecondary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            SettingsSection.ADVANCED -> {
                // ADB Advanced Mode Card (Screenshot 11)
                item {
                    ForensicsCard {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "ADB Advanced Mode",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ForensicsPalette.TextPrimary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    ClassificationBadge(
                                        text = "Optional",
                                        containerColor = ForensicsPalette.SubtleSurfaceAlt,
                                        contentColor = ForensicsPalette.TextSecondary
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Unlocks deep background wakeup checks and system power history using a PC connection (USB debugging).",
                                    fontSize = 13.sp,
                                    color = ForensicsPalette.TextSecondary
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Switch(
                                checked = adbModeEnabled,
                                onCheckedChange = onToggleAdbMode,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = ForensicsPalette.GreenPrimary
                                ),
                                modifier = Modifier.testTag("adb_mode_switch")
                            )
                        }
                    }
                }

                // DEVELOPER DIAGNOSTICS Card (Screenshot 11)
                item {
                    ForensicsCard {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp)
                        ) {
                            Text(
                                text = "DEVELOPER DIAGNOSTICS",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp,
                                color = ForensicsPalette.TextSecondary
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            val devItems = listOf(
                                "Raw battery statistics",
                                "Battery history log",
                                "Event log viewer",
                                "Diagnostic console",
                                "Raw data export"
                            )
                            devItems.forEachIndexed { index, itemTitle ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            if (itemTitle == "Raw data export") {
                                                if (isExportUnlocked) {
                                                    onExportReport("JSON")
                                                } else {
                                                    onUnlockExport()
                                                }
                                            } else {
                                                activeDeveloperTool = itemTitle
                                            }
                                        }
                                        .padding(vertical = 13.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = itemTitle,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = ForensicsPalette.TextPrimary
                                    )
                                    Text(
                                        text = "›",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ForensicsPalette.TextSecondary
                                    )
                                }
                                if (index < devItems.lastIndex) {
                                    HorizontalDivider(color = ForensicsPalette.DividerColor)
                                }
                            }
                        }
                    }
                }

                // This App's Battery Impact Card (Screenshot 11)
                item {
                    ForensicsCard(containerColor = ForensicsPalette.GreenContainer) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "This App's Battery Impact",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ForensicsPalette.TextPrimary
                                )
                                Text(
                                    text = "Low",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ForensicsPalette.GreenPrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .border(1.dp, ForensicsPalette.GreenBorder, CircleShape)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.08f)
                                        .height(8.dp)
                                        .clip(CircleShape)
                                        .background(ForensicsPalette.GreenPrimary)
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "~0.8% estimated daily · Smart low-power tracking · Doesn't drain your battery",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = ForensicsPalette.GreenPrimary
                            )
                        }
                    }
                }

                // Manufacturer Adaptive Profile Card
                item {
                    ForensicsCard {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "PHONE BRAND TIPS: ${manufacturerProfile.manufacturer.uppercase()}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp,
                                    color = ForensicsPalette.TextSecondary,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 8.dp)
                                )
                                ClassificationBadge(
                                    text = manufacturerProfile.osSkinLabel,
                                    containerColor = ForensicsPalette.BlueSoftTile,
                                    contentColor = ForensicsPalette.BluePrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = manufacturerProfile.batterySubsystemNotes,
                                fontSize = 13.sp,
                                color = ForensicsPalette.TextPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            manufacturerProfile.oemSettingsGuidance.forEach { tip ->
                                Text(
                                    text = "• $tip",
                                    fontSize = 12.sp,
                                    color = ForensicsPalette.TextSecondary,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                // Footer App Identity Card (Screenshot 11)
                item {
                    ForensicsCard {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(22.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Battery Forensics",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = ForensicsPalette.TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "v1.0.0 · Android 12–16",
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                color = ForensicsPalette.TextSecondary
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "An honest battery health & drain tracker. No fake boosters or cleaners — just real measurements from your phone.",
                                fontSize = 13.sp,
                                color = ForensicsPalette.TextSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            SettingsSection.FAQ -> {
                // 1. Interactive App Tour Card
                item {
                    ForensicsCard(containerColor = ForensicsPalette.BlueSoftTile) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Diagnostic Onboarding & Setup",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ForensicsPalette.TextPrimary,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 8.dp)
                                )
                                ClassificationBadge(
                                    text = "3-Step Setup",
                                    containerColor = ForensicsPalette.BlueContainer,
                                    contentColor = ForensicsPalette.BluePrimary
                                )
                            }
                            Text(
                                text = "Review how Battery Forensics verifies device signals, labels confidence levels, and lets you choose your starting diagnostic level.",
                                fontSize = 13.sp,
                                color = ForensicsPalette.TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Button(
                                onClick = onOpenOnboardingTour,
                                colors = ButtonDefaults.buttonColors(containerColor = ForensicsPalette.BluePrimary),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("open_onboarding_tour_button")
                            ) {
                                Text(
                                    text = "Open Onboarding Setup →",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                // 2. What Every Statistic Means (Interactive Dictionary)
                item {
                    ForensicsCard {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "WHAT EVERY STATISTIC MEANS",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp,
                                    color = ForensicsPalette.TextSecondary,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 8.dp)
                                )
                                ClassificationBadge(
                                    text = "Tap to Explain",
                                    containerColor = ForensicsPalette.GreenContainer,
                                    contentColor = ForensicsPalette.GreenPrimary
                                )
                            }
                            Text(
                                text = "Tap any topic below (or the 'What's this?' button on any screen) to see plain-English meanings, healthy numbers, and warning thresholds:",
                                fontSize = 12.sp,
                                color = ForensicsPalette.TextSecondary
                            )

                            ForensicsGuideCatalog.topics.values.forEachIndexed { idx, topic ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(ForensicsPalette.SubtleSurface)
                                        .border(1.dp, ForensicsPalette.BorderSubtle, RoundedCornerShape(12.dp))
                                        .clickable { onShowTopicGuide(topic) }
                                        .padding(horizontal = 14.dp, vertical = 12.dp)
                                        .testTag("faq_stat_topic_${topic.key}"),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(end = 8.dp)
                                    ) {
                                        Text(
                                            text = topic.title,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ForensicsPalette.TextPrimary
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "${topic.category} · Healthy: ${topic.healthyRange}",
                                            fontSize = 11.sp,
                                            color = ForensicsPalette.TextSecondary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Text(
                                        text = "Explain →",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ForensicsPalette.BluePrimary
                                    )
                                }
                            }
                        }
                    }
                }

                // 3. Frequently Asked Questions Accordion
                item {
                    Text(
                        text = "FREQUENTLY ASKED QUESTIONS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                        color = ForensicsPalette.TextSecondary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                item {
                    FaqAccordionList(
                        expandedIndex = expandedFaqIndex,
                        onSelectIndex = { idx ->
                            expandedFaqIndex = if (expandedFaqIndex == idx) -1 else idx
                        }
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }

    if (showDeleteConfirmModal) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmModal = false },
            title = { Text("Delete All Local Data?", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "This permanently removes all recorded diagnostic sessions, timeline annotations, charger profiles, and experiments from the local database.",
                        fontSize = 13.sp,
                        color = ForensicsPalette.TextSecondary
                    )
                    TextButton(
                        onClick = {
                            showDeleteConfirmModal = false
                            onExportReport("JSON")
                        }
                    ) {
                        Text("Export backup before deletion →")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmModal = false
                        onDeleteAllData(false)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ForensicsPalette.RedPrimary)
                ) {
                    Text("Delete All Data")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmModal = false
                        onDeleteAllData(true)
                    }
                ) {
                    Text("Reset to Demo Baseline")
                }
            }
        )
    }

    activeDeveloperTool?.let { toolName ->
        var consoleCmd by remember { mutableStateOf("dumpsys batterystats") }
        val output = remember(toolName, consoleCmd, adbModeEnabled) {
            when (toolName) {
                "Raw battery statistics" -> onRunConsoleCommand("batterystats")
                "Battery history log" -> onRunConsoleCommand("deviceidle")
                "Event log viewer" -> onRunConsoleCommand("thermal")
                else -> onRunConsoleCommand(consoleCmd)
            }
        }

        AlertDialog(
            onDismissRequest = { activeDeveloperTool = null },
            title = { Text(toolName, fontWeight = FontWeight.Bold, fontSize = 17.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (toolName == "Diagnostic console") {
                        OutlinedTextField(
                            value = consoleCmd,
                            onValueChange = { consoleCmd = it },
                            label = { Text("Command (batterystats / deviceidle / thermal)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(ForensicsPalette.TextPrimary)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = output,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = ForensicsPalette.GreenContainer
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { activeDeveloperTool = null }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
private fun SettingsNavTile(
    categoryKey: String,
    label: String,
    isSelected: Boolean,
    activeBg: Color,
    activeBorder: Color,
    activeTextColor: Color,
    iconAccent: Color,
    iconBg: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(16.dp)
    Surface(
        modifier = modifier
            .height(60.dp)
            .clip(shape)
            .clickable { onClick() }
            .testTag("settings_nav_${label.lowercase()}"),
        shape = shape,
        color = if (isSelected) activeBg else ForensicsPalette.CardSurface,
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) activeBorder else ForensicsPalette.BorderSubtle
        ),
        shadowElevation = if (isSelected) 2.dp else 1.5.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SettingsCategoryGraphicIcon(
                category = categoryKey,
                accentColor = iconAccent,
                containerColor = if (isSelected) ForensicsPalette.CardSurface else iconBg
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = label,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) activeTextColor else ForensicsPalette.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun PermissionItemRow(
    title: String,
    description: String,
    isGranted: Boolean,
    deniedConsequence: String?,
    onGrantClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = ForensicsPalette.TextPrimary
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = description,
                fontSize = 13.sp,
                color = ForensicsPalette.TextSecondary
            )
            if (!isGranted && deniedConsequence != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = deniedConsequence,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = ForensicsPalette.RedPrimary
                )
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(horizontalAlignment = Alignment.End) {
            if (isGranted) {
                ClassificationBadge(
                    text = "Granted",
                    containerColor = ForensicsPalette.GreenContainer,
                    contentColor = ForensicsPalette.GreenPrimary
                )
            } else {
                ClassificationBadge(
                    text = "Denied",
                    containerColor = ForensicsPalette.SubtleSurfaceAlt,
                    contentColor = ForensicsPalette.TextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Grant →",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = ForensicsPalette.BluePrimary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { onGrantClick() }
                        .padding(vertical = 2.dp, horizontal = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun PrivacyAlwaysOnRow(
    title: String,
    subtitle: String
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
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = ForensicsPalette.TextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = ForensicsPalette.TextSecondary
            )
        }
        ClassificationBadge(
            text = "Always on",
            containerColor = ForensicsPalette.GreenContainer,
            contentColor = ForensicsPalette.GreenPrimary
        )
    }
}

@Composable
private fun AlertToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 15.sp,
            fontWeight = if (checked) FontWeight.Bold else FontWeight.Medium,
            color = if (checked) ForensicsPalette.TextPrimary else ForensicsPalette.TextSecondary,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = ForensicsPalette.GreenPrimary,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = ForensicsPalette.SubtleSurfaceAlt
            )
        )
    }
}
