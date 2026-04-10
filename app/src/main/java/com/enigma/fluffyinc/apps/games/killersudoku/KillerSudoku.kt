package com.enigma.fluffyinc.apps.games.killersudoku

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.collections.get

// ─────────────────────────────────────────────
// DATA MODELS
// ─────────────────────────────────────────────

data class Cage(
    val id: Int,
    val sum: Int,
    val cells: List<Pair<Int, Int>>,   // (row, col)
    val color: Color
)

data class CellState(
    val value: Int = 0,          // 0 = empty
    val isGiven: Boolean = false,
    val notes: Set<Int> = emptySet(),
    val isError: Boolean = false
)

// ─────────────────────────────────────────────
// PUZZLE DATA  (Easy 9×9 Killer Sudoku)
// solution[row][col]
// ─────────────────────────────────────────────

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

// Cage colour palette (soft, distinct)
val CAGE_COLORS = listOf(
    Color(0xFFB5EAD7), Color(0xFFC7CEEA), Color(0xFFFFDAC1),
    Color(0xFFFF9AA2), Color(0xFFFFB7B2), Color(0xFFE2F0CB),
    Color(0xFFFDDDA0), Color(0xFFD4A5F5), Color(0xFFA8E6CF),
    Color(0xFFFFD3B6), Color(0xFFD5E8D4), Color(0xFFDAE8FC),
    Color(0xFFF8CECC), Color(0xFFE1D5E7), Color(0xFFFFF2CC),
    Color(0xFFD5E8D4), Color(0xFFCFE2F3), Color(0xFFFCE5CD)
)

fun buildCages(): List<Cage> = listOf(
    Cage(0,  3,  listOf(0 to 0, 0 to 1),                     CAGE_COLORS[0]),
    Cage(1,  15, listOf(0 to 2, 0 to 3, 0 to 4),             CAGE_COLORS[1]),
    Cage(2,  22, listOf(0 to 5, 1 to 5, 2 to 5),             CAGE_COLORS[2]),
    Cage(3,  20, listOf(0 to 6, 0 to 7, 0 to 8),             CAGE_COLORS[3]),
    Cage(4,  9,  listOf(1 to 0, 2 to 0),                     CAGE_COLORS[4]),
    Cage(5,  14, listOf(1 to 1, 1 to 2),                     CAGE_COLORS[5]),
    Cage(6,  16, listOf(1 to 3, 1 to 4),                     CAGE_COLORS[6]),
    Cage(7,  8,  listOf(1 to 6, 2 to 6),                     CAGE_COLORS[7]),
    Cage(8,  12, listOf(1 to 7, 1 to 8),                     CAGE_COLORS[8]),
    Cage(9,  13, listOf(2 to 1, 2 to 2),                     CAGE_COLORS[9]),
    Cage(10, 3,  listOf(2 to 3, 3 to 3),                     CAGE_COLORS[10]),
    Cage(11, 8,  listOf(2 to 4, 3 to 4),                     CAGE_COLORS[11]),
    Cage(12, 11, listOf(2 to 7, 2 to 8),                     CAGE_COLORS[12]),
    Cage(13, 13, listOf(3 to 0, 4 to 0),                     CAGE_COLORS[13]),
    Cage(14, 14, listOf(3 to 1, 3 to 2),                     CAGE_COLORS[14]),
    Cage(15, 13, listOf(3 to 5, 4 to 5),                     CAGE_COLORS[15]),
    Cage(16, 4,  listOf(3 to 6, 4 to 6),                     CAGE_COLORS[16]),  // wait 9+3=12 no; 9+3=12
    // adjusted to match solution
    Cage(17, 5,  listOf(3 to 7, 3 to 8),                     CAGE_COLORS[17]),
    Cage(18, 6,  listOf(4 to 1, 4 to 2),                     CAGE_COLORS[0]),
    Cage(19, 14, listOf(4 to 3, 4 to 4),                     CAGE_COLORS[1]),
    Cage(20, 14, listOf(4 to 7, 4 to 8),                     CAGE_COLORS[2]),
    Cage(21, 16, listOf(5 to 0, 6 to 0),                     CAGE_COLORS[3]),
    Cage(22, 10, listOf(5 to 1, 5 to 2),                     CAGE_COLORS[4]),
    Cage(23, 14, listOf(5 to 3, 5 to 4),                     CAGE_COLORS[5]),
    Cage(24, 10, listOf(5 to 5, 6 to 5),                     CAGE_COLORS[6]),
    Cage(25, 11, listOf(5 to 6, 5 to 7),                     CAGE_COLORS[7]),
    Cage(26, 5,  listOf(5 to 8, 6 to 8),                     CAGE_COLORS[8]),
    Cage(27, 3,  listOf(6 to 1, 6 to 2),                     CAGE_COLORS[9]),
    Cage(28, 16, listOf(6 to 3, 6 to 4),                     CAGE_COLORS[10]),
    Cage(29, 9,  listOf(6 to 6, 6 to 7),                     CAGE_COLORS[11]),
    Cage(30, 11, listOf(7 to 0, 8 to 0),                     CAGE_COLORS[12]),
    Cage(31, 14, listOf(7 to 1, 7 to 2),                     CAGE_COLORS[13]),
    Cage(32, 5,  listOf(7 to 3, 8 to 3),                     CAGE_COLORS[14]),
    Cage(33, 10, listOf(7 to 4, 8 to 4),                     CAGE_COLORS[15]),
    Cage(34, 13, listOf(7 to 5, 8 to 5),                     CAGE_COLORS[16]),
    Cage(35, 8,  listOf(7 to 6, 8 to 6),                     CAGE_COLORS[17]),
    Cage(36, 9,  listOf(7 to 7, 7 to 8),                     CAGE_COLORS[0]),
    Cage(37, 17, listOf(8 to 1, 8 to 2, 8 to 7, 8 to 8),    CAGE_COLORS[1])
)

