package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AiDoctorExchange
import com.example.ui.components.ClassificationBadge
import com.example.ui.components.ForensicsCard
import com.example.ui.theme.ForensicsPalette

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AiBatteryDoctorSheet(
    history: List<AiDoctorExchange>,
    isLoading: Boolean,
    onAskQuestion: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var questionInput by remember { mutableStateOf("") }
    val presetQuestions = listOf(
        "Why did my battery drop 14% last night?",
        "Which app may be contributing to my drain?",
        "Is my battery health changing?",
        "Why is my phone getting hot?",
        "Is 5G associated with higher drain during my test?",
        "Why is charging slower today?",
        "Did a kernel CPU spike cause drain?"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = ForensicsPalette.ScreenBackground
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 8.dp)
                .testTag("ai_doctor_sheet"),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "AI Battery Doctor",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = ForensicsPalette.TextPrimary
                        )
                        Text(
                            text = "Strictly grounded in measured telemetry. Never invents missing data.",
                            fontSize = 12.sp,
                            color = ForensicsPalette.TextMuted
                        )
                    }
                    ClassificationBadge(
                        text = "Evidence-First",
                        containerColor = ForensicsPalette.PurpleContainer,
                        contentColor = ForensicsPalette.PurplePrimary
                    )
                }
            }

            item {
                Text(
                    text = "SUGGESTED FORENSIC QUESTIONS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.7.sp,
                    color = ForensicsPalette.TextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presetQuestions.forEachIndexed { index, preset ->
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = ForensicsPalette.CardSurface,
                            border = BorderStroke(1.dp, ForensicsPalette.BorderStrong),
                            shadowElevation = 1.dp,
                            modifier = Modifier
                                .clickable { onAskQuestion(preset) }
                                .testTag("ai_doctor_preset_$index")
                        ) {
                            Text(
                                text = preset,
                                fontSize = 12.sp,
                                color = ForensicsPalette.BluePrimary,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                            )
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = questionInput,
                        onValueChange = { questionInput = it },
                        placeholder = { Text("Ask why your battery drained...") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("ai_doctor_input")
                    )
                    Button(
                        onClick = {
                            if (questionInput.isNotBlank()) {
                                onAskQuestion(questionInput)
                                questionInput = ""
                            }
                        },
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = ForensicsPalette.PurplePrimary),
                        modifier = Modifier.testTag("ai_doctor_analyze_button")
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.width(16.dp).height(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Analyze")
                        }
                    }
                }
            }

            items(history.size) { index ->
                val item = history[index]
                ForensicsCard(
                    containerColor = if (item.isInsufficientData) ForensicsPalette.AmberInnerCard else ForensicsPalette.CardSurface
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
                            Text(
                                text = "Q: ${item.question}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = ForensicsPalette.PurplePrimary,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            ClassificationBadge(
                                text = item.confidence.label,
                                containerColor = if (item.isInsufficientData) ForensicsPalette.AmberPill else ForensicsPalette.BlueContainer,
                                contentColor = if (item.isInsufficientData) ForensicsPalette.AmberPrimary else ForensicsPalette.BluePrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = item.conclusion,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ForensicsPalette.TextPrimary
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "EVIDENCE CHAIN:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ForensicsPalette.TextSecondary
                        )
                        item.evidencePoints.forEach { pt ->
                            Text(
                                text = "• $pt",
                                fontSize = 12.sp,
                                color = ForensicsPalette.TextSecondary,
                                modifier = Modifier.padding(top = 3.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = ForensicsPalette.DividerColor)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Contributor: ${item.primaryContributor}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = ForensicsPalette.GreenPrimary
                        )
                        Text(
                            text = "Supporting Data: ${item.supportingData}",
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            color = ForensicsPalette.TextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(ForensicsPalette.SubtleSurface)
                                .padding(8.dp)
                        ) {
                            Text(
                                text = "Data Limitations: ${item.dataLimitations}",
                                fontSize = 11.sp,
                                color = ForensicsPalette.TextMuted
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(28.dp)) }
        }
    }
}
