package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.PortalDestination
import com.example.ui.theme.RoyalNavyCard
import com.example.ui.theme.RoyalNavyDeep
import com.example.ui.theme.RoyalNavySurface
import com.example.ui.theme.TextIvory
import com.example.ui.theme.TextSilver
import com.example.ui.theme.ZummarAlertRed
import com.example.ui.theme.ZummarCyan
import com.example.ui.theme.ZummarCyanLight
import com.example.ui.theme.ZummarEmerald
import com.example.ui.theme.ZummarEmeraldGlow
import com.example.ui.theme.ZummarGold
import com.example.ui.theme.ZummarGoldSoft
import com.example.ui.theme.ZummarGoldWarm
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

enum class VectorSymbol {
    ELECTRIC_GRID,
    WATER_WAVE,
    PHARMACY_CHALICE,
    CLINIC_PULSE,
    LAB_FLASK,
    STOREFRONT,
    MUNICIPAL_ARCH,
    ADMIN_SHIELD,
    ROLE_BADGE,
    POLYGON_DRAW,
    FREEHAND_LASSO,
    CHECK_MARK,
    CLOSE_CROSS,
    PLUS_ADD,
    CHAT_BUBBLE,
    CALENDAR_GRID,
    SEARCH_LENS,
    ARROW_BACK,
    ZOOM_IN,
    ZOOM_OUT,
    RESET_COMPASS,
    RADAR_BEACON,
    TRASH_BIN,
    CLOCK_DIAL
}

/**
 * Pure custom Canvas vector symbol replacing all classic icons across the entire application.
 * Uses smooth cubic/quadratic curves, radial/linear gradients, and live subtle pulse animation.
 */
