package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ForensicsSplashScreen(
    onSplashFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val batteryDrawProgress = remember { Animatable(0f) }
    val pulseTraceProgress = remember { Animatable(0f) }
    val titleAlpha = remember { Animatable(0f) }
    val taglineAlpha = remember { Animatable(0f) }
    val screenAlpha = remember { Animatable(1f) }
    val logoScale = remember { Animatable(0.86f) }

    val infiniteTransition = rememberInfiniteTransition(label = "splash_glow")
    val pulseGlowScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_glow_scale"
    )

    LaunchedEffect(Unit) {
        launch {
            logoScale.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing)
            )
        }
        launch {
            batteryDrawProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 680, easing = FastOutSlowInEasing)
            )
        }
        delay(280)
        launch {
            pulseTraceProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 820, easing = LinearOutSlowInEasing)
            )
        }
        delay(220)
        launch {
            titleAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
            )
        }
        delay(160)
        launch {
            taglineAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 520, easing = FastOutSlowInEasing)
            )
        }
        delay(1050)
        screenAlpha.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 360, easing = FastOutSlowInEasing)
        )
        onSplashFinished()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer { alpha = screenAlpha.value }
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF28333F),
                        Color(0xFF1A222B),
                        Color(0xFF12161B)
                    )
                )
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                onSplashFinished()
            }
            .testTag("forensics_splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(188.dp)
                    .graphicsLayer {
                        scaleX = logoScale.value
                        scaleY = logoScale.value
                    },
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val scale = w / 108f

                    // Ambient golden-slate radial halo behind the battery
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0x38F7B318),
                                Color(0x14F7B318),
                                Color.Transparent
                            ),
                            center = Offset(w * 0.5f, h * 0.5f),
                            radius = (w * 0.44f) * pulseGlowScale
                        ),
                        radius = (w * 0.44f) * pulseGlowScale,
                        center = Offset(w * 0.5f, h * 0.5f)
                    )

                    rotate(degrees = -8f, pivot = Offset(w * 0.5f, h * 0.5f)) {
                        // Inner dark slate battery body fill
                        val bodyPath = Path().apply {
                            addRoundRect(
                                RoundRect(
                                    rect = Rect(
                                        left = 26f * scale,
                                        top = 37f * scale,
                                        right = 76f * scale,
                                        bottom = 71f * scale
                                    ),
                                    cornerRadius = CornerRadius(10f * scale, 10f * scale)
                                )
                            )
                        }
                        drawPath(
                            path = bodyPath,
                            color = Color(0xFF1D252E).copy(alpha = batteryDrawProgress.value)
                        )

                        // Complete battery silhouette + right positive terminal contour
                        val outlinePath = Path().apply {
                            moveTo(36f * scale, 37f * scale)
                            lineTo(66f * scale, 37f * scale)
                            arcTo(
                                rect = Rect(56f * scale, 37f * scale, 76f * scale, 57f * scale),
                                startAngleDegrees = -90f,
                                sweepAngleDegrees = 90f,
                                forceMoveTo = false
                            )
                            lineTo(78.5f * scale, 47f * scale)
                            arcTo(
                                rect = Rect(74.5f * scale, 47f * scale, 82.5f * scale, 55f * scale),
                                startAngleDegrees = -90f,
                                sweepAngleDegrees = 90f,
                                forceMoveTo = false
                            )
                            lineTo(82.5f * scale, 57f * scale)
                            arcTo(
                                rect = Rect(74.5f * scale, 53f * scale, 82.5f * scale, 61f * scale),
                                startAngleDegrees = 0f,
                                sweepAngleDegrees = 90f,
                                forceMoveTo = false
                            )
                            lineTo(76f * scale, 61f * scale)
                            arcTo(
                                rect = Rect(56f * scale, 51f * scale, 76f * scale, 71f * scale),
                                startAngleDegrees = 0f,
                                sweepAngleDegrees = 90f,
                                forceMoveTo = false
                            )
                            lineTo(36f * scale, 71f * scale)
                            arcTo(
                                rect = Rect(26f * scale, 51f * scale, 46f * scale, 71f * scale),
                                startAngleDegrees = 90f,
                                sweepAngleDegrees = 90f,
                                forceMoveTo = false
                            )
                            lineTo(26f * scale, 47f * scale)
                            arcTo(
                                rect = Rect(26f * scale, 37f * scale, 46f * scale, 57f * scale),
                                startAngleDegrees = 180f,
                                sweepAngleDegrees = 90f,
                                forceMoveTo = false
                            )
                            close()
                        }

                        val batteryMeasure = PathMeasure()
                        batteryMeasure.setPath(outlinePath, true)
                        val partialBatteryPath = Path()
                        batteryMeasure.getSegment(
                            startDistance = 0f,
                            stopDistance = batteryMeasure.length * batteryDrawProgress.value,
                            destination = partialBatteryPath,
                            startWithMoveTo = true
                        )

                        drawPath(
                            path = partialBatteryPath,
                            color = Color(0xFFF3F0E6),
                            style = Stroke(
                                width = 5.4f * scale,
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )

                        // Golden-Amber ECG / Pulse Waveform inside battery
                        val pulsePath = Path().apply {
                            moveTo(34.5f * scale, 55.5f * scale)
                            lineTo(40.5f * scale, 55.2f * scale)
                            lineTo(44.2f * scale, 56.5f * scale)
                            lineTo(48.6f * scale, 43.2f * scale)
                            lineTo(55.6f * scale, 64.2f * scale)
                            lineTo(58.2f * scale, 52.8f * scale)
                            cubicTo(
                                59.6f * scale, 48.6f * scale,
                                62.8f * scale, 48.6f * scale,
                                64.4f * scale, 52.8f * scale
                            )
                            lineTo(68.5f * scale, 52.8f * scale)
                        }

                        if (pulseTraceProgress.value > 0f) {
                            val pulseMeasure = PathMeasure()
                            pulseMeasure.setPath(pulsePath, false)
                            val partialPulsePath = Path()
                            val stopDist = pulseMeasure.length * pulseTraceProgress.value
                            pulseMeasure.getSegment(
                                startDistance = 0f,
                                stopDistance = stopDist,
                                destination = partialPulsePath,
                                startWithMoveTo = true
                            )

                            // Subtle outer glow on the ECG waveform
                            drawPath(
                                path = partialPulsePath,
                                color = Color(0x4DF7B318),
                                style = Stroke(
                                    width = 9.2f * scale,
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )

                            // Crisp core golden ECG waveform
                            drawPath(
                                path = partialPulsePath,
                                color = Color(0xFFF7B318),
                                style = Stroke(
                                    width = 5.0f * scale,
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )

                            // Leading spark dot while tracing
                            if (pulseTraceProgress.value < 0.99f) {
                                val tipPos = pulseMeasure.getPosition(stopDist)
                                if (tipPos != Offset.Unspecified) {
                                    drawCircle(
                                        color = Color(0xFFFFD56B),
                                        radius = 3.6f * scale,
                                        center = tipPos
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Battery Forensics",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.4.sp,
                color = Color(0xFFF3F0E6),
                textAlign = TextAlign.Center,
                modifier = Modifier.graphicsLayer {
                    alpha = titleAlpha.value
                    translationY = (1f - titleAlpha.value) * 14f
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Every conclusion traced to evidence.",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.2.sp,
                color = Color(0xFFB0BAC5),
                textAlign = TextAlign.Center,
                modifier = Modifier.graphicsLayer {
                    alpha = taglineAlpha.value
                    translationY = (1f - taglineAlpha.value) * 10f
                }
            )
        }
    }
}
