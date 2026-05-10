package com.enigma.fluffyinc.ui.theme

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.*

// ═══════════════════════════════════════════════════════════
// IMPROVED: SEPTEMBER — Richly textured wood grain
// ═══════════════════════════════════════════════════════════

@Composable
fun September(modifier: Modifier = Modifier) {
    val darkBark   = Color(0xFF4A2408)
    val midWood    = Color(0xFF7B3A14)
    val lightWood  = Color(0xFFB05A28)
    val highlight  = Color(0xFFD4834A)
    val knot       = Color(0xFF3A1A06)

    Box(
        modifier = modifier
            .size(64.dp)
            .aspectRatio(1f)
            .clip(RoundedCornerShape(6.dp))
            .background(midWood)
            .drawWithContent {
                // Base gradient — simulates end-grain depth
                drawRect(
                    brush = Brush.linearGradient(
                        0.00f to darkBark,
                        0.18f to lightWood,
                        0.35f to midWood,
                        0.55f to highlight,
                        0.72f to midWood,
                        0.88f to darkBark,
                        1.00f to midWood,
                        start = Offset(0f, 0f),
                        end   = Offset(size.width * 1.2f, size.height)
                    )
                )

                // Long grain lines — varying width and opacity
                val grainColors = listOf(
                    Color(0xFF3A1A06) to 0.25f,
                    Color(0xFF6B3010) to 0.15f,
                    Color(0xFFCC7040) to 0.20f,
                    Color(0xFF3A1A06) to 0.18f,
                    Color(0xFFAA5A28) to 0.12f,
                )
                val spacings = listOf(7f, 13f, 19f, 10f, 16f, 8f, 22f, 11f, 17f)
                var x = 0f
                spacings.forEachIndexed { i, gap ->
                    x += gap
                    val (color, alpha) = grainColors[i % grainColors.size]
                    val wobble = sin(i * 1.3f) * 3f
                    drawLine(
                        color = color.copy(alpha = alpha),
                        start = Offset(x + wobble, 0f),
                        end   = Offset(x - wobble + 2f, size.height),
                        strokeWidth = if (i % 3 == 0) 2.5f else 1f
                    )
                }

                // Knot circle
                drawCircle(
                    brush = Brush.radialGradient(
                        0.0f to knot,
                        0.4f to midWood.copy(alpha = 0.8f),
                        1.0f to Color.Transparent,
                        center = Offset(size.width * 0.65f, size.height * 0.38f),
                        radius = 10f
                    ),
                    center = Offset(size.width * 0.65f, size.height * 0.38f),
                    radius = 10f
                )

                // Subtle varnish sheen
                drawRect(
                    brush = Brush.verticalGradient(
                        0.0f to Color.White.copy(alpha = 0.12f),
                        0.3f to Color.Transparent,
                        1.0f to Color.Black.copy(alpha = 0.08f)
                    )
                )

                drawContent()
            }
    )
}

// ═══════════════════════════════════════════════════════════
// IMPROVED: ICICLE — Multi-layered ice with internal caustics
// ═══════════════════════════════════════════════════════════

