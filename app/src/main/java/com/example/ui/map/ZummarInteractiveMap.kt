package com.example.ui.map

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.data.FacilityDocument
import com.example.data.NormalizedPoint
import com.example.data.StoreDocument
import com.example.data.ZoneDocument
import com.example.ui.components.CustomVectorSymbol
import com.example.ui.components.VectorSymbol
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
import kotlin.math.hypot
import kotlin.math.sin

enum class MapVisualizationMode {
    ELECTRICITY,
    WATER,
    MEDICAL,
    MARKET
}

enum class ZoneDrawingMode {
    NONE,
    POLYGON_POINTS,
    FREEHAND_LASSO,
    PICK_LOCATION
}

/**
 * Interactive Zummar Sub-District Map Engine (`Interactive Polygon & Freehand Zone Map`):
 * - Supports smooth Zoom & Pan.
 * - Renders Zummar landmarks (Tigris River bend, Zummar Center Highway, Agricultural Road, etc.).
 * - Supports Polygon Point tapping & Freehand Lasso drawing to create new neighborhood zones.
 * - Renders live `Flowing Electric Curves` in ELECTRICITY mode.
 * - Renders live `Flowing Water Waves Canvas` clipped inside polygons in WATER mode.
 * - Renders custom curved medical pins + `Pulsing On-Duty Aura` radar in MEDICAL mode.
 * - Renders custom storefront pins in MARKET mode.
 */
