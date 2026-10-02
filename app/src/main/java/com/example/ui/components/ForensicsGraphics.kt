package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.example.data.CapabilityStatus
import com.example.ui.theme.ForensicsPalette
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun DiagnosticTestGraphicIcon(
    testType: String,
    containerColor: Color,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val borderColor = accentColor.copy(alpha = 0.25f)
    Box(
        modifier = modifier
            .size(50.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(containerColor)
            .border(1.dp, borderColor, RoundedCornerShape(14.dp)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(26.dp)) {
            val w = size.width
            val h = size.height
            val stroke = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)

            when (testType) {
                "idle" -> {
                    // Three crisp geometric 'Z' glyphs ascending diagonally (zZz)
                    fun drawZ(topLeft: Offset, zWidth: Float, zHeight: Float, strokeW: Float) {
                        val p = Path().apply {
                            moveTo(topLeft.x, topLeft.y)
                            lineTo(topLeft.x + zWidth, topLeft.y)
                            lineTo(topLeft.x, topLeft.y + zHeight)
                            lineTo(topLeft.x + zWidth, topLeft.y + zHeight)
                        }
                        drawPath(
                            path = p,
                            color = accentColor,
                            style = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round)
                        )
                    }
                    drawZ(Offset(w * 0.08f, h * 0.56f), w * 0.26f, h * 0.28f, 2.0.dp.toPx())
                    drawZ(Offset(w * 0.36f, h * 0.32f), w * 0.30f, h * 0.32f, 2.3.dp.toPx())
                    drawZ(Offset(w * 0.66f, h * 0.12f), w * 0.24f, h * 0.24f, 1.8.dp.toPx())
                }

                "overnight" -> {
                    // Crescent moon with subtle star accents
                    val moonPath = Path().apply {
                        moveTo(w * 0.62f, h * 0.12f)
                        cubicTo(w * 0.22f, h * 0.16f, w * 0.12f, h * 0.68f, w * 0.48f, h * 0.86f)
                        cubicTo(w * 0.72f, h * 0.94f, w * 0.90f, h * 0.80f, w * 0.92f, h * 0.66f)
                        cubicTo(w * 0.64f, h * 0.72f, w * 0.42f, h * 0.46f, w * 0.62f, h * 0.12f)
                        close()
                    }
                    drawPath(path = moonPath, color = accentColor.copy(alpha = 0.22f))
                    drawPath(path = moonPath, color = accentColor, style = stroke)
                    // Sparkle star
                    drawCircle(color = accentColor, radius = 1.6.dp.toPx(), center = Offset(w * 0.78f, h * 0.26f))
                    drawCircle(color = accentColor, radius = 1.2.dp.toPx(), center = Offset(w * 0.88f, h * 0.42f))
                }

                "screen" -> {
                    // Smartphone bezel + illuminated screen + brightness sun
                    val rectW = w * 0.56f
                    val rectH = h * 0.88f
                    val left = (w - rectW) / 2f
                    val top = (h - rectH) / 2f
                    drawRoundRect(
                        color = accentColor.copy(alpha = 0.18f),
                        topLeft = Offset(left, top),
                        size = Size(rectW, rectH),
                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                    )
                    drawRoundRect(
                        color = accentColor,
                        topLeft = Offset(left, top),
                        size = Size(rectW, rectH),
                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
                        style = stroke
                    )
                    // Brightness indicator center
                    drawCircle(
                        color = accentColor,
                        radius = 2.6.dp.toPx(),
                        center = Offset(w * 0.5f, h * 0.46f)
                    )
                    // Bottom gesture bar
                    drawLine(
                        color = accentColor,
                        start = Offset(w * 0.40f, top + rectH - 3.5.dp.toPx()),
                        end = Offset(w * 0.60f, top + rectH - 3.5.dp.toPx()),
                        strokeWidth = 1.6.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }

                "network" -> {
                    // Satellite / Antenna Dish with radiating signal waves
                    // Base stand
                    val base = Path().apply {
                        moveTo(w * 0.22f, h * 0.88f)
                        lineTo(w * 0.52f, h * 0.88f)
                        moveTo(w * 0.37f, h * 0.88f)
                        lineTo(w * 0.44f, h * 0.62f)
                    }
                    drawPath(path = base, color = accentColor, style = stroke)
                    // Dish arc
                    drawArc(
                        color = accentColor,
                        startAngle = 115f,
                        sweepAngle = 145f,
                        useCenter = false,
                        topLeft = Offset(w * 0.14f, h * 0.22f),
                        size = Size(w * 0.54f, h * 0.54f),
                        style = stroke
                    )
                    // Radiating waves top-right
                    drawArc(
                        color = accentColor,
                        startAngle = -65f,
                        sweepAngle = 70f,
                        useCenter = false,
                        topLeft = Offset(w * 0.42f, h * 0.18f),
                        size = Size(w * 0.34f, h * 0.34f),
                        style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = accentColor,
                        startAngle = -65f,
                        sweepAngle = 70f,
                        useCenter = false,
                        topLeft = Offset(w * 0.32f, h * 0.06f),
                        size = Size(w * 0.56f, h * 0.56f),
                        style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                else -> {
                    // Lightning bolt (5G vs LTE Test)
                    val bolt = Path().apply {
                        moveTo(w * 0.56f, h * 0.06f)
                        lineTo(w * 0.20f, h * 0.54f)
                        lineTo(w * 0.48f, h * 0.54f)
                        lineTo(w * 0.40f, h * 0.94f)
                        lineTo(w * 0.80f, h * 0.44f)
                        lineTo(w * 0.52f, h * 0.44f)
                        close()
                    }
                    drawPath(path = bolt, color = accentColor.copy(alpha = 0.22f))
                    drawPath(path = bolt, color = accentColor, style = stroke)
                }
            }
        }
    }
}

@Composable
fun ChargerPlugGraphicIcon(
    accentColor: Color,
    containerColor: Color,
    modifier: Modifier = Modifier
) {
    val borderColor = accentColor.copy(alpha = 0.28f)
    Box(
        modifier = modifier
            .size(46.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(containerColor)
            .border(1.dp, borderColor, RoundedCornerShape(13.dp)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(24.dp)) {
            val w = size.width
            val h = size.height
            rotate(degrees = -45f, pivot = Offset(w / 2f, h / 2f)) {
                val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                // Prongs
                drawLine(
                    color = accentColor,
                    start = Offset(w * 0.36f, h * 0.10f),
                    end = Offset(w * 0.36f, h * 0.32f),
                    strokeWidth = 2.2.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = accentColor,
                    start = Offset(w * 0.64f, h * 0.10f),
                    end = Offset(w * 0.64f, h * 0.32f),
                    strokeWidth = 2.2.dp.toPx(),
                    cap = StrokeCap.Round
                )
                // Plug head body
                drawRoundRect(
                    color = accentColor.copy(alpha = 0.22f),
                    topLeft = Offset(w * 0.22f, h * 0.32f),
                    size = Size(w * 0.56f, h * 0.36f),
                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                )
                drawRoundRect(
                    color = accentColor,
                    topLeft = Offset(w * 0.22f, h * 0.32f),
                    size = Size(w * 0.56f, h * 0.36f),
                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
                    style = stroke
                )
                // Cord tail
                drawLine(
                    color = accentColor,
                    start = Offset(w * 0.50f, h * 0.68f),
                    end = Offset(w * 0.50f, h * 0.94f),
                    strokeWidth = 2.4.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

@Composable
fun SettingsCategoryGraphicIcon(
    category: String,
    accentColor: Color,
    containerColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(34.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(containerColor)
            .border(1.dp, accentColor.copy(alpha = 0.25f), RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(19.dp)) {
            val w = size.width
            val h = size.height
            val stroke = Stroke(width = 1.9.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)

            when (category) {
                "permissions" -> {
                    // Padlock + Key badge
                    drawArc(
                        color = accentColor,
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = false,
                        topLeft = Offset(w * 0.24f, h * 0.10f),
                        size = Size(w * 0.44f, h * 0.42f),
                        style = stroke
                    )
                    drawRoundRect(
                        color = accentColor.copy(alpha = 0.20f),
                        topLeft = Offset(w * 0.14f, h * 0.40f),
                        size = Size(w * 0.64f, h * 0.50f),
                        cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                    )
                    drawRoundRect(
                        color = accentColor,
                        topLeft = Offset(w * 0.14f, h * 0.40f),
                        size = Size(w * 0.64f, h * 0.50f),
                        cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx()),
                        style = stroke
                    )
                    drawCircle(
                        color = accentColor,
                        radius = 1.8.dp.toPx(),
                        center = Offset(w * 0.46f, h * 0.64f)
                    )
                }

                "privacy" -> {
                    // Shield with lock center
                    val shield = Path().apply {
                        moveTo(w * 0.50f, h * 0.08f)
                        lineTo(w * 0.88f, h * 0.24f)
                        lineTo(w * 0.88f, h * 0.54f)
                        cubicTo(w * 0.88f, h * 0.76f, w * 0.68f, h * 0.90f, w * 0.50f, h * 0.96f)
                        cubicTo(w * 0.32f, h * 0.90f, w * 0.12f, h * 0.76f, w * 0.12f, h * 0.54f)
                        lineTo(w * 0.12f, h * 0.24f)
                        close()
                    }
                    drawPath(path = shield, color = accentColor.copy(alpha = 0.20f))
                    drawPath(path = shield, color = accentColor, style = stroke)
                    drawCircle(color = accentColor, radius = 2.dp.toPx(), center = Offset(w * 0.50f, h * 0.52f))
                }

                "alerts" -> {
                    // Bell icon
                    val bell = Path().apply {
                        moveTo(w * 0.20f, h * 0.72f)
                        lineTo(w * 0.80f, h * 0.72f)
                        lineTo(w * 0.72f, h * 0.44f)
                        cubicTo(w * 0.72f, h * 0.24f, w * 0.62f, h * 0.14f, w * 0.50f, h * 0.14f)
                        cubicTo(w * 0.38f, h * 0.14f, w * 0.28f, h * 0.24f, w * 0.28f, h * 0.44f)
                        close()
                    }
                    drawPath(path = bell, color = accentColor.copy(alpha = 0.22f))
                    drawPath(path = bell, color = accentColor, style = stroke)
                    drawArc(
                        color = accentColor,
                        startAngle = 0f,
                        sweepAngle = 180f,
                        useCenter = false,
                        topLeft = Offset(w * 0.38f, h * 0.72f),
                        size = Size(w * 0.24f, h * 0.18f),
                        style = stroke
                    )
                }

                "faq" -> {
                    // Help Circle with Question Mark
                    val center = Offset(w / 2f, h / 2f)
                    val r = w * 0.40f
                    drawCircle(
                        color = accentColor.copy(alpha = 0.20f),
                        radius = r,
                        center = center
                    )
                    drawCircle(
                        color = accentColor,
                        radius = r,
                        center = center,
                        style = stroke
                    )
                    // Question arc + stem + dot
                    drawArc(
                        color = accentColor,
                        startAngle = 195f,
                        sweepAngle = 215f,
                        useCenter = false,
                        topLeft = Offset(w * 0.36f, h * 0.24f),
                        size = Size(w * 0.28f, h * 0.24f),
                        style = stroke
                    )
                    drawLine(
                        color = accentColor,
                        start = Offset(w * 0.50f, h * 0.48f),
                        end = Offset(w * 0.50f, h * 0.58f),
                        strokeWidth = 2.0.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                    drawCircle(
                        color = accentColor,
                        radius = 1.4.dp.toPx(),
                        center = Offset(w * 0.50f, h * 0.72f)
                    )
                }

                else -> {
                    // Precision Gear
                    val center = Offset(w / 2f, h / 2f)
                    val outerR = w * 0.34f
                    drawCircle(
                        color = accentColor.copy(alpha = 0.20f),
                        radius = outerR,
                        center = center
                    )
                    drawCircle(
                        color = accentColor,
                        radius = outerR,
                        center = center,
                        style = stroke
                    )
                    drawCircle(
                        color = accentColor,
                        radius = w * 0.13f,
                        center = center
                    )
                    for (deg in 0 until 360 step 45) {
                        val rad = Math.toRadians(deg.toDouble())
                        val start = Offset(
                            center.x + (outerR * cos(rad)).toFloat(),
                            center.y + (outerR * sin(rad)).toFloat()
                        )
                        val end = Offset(
                            center.x + ((outerR + 2.8.dp.toPx()) * cos(rad)).toFloat(),
                            center.y + ((outerR + 2.8.dp.toPx()) * sin(rad)).toFloat()
                        )
                        drawLine(
                            color = accentColor,
                            start = start,
                            end = end,
                            strokeWidth = 2.0.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CapabilityStatusGraphicBadge(
    status: CapabilityStatus,
    modifier: Modifier = Modifier
) {
    val (bg, border, fg) = when (status) {
        CapabilityStatus.SUPPORTED -> Triple(
            ForensicsPalette.GreenContainer,
            ForensicsPalette.GreenBorder,
            ForensicsPalette.GreenPrimary
        )
        CapabilityStatus.ESTIMATED_OR_LIMITED -> Triple(
            ForensicsPalette.AmberPill,
            ForensicsPalette.AmberBorder,
            ForensicsPalette.AmberPrimary
        )
        CapabilityStatus.UNAVAILABLE -> Triple(
            ForensicsPalette.RedContainer,
            ForensicsPalette.RedBorder,
            ForensicsPalette.RedPrimary
        )
    }

    Box(
        modifier = modifier
            .size(26.dp)
            .clip(CircleShape)
            .background(bg)
            .border(1.dp, border, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(12.dp)) {
            val w = size.width
            val h = size.height
            val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            when (status) {
                CapabilityStatus.SUPPORTED -> {
                    val p = Path().apply {
                        moveTo(w * 0.12f, h * 0.54f)
                        lineTo(w * 0.40f, h * 0.82f)
                        lineTo(w * 0.88f, h * 0.20f)
                    }
                    drawPath(path = p, color = fg, style = stroke)
                }
                CapabilityStatus.ESTIMATED_OR_LIMITED -> {
                    val p = Path().apply {
                        moveTo(w * 0.10f, h * 0.56f)
                        cubicTo(w * 0.35f, h * 0.18f, w * 0.65f, h * 0.92f, w * 0.90f, h * 0.44f)
                    }
                    drawPath(path = p, color = fg, style = stroke)
                }
                CapabilityStatus.UNAVAILABLE -> {
                    drawLine(
                        color = fg,
                        start = Offset(w * 0.20f, h * 0.20f),
                        end = Offset(w * 0.80f, h * 0.80f),
                        strokeWidth = 2.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = fg,
                        start = Offset(w * 0.80f, h * 0.20f),
                        end = Offset(w * 0.20f, h * 0.80f),
                        strokeWidth = 2.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }
        }
    }
}