@Composable
fun Icicle(modifier: Modifier = Modifier) {
    val frostWhite = Color(0xFFF0FAFF)
    val paleCyan   = Color(0xFFB3E8F8)
    val deepGlacier = Color(0xFF5BB8D4)
    val innerBlue  = Color(0xFF2196C0)

    Box(
        modifier = modifier
            .size(64.dp)
            .aspectRatio(1f)
            .clip(RoundedCornerShape(10.dp))
            .background(paleCyan)
            .drawWithContent {
                // Deep ice base
                drawRect(
                    brush = Brush.verticalGradient(
                        0.0f to frostWhite,
                        0.4f to paleCyan,
                        1.0f to deepGlacier
                    )
                )

                // Internal refraction bands — diagonal light scatter
                for (i in 0..3) {
                    val offset = i * 18f
                    drawLine(
                        brush = Brush.linearGradient(
                            0f to Color.White.copy(alpha = 0f),
                            0.5f to Color.White.copy(alpha = 0.35f),
                            1f to Color.White.copy(alpha = 0f)
                        ),
                        start = Offset(offset, 0f),
                        end   = Offset(offset + size.width * 0.5f, size.height),
                        strokeWidth = 6f + i * 2f
                    )
                }

                // Bubble inclusions
                val bubbles = listOf(
                    Offset(size.width * 0.25f, size.height * 0.55f) to 3f,
                    Offset(size.width * 0.72f, size.height * 0.30f) to 2f,
                    Offset(size.width * 0.55f, size.height * 0.72f) to 4f,
                    Offset(size.width * 0.15f, size.height * 0.25f) to 1.5f,
                )
                bubbles.forEach { (center, r) ->
                    drawCircle(
                        color = innerBlue.copy(alpha = 0.25f),
                        center = center, radius = r,
                        style = Stroke(width = 1f)
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.4f),
                        center = Offset(center.x - r * 0.3f, center.y - r * 0.3f),
                        radius = r * 0.4f
                    )
                }

                // Gloss highlight
                drawPath(
                    path = Path().apply {
                        moveTo(0f, 0f)
                        lineTo(size.width * 0.7f, 0f)
                        lineTo(0f, size.height * 0.6f)
                        close()
                    },
                    color = Color.White.copy(alpha = 0.28f)
                )

                // Bottom depth shadow
                drawRect(
                    brush = Brush.verticalGradient(
                        0.6f to Color.Transparent,
                        1.0f to innerBlue.copy(alpha = 0.3f)
                    )
                )

                drawContent()
            }
            .border(1.dp, Color.White.copy(alpha = 0.7f), RoundedCornerShape(10.dp))
    )
}

// ═══════════════════════════════════════════════════════════
// NEW: OBSIDIAN — Volcanic glass, dark with rainbow sheen
// ═══════════════════════════════════════════════════════════

@Composable
fun Obsidian(modifier: Modifier = Modifier) {
    val baseBlack  = Color(0xFF0D0D0F)
    val deepPurple = Color(0xFF1A0A2E)
    val shimmer1   = Color(0xFF6A0080)
    val shimmer2   = Color(0xFF1A237E)
    val shimmer3   = Color(0xFF006064)
    val glassEdge  = Color(0xFF4A4A6A)

    Box(
        modifier = modifier
            .size(64.dp)
            .aspectRatio(1f)
            .clip(RoundedCornerShape(4.dp))
            .background(baseBlack)
            .drawWithContent {
                // Dark volcanic base
                drawRect(
                    brush = Brush.linearGradient(
                        0.0f to baseBlack,
                        0.4f to deepPurple,
                        0.7f to Color(0xFF0D1A2E),
                        1.0f to baseBlack,
                        start = Offset(0f, 0f),
                        end   = Offset(size.width, size.height)
                    )
                )

                // Iridescent rainbow sheen — the signature obsidian look
                drawRect(
                    brush = Brush.linearGradient(
                        0.0f to shimmer1.copy(alpha = 0.0f),
                        0.2f to shimmer1.copy(alpha = 0.18f),
                        0.4f to shimmer2.copy(alpha = 0.22f),
                        0.6f to shimmer3.copy(alpha = 0.20f),
                        0.8f to shimmer1.copy(alpha = 0.12f),
                        1.0f to Color.Transparent,
                        start = Offset(size.width * 0.1f, 0f),
                        end   = Offset(size.width * 0.9f, size.height)
                    )
                )

                // Conchoidal fracture lines — how obsidian breaks
                val fractures = listOf(
                    Offset(0f, size.height * 0.3f) to Offset(size.width * 0.6f, size.height * 0.1f),
                    Offset(size.width * 0.4f, 0f) to Offset(size.width, size.height * 0.5f),
                    Offset(0f, size.height * 0.7f) to Offset(size.width * 0.8f, size.height),
                )
                fractures.forEach { (start, end) ->
                    drawLine(
                        color = glassEdge.copy(alpha = 0.3f),
                        start = start, end = end,
                        strokeWidth = 0.7f
                    )
                    // Light refraction along fracture
                    drawLine(
                        color = Color.White.copy(alpha = 0.12f),
                        start = Offset(start.x + 1f, start.y + 1f),
                        end   = Offset(end.x + 1f, end.y + 1f),
                        strokeWidth = 0.5f
                    )
                }

                // Top-right specular highlight
                drawCircle(
                    brush = Brush.radialGradient(
                        0.0f to Color.White.copy(alpha = 0.5f),
                        0.3f to Color.White.copy(alpha = 0.15f),
                        1.0f to Color.Transparent,
                        center = Offset(size.width * 0.8f, size.height * 0.15f),
                        radius = 14f
                    ),
                    center = Offset(size.width * 0.8f, size.height * 0.15f),
                    radius = 14f
                )

                drawContent()
            }
            .border(
                0.5.dp,
                Brush.linearGradient(
                    listOf(glassEdge.copy(alpha = 0.6f), Color.Transparent, glassEdge.copy(alpha = 0.3f))
                ),
                RoundedCornerShape(4.dp)
            )
    )
}

