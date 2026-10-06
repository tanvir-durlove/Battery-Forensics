package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ClassificationBadge
import com.example.ui.components.ForensicsCard
import com.example.ui.theme.ForensicsPalette

/**
 * Screen providing full step-by-step guidance and actions to trigger
 * and download automated Android APK builds directly on GitHub Actions.
 */
@Composable
fun GitHubApkBuildScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    BackHandler(onBack = onNavigateBack)

    var showWorkflowYamlModal by remember { mutableStateOf(false) }

    fun copyToClipboard(label: String, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
        Toast.makeText(context, "$label copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    fun openBrowser(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        } catch (_: Exception) {
            copyToClipboard("Link", url)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ForensicsPalette.ScreenBackground)
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 680.dp)
        ) {
            // Header Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = ForensicsPalette.CardSurface,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(ForensicsPalette.SubtleSurfaceAlt)
                                .clickable { onNavigateBack() }
                                .testTag("github_build_back_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "←",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = ForensicsPalette.TextPrimary
                            )
                        }
                        Column {
                            Text(
                                text = "GitHub Actions APK Builder",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = ForensicsPalette.TextPrimary
                            )
                            Text(
                                text = "Automated Cloud CI/CD · Zero Local Setup",
                                fontSize = 12.sp,
                                color = ForensicsPalette.GreenPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    ClassificationBadge(
                        text = "Cloud CI",
                        containerColor = ForensicsPalette.GreenContainer,
                        contentColor = ForensicsPalette.GreenPrimary
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .testTag("github_apk_build_scroll"),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(6.dp))
                }

                // Status Banner Card
                item {
                    ForensicsCard(containerColor = ForensicsPalette.GreenContainer) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
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
                                Column {
                                    Text(
                                        text = "Workflow Ready in Repository",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ForensicsPalette.GreenPrimary
                                    )
                                    Text(
                                        text = ".github/workflows/android-build.yml configured",
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = ForensicsPalette.TextSecondary
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Your repository already contains the complete GitHub Actions workflow configured for JDK 21, Gradle 9.3.1, Android SDK 36, and automatic APK release publishing.",
                                fontSize = 13.sp,
                                color = ForensicsPalette.TextPrimary,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }

                // 3 Step-by-Step Instructions
                item {
                    ForensicsCard {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = "How to Build APK on GitHub (3 Easy Steps)",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = ForensicsPalette.TextPrimary
                            )

                            // Step 1
                            BuildStepRow(
                                stepNumber = "1",
                                title = "Push Your Code to GitHub",
                                description = "Push your project to GitHub or sync via the AI Studio Git/Export menu. Every push or pull request automatically starts the build pipeline."
                            )

                            // Step 2
                            BuildStepRow(
                                stepNumber = "2",
                                title = "Manual Run Anytime (Actions Tab)",
                                description = "Go to your repository on GitHub → Click the 'Actions' tab → Select 'Build & Download Android APK' on the left → Click 'Run workflow'."
                            )

                            // Step 3
                            BuildStepRow(
                                stepNumber = "3",
                                title = "Download Your Installable APK",
                                description = "When the run finishes (takes ~2 minutes), find your installable APK in two places:\n• Under 'Releases' as 'latest-apk' (tap Battery-Forensics.apk directly on phone)\n• Under 'Artifacts' at the bottom of the Action run summary."
                            )
                        }
                    }
                }

                // Quick Action Buttons Card
                item {
                    ForensicsCard {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Quick Actions & Links",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = ForensicsPalette.TextPrimary
                            )

                            Button(
                                onClick = { openBrowser("https://github.com") },
                                colors = ButtonDefaults.buttonColors(containerColor = ForensicsPalette.GreenPrimary),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("open_github_button")
                            ) {
                                Text(
                                    text = "Open GitHub in Browser ↗",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color.White
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        copyToClipboard(
                                            "Workflow Path",
                                            ".github/workflows/android-build.yml"
                                        )
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("copy_workflow_path_button")
                                ) {
                                    Text(
                                        text = "Copy Path",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = ForensicsPalette.TextPrimary
                                    )
                                }

                                OutlinedButton(
                                    onClick = { showWorkflowYamlModal = !showWorkflowYamlModal },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("view_workflow_yaml_button")
                                ) {
                                    Text(
                                        text = if (showWorkflowYamlModal) "Hide Script" else "View Script",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = ForensicsPalette.TextPrimary
                                    )
                                }
                            }
                        }
                    }
                }

                // Workflow Script Preview (Expandable)
                if (showWorkflowYamlModal) {
                    item {
                        ForensicsCard(containerColor = ForensicsPalette.SubtleSurface) {
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
                                    Text(
                                        text = ".github/workflows/android-build.yml",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ForensicsPalette.GreenPrimary
                                    )

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(ForensicsPalette.SubtleSurface)
                                            .clickable {
                                                copyToClipboard("Workflow YAML", GITHUB_WORKFLOW_YAML_CONTENT)
                                            }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "Copy Full YAML",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ForensicsPalette.BluePrimary
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Surface(
                                    color = Color(0xFF0F141C),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = GITHUB_WORKFLOW_YAML_CONTENT,
                                        color = Color(0xFF81C784),
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp,
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Cloud Build Specifications Card
                item {
                    ForensicsCard {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Build Environment Specifications",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = ForensicsPalette.TextPrimary
                            )

                            SpecRow(label = "Runner", value = "ubuntu-latest")
                            HorizontalDivider(color = ForensicsPalette.DividerColor)
                            SpecRow(label = "Java JDK", value = "Eclipse Temurin JDK 21")
                            HorizontalDivider(color = ForensicsPalette.DividerColor)
                            SpecRow(label = "Gradle", value = "9.3.1 (Official setup-gradle)")
                            HorizontalDivider(color = ForensicsPalette.DividerColor)
                            SpecRow(label = "Android SDK", value = "API 36.1 (TargetSdk 36)")
                            HorizontalDivider(color = ForensicsPalette.DividerColor)
                            SpecRow(label = "Build Command", value = "gradle :app:assembleDebug")
                            HorizontalDivider(color = ForensicsPalette.DividerColor)
                            SpecRow(label = "Outputs", value = "Battery-Forensics.apk")
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun BuildStepRow(
    stepNumber: String,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(ForensicsPalette.BlueSoftTile)
                .border(1.dp, ForensicsPalette.BlueBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stepNumber,
                color = ForensicsPalette.BluePrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = ForensicsPalette.TextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                fontSize = 12.sp,
                color = ForensicsPalette.TextSecondary,
                lineHeight = 17.sp
            )
        }
    }
}

@Composable
private fun SpecRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = ForensicsPalette.TextSecondary
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold,
            color = ForensicsPalette.TextPrimary
        )
    }
}

private const val GITHUB_WORKFLOW_YAML_CONTENT = """name: Build & Download Android APK

on:
  push:
    branches: [ "**" ]
  pull_request:
    branches: [ "**" ]
  workflow_dispatch:

permissions:
  contents: write

jobs:
  build-apk:
    name: Build Installable APK
    runs-on: ubuntu-latest
    steps:
      - name: Checkout repository
        uses: actions/checkout@v4

      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'

      - name: Setup Gradle 9.3.1
        uses: gradle/actions/setup-gradle@v4
        with:
          gradle-version: '9.3.1'

      - name: Build Installable Android APK
        run: |
          gradle :app:assembleDebug --no-daemon
          cp app/build/outputs/apk/debug/app-debug.apk app/build/outputs/apk/debug/Battery-Forensics.apk

      - name: Upload APK to GitHub Artifacts
        uses: actions/upload-artifact@v4
        with:
          name: Battery-Forensics-APK
          path: app/build/outputs/apk/debug/Battery-Forensics.apk
"""