@Composable
fun ZummarInteractiveMap(
    mode: MapVisualizationMode,
    zones: List<ZoneDocument>,
    facilities: List<FacilityDocument>,
    stores: List<StoreDocument>,
    selectedZoneId: String?,
    selectedFacilityId: String?,
    selectedStoreId: String?,
    drawingMode: ZoneDrawingMode,
    draftPoints: List<NormalizedPoint>,
    pickedPoint: NormalizedPoint?,
    onSelectZone: (ZoneDocument) -> Unit,
    onSelectFacility: (FacilityDocument) -> Unit,
    onSelectStore: (StoreDocument) -> Unit,
    onAddDraftPoint: (NormalizedPoint) -> Unit,
    onSetDraftPoints: (List<NormalizedPoint>) -> Unit,
    onPickLocation: (NormalizedPoint) -> Unit,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(370.dp)
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }
    var canvasSize by remember { mutableStateOf(IntSize(1, 1)) }

    val transition = rememberInfiniteTransition(label = "zummar_map_live_anim")
    val flowPhase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "flow_phase"
    )
    val radarPhase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radar_phase"
    )
    val auraGlow by transition.animateFloat(
        initialValue = 0.65f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "aura_glow"
    )

    fun screenToNormalized(screenPt: Offset): NormalizedPoint {
        val w = canvasSize.width.coerceAtLeast(1).toFloat()
        val h = canvasSize.height.coerceAtLeast(1).toFloat()
        val cx = w / 2f
        val cy = h / 2f
        val mapX = ((screenPt.x - panOffset.x - cx) / scale + cx) / w
        val mapY = ((screenPt.y - panOffset.y - cy) / scale + cy) / h
        return NormalizedPoint(mapX.coerceIn(0.03f, 0.97f), mapY.coerceIn(0.03f, 0.97f))
    }

    fun normalizedToScreen(normPt: NormalizedPoint, w: Float, h: Float): Offset {
        val cx = w / 2f
        val cy = h / 2f
        val rawX = normPt.x * w
        val rawY = normPt.y * h
        val sx = (rawX - cx) * scale + cx + panOffset.x
        val sy = (rawY - cy) * scale + cy + panOffset.y
        return Offset(sx, sy)
    }

    fun buildSmoothClosedPolygonPath(points: List<NormalizedPoint>, w: Float, h: Float): Path {
        val path = Path()
        if (points.isEmpty()) return path
        val screenPts = points.map { normalizedToScreen(it, w, h) }
        if (screenPts.size < 3) {
            path.moveTo(screenPts.first().x, screenPts.first().y)
            screenPts.drop(1).forEach { path.lineTo(it.x, it.y) }
            return path
        }
        val firstMid = Offset(
            (screenPts.last().x + screenPts.first().x) / 2f,
            (screenPts.last().y + screenPts.first().y) / 2f
        )
        path.moveTo(firstMid.x, firstMid.y)
        for (i in screenPts.indices) {
            val current = screenPts[i]
            val next = screenPts[(i + 1) % screenPts.size]
            val mid = Offset((current.x + next.x) / 2f, (current.y + next.y) / 2f)
            path.quadraticBezierTo(current.x, current.y, mid.x, mid.y)
        }
        path.close()
        return path
    }

    fun isPointInsidePolygon(pt: NormalizedPoint, poly: List<NormalizedPoint>): Boolean {
        if (poly.size < 3) return false
        var inside = false
        var j = poly.size - 1
        for (i in poly.indices) {
            val xi = poly[i].x
            val yi = poly[i].y
            val xj = poly[j].x
            val yj = poly[j].y
            val intersect = ((yi > pt.y) != (yj > pt.y)) &&
                (pt.x < (xj - xi) * (pt.y - yi) / ((yj - yi).let { if (it == 0f) 0.0001f else it }) + xi)
            if (intersect) inside = !inside
            j = i
        }
        return inside
    }

    val labelPaint = remember {
        Paint().apply {
            isAntiAlias = true
            color = android.graphics.Color.WHITE
            textSize = 27f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            setShadowLayer(8f, 0f, 2f, android.graphics.Color.BLACK)
        }
    }

    val subLabelPaint = remember {
        Paint().apply {
            isAntiAlias = true
            color = android.graphics.Color.parseColor("#CBD5E1")
            textSize = 21f
            textAlign = Paint.Align.CENTER
            setShadowLayer(6f, 0f, 1f, android.graphics.Color.BLACK)
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(RoyalNavyDeep)
            .border(
                width = 1.5.dp,
                brush = Brush.linearGradient(
                    colors = when (mode) {
                        MapVisualizationMode.ELECTRICITY -> listOf(ZummarGold, ZummarCyan.copy(alpha = 0.5f))
                        MapVisualizationMode.WATER -> listOf(ZummarCyan, ZummarCyanLight)
                        MapVisualizationMode.MEDICAL -> listOf(ZummarEmerald, ZummarCyan)
                        MapVisualizationMode.MARKET -> listOf(ZummarGold, ZummarEmerald)
                    }
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .testTag("zummar_interactive_map")
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { canvasSize = it }
                .pointerInput(drawingMode) {
                    if (drawingMode == ZoneDrawingMode.NONE) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            val newScale = (scale * zoom).coerceIn(0.85f, 3.2f)
                            scale = newScale
                            val maxPanX = size.width * 0.65f * newScale
                            val maxPanY = size.height * 0.65f * newScale
                            panOffset = Offset(
                                (panOffset.x + pan.x).coerceIn(-maxPanX, maxPanX),
                                (panOffset.y + pan.y).coerceIn(-maxPanY, maxPanY)
                            )
                        }
                    } else if (drawingMode == ZoneDrawingMode.FREEHAND_LASSO) {
                        val lassoPts = mutableListOf<NormalizedPoint>()
                        detectDragGestures(
                            onDragStart = { startOffset ->
                                lassoPts.clear()
                                val norm = screenToNormalized(startOffset)
                                lassoPts.add(norm)
                                onSetDraftPoints(lassoPts.toList())
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                val norm = screenToNormalized(change.position)
                                val last = lassoPts.lastOrNull()
                                if (last == null || hypot(norm.x - last.x, norm.y - last.y) > 0.028f) {
                                    lassoPts.add(norm)
                                    onSetDraftPoints(lassoPts.toList())
                                }
                            }
                        )
                    }
                }
                .pointerInput(drawingMode, zones, facilities, stores, mode, scale, panOffset) {
                    detectTapGestures { tapOffset ->
                        val normTap = screenToNormalized(tapOffset)
                        when (drawingMode) {
                            ZoneDrawingMode.POLYGON_POINTS -> {
                                onAddDraftPoint(normTap)
                            }
                            ZoneDrawingMode.PICK_LOCATION -> {
                                onPickLocation(normTap)
                            }
                            ZoneDrawingMode.FREEHAND_LASSO -> {
                                onAddDraftPoint(normTap)
                            }
                            ZoneDrawingMode.NONE -> {
                                // Check pin hits first in MEDICAL or MARKET mode
                                if (mode == MapVisualizationMode.MEDICAL) {
                                    val hitFacility = facilities.minByOrNull { f ->
                                        hypot(f.mapX.toFloat() - normTap.x, f.mapY.toFloat() - normTap.y)
                                    }
                                    if (hitFacility != null &&
                                        hypot(hitFacility.mapX.toFloat() - normTap.x, hitFacility.mapY.toFloat() - normTap.y) < 0.085f
                                    ) {
                                        onSelectFacility(hitFacility)
                                        return@detectTapGestures
                                    }
                                }
                                if (mode == MapVisualizationMode.MARKET) {
                                    val hitStore = stores.minByOrNull { s ->
                                        hypot(s.mapX.toFloat() - normTap.x, s.mapY.toFloat() - normTap.y)
                                    }
                                    if (hitStore != null &&
                                        hypot(hitStore.mapX.toFloat() - normTap.x, hitStore.mapY.toFloat() - normTap.y) < 0.085f
                                    ) {
                                        onSelectStore(hitStore)
                                        return@detectTapGestures
                                    }
                                }
                                // Check neighborhood polygon hit
                                val hitZone = zones.firstOrNull { z ->
                                    isPointInsidePolygon(normTap, z.parsePoints())
                                } ?: zones.minByOrNull { z ->
                                    val c = z.centerPoint()
                                    hypot(c.x - normTap.x, c.y - normTap.y)
                                }?.takeIf { z ->
                                    val c = z.centerPoint()
                                    hypot(c.x - normTap.x, c.y - normTap.y) < 0.14f
                                }

                                if (hitZone != null) {
                                    onSelectZone(hitZone)
                                }
                            }
                        }
                    }
                }
        ) {
            val w = size.width
            val h = size.height

            // 1. Night Satellite-Styled Topographic Cartography Background
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF132247), Color(0xFF0A1128), Color(0xFF060B19)),
                    center = Offset(w * 0.5f, h * 0.5f),
                    radius = w * 0.85f
                )
            )

            // Subtle coordinate grid lines
            val gridCount = 8
            for (i in 1 until gridCount) {
                val gx = normalizedToScreen(NormalizedPoint(i / gridCount.toFloat(), 0f), w, h).x
                val gy = normalizedToScreen(NormalizedPoint(0f, i / gridCount.toFloat()), w, h).y
                drawLine(
                    color = ZummarCyan.copy(alpha = 0.08f),
                    start = Offset(gx, 0f),
                    end = Offset(gx, h),
                    strokeWidth = 1f
                )
                drawLine(
                    color = ZummarCyan.copy(alpha = 0.08f),
                    start = Offset(0f, gy),
                    end = Offset(w, gy),
                    strokeWidth = 1f
                )
            }

            // 2. Tigris River & Mosul Dam Lake Bend along North-East of Zummar
            val riverPath = Path().apply {
                val r0 = normalizedToScreen(NormalizedPoint(0.02f, 0.14f), w, h)
                val r1 = normalizedToScreen(NormalizedPoint(0.35f, 0.08f), w, h)
                val r2 = normalizedToScreen(NormalizedPoint(0.68f, 0.18f), w, h)
                val r3 = normalizedToScreen(NormalizedPoint(0.98f, 0.12f), w, h)
                moveTo(r0.x, r0.y)
                cubicTo(r1.x, r1.y, r2.x, r2.y, r3.x, r3.y)
            }
            drawPath(
                path = riverPath,
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFF005082).copy(alpha = 0.65f), ZummarCyan.copy(alpha = 0.55f), Color(0xFF0077B6).copy(alpha = 0.65f))
                ),
                style = Stroke(width = 24f * scale, cap = StrokeCap.Round)
            )
            drawPath(
                path = riverPath,
                color = ZummarCyanLight.copy(alpha = 0.45f),
                style = Stroke(width = 4f * scale, cap = StrokeCap.Round)
            )

            // 3. Main Zummar Arterial Roads (Highway through Center Zummar, Ain Helwa, Talul Al-Baq)
            val mainRoadPath = Path().apply {
                val p0 = normalizedToScreen(NormalizedPoint(0.04f, 0.55f), w, h)
                val p1 = normalizedToScreen(NormalizedPoint(0.45f, 0.48f), w, h)
                val p2 = normalizedToScreen(NormalizedPoint(0.96f, 0.52f), w, h)
                moveTo(p0.x, p0.y)
                quadraticBezierTo(p1.x, p1.y, p2.x, p2.y)

                val v0 = normalizedToScreen(NormalizedPoint(0.48f, 0.16f), w, h)
                val v1 = normalizedToScreen(NormalizedPoint(0.50f, 0.50f), w, h)
                val v2 = normalizedToScreen(NormalizedPoint(0.54f, 0.94f), w, h)
                moveTo(v0.x, v0.y)
                quadraticBezierTo(v1.x, v1.y, v2.x, v2.y)
            }
            drawPath(
                path = mainRoadPath,
                color = Color(0xFF283E6B).copy(alpha = 0.75f),
                style = Stroke(width = 9f * scale, cap = StrokeCap.Round)
            )
            drawPath(
                path = mainRoadPath,
                color = ZummarGold.copy(alpha = 0.25f),
                style = Stroke(
                    width = 2f * scale,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 12f), 0f)
                )
            )

            // Landmark labels on map
            val riverLabelPos = normalizedToScreen(NormalizedPoint(0.28f, 0.11f), w, h)
            drawContext.canvas.nativeCanvas.drawText(
                "ضفاف دجلة - بحيرة زمار",
                riverLabelPos.x,
                riverLabelPos.y,
                subLabelPaint
            )

            // 4. Draw All Neighborhood Polygon Zones
            zones.forEach { zone ->
                val pts = zone.parsePoints()
                if (pts.size >= 3) {
                    val polyPath = buildSmoothClosedPolygonPath(pts, w, h)
                    val centerScreen = normalizedToScreen(zone.centerPoint(), w, h)
                    val isSelected = zone.id == selectedZoneId

                    when (mode) {
                        MapVisualizationMode.ELECTRICITY -> {
                            val isPowerOn = zone.powerStatus == "NATIONAL" || zone.powerStatus == "GENERATOR"
                            val isFault = zone.powerStatus == "FAULT"
                            val baseColor = when (zone.powerStatus) {
                                "NATIONAL" -> ZummarGold
                                "GENERATOR" -> Color(0xFFFF9F1C)
                                "FAULT" -> ZummarAlertRed
                                else -> Color(0xFF334155)
                            }

                            // Fill polygon with glowing gradient when ON
                            drawPath(
                                path = polyPath,
                                brush = Brush.radialGradient(
                                    colors = if (isPowerOn) {
                                        listOf(
                                            baseColor.copy(alpha = 0.46f * auraGlow),
                                            baseColor.copy(alpha = 0.18f)
                                        )
                                    } else if (isFault) {
                                        listOf(
                                            ZummarAlertRed.copy(alpha = 0.42f * auraGlow),
                                            ZummarAlertRed.copy(alpha = 0.14f)
                                        )
                                    } else {
                                        listOf(
                                            Color(0xFF1E293B).copy(alpha = 0.45f),
                                            Color(0xFF0F172A).copy(alpha = 0.35f)
                                        )
                                    },
                                    center = centerScreen,
                                    radius = (w * 0.24f * scale).coerceAtLeast(20f)
                                )
                            )

                            // Flowing Electric Curves inside the polygon when power is ON
                            if (isPowerOn) {
                                clipPath(polyPath) {
                                    for (idx in 0..2) {
                                        val offsetY = (idx - 1) * 22f * scale
                                        val waveShift = sin((flowPhase + idx * 0.3f) * 2 * PI).toFloat() * 18f * scale
                                        val electricWave = Path().apply {
                                            moveTo(centerScreen.x - 130f * scale, centerScreen.y + offsetY)
                                            cubicTo(
                                                centerScreen.x - 45f * scale,
                                                centerScreen.y + offsetY - waveShift,
                                                centerScreen.x + 45f * scale,
                                                centerScreen.y + offsetY + waveShift,
                                                centerScreen.x + 130f * scale,
                                                centerScreen.y + offsetY
                                            )
                                        }
                                        drawPath(
                                            path = electricWave,
                                            brush = Brush.horizontalGradient(
                                                colors = listOf(
                                                    Color.Transparent,
                                                    ZummarGoldSoft.copy(alpha = 0.85f),
                                                    ZummarGold,
                                                    Color.Transparent
                                                )
                                            ),
                                            style = Stroke(width = 3.2f * scale, cap = StrokeCap.Round)
                                        )
                                    }
                                }
                            }

                            // Animated boundary current pulses (`Flowing Electric Curves`)
                            drawPath(
                                path = polyPath,
                                color = if (isSelected) Color.White else baseColor.copy(alpha = 0.85f),
                                style = Stroke(
                                    width = if (isSelected) 4.5f * scale else 2.8f * scale,
                                    pathEffect = if (isPowerOn) {
                                        PathEffect.dashPathEffect(
                                            floatArrayOf(22f * scale, 10f * scale),
                                            phase = -flowPhase * 120f * scale
                                        )
                                    } else null
                                )
                            )
                        }

                        MapVisualizationMode.WATER -> {
                            val isPumping = zone.waterStatus == "PUMPING"
                            val isMaint = zone.waterStatus == "MAINTENANCE"
                            val waterColor = when (zone.waterStatus) {
                                "PUMPING" -> ZummarCyan
                                "MAINTENANCE" -> ZummarGoldWarm
                                else -> Color(0xFF334155)
                            }

                            drawPath(
                                path = polyPath,
                                brush = Brush.radialGradient(
                                    colors = if (isPumping) {
                                        listOf(
                                            ZummarCyanLight.copy(alpha = 0.42f * auraGlow),
                                            ZummarCyan.copy(alpha = 0.20f)
                                        )
                                    } else if (isMaint) {
                                        listOf(
                                            ZummarGoldWarm.copy(alpha = 0.38f * auraGlow),
                                            ZummarAlertRed.copy(alpha = 0.15f)
                                        )
                                    } else {
                                        listOf(
                                            Color(0xFF1E293B).copy(alpha = 0.45f),
                                            Color(0xFF0F172A).copy(alpha = 0.35f)
                                        )
                                    },
                                    center = centerScreen,
                                    radius = (w * 0.24f * scale).coerceAtLeast(20f)
                                )
                            )

                            // Multi-layer Flowing Water Waves Canvas inside the active sector
                            if (isPumping) {
                                clipPath(polyPath) {
                                    for (waveIdx in -2..2) {
                                        val baseY = centerScreen.y + waveIdx * 18f * scale
                                        val amp = 14f * scale * sin((flowPhase + waveIdx * 0.2f) * 2 * PI).toFloat()
                                        val waterWavePath = Path().apply {
                                            moveTo(centerScreen.x - 140f * scale, baseY)
                                            quadraticBezierTo(
                                                centerScreen.x - 70f * scale,
                                                baseY - amp,
                                                centerScreen.x,
                                                baseY
                                            )
                                            quadraticBezierTo(
                                                centerScreen.x + 70f * scale,
                                                baseY + amp,
                                                centerScreen.x + 140f * scale,
                                                baseY
                                            )
                                        }
                                        drawPath(
                                            path = waterWavePath,
                                            brush = Brush.horizontalGradient(
                                                colors = listOf(
                                                    ZummarCyan.copy(alpha = 0.25f),
                                                    ZummarCyanLight.copy(alpha = 0.85f),
                                                    Color(0xFF90E0EF).copy(alpha = 0.85f),
                                                    ZummarCyan.copy(alpha = 0.25f)
                                                )
                                            ),
                                            style = Stroke(width = 3.5f * scale, cap = StrokeCap.Round)
                                        )
                                    }
                                }
                            }

                            drawPath(
                                path = polyPath,
                                color = if (isSelected) Color.White else waterColor.copy(alpha = 0.9f),
                                style = Stroke(width = if (isSelected) 4.5f * scale else 2.8f * scale)
                            )
                        }

                        else -> {
                            // Subtle neighborhood boundaries in Medical / Market mode
                            drawPath(
                                path = polyPath,
                                color = ZummarCyan.copy(alpha = 0.10f)
                            )
                            drawPath(
                                path = polyPath,
                                color = ZummarCyan.copy(alpha = 0.30f),
                                style = Stroke(width = 1.5f * scale)
                            )
                        }
                    }

                    // Draw Neighborhood Title & Status Pill
                    drawContext.canvas.nativeCanvas.drawText(
                        zone.name,
                        centerScreen.x,
                        centerScreen.y - 6f * scale,
                        labelPaint
                    )
                    val statusLabel = when (mode) {
                        MapVisualizationMode.ELECTRICITY -> when (zone.powerStatus) {
                            "NATIONAL" -> "كهرباء وطنية"
                            "GENERATOR" -> "مولدة أهلية"
                            "FAULT" -> "خلل / صيانة"
                            else -> "منطفئة"
                        }
                        MapVisualizationMode.WATER -> when (zone.waterStatus) {
                            "PUMPING" -> "الضخ يعمل الآن"
                            "MAINTENANCE" -> "صيانة أنبوب"
                            else -> "متوقف حالياً"
                        }
                        else -> ""
                    }
                    if (statusLabel.isNotEmpty()) {
                        drawContext.canvas.nativeCanvas.drawText(
                            statusLabel,
                            centerScreen.x,
                            centerScreen.y + 20f * scale,
                            subLabelPaint
                        )
                    }
                }
            }

            // 5. Draw Medical Facilities on Map (in MEDICAL mode)
            if (mode == MapVisualizationMode.MEDICAL) {
                facilities.forEach { facility ->
                    val pos = normalizedToScreen(
                        NormalizedPoint(facility.mapX.toFloat(), facility.mapY.toFloat()),
                        w,
                        h
                    )
                    val isSelected = facility.id == selectedFacilityId
                    val pinColor = when (facility.facilityType) {
                        "PHARMACY" -> ZummarEmerald
                        "CLINIC" -> ZummarCyanLight
                        else -> ZummarGold
                    }

                    // Pulsing On-Duty Radar Aura for On-Duty Pharmacies (`Pulsing On-Duty Aura`)
                    if (facility.facilityType == "PHARMACY" && facility.isOnDuty) {
                        val maxRadius = 56f * scale
                        drawCircle(
                            color = ZummarEmeraldGlow.copy(alpha = (1f - radarPhase) * 0.65f),
                            radius = 16f * scale + maxRadius * radarPhase,
                            center = pos,
                            style = Stroke(width = 3.5f * scale)
                        )
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    ZummarEmerald.copy(alpha = 0.50f * auraGlow),
                                    Color.Transparent
                                ),
                                center = pos,
                                radius = 42f * scale
                            ),
                            radius = 42f * scale,
                            center = pos
                        )
                    }

                    // Custom curved teardrop map pin drawn via Path
                    val pinH = 36f * scale
                    val pinW = 26f * scale
                    val pinPath = Path().apply {
                        moveTo(pos.x, pos.y)
                        cubicTo(
                            pos.x - pinW, pos.y - pinH * 0.55f,
                            pos.x - pinW * 0.85f, pos.y - pinH,
                            pos.x, pos.y - pinH
                        )
                        cubicTo(
                            pos.x + pinW * 0.85f, pos.y - pinH,
                            pos.x + pinW, pos.y - pinH * 0.55f,
                            pos.x, pos.y
                        )
                        close()
                    }
                    drawPath(
                        path = pinPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(pinColor, RoyalNavyDeep)
                        )
                    )
                    drawPath(
                        path = pinPath,
                        color = if (isSelected) Color.White else pinColor,
                        style = Stroke(width = if (isSelected) 3.5f else 2f)
                    )

                    // Inner custom emblem inside the pin head
                    val headCenter = Offset(pos.x, pos.y - pinH * 0.62f)
                    val emblemPath = Path().apply {
                        when (facility.facilityType) {
                            "PHARMACY" -> {
                                // Medical chalice curve
                                moveTo(headCenter.x - 6f * scale, headCenter.y - 4f * scale)
                                quadraticBezierTo(headCenter.x, headCenter.y + 6f * scale, headCenter.x + 6f * scale, headCenter.y - 4f * scale)
                                moveTo(headCenter.x, headCenter.y + 2f * scale)
                                lineTo(headCenter.x, headCenter.y + 8f * scale)
                            }
                            "CLINIC" -> {
                                // Heartbeat pulse wave
                                moveTo(headCenter.x - 8f * scale, headCenter.y)
                                lineTo(headCenter.x - 3f * scale, headCenter.y - 6f * scale)
                                lineTo(headCenter.x + 2f * scale, headCenter.y + 6f * scale)
                                lineTo(headCenter.x + 8f * scale, headCenter.y)
                            }
                            else -> {
                                // Analytical lab flask
                                moveTo(headCenter.x - 3f * scale, headCenter.y - 5f * scale)
                                lineTo(headCenter.x - 6f * scale, headCenter.y + 5f * scale)
                                lineTo(headCenter.x + 6f * scale, headCenter.y + 5f * scale)
                                lineTo(headCenter.x + 3f * scale, headCenter.y - 5f * scale)
                            }
                        }
                    }
                    drawPath(
                        path = emblemPath,
                        color = Color.White,
                        style = Stroke(width = 2.2f * scale, cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )

                    drawContext.canvas.nativeCanvas.drawText(
                        facility.name,
                        pos.x,
                        pos.y + 24f * scale,
                        labelPaint
                    )
                }
            }

            // 6. Draw Commercial Storefronts on Map (in MARKET mode)
            if (mode == MapVisualizationMode.MARKET) {
                stores.forEach { store ->
                    val pos = normalizedToScreen(
                        NormalizedPoint(store.mapX.toFloat(), store.mapY.toFloat()),
                        w,
                        h
                    )
                    val isSelected = store.id == selectedStoreId

                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(ZummarGold.copy(alpha = 0.45f * auraGlow), Color.Transparent),
                            center = pos,
                            radius = 38f * scale
                        ),
                        radius = 38f * scale,
                        center = pos
                    )

                    // Custom curved storefront pin
                    val bw = 22f * scale
                    val bh = 20f * scale
                    val storePinPath = Path().apply {
                        moveTo(pos.x - bw, pos.y - bh)
                        quadraticBezierTo(pos.x, pos.y - bh * 1.55f, pos.x + bw, pos.y - bh)
                        lineTo(pos.x + bw * 0.85f, pos.y + bh * 0.4f)
                        lineTo(pos.x, pos.y + bh * 0.9f)
                        lineTo(pos.x - bw * 0.85f, pos.y + bh * 0.4f)
                        close()
                    }
                    drawPath(
                        path = storePinPath,
                        brush = Brush.verticalGradient(listOf(ZummarGoldSoft, ZummarGoldWarm))
                    )
                    drawPath(
                        path = storePinPath,
                        color = if (isSelected) Color.White else RoyalNavyDeep,
                        style = Stroke(width = if (isSelected) 3.5f else 2f)
                    )

                    drawContext.canvas.nativeCanvas.drawText(
                        store.name,
                        pos.x,
                        pos.y + bh + 22f * scale,
                        labelPaint
                    )
                }
            }

            // 7. Active Draft Polygon / Freehand Lasso Overlay
            if (draftPoints.isNotEmpty()) {
                val draftPath = buildSmoothClosedPolygonPath(draftPoints, w, h)
                drawPath(
                    path = draftPath,
                    color = ZummarGold.copy(alpha = 0.28f)
                )
                drawPath(
                    path = draftPath,
                    color = ZummarGoldSoft,
                    style = Stroke(
                        width = 3.5f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 8f), 0f)
                    )
                )
                draftPoints.forEach { pt ->
                    val sPt = normalizedToScreen(pt, w, h)
                    drawCircle(color = ZummarGold, radius = 7f, center = sPt)
                    drawCircle(color = RoyalNavyDeep, radius = 3.5f, center = sPt)
                }
            }

            // 8. Picked Pin Location Overlay (when registering a Pharmacy, Clinic, Lab, or Store)
            if (pickedPoint != null && drawingMode == ZoneDrawingMode.PICK_LOCATION) {
                val sPt = normalizedToScreen(pickedPoint, w, h)
                drawCircle(
                    color = ZummarEmeraldGlow.copy(alpha = 0.4f * auraGlow),
                    radius = 28f,
                    center = sPt
                )
                drawCircle(color = ZummarEmerald, radius = 10f, center = sPt)
                drawCircle(color = Color.White, radius = 4f, center = sPt)
            }
        }

        // Top-Left Zoom & Reset Map Controls (Custom Canvas Vectors)
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(RoyalNavySurface.copy(alpha = 0.90f))
                    .border(1.dp, ZummarCyan.copy(alpha = 0.5f), CircleShape)
                    .clickable { scale = (scale * 1.25f).coerceAtMost(3.2f) }
                    .testTag("map_zoom_in_button"),
                contentAlignment = Alignment.Center
            ) {
                CustomVectorSymbol(
                    symbol = VectorSymbol.ZOOM_IN,
                    modifier = Modifier.size(22.dp),
                    primaryColor = ZummarCyanLight,
                    secondaryColor = ZummarGold,
                    animated = false
                )
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(RoyalNavySurface.copy(alpha = 0.90f))
                    .border(1.dp, ZummarCyan.copy(alpha = 0.5f), CircleShape)
                    .clickable { scale = (scale / 1.25f).coerceAtLeast(0.85f) }
                    .testTag("map_zoom_out_button"),
                contentAlignment = Alignment.Center
            ) {
                CustomVectorSymbol(
                    symbol = VectorSymbol.ZOOM_OUT,
                    modifier = Modifier.size(22.dp),
                    primaryColor = ZummarCyanLight,
                    secondaryColor = ZummarGold,
                    animated = false
                )
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(RoyalNavySurface.copy(alpha = 0.90f))
                    .border(1.dp, ZummarGold.copy(alpha = 0.5f), CircleShape)
                    .clickable {
                        scale = 1f
                        panOffset = Offset.Zero
                    }
                    .testTag("map_reset_button"),
                contentAlignment = Alignment.Center
            ) {
                CustomVectorSymbol(
                    symbol = VectorSymbol.RESET_COMPASS,
                    modifier = Modifier.size(22.dp),
                    primaryColor = ZummarGold,
                    secondaryColor = ZummarCyanLight,
                    animated = false
                )
            }
        }

        // Bottom Map Status & Drawing Instruction Banner
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, RoyalNavyDeep.copy(alpha = 0.92f))
                    )
                )
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val helperText = when (drawingMode) {
                ZoneDrawingMode.POLYGON_POINTS -> "انقر على الخريطة لتحديد زوايا الحي (${draftPoints.size} نقاط محددة)"
                ZoneDrawingMode.FREEHAND_LASSO -> "مرر إصبعك بحرية حول البقعة الجغرافية لرسم حدود الحي"
                ZoneDrawingMode.PICK_LOCATION -> "انقر على الموقع الدقيق في خريطة زمار لتثبيت الإحداثيات"
                ZoneDrawingMode.NONE -> when (mode) {
                    MapVisualizationMode.ELECTRICITY -> "انقر على أي حي لعرض حالة الكهرباء الوطنية والمولدة والأعطال"
                    MapVisualizationMode.WATER -> "انقر على أي قاطع لعرض حالة ضخ المياه والجداول والصيانة"
                    MapVisualizationMode.MEDICAL -> "انقر على الصيدلية أو العيادة أو المختبر للحجز والمراسلة"
                    MapVisualizationMode.MARKET -> "انقر على أي محل لفتح واجهته التفاعلية وتصفح بضائعه"
                }
            }
            Text(
                text = helperText,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSilver
            )
        }
    }
}