// ═══════════════════════════════════════════════════════════
// NEW: EMBER — Living fire with animated heat shimmer
// ═══════════════════════════════════════════════════════════

@Composable
fun Ember(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "ember")
    val flicker by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue  = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "flicker"
    )

    val charcoal = Color(0xFF1A0A00)
    val deepRed  = Color(0xFF8B1500)
    val orange   = Color(0xFFE84000)
    val amber    = Color(0xFFFF8C00)
    val yellow   = Color(0xFFFFD000)
    val white    = Color(0xFFFFF8E0)

    Box(
        modifier = modifier
            .size(64.dp)
            .aspectRatio(1f)
            .clip(RoundedCornerShape(6.dp))
            .background(charcoal)
            .drawWithContent {
                // Hot core at bottom
                drawRect(
                    brush = Brush.verticalGradient(
                        0.0f to charcoal,
                        0.3f to deepRed.copy(alpha = 0.6f),
                        0.6f to orange,
                        0.85f to amber,
                        1.0f to yellow.copy(alpha = 0.9f)
                    )
                )

                // Animated flame tongues
                for (i in 0..4) {
                    val phase    = flicker + i * (PI.toFloat() / 2.5f)
                    val centerX  = size.width * (0.15f + i * 0.18f)
                    val flameH   = size.height * (0.45f + sin(phase) * 0.12f)
                    val flameW   = size.width * (0.12f + cos(phase * 0.7f) * 0.04f)
                    val baseY    = size.height * (0.6f + cos(phase * 0.4f) * 0.08f)

                    drawPath(
                        path = Path().apply {
                            moveTo(centerX, baseY)
                            cubicTo(
                                centerX - flameW, baseY - flameH * 0.4f,
                                centerX - flameW * 0.5f, baseY - flameH * 0.8f,
                                centerX, baseY - flameH
                            )
                            cubicTo(
                                centerX + flameW * 0.5f, baseY - flameH * 0.8f,
                                centerX + flameW, baseY - flameH * 0.4f,
                                centerX, baseY
                            )
                        },
                        brush = Brush.verticalGradient(
                            0.0f to yellow.copy(alpha = 0.9f),
                            0.4f to amber.copy(alpha = 0.85f),
                            0.7f to orange.copy(alpha = 0.7f),
                            1.0f to Color.Transparent,
                            startY = baseY - flameH,
                            endY   = baseY
                        )
                    )
                }

                // Embers / sparks — tiny dots rising
                for (i in 0..7) {
                    val phase  = flicker * 0.6f + i * 0.8f
                    val ex     = size.width * ((i * 0.13f + sin(phase * 0.5f) * 0.08f) % 1f)
                    val ey     = size.height * (0.8f - (phase % (2 * PI.toFloat())) / (2 * PI.toFloat()) * 0.9f)
                    val er     = 1.5f + cos(phase) * 0.5f
                    val alpha  = (0.9f - (0.8f - ey / size.height)).coerceIn(0f, 1f)
                    drawCircle(
                        color = amber.copy(alpha = alpha * 0.8f),
                        center = Offset(ex, ey), radius = er
                    )
                }

                // Bright hotspot glow at base
                drawCircle(
                    brush = Brush.radialGradient(
                        0.0f to white.copy(alpha = 0.6f),
                        0.3f to yellow.copy(alpha = 0.4f),
                        1.0f to Color.Transparent,
                        center = Offset(size.width * 0.5f, size.height * 0.88f),
                        radius = size.width * 0.4f
                    ),
                    center = Offset(size.width * 0.5f, size.height * 0.88f),
                    radius = size.width * 0.4f
                )

                drawContent()
            }
    )
}