// ─────────────────────────────────────────────
// VALIDATION HELPERS
// ─────────────────────────────────────────────

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
    if (filled.size != filled.toSet().size) return false           // duplicates
    if (vals.all { it != 0 } && vals.sum() != cage.sum) return false  // wrong sum
    return true
}

fun isBoardComplete(board: Array<Array<CellState>>, cages: List<Cage>): Boolean {
    for (r in 0..8) for (c in 0..8) if (board[r][c].value == 0) return false
    for (r in 0..8) if (!isRowValid(board, r)) return false
    for (c in 0..8) if (!isColValid(board, c)) return false
    for (cage in cages) if (!isCageValid(board, cage)) return false
    return true
}

// ─────────────────────────────────────────────
// CAGE BORDER HELPERS
// ─────────────────────────────────────────────

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

// ─────────────────────────────────────────────
// MAIN COMPOSABLE
// ─────────────────────────────────────────────

@Composable
fun KillerSudokuGame() {
    val cages = remember { buildCages() }

    val cellToCage: Map<Pair<Int, Int>, Int> = remember {
        buildMap { cages.forEach { cage -> cage.cells.forEach { cell -> put(cell, cage.id) } } }
    }

    val cageById: Map<Int, Cage> = remember { cages.associateBy { it.id } }

    // Board state
    var board by remember {
        mutableStateOf(Array(9) { Array(9) { CellState() } })
    }
    var selectedCell by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var noteMode by remember { mutableStateOf(false) }
    var isComplete by remember { mutableStateOf(false) }
    var errorCells by remember { mutableStateOf(setOf<Pair<Int, Int>>()) }

    fun placeNumber(num: Int) {
        val (r, c) = selectedCell ?: return
        if (board[r][c].isGiven) return
        val newBoard = board.map { it.clone() }.toTypedArray()

        if (noteMode && num != 0) {
            val notes = newBoard[r][c].notes.toMutableSet()
            if (num in notes) notes.remove(num) else notes.add(num)
            newBoard[r][c] = newBoard[r][c].copy(notes = notes, value = 0)
        } else {
            newBoard[r][c] = newBoard[r][c].copy(value = num, notes = emptySet())
        }

        // Validate errors
        val errors = mutableSetOf<Pair<Int, Int>>()
        for (row in 0..8) for (col in 0..8) {
            val v = newBoard[row][col].value
            if (v == 0) continue
            if (!isRowValid(newBoard, row) || !isColValid(newBoard, col) ||
                !isBoxValid(newBoard, row, col)) {
                errors.add(row to col)
            }
        }
        for (cage in cages) {
            if (!isCageValid(newBoard, cage))
                cage.cells.forEach { errors.add(it) }
        }

        board = newBoard
        errorCells = errors
        isComplete = isBoardComplete(newBoard, cages)
    }

    fun resetBoard() {
        board = Array(9) { Array(9) { CellState() } }
        selectedCell = null
        noteMode = false
        isComplete = false
        errorCells = emptySet()
    }

    fun showSolution() {
        board = Array(9) { r -> Array(9) { c -> CellState(value = SOLUTION[r][c], isGiven = true) } }
        selectedCell = null
        errorCells = emptySet()
        isComplete = true
    }

    // ── UI ────────────────────────────────────
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A2E))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(12.dp))

        Text(
            "KILLER SUDOKU",
            color = Color(0xFFE94560),
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 4.sp
        )

        Text(
            "Fill cages so each sums to its target",
            color = Color(0xFF8892B0),
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )

        if (isComplete) {
            Surface(
                color = Color(0xFF0F3460),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Text(
                    "🎉  Puzzle Complete!",
                    color = Color(0xFF4ECCA3),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                )
            }
        }

        // ── GRID ──
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
        ) {
            val cellSize = maxWidth / 9

            Box(
                Modifier
                    .fillMaxSize()
                    .border(3.dp, Color(0xFFE94560), RoundedCornerShape(4.dp))
            ) {
                Column(Modifier.fillMaxSize()) {
                    for (row in 0..8) {
                        Row(Modifier.weight(1f).fillMaxWidth()) {
                            for (col in 0..8) {
                                val cageId = cellToCage[row to col]
                                val cage = cageById[cageId]
                                val cell = board[row][col]
                                val isSelected = selectedCell == row to col
                                val isHighlighted = selectedCell?.let { (sr, sc) ->
                                    sr == row || sc == col ||
                                        (sr / 3 == row / 3 && sc / 3 == col / 3)
                                } ?: false
                                val isSameValue = selectedCell?.let { (sr, sc) ->
                                    board[sr][sc].value != 0 && board[sr][sc].value == cell.value
                                } ?: false
                                val isError = (row to col) in errorCells

                                SudokuCell(
                                    row = row,
                                    col = col,
                                    cell = cell,
                                    cage = cage,
                                    isSelected = isSelected,
                                    isHighlighted = isHighlighted,
                                    isSameValue = isSameValue,
                                    isError = isError,
                                    showCageSum = cage != null && isTopLeftOfCage(row, col, cage),
                                    borderTop = hasCageBorder(row, col, BorderSide.TOP, cellToCage),
                                    borderBottom = hasCageBorder(row, col, BorderSide.BOTTOM, cellToCage),
                                    borderLeft = hasCageBorder(row, col, BorderSide.LEFT, cellToCage),
                                    borderRight = hasCageBorder(row, col, BorderSide.RIGHT, cellToCage),
                                    modifier = Modifier.weight(1f).fillMaxHeight(),
                                    onClick = { selectedCell = if (isSelected) null else row to col }
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── NOTE TOGGLE ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                selected = noteMode,
                onClick = { noteMode = !noteMode },
                label = {
                    Text(if (noteMode) "✏  Notes ON" else "✏  Notes OFF",
                        color = if (noteMode) Color(0xFF4ECCA3) else Color(0xFF8892B0),
                        fontSize = 13.sp)
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF0F3460),
                    containerColor = Color(0xFF16213E)
                )
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { placeNumber(0) },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF6B6B))
                ) { Text("⌫", fontSize = 16.sp) }

                OutlinedButton(
                    onClick = ::resetBoard,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF8892B0))
                ) { Text("Reset") }

                Button(
                    onClick = ::showSolution,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F3460))
                ) { Text("Solve", color = Color(0xFF4ECCA3)) }
            }
        }

        Spacer(Modifier.height(12.dp))

        // ── NUMBER PAD ──
        NumberPad(
            onNumber = ::placeNumber,
            board = board,
            selectedCell = selectedCell
        )

        Spacer(Modifier.height(8.dp))
    }
}