@Composable
fun CustomVectorSymbol(
    symbol: VectorSymbol,
    modifier: Modifier = Modifier.size(28.dp),
    primaryColor: Color = ZummarCyan,
    secondaryColor: Color = ZummarGold,
    animated: Boolean = true
) {
    val transition = rememberInfiniteTransition(label = "symbol_anim")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )
    val pulse by transition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val activePulse = if (animated) pulse else 1f
    val activePhase = if (animated) phase else 0.3f

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        val strokeW = (w * 0.085f).coerceAtLeast(2.2f)

        val mainBrush = Brush.linearGradient(
            colors = listOf(primaryColor, secondaryColor, primaryColor),
            start = Offset(0f, 0f),
            end = Offset(w, h)
        )

        // Subtle glowing aura behind every custom symbol
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(primaryColor.copy(alpha = 0.25f * activePulse), Color.Transparent),
                center = Offset(cx, cy),
                radius = (w * 0.5f * activePulse).coerceAtLeast(4f)
            ),
            radius = (w * 0.5f * activePulse).coerceAtLeast(4f),
            center = Offset(cx, cy)
        )

        when (symbol) {
            VectorSymbol.ELECTRIC_GRID -> {
                val boltPath = Path().apply {
                    moveTo(w * 0.56f, h * 0.10f)
                    quadraticBezierTo(w * 0.34f, h * 0.35f, w * 0.24f, h * 0.52f)
                    lineTo(w * 0.50f, h * 0.52f)
                    quadraticBezierTo(w * 0.44f, h * 0.72f, w * 0.38f, h * 0.90f)
                    quadraticBezierTo(w * 0.64f, h * 0.62f, w * 0.78f, h * 0.44f)
                    lineTo(w * 0.52f, h * 0.44f)
                    close()
                }
                drawPath(
                    path = boltPath,
                    brush = Brush.linearGradient(
                        colors = listOf(ZummarGoldSoft, ZummarGold, ZummarGoldWarm)
                    )
                )
                // Energy arc curves
                val arcPath = Path().apply {
                    moveTo(w * 0.14f, h * 0.32f)
                    cubicTo(
                        w * 0.08f, h * (0.45f + 0.08f * sin(activePhase * 2 * PI).toFloat()),
                        w * 0.12f, h * 0.65f,
                        w * 0.22f, h * 0.76f
                    )
                }
                drawPath(arcPath, color = ZummarGoldSoft.copy(alpha = 0.85f), style = Stroke(width = strokeW * 0.7f, cap = StrokeCap.Round))
            }

            VectorSymbol.WATER_WAVE -> {
                val dropPath = Path().apply {
                    moveTo(cx, h * 0.12f)
                    cubicTo(w * 0.82f, h * 0.46f, w * 0.84f, h * 0.78f, cx, h * 0.88f)
                    cubicTo(w * 0.16f, h * 0.78f, w * 0.18f, h * 0.46f, cx, h * 0.12f)
                    close()
                }
                drawPath(
                    path = dropPath,
                    brush = Brush.linearGradient(
                        colors = listOf(ZummarCyanLight, ZummarCyan, Color(0xFF0077B6)),
                        start = Offset(cx, h * 0.1f),
                        end = Offset(cx, h * 0.9f)
                    ),
                    style = Stroke(width = strokeW, join = StrokeJoin.Round)
                )
                val wavePath = Path().apply {
                    val waveY = h * 0.58f
                    moveTo(w * 0.26f, waveY)
                    quadraticBezierTo(
                        w * 0.38f, waveY - h * 0.10f * sin(activePhase * 2 * PI).toFloat(),
                        cx, waveY
                    )
                    quadraticBezierTo(
                        w * 0.62f, waveY + h * 0.10f * sin(activePhase * 2 * PI).toFloat(),
                        w * 0.74f, waveY
                    )
                }
                drawPath(wavePath, color = ZummarCyanLight, style = Stroke(width = strokeW * 0.85f, cap = StrokeCap.Round))
            }

            VectorSymbol.PHARMACY_CHALICE -> {
                // Medical chalice cup + emerald pulse curve
                val cupPath = Path().apply {
                    moveTo(w * 0.24f, h * 0.22f)
                    quadraticBezierTo(cx, h * 0.28f, w * 0.76f, h * 0.22f)
                    cubicTo(w * 0.74f, h * 0.55f, w * 0.58f, h * 0.62f, cx, h * 0.64f)
                    cubicTo(w * 0.42f, h * 0.62f, w * 0.26f, h * 0.55f, w * 0.24f, h * 0.22f)
                }
                drawPath(cupPath, brush = mainBrush, style = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round))
                // Stem & base
                val stemPath = Path().apply {
                    moveTo(cx, h * 0.64f)
                    lineTo(cx, h * 0.84f)
                    moveTo(w * 0.32f, h * 0.84f)
                    quadraticBezierTo(cx, h * 0.80f, w * 0.68f, h * 0.84f)
                }
                drawPath(stemPath, color = primaryColor, style = Stroke(width = strokeW, cap = StrokeCap.Round))
            }

            VectorSymbol.CLINIC_PULSE -> {
                val pulsePath = Path().apply {
                    moveTo(w * 0.10f, cy)
                    lineTo(w * 0.28f, cy)
                    quadraticBezierTo(w * 0.34f, cy, w * 0.39f, h * 0.22f)
                    lineTo(w * 0.50f, h * 0.80f)
                    lineTo(w * 0.61f, h * 0.34f)
                    quadraticBezierTo(w * 0.66f, cy, w * 0.72f, cy)
                    lineTo(w * 0.90f, cy)
                }
                drawPath(pulsePath, brush = mainBrush, style = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round))
            }

            VectorSymbol.LAB_FLASK -> {
                val flaskPath = Path().apply {
                    moveTo(w * 0.40f, h * 0.16f)
                    lineTo(w * 0.60f, h * 0.16f)
                    moveTo(w * 0.44f, h * 0.16f)
                    lineTo(w * 0.44f, h * 0.38f)
                    cubicTo(w * 0.24f, h * 0.64f, w * 0.20f, h * 0.82f, w * 0.32f, h * 0.86f)
                    lineTo(w * 0.68f, h * 0.86f)
                    cubicTo(w * 0.80f, h * 0.82f, w * 0.76f, h * 0.64f, w * 0.56f, h * 0.38f)
                    lineTo(w * 0.56f, h * 0.16f)
                }
                drawPath(flaskPath, brush = mainBrush, style = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round))
                val liquidPath = Path().apply {
                    moveTo(w * 0.28f, h * 0.70f)
                    quadraticBezierTo(cx, h * (0.64f + 0.06f * sin(activePhase * 2 * PI).toFloat()), w * 0.72f, h * 0.70f)
                }
                drawPath(liquidPath, color = secondaryColor, style = Stroke(width = strokeW * 0.8f, cap = StrokeCap.Round))
            }

            VectorSymbol.STOREFRONT -> {
                // Storefront awning + arched entrance
                val awningPath = Path().apply {
                    moveTo(w * 0.14f, h * 0.38f)
                    lineTo(w * 0.24f, h * 0.18f)
                    lineTo(w * 0.76f, h * 0.18f)
                    lineTo(w * 0.86f, h * 0.38f)
                    quadraticBezierTo(w * 0.68f, h * 0.48f, cx, h * 0.38f)
                    quadraticBezierTo(w * 0.32f, h * 0.48f, w * 0.14f, h * 0.38f)
                }
                drawPath(awningPath, brush = mainBrush, style = Stroke(width = strokeW, join = StrokeJoin.Round))
                val wallsPath = Path().apply {
                    moveTo(w * 0.22f, h * 0.42f)
                    lineTo(w * 0.22f, h * 0.84f)
                    lineTo(w * 0.78f, h * 0.84f)
                    lineTo(w * 0.78f, h * 0.42f)
                    moveTo(w * 0.40f, h * 0.84f)
                    quadraticBezierTo(cx, h * 0.52f, w * 0.60f, h * 0.84f)
                }
                drawPath(wallsPath, color = primaryColor, style = Stroke(width = strokeW, cap = StrokeCap.Round))
            }

            VectorSymbol.MUNICIPAL_ARCH -> {
                val archPath = Path().apply {
                    moveTo(w * 0.16f, h * 0.82f)
                    lineTo(w * 0.16f, h * 0.40f)
                    quadraticBezierTo(cx, h * 0.10f, w * 0.84f, h * 0.40f)
                    lineTo(w * 0.84f, h * 0.82f)
                    moveTo(w * 0.32f, h * 0.82f)
                    quadraticBezierTo(cx, h * 0.34f, w * 0.68f, h * 0.82f)
                }
                drawPath(archPath, brush = mainBrush, style = Stroke(width = strokeW, cap = StrokeCap.Round))
            }

            VectorSymbol.ADMIN_SHIELD -> {
                val shieldPath = Path().apply {
                    moveTo(cx, h * 0.12f)
                    quadraticBezierTo(w * 0.80f, h * 0.18f, w * 0.82f, h * 0.44f)
                    cubicTo(w * 0.82f, h * 0.68f, w * 0.62f, h * 0.82f, cx, h * 0.90f)
                    cubicTo(w * 0.38f, h * 0.82f, w * 0.18f, h * 0.68f, w * 0.18f, h * 0.44f)
                    quadraticBezierTo(w * 0.20f, h * 0.18f, cx, h * 0.12f)
                    close()
                }
                drawPath(shieldPath, brush = mainBrush, style = Stroke(width = strokeW, join = StrokeJoin.Round))
                val check = Path().apply {
                    moveTo(w * 0.36f, h * 0.52f)
                    quadraticBezierTo(w * 0.45f, h * 0.64f, w * 0.47f, h * 0.64f)
                    quadraticBezierTo(w * 0.58f, h * 0.45f, w * 0.66f, h * 0.38f)
                }
                drawPath(check, color = secondaryColor, style = Stroke(width = strokeW, cap = StrokeCap.Round))
            }

            VectorSymbol.ROLE_BADGE -> {
                val badgePath = Path().apply {
                    moveTo(cx, h * 0.16f)
                    cubicTo(w * 0.72f, h * 0.16f, w * 0.72f, h * 0.48f, cx, h * 0.48f)
                    cubicTo(w * 0.28f, h * 0.48f, w * 0.28f, h * 0.16f, cx, h * 0.16f)
                    moveTo(w * 0.20f, h * 0.84f)
                    cubicTo(w * 0.24f, h * 0.58f, w * 0.76f, h * 0.58f, w * 0.80f, h * 0.84f)
                }
                drawPath(badgePath, brush = mainBrush, style = Stroke(width = strokeW, cap = StrokeCap.Round))
            }

            VectorSymbol.POLYGON_DRAW -> {
                val poly = Path().apply {
                    moveTo(w * 0.22f, h * 0.26f)
                    lineTo(w * 0.76f, h * 0.20f)
                    lineTo(w * 0.82f, h * 0.72f)
                    lineTo(w * 0.30f, h * 0.82f)
                    close()
                }
                drawPath(poly, brush = mainBrush, style = Stroke(width = strokeW * 0.85f, join = StrokeJoin.Round))
                listOf(
                    Offset(w * 0.22f, h * 0.26f),
                    Offset(w * 0.76f, h * 0.20f),
                    Offset(w * 0.82f, h * 0.72f),
                    Offset(w * 0.30f, h * 0.82f)
                ).forEach { pt ->
                    drawCircle(color = secondaryColor, radius = strokeW * 1.15f, center = pt)
                }
            }

            VectorSymbol.FREEHAND_LASSO -> {
                val lasso = Path().apply {
                    moveTo(w * 0.20f, h * 0.52f)
                    cubicTo(w * 0.18f, h * 0.14f, w * 0.82f, h * 0.14f, w * 0.80f, h * 0.48f)
                    cubicTo(w * 0.78f, h * 0.82f, w * 0.28f, h * 0.82f, w * 0.24f, h * 0.56f)
                    quadraticBezierTo(w * 0.22f, h * 0.84f, w * 0.46f, h * 0.88f)
                }
                drawPath(lasso, brush = mainBrush, style = Stroke(width = strokeW, cap = StrokeCap.Round))
            }

            VectorSymbol.CHECK_MARK -> {
                val p = Path().apply {
                    moveTo(w * 0.20f, h * 0.52f)
                    quadraticBezierTo(w * 0.38f, h * 0.72f, w * 0.42f, h * 0.74f)
                    quadraticBezierTo(w * 0.62f, h * 0.44f, w * 0.80f, h * 0.26f)
                }
                drawPath(p, brush = mainBrush, style = Stroke(width = strokeW * 1.1f, cap = StrokeCap.Round, join = StrokeJoin.Round))
            }

            VectorSymbol.CLOSE_CROSS -> {
                val p = Path().apply {
                    moveTo(w * 0.24f, h * 0.24f)
                    quadraticBezierTo(cx, cy, w * 0.76f, h * 0.76f)
                    moveTo(w * 0.76f, h * 0.24f)
                    quadraticBezierTo(cx, cy, w * 0.24f, h * 0.76f)
                }
                drawPath(p, brush = mainBrush, style = Stroke(width = strokeW * 1.1f, cap = StrokeCap.Round))
            }

            VectorSymbol.PLUS_ADD -> {
                val p = Path().apply {
                    moveTo(cx, h * 0.20f)
                    lineTo(cx, h * 0.80f)
                    moveTo(w * 0.20f, cy)
                    lineTo(w * 0.80f, cy)
                }
                drawPath(p, brush = mainBrush, style = Stroke(width = strokeW * 1.1f, cap = StrokeCap.Round))
            }

            VectorSymbol.CHAT_BUBBLE -> {
                val p = Path().apply {
                    moveTo(w * 0.20f, h * 0.26f)
                    quadraticBezierTo(cx, h * 0.16f, w * 0.80f, h * 0.26f)
                    quadraticBezierTo(w * 0.90f, h * 0.48f, w * 0.78f, h * 0.68f)
                    quadraticBezierTo(w * 0.58f, h * 0.74f, w * 0.42f, h * 0.72f)
                    lineTo(w * 0.22f, h * 0.86f)
                    lineTo(w * 0.26f, h * 0.66f)
                    quadraticBezierTo(w * 0.12f, h * 0.48f, w * 0.20f, h * 0.26f)
                    close()
                }
                drawPath(p, brush = mainBrush, style = Stroke(width = strokeW, join = StrokeJoin.Round))
            }

            VectorSymbol.CALENDAR_GRID -> {
                drawRoundRect(
                    brush = mainBrush,
                    topLeft = Offset(w * 0.16f, h * 0.22f),
                    size = Size(w * 0.68f, h * 0.62f),
                    cornerRadius = CornerRadius(w * 0.14f, w * 0.14f),
                    style = Stroke(width = strokeW)
                )
                val lines = Path().apply {
                    moveTo(w * 0.16f, h * 0.40f)
                    lineTo(w * 0.84f, h * 0.40f)
                    moveTo(w * 0.34f, h * 0.14f)
                    lineTo(w * 0.34f, h * 0.28f)
                    moveTo(w * 0.66f, h * 0.14f)
                    lineTo(w * 0.66f, h * 0.28f)
                }
                drawPath(lines, color = secondaryColor, style = Stroke(width = strokeW, cap = StrokeCap.Round))
            }

            VectorSymbol.SEARCH_LENS -> {
                drawCircle(
                    brush = mainBrush,
                    radius = w * 0.24f,
                    center = Offset(w * 0.44f, h * 0.44f),
                    style = Stroke(width = strokeW)
                )
                val handle = Path().apply {
                    moveTo(w * 0.62f, h * 0.62f)
                    quadraticBezierTo(w * 0.72f, h * 0.72f, w * 0.84f, h * 0.84f)
                }
                drawPath(handle, color = secondaryColor, style = Stroke(width = strokeW * 1.15f, cap = StrokeCap.Round))
            }

            VectorSymbol.ARROW_BACK -> {
                val p = Path().apply {
                    moveTo(w * 0.36f, h * 0.22f)
                    quadraticBezierTo(w * 0.68f, cy, w * 0.72f, cy)
                    quadraticBezierTo(w * 0.68f, cy, w * 0.36f, h * 0.78f)
                }
                drawPath(p, brush = mainBrush, style = Stroke(width = strokeW * 1.1f, cap = StrokeCap.Round, join = StrokeJoin.Round))
            }

            VectorSymbol.ZOOM_IN -> {
                drawCircle(brush = mainBrush, radius = w * 0.34f, center = Offset(cx, cy), style = Stroke(width = strokeW))
                val p = Path().apply {
                    moveTo(cx, h * 0.32f); lineTo(cx, h * 0.68f)
                    moveTo(w * 0.32f, cy); lineTo(w * 0.68f, cy)
                }
                drawPath(p, color = secondaryColor, style = Stroke(width = strokeW, cap = StrokeCap.Round))
            }

            VectorSymbol.ZOOM_OUT -> {
                drawCircle(brush = mainBrush, radius = w * 0.34f, center = Offset(cx, cy), style = Stroke(width = strokeW))
                val p = Path().apply {
                    moveTo(w * 0.32f, cy); lineTo(w * 0.68f, cy)
                }
                drawPath(p, color = secondaryColor, style = Stroke(width = strokeW, cap = StrokeCap.Round))
            }

            VectorSymbol.RESET_COMPASS -> {
                val diamond = Path().apply {
                    moveTo(cx, h * 0.14f)
                    lineTo(w * 0.74f, cy)
                    lineTo(cx, h * 0.86f)
                    lineTo(w * 0.26f, cy)
                    close()
                }
                drawPath(diamond, brush = mainBrush, style = Stroke(width = strokeW, join = StrokeJoin.Round))
                drawCircle(color = secondaryColor, radius = strokeW, center = Offset(cx, cy))
            }

            VectorSymbol.RADAR_BEACON -> {
                drawCircle(
                    color = primaryColor.copy(alpha = (1f - activePhase) * 0.65f),
                    radius = w * (0.18f + 0.30f * activePhase),
                    center = Offset(cx, cy),
                    style = Stroke(width = strokeW * 0.75f)
                )
                drawCircle(color = primaryColor, radius = w * 0.18f, center = Offset(cx, cy))
            }

            VectorSymbol.TRASH_BIN -> {
                val p = Path().apply {
                    moveTo(w * 0.24f, h * 0.28f)
                    lineTo(w * 0.76f, h * 0.28f)
                    moveTo(w * 0.30f, h * 0.28f)
                    lineTo(w * 0.34f, h * 0.84f)
                    lineTo(w * 0.66f, h * 0.84f)
                    lineTo(w * 0.70f, h * 0.28f)
                }
                drawPath(p, brush = mainBrush, style = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round))
            }

            VectorSymbol.CLOCK_DIAL -> {
                drawCircle(brush = mainBrush, radius = w * 0.36f, center = Offset(cx, cy), style = Stroke(width = strokeW))
                val hands = Path().apply {
                    moveTo(cx, cy)
                    lineTo(cx, h * 0.28f)
                    moveTo(cx, cy)
                    lineTo(w * 0.68f, cy)
                }
                drawPath(hands, color = secondaryColor, style = Stroke(width = strokeW, cap = StrokeCap.Round))
            }
        }
    }
}