// ═══════════════════════════════════════════════════════════
// NEW: MALACHITE — Banded green gemstone
// ═══════════════════════════════════════════════════════════

@Composable
fun Malachite(modifier: Modifier = Modifier) {
    val darkGreen  = Color(0xFF1B4A1E)
    val midGreen   = Color(0xFF2E7D32)
    val brightGreen = Color(0xFF43A047)
    val paleGreen  = Color(0xFF81C784)
    val deepest    = Color(0xFF0D2810)

    Box(
        modifier = modifier
            .size(64.dp)
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(midGreen)
            .drawWithContent {
                drawRect(
                    brush = Brush.linearGradient(
                        0.0f to deepest,
                        0.3f to midGreen,
                        0.6f to brightGreen,
                        1.0f to darkGreen,
                        start = Offset(0f, size.height),
                        end   = Offset(size.width, 0f)
                    )
                )

                // Concentric oval banding — characteristic of malachite
                val bands = listOf(
                    Triple(Offset(size.width * 0.45f, size.height * 0.5f), Size(size.width * 0.9f, size.height * 0.7f), deepest.copy(alpha = 0.7f)),
                    Triple(Offset(size.width * 0.45f, size.height * 0.5f), Size(size.width * 0.7f, size.height * 0.5f), paleGreen.copy(alpha = 0.4f)),
                    Triple(Offset(size.width * 0.45f, size.height * 0.5f), Size(size.width * 0.5f, size.height * 0.35f), darkGreen.copy(alpha = 0.6f)),
                    Triple(Offset(size.width * 0.45f, size.height * 0.5f), Size(size.width * 0.3f, size.height * 0.2f), brightGreen.copy(alpha = 0.5f)),
                    // Second eye, offset
                    Triple(Offset(size.width * 0.75f, size.height * 0.25f), Size(size.width * 0.45f, size.height * 0.38f), deepest.copy(alpha = 0.5f)),
                    Triple(Offset(size.width * 0.75f, size.height * 0.25f), Size(size.width * 0.28f, size.height * 0.22f), paleGreen.copy(alpha = 0.35f)),
                )
                bands.forEach { (center, sz, color) ->
                    drawOval(
                        color = color,
                        topLeft = Offset(center.x - sz.width / 2, center.y - sz.height / 2),
                        size    = sz,
                        style   = Stroke(width = 2.5f)
                    )
                }

                // Silky surface sheen — directional light across mineral surface
                for (i in 0..5) {
                    drawLine(
                        brush = Brush.linearGradient(
                            listOf(Color.Transparent, paleGreen.copy(alpha = 0.18f), Color.Transparent)
                        ),
                        start = Offset(0f, i * 12f + 3f),
                        end   = Offset(size.width, i * 12f),
                        strokeWidth = 3f
                    )
                }

                // Polished top-left reflection
                drawPath(
                    path = Path().apply {
                        moveTo(0f, 0f)
                        lineTo(size.width * 0.55f, 0f)
                        lineTo(0f, size.height * 0.45f)
                        close()
                    },
                    color = Color.White.copy(alpha = 0.14f)
                )

                drawContent()
            }
            .border(1.dp, paleGreen.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
    )
}

// ═══════════════════════════════════════════════════════════
// NEW: SANDSTORM — Warm desert sand layers with wind texture
// ═══════════════════════════════════════════════════════════

@Composable
fun Sandstorm(modifier: Modifier = Modifier) {
    val dune       = Color(0xFFD4A050)
    val deepSand   = Color(0xFFA0722A)
    val paleSand   = Color(0xFFEED598)
    val rust       = Color(0xFF8B4513)
    val dusty      = Color(0xFFC8A060)

    Box(
        modifier = modifier
            .size(64.dp)
            .aspectRatio(1f)
            .clip(RoundedCornerShape(6.dp))
            .background(dune)
            .drawWithContent {
                // Stratified sand layers — compressed horizontal bands
                val layers = listOf(
                    0.00f to 0.12f to deepSand,
                    0.12f to 0.23f to paleSand,
                    0.23f to 0.34f to dune,
                    0.34f to 0.42f to deepSand.copy(alpha = 0.8f),
                    0.42f to 0.56f to paleSand,
                    0.56f to 0.68f to rust.copy(alpha = 0.6f),
                    0.68f to 0.80f to dune,
                    0.80f to 1.00f to deepSand,
                )
                layers.forEach { (range, color) ->
                    val (start, end) = range
                    drawRect(
                        color   = color,
                        topLeft = Offset(0f, size.height * start),
                        size    = Size(size.width, size.height * (end - start))
                    )
                }

                // Wind ripples — diagonal sinusoidal streaks
                for (i in 0..14) {
                    val y     = size.height * i / 15f
                    val alpha = 0.08f + (i % 3) * 0.05f
                    drawLine(
                        brush = Brush.linearGradient(
                            0f to Color.Transparent,
                            0.2f to paleSand.copy(alpha = alpha),
                            0.8f to dusty.copy(alpha = alpha),
                            1f to Color.Transparent
                        ),
                        start = Offset(0f, y + sin(i * 0.9f) * 2f),
                        end   = Offset(size.width, y + sin(i * 0.9f + 1f) * 2f),
                        strokeWidth = 1f
                    )
                }

                // Mica sparkle — tiny glint particles
                val glints = listOf(
                    Offset(0.2f, 0.18f), Offset(0.7f, 0.33f), Offset(0.45f, 0.55f),
                    Offset(0.8f, 0.72f), Offset(0.15f, 0.65f), Offset(0.6f, 0.82f),
                    Offset(0.35f, 0.08f), Offset(0.9f, 0.45f),
                )
                glints.forEach { (nx, ny) ->
                    val cx = size.width * nx
                    val cy = size.height * ny
                    drawLine(Color.White.copy(alpha = 0.6f), Offset(cx - 2f, cy), Offset(cx + 2f, cy), 0.8f)
                    drawLine(Color.White.copy(alpha = 0.6f), Offset(cx, cy - 2f), Offset(cx, cy + 2f), 0.8f)
                }

                // Sun-bleached top highlight
                drawRect(
                    brush = Brush.verticalGradient(
                        0.0f to Color.White.copy(alpha = 0.18f),
                        0.25f to Color.Transparent
                    )
                )

                drawContent()
            }
    )
}

// ═══════════════════════════════════════════════════════════
// NEW: CIRCUIT — Neon PCB trace aesthetic
// ═══════════════════════════════════════════════════════════

@Composable
fun Circuit(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "circuit")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue  = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse"
    )

    val pcbGreen   = Color(0xFF0A2A1A)
    val traceGreen = Color(0xFF1DB954)
    val dimTrace   = Color(0xFF0D6B2E)
    val copper     = Color(0xFFB87333)
    val solderPad  = Color(0xFFD4AF37)

    Box(
        modifier = modifier
            .size(64.dp)
            .aspectRatio(1f)
            .clip(RoundedCornerShape(4.dp))
            .background(pcbGreen)
            .drawWithContent {
                // PCB substrate
                drawRect(
                    brush = Brush.radialGradient(
                        0.0f to Color(0xFF0D3520),
                        1.0f to pcbGreen
                    )
                )

                // Circuit traces — horizontal and vertical segments
                val traceAlpha = 0.5f + pulse * 0.3f
                val traces = listOf(
                    // format: startX%, startY%, endX%, endY%
                    listOf(0f, 0.2f, 0.6f, 0.2f),
                    listOf(0.6f, 0.2f, 0.6f, 0.5f),
                    listOf(0.6f, 0.5f, 1f, 0.5f),
                    listOf(0.3f, 0f, 0.3f, 0.7f),
                    listOf(0f, 0.7f, 0.3f, 0.7f),
                    listOf(0.3f, 0.7f, 0.3f, 1f),
                    listOf(0.8f, 0f, 0.8f, 0.35f),
                    listOf(0.8f, 0.35f, 1f, 0.35f),
                    listOf(0f, 0.45f, 0.15f, 0.45f),
                    listOf(0.15f, 0.45f, 0.15f, 1f),
                )
                traces.forEach { (x1, y1, x2, y2) ->
                    drawLine(
                        color = dimTrace.copy(alpha = 0.8f),
                        start = Offset(size.width * x1, size.height * y1),
                        end   = Offset(size.width * x2, size.height * y2),
                        strokeWidth = 2f
                    )
                }

                // Animated signal pulse — travels along a path
                val pathProgress = pulse
                val pathPoints = listOf(
                    Offset(0f, size.height * 0.2f),
                    Offset(size.width * 0.6f, size.height * 0.2f),
                    Offset(size.width * 0.6f, size.height * 0.5f),
                    Offset(size.width, size.height * 0.5f),
                )
                // Simple approximation: glow dot along first segment
                val totalSegs = pathPoints.size - 1
                val segIndex = (pathProgress * totalSegs).toInt().coerceAtMost(totalSegs - 1)
                val segT = (pathProgress * totalSegs) - segIndex
                val pA = pathPoints[segIndex]
                val pB = pathPoints[segIndex + 1]
                val signalPos = Offset(
                    pA.x + (pB.x - pA.x) * segT,
                    pA.y + (pB.y - pA.y) * segT
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        0.0f to Color.White,
                        0.4f to traceGreen,
                        1.0f to Color.Transparent,
                        center = signalPos, radius = 8f
                    ),
                    center = signalPos, radius = 8f
                )

                // Solder pads — small gold circles at junctions
                val pads = listOf(
                    Offset(0.6f, 0.2f), Offset(0.6f, 0.5f), Offset(0.3f, 0.7f),
                    Offset(0.8f, 0.35f), Offset(0.15f, 0.45f),
                )
                pads.forEach { (nx, ny) ->
                    val cx = size.width * nx
                    val cy = size.height * ny
                    drawCircle(color = copper, center = Offset(cx, cy), radius = 4f)
                    drawCircle(color = solderPad, center = Offset(cx, cy), radius = 2f)
                    drawCircle(color = pcbGreen, center = Offset(cx, cy), radius = 1f)
                }

                // Bright trace highlight along lit traces
                traces.take(4).forEach { (x1, y1, x2, y2) ->
                    drawLine(
                        color = traceGreen.copy(alpha = traceAlpha * 0.3f),
                        start = Offset(size.width * x1, size.height * y1),
                        end   = Offset(size.width * x2, size.height * y2),
                        strokeWidth = 1f
                    )
                }

                drawContent()
            }
            .border(1.dp, traceGreen.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
    )
}