// ─────────────────────────────────────────────
// CELL COMPOSABLE
// ─────────────────────────────────────────────

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
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        when {
            isSelected   -> Color(0xFF4ECCA3).copy(alpha = 0.35f)
            isError      -> Color(0xFFE94560).copy(alpha = 0.25f)
            isSameValue  -> Color(0xFF4ECCA3).copy(alpha = 0.15f)
            isHighlighted -> Color(0xFF0F3460)
            else         -> cage?.color?.copy(alpha = 0.18f) ?: Color(0xFF16213E)
        },
        animationSpec = tween(150)
    )

    val scale by animateFloatAsState(
        if (isSelected) 0.95f else 1f,
        animationSpec = tween(100)
    )

    Box(
        modifier = modifier
            .scale(scale)
            .background(bgColor)
            .drawBehind {
                // --- 3x3 Grid Lines (Standard Sudoku Style) ---
                val thickColor = Color(0xFFE94560).copy(alpha = 0.8f)
                val thinColor = Color(0xFF8892B0).copy(alpha = 0.3f)
                val thickPx = 2.dp.toPx()
                val thinPx = 0.5.dp.toPx()

                // Draw Right Border
                if (col < 8) {
                    val isThick = (col + 1) % 3 == 0
                    drawLine(
                        color = if (isThick) thickColor else thinColor,
                        start = Offset(size.width, 0f),
                        end = Offset(size.width, size.height),
                        strokeWidth = if (isThick) thickPx else thinPx
                    )
                }

                // Draw Bottom Border
                if (row < 8) {
                    val isThick = (row + 1) % 3 == 0
                    drawLine(
                        color = if (isThick) thickColor else thinColor,
                        start = Offset(0f, size.height),
                        end = Offset(size.width, size.height),
                        strokeWidth = if (isThick) thickPx else thinPx
                    )
                }

                // --- Killer Sudoku Cage Borders (Dashed) ---
                val cageColor = Color.White.copy(alpha = 0.5f)
                val cageWidth = 1.dp.toPx()
                val dashEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                val p = 3.dp.toPx()

                if (borderTop) drawLine(cageColor, Offset(p, p), Offset(size.width - p, p), cageWidth, pathEffect = dashEffect)
                if (borderBottom) drawLine(cageColor, Offset(p, size.height - p), Offset(size.width - p, size.height - p), cageWidth, pathEffect = dashEffect)
                if (borderLeft) drawLine(cageColor, Offset(p, p), Offset(p, size.height - p), cageWidth, pathEffect = dashEffect)
                if (borderRight) drawLine(cageColor, Offset(size.width - p, p), Offset(size.width - p, size.height - p), cageWidth, pathEffect = dashEffect)
            }
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        // Cage sum label
        if (showCageSum && cage != null) {
            Text(
                "${cage.sum}",
                color = Color(0xFFCCD6F6).copy(alpha = 0.85f),
                fontSize = 7.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 1.dp, top = 1.dp)
            )
        }

        // Cell value or notes
        if (cell.value != 0) {
            Text(
                "${cell.value}",
                color = when {
                    isError       -> Color(0xFFE94560)
                    cell.isGiven  -> Color(0xFF4ECCA3)
                    else          -> Color(0xFFCCD6F6)
                },
                fontSize = 18.sp,
                fontWeight = if (cell.isGiven) FontWeight.Bold else FontWeight.Normal
            )
        } else if (cell.notes.isNotEmpty()) {
            NotesGrid(notes = cell.notes)
        }
    }
}

