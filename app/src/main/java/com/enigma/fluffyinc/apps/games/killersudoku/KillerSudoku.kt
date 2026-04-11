package com.enigma.fluffyinc.apps.games.killersudoku

import android.annotation.SuppressLint
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import org.json.JSONArray
import org.json.JSONObject

// ─────────────────────────────────────────────────────────────────────────────
// DATA MODELS
// ─────────────────────────────────────────────────────────────────────────────

data class Cage(
    val id: Int,
    val sum: Int,
    val cells: List<Pair<Int, Int>>,
    val color: Color
)

data class CellState(
    val value: Int = 0,
    val isGiven: Boolean = false,
    val notes: Set<Int> = emptySet(),
    val isError: Boolean = false
)

// ─────────────────────────────────────────────────────────────────────────────
// LEVEL SYSTEM
// ─────────────────────────────────────────────────────────────────────────────

enum class Level(
    val displayName: String,
    val emoji: String,
    val description: String,
    val accentColor: Color,
    val bgColor: Color,
    val surfaceColor: Color,
    val dimColor: Color
) {
    EASY(
        displayName = "EASY",
        emoji = "🌿",
        description = "Small cages · Gentle challenge",
        accentColor = Color(0xFF4ECCA3),
        bgColor = Color(0xFF0D1B2A),
        surfaceColor = Color(0xFF112233),
        dimColor = Color(0xFF3A6B5A)
    ),
    MEDIUM(
        displayName = "MEDIUM",
        emoji = "⚡",
        description = "Balanced cages · Sharp thinking",
        accentColor = Color(0xFFFFB830),
        bgColor = Color(0xFF1A1508),
        surfaceColor = Color(0xFF221C00),
        dimColor = Color(0xFF6B5A20)
    ),
    HARD(
        displayName = "HARD",
        emoji = "🔥",
        description = "Large cages · High deduction",
        accentColor = Color(0xFFE94560),
        bgColor = Color(0xFF1A0A0A),
        surfaceColor = Color(0xFF220D0D),
        dimColor = Color(0xFF6B2030)
    ),
    EXPERT(
        displayName = "EXPERT",
        emoji = "💀",
        description = "Massive cages · Master level",
        accentColor = Color(0xFFB44FE8),
        bgColor = Color(0xFF110A1A),
        surfaceColor = Color(0xFF1A0D22),
        dimColor = Color(0xFF5A2080)
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// CAGE COLOUR PALETTE  (richer, more distinct sets per level theme)
// ─────────────────────────────────────────────────────────────────────────────

val CAGE_COLORS_EASY = listOf(
    Color(0xFF1E4D3A), Color(0xFF1A3D52), Color(0xFF3D3A1E),
    Color(0xFF3A1E2E), Color(0xFF1E2E3A), Color(0xFF2E3A1E),
    Color(0xFF3A2E1E), Color(0xFF1E3A3A), Color(0xFF2E1E3A),
    Color(0xFF3A1E1E), Color(0xFF1E3A2E), Color(0xFF2A3D2A),
    Color(0xFF3D2A1A), Color(0xFF1A2A3D), Color(0xFF3D1A2A),
    Color(0xFF2A1A3D), Color(0xFF1A3D2A), Color(0xFF3D3D1A)
)

val CAGE_COLORS_MEDIUM = listOf(
    Color(0xFF3D2E00), Color(0xFF2E3D00), Color(0xFF3D1800),
    Color(0xFF001E3D), Color(0xFF3D003D), Color(0xFF003D3D),
    Color(0xFF3D2800), Color(0xFF003D28), Color(0xFF28003D),
    Color(0xFF3D0028), Color(0xFF28003D), Color(0xFF003D3D),
    Color(0xFF3D3800), Color(0xFF003838), Color(0xFF380038),
    Color(0xFF383800), Color(0xFF003838), Color(0xFF380038)
)

val CAGE_COLORS_HARD = listOf(
    Color(0xFF3D0A0A), Color(0xFF0A3D0A), Color(0xFF0A0A3D),
    Color(0xFF3D200A), Color(0xFF0A3D20), Color(0xFF200A3D),
    Color(0xFF3D0A20), Color(0xFF203D0A), Color(0xFF0A203D),
    Color(0xFF3D1A0A), Color(0xFF0A3D1A), Color(0xFF1A0A3D),
    Color(0xFF3D080A), Color(0xFF0A3D08), Color(0xFF080A3D),
    Color(0xFF3D1500), Color(0xFF003D15), Color(0xFF15003D)
)

val CAGE_COLORS_EXPERT = listOf(
    Color(0xFF220A3D), Color(0xFF3D0A22), Color(0xFF0A223D),
    Color(0xFF2E0A3D), Color(0xFF3D0A2E), Color(0xFF0A2E3D),
    Color(0xFF1A0A3D), Color(0xFF3D0A1A), Color(0xFF0A1A3D),
    Color(0xFF3A0A3D), Color(0xFF3D0A3A), Color(0xFF0A3A3D),
    Color(0xFF160A3D), Color(0xFF3D0A16), Color(0xFF0A163D),
    Color(0xFF300A3D), Color(0xFF3D0A30), Color(0xFF0A303D)
)

fun cageColorsForLevel(level: Level) = when (level) {
    Level.EASY   -> CAGE_COLORS_EASY
    Level.MEDIUM -> CAGE_COLORS_MEDIUM
    Level.HARD   -> CAGE_COLORS_HARD
    Level.EXPERT -> CAGE_COLORS_EXPERT
}

// Legacy alias used by the generator
val CAGE_COLORS = CAGE_COLORS_EASY

// ─────────────────────────────────────────────────────────────────────────────
// PUZZLE DATA (Easy puzzle – static reference)
// ─────────────────────────────────────────────────────────────────────────────

val SOLUTION = arrayOf(
    intArrayOf(2, 1, 5, 6, 4, 7, 3, 9, 8),
    intArrayOf(3, 6, 8, 9, 5, 2, 1, 7, 4),
    intArrayOf(7, 9, 4, 3, 8, 1, 6, 5, 2),
    intArrayOf(5, 8, 6, 2, 7, 4, 9, 3, 1),
    intArrayOf(1, 4, 2, 5, 9, 3, 8, 6, 7),
    intArrayOf(9, 7, 3, 8, 1, 6, 4, 2, 5),
    intArrayOf(8, 2, 1, 7, 3, 9, 5, 4, 6),
    intArrayOf(6, 5, 9, 4, 2, 8, 7, 1, 3),
    intArrayOf(4, 3, 7, 1, 6, 5, 2, 8, 9)
)

fun buildCages(level: Level = Level.EASY): List<Cage> {
    val colors = cageColorsForLevel(level)
    return listOf(
        Cage(0,  3,  listOf(0 to 0, 0 to 1),                     colors[0]),
        Cage(1,  15, listOf(0 to 2, 0 to 3, 0 to 4),             colors[1]),
        Cage(2,  22, listOf(0 to 5, 1 to 5, 2 to 5),             colors[2]),
        Cage(3,  20, listOf(0 to 6, 0 to 7, 0 to 8),             colors[3]),
        Cage(4,  9,  listOf(1 to 0, 2 to 0),                     colors[4]),
        Cage(5,  14, listOf(1 to 1, 1 to 2),                     colors[5]),
        Cage(6,  16, listOf(1 to 3, 1 to 4),                     colors[6]),
        Cage(7,  8,  listOf(1 to 6, 2 to 6),                     colors[7]),
        Cage(8,  12, listOf(1 to 7, 1 to 8),                     colors[8]),
        Cage(9,  13, listOf(2 to 1, 2 to 2),                     colors[9]),
        Cage(10, 3,  listOf(2 to 3, 3 to 3),                     colors[10]),
        Cage(11, 8,  listOf(2 to 4, 3 to 4),                     colors[11]),
        Cage(12, 11, listOf(2 to 7, 2 to 8),                     colors[12]),
        Cage(13, 13, listOf(3 to 0, 4 to 0),                     colors[13]),
        Cage(14, 14, listOf(3 to 1, 3 to 2),                     colors[14]),
        Cage(15, 13, listOf(3 to 5, 4 to 5),                     colors[15]),
        Cage(16, 4,  listOf(3 to 6, 4 to 6),                     colors[16]),
        Cage(17, 5,  listOf(3 to 7, 3 to 8),                     colors[17]),
        Cage(18, 6,  listOf(4 to 1, 4 to 2),                     colors[0]),
        Cage(19, 14, listOf(4 to 3, 4 to 4),                     colors[1]),
        Cage(20, 14, listOf(4 to 7, 4 to 8),                     colors[2]),
        Cage(21, 16, listOf(5 to 0, 6 to 0),                     colors[3]),
        Cage(22, 10, listOf(5 to 1, 5 to 2),                     colors[4]),
        Cage(23, 14, listOf(5 to 3, 5 to 4),                     colors[5]),
        Cage(24, 10, listOf(5 to 5, 6 to 5),                     colors[6]),
        Cage(25, 11, listOf(5 to 6, 5 to 7),                     colors[7]),
        Cage(26, 5,  listOf(5 to 8, 6 to 8),                     colors[8]),
        Cage(27, 3,  listOf(6 to 1, 6 to 2),                     colors[9]),
        Cage(28, 16, listOf(6 to 3, 6 to 4),                     colors[10]),
        Cage(29, 9,  listOf(6 to 6, 6 to 7),                     colors[11]),
        Cage(30, 11, listOf(7 to 0, 8 to 0),                     colors[12]),
        Cage(31, 14, listOf(7 to 1, 7 to 2),                     colors[13]),
        Cage(32, 5,  listOf(7 to 3, 8 to 3),                     colors[14]),
        Cage(33, 10, listOf(7 to 4, 8 to 4),                     colors[15]),
        Cage(34, 13, listOf(7 to 5, 8 to 5),                     colors[16]),
        Cage(35, 8,  listOf(7 to 6, 8 to 6),                     colors[17]),
        Cage(36, 9,  listOf(7 to 7, 7 to 8),                     colors[0]),
        Cage(37, 17, listOf(8 to 1, 8 to 2, 8 to 7, 8 to 8),    colors[1])
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// GAME STATE PERSISTENCE  (SharedPreferences JSON)
// ─────────────────────────────────────────────────────────────────────────────

private const val PREFS_NAME  = "killer_sudoku_save"
private const val KEY_BOARD   = "board"
private const val KEY_LEVEL   = "level"
private const val KEY_ELAPSED = "elapsed_seconds"
private const val KEY_ERRORS  = "error_count"

data class SavedGameState(
    val board: Array<Array<CellState>>,
    val level: Level,
    val elapsedSeconds: Long,
    val errorCount: Int
)

fun saveGame(
    context: Context,
    board: Array<Array<CellState>>,
    level: Level,
    elapsedSeconds: Long,
    errorCount: Int
) {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val boardJson = JSONArray()
    for (r in 0..8) {
        val rowArr = JSONArray()
        for (c in 0..8) {
            val cell = board[r][c]
            val obj = JSONObject().apply {
                put("v", cell.value)
                put("g", cell.isGiven)
                val notesArr = JSONArray()
                cell.notes.forEach { notesArr.put(it) }
                put("n", notesArr)
            }
            rowArr.put(obj)
        }
        boardJson.put(rowArr)
    }
    prefs.edit()
        .putString(KEY_BOARD, boardJson.toString())
        .putString(KEY_LEVEL, level.name)
        .putLong(KEY_ELAPSED, elapsedSeconds)
        .putInt(KEY_ERRORS, errorCount)
        .apply()
}

fun loadGame(context: Context): SavedGameState? {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val boardStr = prefs.getString(KEY_BOARD, null) ?: return null
    return try {
        val boardJson = JSONArray(boardStr)
        val board = Array(9) { r ->
            Array(9) { c ->
                val obj = boardJson.getJSONArray(r).getJSONObject(c)
                val notesArr = obj.getJSONArray("n")
                val notes = (0 until notesArr.length()).map { notesArr.getInt(it) }.toSet()
                CellState(
                    value = obj.getInt("v"),
                    isGiven = obj.getBoolean("g"),
                    notes = notes
                )
            }
        }
        val level = Level.valueOf(prefs.getString(KEY_LEVEL, Level.EASY.name) ?: Level.EASY.name)
        val elapsed = prefs.getLong(KEY_ELAPSED, 0L)
        val errors = prefs.getInt(KEY_ERRORS, 0)
        SavedGameState(board, level, elapsed, errors)
    } catch (e: Exception) {
        null
    }
}

fun clearSave(context: Context) {
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().clear().apply()
}

// ─────────────────────────────────────────────────────────────────────────────
// VALIDATION HELPERS
// ─────────────────────────────────────────────────────────────────────────────

fun isRowValid(board: Array<Array<CellState>>, row: Int): Boolean {
    val vals = board[row].map { it.value }.filter { it != 0 }
    return vals.size == vals.toSet().size
}

fun isColValid(board: Array<Array<CellState>>, col: Int): Boolean {
    val vals = board.map { it[col].value }.filter { it != 0 }
    return vals.size == vals.toSet().size
}

fun isBoxValid(board: Array<Array<CellState>>, row: Int, col: Int): Boolean {
    val startR = (row / 3) * 3
    val startC = (col / 3) * 3
    val vals = mutableListOf<Int>()
    for (r in startR until startR + 3)
        for (c in startC until startC + 3)
            if (board[r][c].value != 0) vals.add(board[r][c].value)
    return vals.size == vals.toSet().size
}

fun isCageValid(board: Array<Array<CellState>>, cage: Cage): Boolean {
    val vals = cage.cells.map { (r, c) -> board[r][c].value }
    val filled = vals.filter { it != 0 }
    if (filled.size != filled.toSet().size) return false
    if (vals.all { it != 0 } && vals.sum() != cage.sum) return false
    return true
}

fun isBoardComplete(board: Array<Array<CellState>>, cages: List<Cage>): Boolean {
    for (r in 0..8) for (c in 0..8) if (board[r][c].value == 0) return false
    for (r in 0..8) if (!isRowValid(board, r)) return false
    for (c in 0..8) if (!isColValid(board, c)) return false
    for (cage in cages) if (!isCageValid(board, cage)) return false
    return true
}

/** Compute exactly which cells are in error right now. */
fun computeErrors(board: Array<Array<CellState>>, cages: List<Cage>): Set<Pair<Int, Int>> {
    val errors = mutableSetOf<Pair<Int, Int>>()
    for (row in 0..8) for (col in 0..8) {
        if (board[row][col].value == 0) continue
        if (!isRowValid(board, row) || !isColValid(board, col) || !isBoxValid(board, row, col))
            errors.add(row to col)
    }
    for (cage in cages) {
        if (!isCageValid(board, cage))
            cage.cells.filter { (r, c) -> board[r][c].value != 0 }.forEach { errors.add(it) }
    }
    return errors
}

// ─────────────────────────────────────────────────────────────────────────────
// CAGE BORDER HELPERS
// ─────────────────────────────────────────────────────────────────────────────

enum class BorderSide { TOP, BOTTOM, LEFT, RIGHT }

fun hasCageBorder(row: Int, col: Int, side: BorderSide, cellToCage: Map<Pair<Int,Int>, Int>): Boolean {
    val cageId = cellToCage[row to col] ?: return true
    val neighbor = when (side) {
        BorderSide.TOP    -> cellToCage[(row - 1) to col]
        BorderSide.BOTTOM -> cellToCage[(row + 1) to col]
        BorderSide.LEFT   -> cellToCage[row to (col - 1)]
        BorderSide.RIGHT  -> cellToCage[row to (col + 1)]
    }
    return neighbor != cageId
}

fun isTopLeftOfCage(row: Int, col: Int, cage: Cage): Boolean =
    cage.cells.minWithOrNull(compareBy({ it.first }, { it.second })) == row to col

// ─────────────────────────────────────────────────────────────────────────────
// LEVEL SELECTOR SCREEN
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun LevelSelector(
    hasSave: Boolean,
    savedLevel: Level?,
    onLevelSelected: (Level) -> Unit,
    onResume: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A14))
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(24.dp))

        Text(
            "KILLER",
            color = Color(0xFFE94560),
            fontSize = 44.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 12.sp
        )
        Text(
            "SUDOKU",
            color = Color(0xFFCCD6F6),
            fontSize = 44.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 12.sp
        )
        Text(
            "Choose your challenge",
            color = Color(0xFF8892B0),
            fontSize = 13.sp,
            modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
        )

        // Resume card (if save exists)
        if (hasSave && savedLevel != null) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .clickable { onResume() },
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF1A2040),
                tonalElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("⏸", fontSize = 32.sp)
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            "RESUME GAME",
                            color = Color(0xFF4ECCA3),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )
                        Text(
                            "Continue ${savedLevel.emoji} ${savedLevel.displayName} puzzle",
                            color = Color(0xFF8892B0),
                            fontSize = 12.sp
                        )
                    }
                    Text("▶", color = Color(0xFF4ECCA3), fontSize = 20.sp)
                }
            }
        }

        Level.values().forEach { level ->
            LevelCard(level = level, onClick = { onLevelSelected(level) })
            Spacer(Modifier.height(12.dp))
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
fun LevelCard(level: Level, onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(level.surfaceColor, level.bgColor)
                )
            )
            .border(
                width = 1.dp,
                color = level.accentColor.copy(alpha = glowAlpha),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(level.emoji, fontSize = 36.sp)
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    level.displayName,
                    color = level.accentColor,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 3.sp
                )
                Text(
                    level.description,
                    color = Color(0xFF8892B0),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(level.accentColor.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("→", color = level.accentColor, fontSize = 18.sp)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// MAIN GAME COMPOSABLE
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun KillerSudokuGame() {
    val context = LocalContext.current

    // ── Navigation state ──────────────────────────────────────────────────────
    var currentScreen by remember { mutableStateOf<Screen>(Screen.LevelSelect) }
    var currentLevel  by remember { mutableStateOf(Level.EASY) }

    // ── Check for saved game ──────────────────────────────────────────────────
    val savedState = remember { loadGame(context) }

    when (currentScreen) {
        Screen.LevelSelect -> {
            LevelSelector(
                hasSave    = savedState != null,
                savedLevel = savedState?.level,
                onLevelSelected = { level ->
                    currentLevel  = level
                    currentScreen = Screen.Game(level, savedState = null)
                },
                onResume = {
                    if (savedState != null) {
                        currentLevel  = savedState.level
                        currentScreen = Screen.Game(savedState.level, savedState)
                    }
                }
            )
        }
        is Screen.Game -> {
            val gameScreen = currentScreen as Screen.Game
            KillerSudokuGameBoard(
                level      = gameScreen.level,
                savedState = gameScreen.savedState,
                context    = context,
                onBack     = { currentScreen = Screen.LevelSelect }
            )
        }
    }
}

sealed class Screen {
    object LevelSelect : Screen()
    data class Game(val level: Level, val savedState: SavedGameState?) : Screen()
}

// ─────────────────────────────────────────────────────────────────────────────
// GAME BOARD COMPOSABLE
// ─────────────────────────────────────────────────────────────────────────────

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun KillerSudokuGameBoard(
    level: Level,
    savedState: SavedGameState?,
    context: Context,
    onBack: () -> Unit
) {
    val cages = remember(level) { buildCages(level) }

    val cellToCage: Map<Pair<Int, Int>, Int> = remember(cages) {
        buildMap { cages.forEach { cage -> cage.cells.forEach { cell -> put(cell, cage.id) } } }
    }
    val cageById: Map<Int, Cage> = remember(cages) { cages.associateBy { it.id } }

    // ── Board state ───────────────────────────────────────────────────────────
    var board by remember {
        mutableStateOf(
            savedState?.board ?: Array(9) { Array(9) { CellState() } }
        )
    }
    var selectedCell  by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var noteMode      by remember { mutableStateOf(false) }
    var isComplete    by remember { mutableStateOf(false) }
    var errorCells    by remember { mutableStateOf(setOf<Pair<Int, Int>>()) }
    var errorCount    by remember { mutableStateOf(savedState?.errorCount ?: 0) }
    var elapsedSecs   by remember { mutableStateOf(savedState?.elapsedSeconds ?: 0L) }

    // ── Timer ─────────────────────────────────────────────────────────────────
    LaunchedEffect(isComplete) {
        if (!isComplete) {
            while (true) {
                kotlinx.coroutines.delay(1000L)
                elapsedSecs++
            }
        }
    }

    // ── Auto-save every 30 s ──────────────────────────────────────────────────
    LaunchedEffect(elapsedSecs) {
        if (elapsedSecs % 30L == 0L && elapsedSecs > 0L && !isComplete) {
            saveGame(context, board, level, elapsedSecs, errorCount)
        }
    }

    // ── Actions ───────────────────────────────────────────────────────────────
    fun placeNumber(num: Int) {
        val (r, c) = selectedCell ?: return
        if (board[r][c].isGiven) return
        val newBoard = board.map { it.clone() }.toTypedArray()

        if (noteMode && num != 0) {
            val notes = newBoard[r][c].notes.toMutableSet()
            if (num in notes) notes.remove(num) else notes.add(num)
            newBoard[r][c] = newBoard[r][c].copy(notes = notes, value = 0)
        } else {
            val prevVal = newBoard[r][c].value
            newBoard[r][c] = newBoard[r][c].copy(value = num, notes = emptySet())

            // Count error on wrong placement
            if (num != 0 && SOLUTION[r][c] != num && prevVal != num) {
                errorCount++
            }
        }

        val errors = computeErrors(newBoard, cages)
        board      = newBoard
        errorCells = errors
        isComplete = errors.isEmpty() && isBoardComplete(newBoard, cages)

        if (isComplete) saveGame(context, newBoard, level, elapsedSecs, errorCount)
    }

    fun resetBoard() {
        board         = Array(9) { Array(9) { CellState() } }
        selectedCell  = null
        noteMode      = false
        isComplete    = false
        errorCells    = emptySet()
        errorCount    = 0
        elapsedSecs   = 0L
        clearSave(context)
    }

    fun showSolution() {
        board         = Array(9) { r -> Array(9) { c -> CellState(value = SOLUTION[r][c], isGiven = true) } }
        selectedCell  = null
        errorCells    = emptySet()
        isComplete    = true
        saveGame(context, board, level, elapsedSecs, errorCount)
    }

    // ── Format timer ─────────────────────────────────────────────────────────
    val timerText = remember(elapsedSecs) {
        val m = elapsedSecs / 60
        val s = elapsedSecs % 60
        "%02d:%02d".format(m, s)
    }

    // ── UI ────────────────────────────────────────────────────────────────────
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(level.bgColor, Color(0xFF0A0A12)))
            )
            .padding(horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ── Top Bar ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back button
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(level.surfaceColor, RoundedCornerShape(10.dp))
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                Text("←", color = Color(0xFF8892B0), fontSize = 18.sp)
            }

            // Title + level badge
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "KILLER SUDOKU",
                    color = Color(0xFFCCD6F6),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 3.sp
                )
                Text(
                    "${level.emoji} ${level.displayName}",
                    color = level.accentColor,
                    fontSize = 11.sp,
                    letterSpacing = 2.sp
                )
            }

            // Timer
            Box(
                modifier = Modifier
                    .background(level.surfaceColor, RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    timerText,
                    color = level.accentColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // ── Stats row ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatChip(label = "Errors", value = "$errorCount", accent = Color(0xFFE94560))
            StatChip(
                label = "Filled",
                value = "${board.sumOf { row -> row.count { it.value != 0 } }}/81",
                accent = level.accentColor
            )
            StatChip(
                label = "Remaining",
                value = "${board.sumOf { row -> row.count { it.value == 0 } }}",
                accent = Color(0xFF8892B0)
            )
        }

        // ── Complete Banner ──
        if (isComplete) {
            CompletionBanner(level = level, elapsedSecs = elapsedSecs, errorCount = errorCount)
        }

        // ── GRID ──
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .border(2.dp, level.accentColor.copy(alpha = 0.8f), RoundedCornerShape(6.dp))
            ) {
                Column(Modifier.fillMaxSize()) {
                    for (row in 0..8) {
                        Row(
                            Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            for (col in 0..8) {
                                val cageId = cellToCage[row to col]
                                val cage   = cageById[cageId]
                                val cell   = board[row][col]
                                val isSelected   = selectedCell == row to col
                                val isHighlighted = selectedCell?.let { (sr, sc) ->
                                    sr == row || sc == col ||
                                        (sr / 3 == row / 3 && sc / 3 == col / 3)
                                } ?: false
                                val isSameValue = selectedCell?.let { (sr, sc) ->
                                    board[sr][sc].value != 0 && board[sr][sc].value == cell.value
                                } ?: false
                                val isError = (row to col) in errorCells

                                SudokuCell(
                                    row          = row,
                                    col          = col,
                                    cell         = cell,
                                    cage         = cage,
                                    isSelected   = isSelected,
                                    isHighlighted = isHighlighted,
                                    isSameValue  = isSameValue,
                                    isError      = isError,
                                    showCageSum  = cage != null && isTopLeftOfCage(row, col, cage),
                                    borderTop    = hasCageBorder(row, col, BorderSide.TOP,    cellToCage),
                                    borderBottom = hasCageBorder(row, col, BorderSide.BOTTOM, cellToCage),
                                    borderLeft   = hasCageBorder(row, col, BorderSide.LEFT,   cellToCage),
                                    borderRight  = hasCageBorder(row, col, BorderSide.RIGHT,  cellToCage),
                                    level        = level,
                                    modifier     = Modifier.weight(1f).fillMaxHeight(),
                                    onClick      = { selectedCell = if (isSelected) null else row to col }
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        // ── Controls Row ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Note mode toggle
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (noteMode) level.accentColor.copy(alpha = 0.2f) else level.surfaceColor)
                    .border(1.dp, if (noteMode) level.accentColor else Color(0xFF333355), RoundedCornerShape(10.dp))
                    .clickable { noteMode = !noteMode }
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("✏️", fontSize = 14.sp)
                    Text(
                        if (noteMode) "Notes" else "Notes",
                        color = if (noteMode) level.accentColor else Color(0xFF8892B0),
                        fontSize = 13.sp,
                        fontWeight = if (noteMode) FontWeight.Bold else FontWeight.Normal
                    )
                    if (noteMode) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(level.accentColor, CircleShape)
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Erase
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(level.surfaceColor, RoundedCornerShape(10.dp))
                        .clickable { placeNumber(0) },
                    contentAlignment = Alignment.Center
                ) {
                    Text("⌫", fontSize = 18.sp)
                }

                // Reset
                Box(
                    modifier = Modifier
                        .background(level.surfaceColor, RoundedCornerShape(10.dp))
                        .clickable { resetBoard() }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text("Reset", color = Color(0xFF8892B0), fontSize = 13.sp)
                }

                // Solve
                Box(
                    modifier = Modifier
                        .background(level.accentColor.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                        .border(1.dp, level.accentColor.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                        .clickable { showSolution() }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text("Solve", color = level.accentColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        // ── NUMBER PAD ──
        NumberPad(
            onNumber     = ::placeNumber,
            board        = board,
            selectedCell = selectedCell,
            level        = level
        )

        Spacer(Modifier.height(8.dp))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// STAT CHIP
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun StatChip(label: String, value: String, accent: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = accent, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Text(label, color = Color(0xFF8892B0), fontSize = 10.sp, letterSpacing = 1.sp)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// COMPLETION BANNER
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun CompletionBanner(level: Level, elapsedSecs: Long, errorCount: Int) {
    val m = elapsedSecs / 60; val s = elapsedSecs % 60
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .background(
                Brush.horizontalGradient(
                    listOf(level.accentColor.copy(alpha = 0.2f), level.accentColor.copy(alpha = 0.05f))
                ),
                RoundedCornerShape(12.dp)
            )
            .border(1.dp, level.accentColor.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("🎉  PUZZLE COMPLETE!", color = level.accentColor, fontSize = 15.sp, fontWeight = FontWeight.Black)
                Text(
                    "Time: %02d:%02d  •  Errors: $errorCount".format(m, s),
                    color = Color(0xFF8892B0),
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            Text(level.emoji, fontSize = 32.sp)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// CELL COMPOSABLE
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun SudokuCell(
    row: Int,
    col: Int,
    cell: CellState,
    cage: Cage?,
    isSelected: Boolean,
    isHighlighted: Boolean,
    isSameValue: Boolean,
    isError: Boolean,
    showCageSum: Boolean,
    borderTop: Boolean,
    borderBottom: Boolean,
    borderLeft: Boolean,
    borderRight: Boolean,
    level: Level,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    // ── Error pulse animation ─────────────────────────────────────────────────
    val errorShake by animateFloatAsState(
        if (isError) 1f else 0f,
        animationSpec = tween(200),
        label = "error_shake"
    )

    val bgColor by animateColorAsState(
        when {
            isSelected    -> level.accentColor.copy(alpha = 0.35f)
            isError       -> Color(0xFFE94560).copy(alpha = 0.22f)
            isSameValue   -> level.accentColor.copy(alpha = 0.12f)
            isHighlighted -> level.dimColor.copy(alpha = 0.25f)
            else          -> cage?.color?.copy(alpha = 0.30f) ?: Color(0xFF0D1020)
        },
        animationSpec = tween(120)
    )

    val scale by animateFloatAsState(
        if (isSelected) 0.93f else 1f,
        animationSpec = tween(100)
    )

    Box(
        modifier = modifier
            .scale(scale)
            .background(bgColor)
            .drawBehind {
                // ════════════════════════════════════════════════════════════
                // LAYER 1 – Standard Sudoku 3×3 box grid lines
                //   • Thick box dividers (every 3rd line): bright neutral white,
                //     clearly visible so the 3×3 boxes read at a glance.
                //   • Thin cell dividers: very muted, barely-there grey.
                //   These intentionally use WHITE/GREY – not the level accent –
                //   so they are visually distinct from cage borders.
                // ════════════════════════════════════════════════════════════
                val boxDividerColor = Color(0xFFFFFFFF).copy(alpha = 0.55f)   // bright neutral
                val cellDividerColor = Color(0xFFFFFFFF).copy(alpha = 0.08f)  // near-invisible
                val boxDividerPx    = 2.5.dp.toPx()
                val cellDividerPx   = 0.6.dp.toPx()

                if (col < 8) {
                    val isBoxEdge = (col + 1) % 3 == 0
                    drawLine(
                        color       = if (isBoxEdge) boxDividerColor else cellDividerColor,
                        start       = Offset(size.width, 0f),
                        end         = Offset(size.width, size.height),
                        strokeWidth = if (isBoxEdge) boxDividerPx else cellDividerPx
                    )
                }
                if (row < 8) {
                    val isBoxEdge = (row + 1) % 3 == 0
                    drawLine(
                        color       = if (isBoxEdge) boxDividerColor else cellDividerColor,
                        start       = Offset(0f, size.height),
                        end         = Offset(size.width, size.height),
                        strokeWidth = if (isBoxEdge) boxDividerPx else cellDividerPx
                    )
                }

                // ════════════════════════════════════════════════════════════
                // LAYER 2 – Killer Sudoku cage borders
                //   • Drawn INSET so they don't sit on top of the box lines.
                //   • Use level accent colour with a DASHED pattern so they
                //     look completely different from the box dividers.
                //   • Slightly transparent so box lines show through at corners.
                // ════════════════════════════════════════════════════════════
                val cageColor  = level.accentColor.copy(alpha = 0.80f)
                val cagePx     = 1.6.dp.toPx()
                val inset      = 3.dp.toPx()   // keeps cage line away from box line
                val dashEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 5f), 0f)

                if (borderTop)
                    drawLine(cageColor, Offset(inset, inset), Offset(size.width - inset, inset), cagePx, pathEffect = dashEffect)
                if (borderBottom)
                    drawLine(cageColor, Offset(inset, size.height - inset), Offset(size.width - inset, size.height - inset), cagePx, pathEffect = dashEffect)
                if (borderLeft)
                    drawLine(cageColor, Offset(inset, inset), Offset(inset, size.height - inset), cagePx, pathEffect = dashEffect)
                if (borderRight)
                    drawLine(cageColor, Offset(size.width - inset, inset), Offset(size.width - inset, size.height - inset), cagePx, pathEffect = dashEffect)

                // ════════════════════════════════════════════════════════════
                // LAYER 3 – Error flash overlay
                // ════════════════════════════════════════════════════════════
                if (isError && errorShake > 0f) {
                    drawRect(
                        color = Color(0xFFE94560).copy(alpha = 0.10f * errorShake),
                        size  = size
                    )
                }
            }
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        // ── Cage sum label (larger, more prominent) ───────────────────────────
        if (showCageSum && cage != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 2.dp, top = 1.dp)
                    .background(
                        level.accentColor.copy(alpha = 0.18f),
                        RoundedCornerShape(bottomEnd = 4.dp)
                    )
                    .padding(horizontal = 2.dp, vertical = 1.dp)
            ) {
                Text(
                    "${cage.sum}",
                    color = level.accentColor.copy(alpha = 0.95f),
                    fontSize = 9.sp,              // ← was 7.sp, now larger
                    fontWeight = FontWeight.ExtraBold,
                    lineHeight = 9.sp
                )
            }
        }

        // ── Cell value or notes ───────────────────────────────────────────────
        if (cell.value != 0) {
            Text(
                "${cell.value}",
                color = when {
                    isError      -> Color(0xFFE94560)
                    cell.isGiven -> level.accentColor
                    else         -> Color(0xFFCCD6F6)
                },
                fontSize = 20.sp,
                fontWeight = if (cell.isGiven) FontWeight.Black else FontWeight.SemiBold
            )
        } else if (cell.notes.isNotEmpty()) {
            NotesGrid(notes = cell.notes, level = level)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// NOTES GRID  (larger, clearer)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun BoxScope.NotesGrid(notes: Set<Int>, level: Level) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(2.dp),
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        for (noteRow in 0..2) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (noteCol in 0..2) {
                    val n = noteRow * 3 + noteCol + 1
                    if (n in notes) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "$n",
                                color = level.accentColor.copy(alpha = 0.85f),
                                fontSize = 7.5.sp,          // ← was 5.sp, now noticeably bigger
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// NUMBER PAD
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun NumberPad(
    onNumber: (Int) -> Unit,
    board: Array<Array<CellState>>,
    selectedCell: Pair<Int, Int>?,
    level: Level
) {
    val counts = IntArray(10)
    for (r in 0..8) for (c in 0..8) {
        val v = board[r][c].value
        if (v != 0) counts[v]++
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        for (n in 1..9) {
            val remaining    = 9 - counts[n]
            val isCurrentVal = selectedCell?.let { (r, c) -> board[r][c].value == n } ?: false

            NumberButton(
                number    = n,
                remaining = remaining,
                isActive  = isCurrentVal,
                level     = level,
                onClick   = { onNumber(n) }
            )
        }
    }
}

@Composable
fun NumberButton(
    number: Int,
    remaining: Int,
    isActive: Boolean,
    level: Level,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        if (isActive) level.accentColor else level.surfaceColor,
        animationSpec = tween(150)
    )

    val textColor by animateColorAsState(
        if (isActive) Color(0xFF0A0A14) else Color(0xFFCCD6F6),
        animationSpec = tween(150)
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(36.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(bgColor)
                .border(
                    width = if (isActive) 0.dp else 1.dp,
                    color = if (remaining == 0) Color(0xFF333355) else level.accentColor.copy(alpha = 0.25f),
                    shape = RoundedCornerShape(10.dp)
                )
                .clickable(enabled = remaining > 0, onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "$number",
                color = if (remaining == 0) Color(0xFF444466) else textColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Remaining indicator dots
        Row(
            modifier = Modifier
                .padding(top = 3.dp)
                .height(5.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            val dotsToShow = remaining.coerceAtMost(5)
            repeat(dotsToShow) {
                Box(
                    modifier = Modifier
                        .size(3.dp)
                        .background(
                            if (remaining <= 2) Color(0xFFE94560).copy(alpha = 0.7f)
                            else level.accentColor.copy(alpha = 0.5f),
                            CircleShape
                        )
                )
            }
            if (remaining == 0) {
                Text("✓", color = level.accentColor, fontSize = 8.sp)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ENTRY POINT
// ─────────────────────────────────────────────────────────────────────────────
// Place KillerSudokuGame() inside your Activity's setContent { } block.