// ═══════════════════════════════════════════════════════════
// NEW: AURORA — Shifting polar light curtains
// ═══════════════════════════════════════════════════════════

@Composable
fun Aurora(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "aurora")
    val wave by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue  = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave"
    )

    val nightSky  = Color(0xFF030A1A)
    val deepSpace = Color(0xFF050D20)
    val green     = Color(0xFF00FF88)
    val teal      = Color(0xFF00E5CC)
    val violet    = Color(0xFF9B59FF)
    val pink      = Color(0xFFFF5FBF)

    Box(
        modifier = modifier
            .size(64.dp)
            .aspectRatio(1f)
            .clip(RoundedCornerShape(10.dp))
            .background(nightSky)
            .drawWithContent {
                // Night sky base
                drawRect(
                    brush = Brush.verticalGradient(
                        listOf(deepSpace, nightSky, Color(0xFF020715))
                    )
                )

                // Stars — tiny fixed points
                val stars = listOf(
                    Offset(0.1f, 0.08f), Offset(0.85f, 0.05f), Offset(0.5f, 0.12f),
                    Offset(0.2f, 0.18f), Offset(0.7f, 0.22f), Offset(0.35f, 0.06f),
                    Offset(0.92f, 0.15f), Offset(0.65f, 0.1f),
                )
                stars.forEach { (nx, ny) ->
                    drawCircle(
                        color = Color.White.copy(alpha = 0.7f),
                        center = Offset(size.width * nx, size.height * ny),
                        radius = 0.8f
                    )
                }

                // Aurora curtains — vertical gradient bands with sine wave motion
                val curtains = listOf(
                    Triple(0.2f, green,  0.55f),
                    Triple(0.45f, teal,  0.50f),
                    Triple(0.65f, violet, 0.45f),
                    Triple(0.8f, pink,   0.40f),
                )
                curtains.forEachIndexed { i, (nx, color, intensity) ->
                    val cx    = size.width * (nx + sin(wave + i * 0.8f) * 0.06f)
                    val width = size.width * 0.22f
                    val topY  = size.height * (0.15f + sin(wave * 0.7f + i) * 0.08f)
                    val botY  = size.height * (0.75f + cos(wave * 0.5f + i * 0.6f) * 0.1f)

                    // Soft vertical curtain
                    drawRect(
                        brush = Brush.horizontalGradient(
                            0.0f to Color.Transparent,
                            0.3f to color.copy(alpha = intensity * 0.6f),
                            0.5f to color.copy(alpha = intensity),
                            0.7f to color.copy(alpha = intensity * 0.6f),
                            1.0f to Color.Transparent,
                            startX = cx - width / 2,
                            endX   = cx + width / 2
                        ),
                        topLeft = Offset(cx - width / 2, topY),
                        size    = Size(width, botY - topY)
                    )
                }

                // Atmospheric glow at horizon
                drawRect(
                    brush = Brush.verticalGradient(
                        0.55f to Color.Transparent,
                        0.85f to teal.copy(alpha = 0.15f),
                        1.00f to nightSky
                    )
                )

                drawContent()
            }
    )
}