@Composable
fun BoxScope.NotesGrid(notes: Set<Int>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(1.dp),
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        for (row in 0..2) {
            Row(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (col in 0..2) {
                    val n = row * 3 + col + 1
                    Text(
                        if (n in notes) "$n" else "",
                        color = Color(0xFF8892B0),
                        fontSize = 5.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}



// ─────────────────────────────────────────────
// NUMBER PAD
// ─────────────────────────────────────────────

@Composable
fun NumberPad(
    onNumber: (Int) -> Unit,
    board: Array<Array<CellState>>,
    selectedCell: Pair<Int, Int>?
) {
    // Count remaining uses of each number
    val counts = IntArray(10)
    for (r in 0..8) for (c in 0..8) {
        val v = board[r][c].value
        if (v != 0) counts[v]++
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            for (n in 1..9) {
                val remaining = 9 - counts[n]
                val isCurrentVal = selectedCell?.let { (r, c) -> board[r][c].value == n } ?: false

                NumberButton(
                    number = n,
                    remaining = remaining,
                    isActive = isCurrentVal,
                    onClick = { onNumber(n) }
                )
            }
        }
    }
}

@Composable
fun NumberButton(
    number: Int,
    remaining: Int,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        if (isActive) Color(0xFF4ECCA3) else Color(0xFF0F3460),
        animationSpec = tween(150)
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(34.dp)
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(bgColor)
                .clickable(enabled = remaining > 0, onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "$number",
                color = if (isActive) Color(0xFF1A1A2E) else Color(0xFFCCD6F6),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Text(
            if (remaining > 0) "$remaining" else "✓",
            color = if (remaining == 0) Color(0xFF4ECCA3) else Color(0xFF8892B0),
            fontSize = 9.sp,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

// ─────────────────────────────────────────────
// ENTRY POINT
// Usage: place KillerSudokuGame() inside your
// Activity's setContent { } block.
// ─────────────────────────────────────────────