/**
 * Welcome Hero Panorama Canvas:
 * Renders an artistic, living night panorama of Zummar Sub-District (Tigris river curve,
 * power towers, water network, medical beacon, and market arches) connected by glowing pulses.
 */
@Composable
fun ZummarWelcomeHeroCanvas(
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(210.dp)
) {
    val transition = rememberInfiniteTransition(label = "zummar_hero")
    val flowPhase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "flow_phase"
    )
    val radarPulse by transition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "radar_pulse"
    )

    Canvas(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .border(
                width = 1.5.dp,
                brush = Brush.linearGradient(listOf(ZummarCyan.copy(alpha = 0.6f), ZummarGold.copy(alpha = 0.6f), ZummarEmerald.copy(alpha = 0.6f))),
                shape = RoundedCornerShape(24.dp)
            )
    ) {
        val w = size.width
        val h = size.height

        // Deep Royal Navy sky background
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF070C1F), RoyalNavyDeep, RoyalNavySurface)
            )
        )

        // Glowing horizon aura
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(ZummarGold.copy(alpha = 0.20f), ZummarCyan.copy(alpha = 0.10f), Color.Transparent),
                center = Offset(w * 0.5f, h * 0.55f),
                radius = w * 0.55f
            ),
            radius = w * 0.55f,
            center = Offset(w * 0.5f, h * 0.55f)
        )

        // Zummar rolling hills silhouette
        val hillsPath = Path().apply {
            moveTo(0f, h * 0.72f)
            cubicTo(w * 0.25f, h * 0.56f, w * 0.45f, h * 0.78f, w * 0.72f, h * 0.60f)
            quadraticBezierTo(w * 0.88f, h * 0.52f, w, h * 0.66f)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(
            path = hillsPath,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF132347), Color(0xFF0A1329))
            )
        )

        // Animated Tigris River & Water Network waves at bottom
        for (layer in 0..2) {
            val layerOffset = layer * 0.33f
            val waveY = h * (0.76f + layer * 0.06f)
            val waterPath = Path().apply {
                moveTo(0f, waveY)
                val shift = sin((flowPhase + layerOffset) * 2 * PI).toFloat() * (h * 0.045f)
                cubicTo(
                    w * 0.30f, waveY - shift,
                    w * 0.65f, waveY + shift,
                    w, waveY - shift * 0.5f
                )
            }
            drawPath(
                path = waterPath,
                brush = Brush.horizontalGradient(
                    colors = listOf(ZummarCyan.copy(alpha = 0.35f), ZummarCyanLight.copy(alpha = 0.85f), ZummarEmerald.copy(alpha = 0.5f))
                ),
                style = Stroke(width = (4f - layer).coerceAtLeast(2f), cap = StrokeCap.Round)
            )
        }

        // Architectural Zummar Gate Arch in Center
        val gateLeft = w * 0.39f
        val gateRight = w * 0.61f
        val gateTop = h * 0.20f
        val gateBottom = h * 0.74f
        val archPath = Path().apply {
            moveTo(gateLeft, gateBottom)
            lineTo(gateLeft, gateTop + h * 0.16f)
            quadraticBezierTo(w * 0.5f, gateTop - h * 0.04f, gateRight, gateTop + h * 0.16f)
            lineTo(gateRight, gateBottom)
            // Inner arch
            moveTo(gateLeft + w * 0.04f, gateBottom)
            lineTo(gateLeft + w * 0.04f, gateTop + h * 0.22f)
            quadraticBezierTo(w * 0.5f, gateTop + h * 0.06f, gateRight - w * 0.04f, gateTop + h * 0.22f)
            lineTo(gateRight - w * 0.04f, gateBottom)
        }
        drawPath(
            path = archPath,
            brush = Brush.verticalGradient(listOf(ZummarGoldSoft, ZummarGold, ZummarGoldWarm)),
            style = Stroke(width = 5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // Connected network nodes (Power Left, Gate Center, Medical & Market Right)
        val nodes = listOf(
            Offset(w * 0.16f, h * 0.42f) to ZummarGold,
            Offset(w * 0.32f, h * 0.56f) to ZummarCyan,
            Offset(w * 0.50f, h * 0.36f) to ZummarGoldSoft,
            Offset(w * 0.68f, h * 0.52f) to ZummarEmerald,
            Offset(w * 0.84f, h * 0.38f) to ZummarCyanLight
        )

        // Draw curved fiber/power connections with moving light pulses
        for (i in 0 until nodes.size - 1) {
            val start = nodes[i].first
            val end = nodes[i + 1].first
            val midX = (start.x + end.x) / 2f
            val midY = minOf(start.y, end.y) - h * 0.12f
            val connPath = Path().apply {
                moveTo(start.x, start.y)
                quadraticBezierTo(midX, midY, end.x, end.y)
            }
            drawPath(
                path = connPath,
                brush = Brush.linearGradient(
                    colors = listOf(nodes[i].second.copy(alpha = 0.55f), nodes[i + 1].second.copy(alpha = 0.55f)),
                    start = start,
                    end = end
                ),
                style = Stroke(width = 2.8f, cap = StrokeCap.Round)
            )

            // Travelling pulse orb along the quadratic Bezier curve
            val t = (flowPhase + i * 0.22f) % 1f
            val oneMinusT = 1f - t
            val px = oneMinusT * oneMinusT * start.x + 2f * oneMinusT * t * midX + t * t * end.x
            val py = oneMinusT * oneMinusT * start.y + 2f * oneMinusT * t * midY + t * t * end.y
            drawCircle(
                color = Color.White,
                radius = 4.5f,
                center = Offset(px, py)
            )
            drawCircle(
                color = nodes[i].second.copy(alpha = 0.45f),
                radius = 11f,
                center = Offset(px, py)
            )
        }

        // Draw glowing nodes
        nodes.forEach { (pos, col) ->
            drawCircle(
                color = col.copy(alpha = 0.25f),
                radius = 14f * radarPulse,
                center = pos
            )
            drawCircle(
                color = col,
                radius = 6.5f,
                center = pos
            )
        }
    }
}

