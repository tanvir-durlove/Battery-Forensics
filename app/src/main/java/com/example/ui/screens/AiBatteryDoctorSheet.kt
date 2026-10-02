package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ForensicsPalette

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AiBatteryDoctorSheet(
    selectedQuestion: String?,
    generatedPrompt: String?,
    onSelectQuestion: (String) -> Unit,
    onPromptCopied: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val presetQuestions = listOf(
        "Why did my battery drain last night?",
        "Which app is causing drain?",
        "Why is my phone getting warm?",
        "Why is charging slow?",
        "Is my battery healthy?",
        "How much did weak signal cost me?",
        "Did the latest update make battery worse?"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ForensicsPalette.ScreenBackground,
        modifier = Modifier.testTag("ai_doctor_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
                .padding(bottom = 28.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "AI Battery Doctor",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = ForensicsPalette.TextPrimary
                    )
                    Text(
                        text = "Select a question to generate a forensic prompt with your device's battery data",
                        fontSize = 12.sp,
                        color = ForensicsPalette.TextSecondary
                    )
                }
                TextButton(onClick = onDismiss) {
                    Text("Close", fontWeight = FontWeight.Bold, color = ForensicsPalette.PurplePrimary)
                }
            }

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                presetQuestions.forEachIndexed { idx, q ->
                    val isSelected = selectedQuestion == q
                    val chipShape = RoundedCornerShape(10.dp)
                    Box(
                        modifier = Modifier
                            .clip(chipShape)
                            .background(
                                if (isSelected) ForensicsPalette.PurpleContainer
                                else ForensicsPalette.CardSurface
                            )
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) ForensicsPalette.PurplePrimary else ForensicsPalette.BorderSubtle,
                                shape = chipShape
                            )
                            .clickable { onSelectQuestion(q) }
                            .padding(horizontal = 12.dp, vertical = 9.dp)
                            .testTag("ai_doctor_preset_$idx")
                    ) {
                        Text(
                            text = q,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                            color = if (isSelected) ForensicsPalette.PurplePrimary else ForensicsPalette.TextPrimary
                        )
                    }
                }
            }

            if (generatedPrompt != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(ForensicsPalette.CardSurface)
                        .border(1.dp, ForensicsPalette.PurpleBorder, RoundedCornerShape(14.dp))
                        .padding(14.dp)
                        .testTag("unlocked_gemini_prompt_box"),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                            clipboard?.setPrimaryClip(
                                ClipData.newPlainText("Battery Forensics Prompt", generatedPrompt)
                            )
                            onPromptCopied()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ForensicsPalette.PurpleDeepButton),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("copy_gemini_prompt_button")
                    ) {
                        Text(
                            text = "Copy Prompt",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(ForensicsPalette.SubtleSurface)
                            .border(1.dp, ForensicsPalette.BorderSubtle, RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = generatedPrompt,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 16.sp,
                            color = ForensicsPalette.TextPrimary
                        )
                    }
                }
            }
        }
    }
}