// ═══════════════════════════════════════════════════════════
// NEW: MARBLE — Classic Carrara veined stone
// ═══════════════════════════════════════════════════════════

@Composable
fun Marble(modifier: Modifier = Modifier) {
    val snow     = Color(0xFFF8F5F0)
    val cream    = Color(0xFFEDE8DF)
    val veinGray = Color(0xFFB0A898)
    val darkVein = Color(0xFF7A7068)
    val rosé     = Color(0xFFD4B8A8)

    Box(
        modifier = modifier
            .size(64.dp)
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(snow)
            .drawWithContent {
                // Warm white base with subtle warmth variation
                drawRect(
                    brush = Brush.linearGradient(
                        0.0f to snow,
                        0.5f to cream,
                        1.0f to Color(0xFFF0EBE3),
                        start = Offset(0f, 0f),
                        end   = Offset(size.width, size.height)
                    )
                )

                // Primary vein — sweeping diagonal
                fun DrawScope.drawVein(
                    startX: Float, startY: Float,
                    ctrl1X: Float, ctrl1Y: Float,
                    ctrl2X: Float, ctrl2Y: Float,
                    endX: Float, endY: Float,
                    color: Color, width: Float
                ) {
                    val path = Path().apply {
                        moveTo(startX, startY)
                        cubicTo(ctrl1X, ctrl1Y, ctrl2X, ctrl2Y, endX, endY)
                    }
                    drawPath(path, color = color, style = Stroke(width = width))
                }

                // Main veins
                drawVein(0f, size.height * 0.3f, size.width * 0.3f, size.height * 0.1f, size.width * 0.7f, size.height * 0.5f, size.width, size.height * 0.35f, darkVein, 1.8f)
                drawVein(0f, size.height * 0.3f, size.width * 0.3f, size.height * 0.1f, size.width * 0.7f, size.height * 0.5f, size.width, size.height * 0.35f, veinGray.copy(alpha = 0.4f), 3.5f)

                // Secondary vein
                drawVein(size.width * 0.1f, 0f, size.width * 0.4f, size.height * 0.3f, size.width * 0.2f, size.height * 0.6f, size.width * 0.5f, size.height, darkVein.copy(alpha = 0.6f), 1f)

                // Hairline feather veins branching off
                val hairlines = listOf(
                    listOf(0.35f, 0.28f, 0.45f, 0.15f, 0.55f, 0.22f, 0.65f, 0.18f),
                    listOf(0.5f, 0.42f, 0.55f, 0.55f, 0.6f, 0.50f, 0.7f, 0.58f),
                    listOf(0.15f, 0.35f, 0.20f, 0.48f, 0.18f, 0.55f, 0.25f, 0.65f),
                )
                hairlines.forEach { coords ->
                    drawVein(
                        size.width * coords[0], size.height * coords[1],
                        size.width * coords[2], size.height * coords[3],
                        size.width * coords[4], size.height * coords[5],
                        size.width * coords[6], size.height * coords[7],
                        veinGray.copy(alpha = 0.4f), 0.7f
                    )
                }

                // Warm pink mineral blush areas
                drawCircle(
                    brush = Brush.radialGradient(
                        0.0f to rosé.copy(alpha = 0.25f),
                        1.0f to Color.Transparent,
                        center = Offset(size.width * 0.3f, size.height * 0.65f),
                        radius = size.width * 0.4f
                    ),
                    center = Offset(size.width * 0.3f, size.height * 0.65f),
                    radius = size.width * 0.4f
                )

                // Polished surface specular
                drawPath(
                    path = Path().apply {
                        moveTo(0f, 0f)
                        lineTo(size.width * 0.8f, 0f)
                        lineTo(0f, size.height * 0.4f)
                        close()
                    },
                    color = Color.White.copy(alpha = 0.2f)
                )

                drawContent()
            }
            .border(0.5.dp, veinGray.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
    )
}

// ═══════════════════════════════════════════════════════════
// PREVIEW GALLERY
// ═══════════════════════════════════════════════════════════

@Preview(showBackground = true, backgroundColor = 0xFF222222)
@Composable
fun ThemeGalleryPreview() {
    val themes = listOf<Pair<String, @Composable () -> Unit>>(
        "September" to { September() },
        "Icicle"    to { Icicle() },
        "Obsidian"  to { Obsidian() },
        "Ember"     to { Ember() },
        "Malachite" to { Malachite() },
        "Sandstorm" to { Sandstorm() },
        "Circuit"   to { Circuit() },
        "Aurora"    to { Aurora() },
        "Marble"    to { Marble() },
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement   = Arrangement.spacedBy(16.dp)
    ) {
        items(themes.size) { i ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                themes[i].second()
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text     = themes[i].first,
                    color    = Color.White.copy(alpha = 0.8f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// Individual previews
@Preview @Composable private fun PreviewSeptember()  = September()
@Preview @Composable private fun PreviewIcicle()     = Icicle()
@Preview @Composable private fun PreviewObsidian()   = Obsidian()
@Preview @Composable private fun PreviewEmber()      = Ember()
@Preview @Composable private fun PreviewMalachite()  = Malachite()
@Preview @Composable private fun PreviewSandstorm()  = Sandstorm()
@Preview @Composable private fun PreviewCircuit()    = Circuit()
@Preview @Composable private fun PreviewAurora()     = Aurora()
@Preview @Composable private fun PreviewMarble()     = Marble()