/**
 * Custom Canvas card illustration for each of the 5 service destinations on the Welcome Screen.
 */
@Composable
fun DestinationCardCanvasIllustration(
    destination: PortalDestination,
    modifier: Modifier = Modifier.size(56.dp)
) {
    when (destination) {
        PortalDestination.ELECTRICITY -> CustomVectorSymbol(
            symbol = VectorSymbol.ELECTRIC_GRID,
            modifier = modifier,
            primaryColor = ZummarGold,
            secondaryColor = ZummarGoldSoft
        )
        PortalDestination.WATER -> CustomVectorSymbol(
            symbol = VectorSymbol.WATER_WAVE,
            modifier = modifier,
            primaryColor = ZummarCyan,
            secondaryColor = ZummarCyanLight
        )
        PortalDestination.MEDICAL -> CustomVectorSymbol(
            symbol = VectorSymbol.PHARMACY_CHALICE,
            modifier = modifier,
            primaryColor = ZummarEmerald,
            secondaryColor = ZummarEmeraldGlow
        )
        PortalDestination.MARKET -> CustomVectorSymbol(
            symbol = VectorSymbol.STOREFRONT,
            modifier = modifier,
            primaryColor = ZummarGold,
            secondaryColor = ZummarCyanLight
        )
        PortalDestination.MUNICIPALITY -> CustomVectorSymbol(
            symbol = VectorSymbol.MUNICIPAL_ARCH,
            modifier = modifier,
            primaryColor = ZummarCyanLight,
            secondaryColor = ZummarEmerald
        )
        PortalDestination.ADMIN_PANEL -> CustomVectorSymbol(
            symbol = VectorSymbol.ADMIN_SHIELD,
            modifier = modifier,
            primaryColor = ZummarGold,
            secondaryColor = ZummarEmerald
        )
        PortalDestination.WELCOME -> CustomVectorSymbol(
            symbol = VectorSymbol.MUNICIPAL_ARCH,
            modifier = modifier,
            primaryColor = ZummarGold,
            secondaryColor = ZummarCyan
        )
    }
}

