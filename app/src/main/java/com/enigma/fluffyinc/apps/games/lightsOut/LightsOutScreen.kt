package com.enigma.fluffyinc.apps.games.lightsOut

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

// ─────────────────────────────────────────────────────────────────────────────
//  Colour palette  (works with both light and dark Material 3 themes)
// ─────────────────────────────────────────────────────────────────────────────

private val CellOn        = Color(0xFFFFD060)   // warm amber glow
private val CellOnCenter  = Color(0xFFFFF4B0)   // bright core
private val CellOff       = Color(0xFF1E2235)   // deep navy
private val CellOffBorder = Color(0xFF2E3555)
private val HintRing      = Color(0xFF60EFFF)   // electric cyan hint ring
private val SolvedGlow    = Color(0xFF60EFFF)

// ─────────────────────────────────────────────────────────────────────────────
//  Root screen
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun LightsOutScreen(vm: LightsOutViewModel = viewModel()) {

    val state       by vm.uiState.collectAsStateWithLifecycle()
    val generating  by vm.isGenerating.collectAsStateWithLifecycle()

    // Difficulty selector tab state
    var selectedDiff by remember { mutableStateOf(LightsOutDifficulty.MEDIUM) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color    = Color(0xFF0D0F1E)   // near-black indigo background
    ) {
        Column(
            modifier            = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(Modifier.height(24.dp))

            // ── Title ──────────────────────────────────────────────────────
            Text(
                text       = "LIGHTS OUT",
                style      = MaterialTheme.typography.displaySmall.copy(
                    fontWeight   = FontWeight.Black,
                    letterSpacing = 6.sp,
                    color        = Color(0xFFFFD060)
                )
            )
            Text(
                text  = "Toggle all lights off",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color        = Color(0xFF8890BB),
                    letterSpacing = 1.sp
                )
            )

            Spacer(Modifier.height(24.dp))

            // ── Stats row ─────────────────────────────────────────────────
            Row(
                modifier            = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatChip(label = "MOVES", value = state.moveCount.toString())
                StatChip(label = "OPTIMAL", value = state.solution.sum().toString())
                StatChip(
                    label = "LIT",
                    value = state.cells.sum().toString(),
                    highlight = state.cells.sum() == 0
                )
            }

            Spacer(Modifier.height(24.dp))

            // ── Board ──────────────────────────────────────────────────────
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val cellSize = (maxWidth - 16.dp) / 5   // 4 gaps of 4dp
                LightsOutBoard(
                    state    = state,
                    cellSize = cellSize,
                    onPress  = { if (!generating) vm.onCellPressed(it) }
                )
            }

            Spacer(Modifier.height(24.dp))

            // ── Solved overlay ─────────────────────────────────────────────
            if (state.isSolved) {
                SolvedBanner(moveCount = state.moveCount, optimal = state.solution.sum())
                Spacer(Modifier.height(16.dp))
            }

            // ── Difficulty tabs ────────────────────────────────────────────
            DifficultySelector(
                selected = selectedDiff,
                onSelect = { selectedDiff = it }
            )

            Spacer(Modifier.height(16.dp))

            // ── Action buttons ─────────────────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Hint
                OutlinedButton(
                    onClick  = { vm.toggleHint() },
                    modifier = Modifier.weight(1f),
                    colors   = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (state.showHint) HintRing else Color(0xFF8890BB)
                    ),
                    border   = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = if (state.showHint)
                            Brush.horizontalGradient(listOf(HintRing, HintRing))
                        else
                            Brush.horizontalGradient(listOf(Color(0xFF3A4060), Color(0xFF3A4060)))
                    )
                ) {
                    Icon(Icons.Default.Lightbulb, contentDescription = "Hint", modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(if (state.showHint) "HIDE" else "HINT")
                }

                // New game
                Button(
                    onClick  = { vm.newGame(selectedDiff) },
                    enabled  = !generating,
                    modifier = Modifier.weight(1f),
                    colors   = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFD060),
                        contentColor   = Color(0xFF0D0F1E)
                    )
                ) {
                    if (generating) {
                        CircularProgressIndicator(
                            modifier  = Modifier.size(18.dp),
                            color     = Color(0xFF0D0F1E),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.Refresh, contentDescription = "New game", modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("NEW GAME", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Board grid
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun LightsOutBoard(
    state:    LightsOutGameState,
    cellSize: Dp,
    onPress:  (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        for (row in 0..4) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                for (col in 0..4) {
                    val idx = row * 5 + col
                    LightCell(
                        isOn     = state.cells[idx] == 1,
                        isHint   = state.isHintCell(idx),
                        size     = cellSize,
                        onClick  = { onPress(idx) }
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Individual cell
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun LightCell(
    isOn:    Boolean,
    isHint:  Boolean,
    size:    Dp,
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    // Press-scale animation
    val interactionSource = remember { MutableInteractionSource() }
    val pressScale by animateFloatAsState(
        targetValue  = if (isOn) 1f else 0.94f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label        = "scale"
    )

    // Cell colour
    val bgColor by animateColorAsState(
        targetValue    = if (isOn) CellOn else CellOff,
        animationSpec  = tween(180),
        label          = "cellColor"
    )

    // Glow intensity via elevation-like shadow
    val elevation by animateDpAsState(
        targetValue   = if (isOn) 14.dp else 0.dp,
        animationSpec = tween(200),
        label         = "elevation"
    )

    // Pulsing ring for hint cells
    val hintAlpha by animateFloatAsState(
        targetValue   = if (isHint) 1f else 0f,
        animationSpec = tween(300),
        label         = "hint"
    )

    Box(
        modifier = Modifier
            .size(size)
            .scale(pressScale)
            .shadow(
                elevation       = elevation,
                shape           = RoundedCornerShape(8.dp),
                ambientColor    = if (isOn) CellOn.copy(alpha = 0.6f) else Color.Transparent,
                spotColor       = if (isOn) CellOn.copy(alpha = 0.8f) else Color.Transparent
            )
            .clip(RoundedCornerShape(8.dp))
            .background(
                brush = if (isOn)
                    Brush.radialGradient(listOf(CellOnCenter, CellOn))
                else
                    Brush.linearGradient(listOf(CellOff, Color(0xFF161828)))
            )
            .border(
                width = if (isHint) 2.dp else 1.dp,
                color = when {
                    isHint -> HintRing.copy(alpha = hintAlpha)
                    isOn   -> CellOn.copy(alpha = 0.5f)
                    else   -> CellOffBorder
                },
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication        = null    // custom feedback via haptic + scale
            ) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        // Inner glow dot for lit cells
        if (isOn) {
            Box(
                modifier = Modifier
                    .size(size * 0.3f)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.55f))
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Stat chip
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun StatChip(label: String, value: String, highlight: Boolean = false) {
    Column(
        modifier            = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF161828))
            .border(
                1.dp,
                if (highlight) SolvedGlow.copy(alpha = 0.6f) else Color(0xFF2E3555),
                RoundedCornerShape(10.dp)
            )
            .padding(horizontal = 18.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text  = value,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Black,
                color      = if (highlight) SolvedGlow else Color(0xFFFFD060)
            )
        )
        Text(
            text  = label,
            style = MaterialTheme.typography.labelSmall.copy(
                color        = Color(0xFF5A6380),
                letterSpacing = 1.5.sp
            )
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Difficulty selector
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun DifficultySelector(
    selected: LightsOutDifficulty,
    onSelect: (LightsOutDifficulty) -> Unit
) {
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF161828))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        LightsOutDifficulty.entries.forEach { diff ->
            val isSelected = diff == selected
            val bgAnim by animateColorAsState(
                targetValue   = if (isSelected) Color(0xFFFFD060) else Color.Transparent,
                animationSpec = tween(200),
                label         = "diffBg"
            )
            val textAnim by animateColorAsState(
                targetValue   = if (isSelected) Color(0xFF0D0F1E) else Color(0xFF5A6380),
                animationSpec = tween(200),
                label         = "diffText"
            )
            Box(
                modifier            = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(bgAnim)
                    .clickable { onSelect(diff) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text  = diff.name.take(3),   // EASY→EAS, MEDIUM→MED, etc.
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight   = if (isSelected) FontWeight.Black else FontWeight.Normal,
                        color        = textAnim,
                        letterSpacing = 1.sp
                    )
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Solved banner
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun SolvedBanner(moveCount: Int, optimal: Int) {
    val efficiency = if (moveCount > 0) (optimal * 100) / moveCount else 100
    val isPerfect  = moveCount == optimal

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xFF0D2B2E), Color(0xFF0A1E28))
                )
            )
            .border(1.dp, SolvedGlow.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
            .padding(16.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(
                text  = if (isPerfect) "✦ PERFECT SOLVE ✦" else "✦ SOLVED ✦",
                style = MaterialTheme.typography.titleMedium.copy(
                    color        = SolvedGlow,
                    fontWeight   = FontWeight.Black,
                    letterSpacing = 3.sp
                )
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text      = "$moveCount moves  ·  $efficiency% efficiency",
                style     = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF8890BB)),
                textAlign = TextAlign.Center
            )
        }
    }
}