/**
 * Interactive Opening Storefront Canvas (`Interactive Opening Storefront Vector`):
 * When a citizen selects a store on the Zummar map, the storefront awning lifts and
 * its double doors smoothly slide open to reveal the illuminated shelves and products inside!
 */
@Composable
fun InteractiveOpeningStorefrontCanvas(
    isOpen: Boolean,
    storeName: String,
    productCount: Int,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(175.dp)
) {
    val openProgress by animateFloatAsState(
        targetValue = if (isOpen) 1f else 0f,
        animationSpec = tween(durationMillis = 850, easing = FastOutSlowInEasing),
        label = "storefront_open_progress"
    )
    val glowTransition = rememberInfiniteTransition(label = "interior_glow")
    val shelfShimmer by glowTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shelf_shimmer"
    )

    Canvas(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .border(1.5.dp, ZummarGold.copy(alpha = 0.55f), RoundedCornerShape(20.dp))
    ) {
        val w = size.width
        val h = size.height

        // Background night facade
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF0B1530), Color(0xFF15264E))
            )
        )

        // Store interior (revealed behind the opening doors)
        val interiorLeft = w * 0.15f
        val interiorRight = w * 0.85f
        val interiorTop = h * 0.26f
        val interiorBottom = h * 0.88f
        val interiorW = interiorRight - interiorLeft
        val interiorH = interiorBottom - interiorTop

        // Warm golden interior illumination
        drawRoundRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    ZummarGold.copy(alpha = 0.36f * openProgress * shelfShimmer),
                    ZummarCyan.copy(alpha = 0.18f * openProgress),
                    Color(0xFF0A1128)
                ),
                center = Offset(w * 0.5f, h * 0.58f),
                radius = interiorW * 0.7f
            ),
            topLeft = Offset(interiorLeft, interiorTop),
            size = Size(interiorW, interiorH),
            cornerRadius = CornerRadius(14f, 14f)
        )

        // Draw 2 interior merchandise shelves with colorful product boxes
        val shelf1Y = interiorTop + interiorH * 0.44f
        val shelf2Y = interiorTop + interiorH * 0.76f
        listOf(shelf1Y, shelf2Y).forEachIndexed { rowIdx, sy ->
            drawLine(
                color = ZummarGoldSoft.copy(alpha = 0.75f * openProgress),
                start = Offset(interiorLeft + 18f, sy),
                end = Offset(interiorRight - 18f, sy),
                strokeWidth = 4f,
                cap = StrokeCap.Round
            )
            // Merchandise items on the shelf
            val itemCount = 5
            val step = (interiorW - 56f) / itemCount
            for (i in 0 until itemCount) {
                val itemX = interiorLeft + 28f + i * step
                val itemColor = when ((i + rowIdx) % 3) {
                    0 -> ZummarGold
                    1 -> ZummarCyanLight
                    else -> ZummarEmeraldGlow
                }
                drawRoundRect(
                    color = itemColor.copy(alpha = 0.85f * openProgress),
                    topLeft = Offset(itemX, sy - 24f),
                    size = Size(step * 0.62f, 22f),
                    cornerRadius = CornerRadius(6f, 6f)
                )
            }
        }

        // Sliding Double Doors (slide outward as openProgress goes from 0 -> 1)
        val halfDoorWidth = (interiorW / 2f) * (1f - 0.78f * openProgress)
        // Left door leaf
        drawRoundRect(
            brush = Brush.horizontalGradient(
                colors = listOf(Color(0xFF1A2E5A), Color(0xFF274482))
            ),
            topLeft = Offset(interiorLeft, interiorTop),
            size = Size(halfDoorWidth, interiorH),
            cornerRadius = CornerRadius(8f, 8f)
        )
        drawRoundRect(
            color = ZummarGold.copy(alpha = 0.7f),
            topLeft = Offset(interiorLeft, interiorTop),
            size = Size(halfDoorWidth, interiorH),
            cornerRadius = CornerRadius(8f, 8f),
            style = Stroke(width = 2.5f)
        )

        // Right door leaf
        drawRoundRect(
            brush = Brush.horizontalGradient(
                colors = listOf(Color(0xFF274482), Color(0xFF1A2E5A))
            ),
            topLeft = Offset(interiorRight - halfDoorWidth, interiorTop),
            size = Size(halfDoorWidth, interiorH),
            cornerRadius = CornerRadius(8f, 8f)
        )
        drawRoundRect(
            color = ZummarGold.copy(alpha = 0.7f),
            topLeft = Offset(interiorRight - halfDoorWidth, interiorTop),
            size = Size(halfDoorWidth, interiorH),
            cornerRadius = CornerRadius(8f, 8f),
            style = Stroke(width = 2.5f)
        )

        // Animated Striped Storefront Awning at top (lifts gently when opened)
        val awningLift = h * 0.04f * openProgress
        val awningTop = h * 0.08f - awningLift
        val awningBottom = h * 0.30f - awningLift
        val stripes = 6
        val stripeW = (w * 0.78f) / stripes
        val startX = w * 0.11f

        for (s in 0 until stripes) {
            val sx = startX + s * stripeW
            val stripePath = Path().apply {
                moveTo(sx + stripeW * 0.1f, awningTop)
                lineTo(sx + stripeW * 0.9f, awningTop)
                lineTo(sx + stripeW, awningBottom)
                quadraticBezierTo(
                    sx + stripeW * 0.5f,
                    awningBottom + h * 0.07f,
                    sx,
                    awningBottom
                )
                close()
            }
            drawPath(
                path = stripePath,
                brush = Brush.verticalGradient(
                    colors = if (s % 2 == 0) {
                        listOf(ZummarGoldSoft, ZummarGoldWarm)
                    } else {
                        listOf(ZummarCyanLight, ZummarCyan)
                    }
                )
            )
        }

        // Bottom threshold step
        drawRoundRect(
            brush = Brush.horizontalGradient(listOf(ZummarGoldWarm, ZummarGold, ZummarGoldWarm)),
            topLeft = Offset(w * 0.10f, interiorBottom),
            size = Size(w * 0.80f, h * 0.06f),
            cornerRadius = CornerRadius(8f, 8f)
        )
    }
}

/**
 * Interactive Arabic Calendar Date Picker Dialog drawn with custom Canvas controls.
 */
@Composable
fun ZummarCalendarPickerDialog(
    initialDate: String,
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val monthsAr = listOf(
        "كانون الثاني", "شباط", "آذار", "نيسان", "أيار", "حزيران",
        "تموز", "آب", "أيلول", "تشرين الأول", "تشرين الثاني", "كانون الأول"
    )
    var selectedMonthIndex by remember { mutableIntStateOf(10) } // November
    var selectedDay by remember { mutableIntStateOf(16) }
    val year = 2026

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = RoyalNavySurface,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, ZummarEmerald.copy(alpha = 0.6f), RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CustomVectorSymbol(
                            symbol = VectorSymbol.CALENDAR_GRID,
                            modifier = Modifier.size(26.dp),
                            primaryColor = ZummarEmerald,
                            secondaryColor = ZummarGold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "اختيار تاريخ الموعد الطبي",
                            style = MaterialTheme.typography.titleLarge,
                            color = TextIvory
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(RoyalNavyCard)
                            .clickable { onDismiss() },
                        contentAlignment = Alignment.Center
                    ) {
                        CustomVectorSymbol(
                            symbol = VectorSymbol.CLOSE_CROSS,
                            modifier = Modifier.size(18.dp),
                            primaryColor = TextSilver,
                            secondaryColor = ZummarAlertRed,
                            animated = false
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Month Selector Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(RoyalNavyCard)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(RoyalNavyDeep)
                            .clickable {
                                selectedMonthIndex = (selectedMonthIndex - 1 + 12) % 12
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("الشهر السابق", style = MaterialTheme.typography.bodyMedium, color = ZummarCyanLight)
                    }

                    Text(
                        text = "${monthsAr[selectedMonthIndex]} $year",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ZummarGold
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(RoyalNavyDeep)
                            .clickable {
                                selectedMonthIndex = (selectedMonthIndex + 1) % 12
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("الشهر التالي", style = MaterialTheme.typography.bodyMedium, color = ZummarCyanLight)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Days of week header
                val weekDays = listOf("أحد", "إثنين", "ثلاثاء", "أربعاء", "خميس", "جمعة", "سبت")
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    weekDays.forEach { dayName ->
                        Text(
                            text = dayName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSilver,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 30 Days Grid (5 rows x 6 or 7 columns)
                for (row in 0 until 5) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        for (col in 1..6) {
                            val dayNum = row * 6 + col
                            val isSelected = dayNum == selectedDay
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 3.dp)
                                    .height(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (isSelected) ZummarEmerald else RoyalNavyCard
                                    )
                                    .border(
                                        width = if (isSelected) 1.5.dp else 0.5.dp,
                                        color = if (isSelected) ZummarGold else Color.Transparent,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable { selectedDay = dayNum },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = dayNum.toString(),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (isSelected) RoyalNavyDeep else TextIvory
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                val formatted = "$year-${(selectedMonthIndex + 1).toString().padStart(2, '0')}-${selectedDay.toString().padStart(2, '0')} (${selectedDay} ${monthsAr[selectedMonthIndex]})"
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.horizontalGradient(listOf(ZummarEmerald, ZummarCyan))
                        )
                        .clickable {
                            onDateSelected(formatted)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "اعتماد التاريخ: $formatted",
                        style = MaterialTheme.typography.labelLarge,
                        color = RoyalNavyDeep
                    )
                }
            }
        }
    }
}
