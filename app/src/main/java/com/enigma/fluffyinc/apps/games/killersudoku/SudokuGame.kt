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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack

// ─────────────────────────────────────────────
// MAIN COMPOSABLE - STANDALONE SUDOKU
// ─────────────────────────────────────────────

enum class SudokuScreen {
    MENU, PLAYING
}

@Composable
fun SudokuGame() {
    var currentScreen by remember { mutableStateOf(SudokuScreen.MENU) }
    var selectedDifficulty by remember { mutableStateOf(Difficulty.MEDIUM) }

    when (currentScreen) {
        SudokuScreen.MENU -> {
            SudokuMenu(
                onStartGame = { difficulty ->
                    selectedDifficulty = difficulty
                    currentScreen = SudokuScreen.PLAYING
                }
            )
        }
        SudokuScreen.PLAYING -> {
            SudokuPlayScreen(
                initialDifficulty = selectedDifficulty,
                onBack = { currentScreen = SudokuScreen.MENU }
            )
        }
    }
}

@Composable
fun SudokuMenu(onStartGame: (Difficulty) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A2E))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(100.dp),
            color = Color(0xFF0F3460),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(2.dp, Color(0xFF4ECCA3))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    "9",
                    color = Color(0xFF4ECCA3),
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        Text(
            "SUDOKU",
            color = Color(0xFF4ECCA3),
            fontSize = 48.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 8.sp
        )

        Text(
            "CHALLENGE YOUR MIND",
            color = Color(0xFF8892B0),
            fontSize = 12.sp,
            letterSpacing = 2.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(64.dp))

        Text(
            "SELECT DIFFICULTY",
            color = Color(0xFFCCD6F6).copy(alpha = 0.6f),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp
        )

        Spacer(Modifier.height(16.dp))

        Difficulty.entries.forEach { difficulty ->
            Button(
                onClick = { onStartGame(difficulty) },
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(56.dp)
                    .padding(vertical = 4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0F3460),
                    contentColor = Color(0xFF4ECCA3)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    difficulty.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun SudokuPlayScreen(
    initialDifficulty: Difficulty,
    onBack: () -> Unit
) {
    val generator = remember { KillerSudokuGenerator() }
    
    // We use Difficulty to control how many numbers are revealed
    var difficulty by remember { mutableStateOf(initialDifficulty) }
    
    // The "puzzle" state contains the solution and the cage structure (which we mostly ignore for basic Sudoku)
    var currentPuzzle by remember {
        mutableStateOf(generator.generate(difficulty.toLevel()))
    }

    // Board state
    var board by remember {
        mutableStateOf(createInitialBoard(currentPuzzle, difficulty))
    }
    
    var selectedCell by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var noteMode by remember { mutableStateOf(false) }
    var isComplete by remember { mutableStateOf(false) }
    var errorCells by remember { mutableStateOf(setOf<Pair<Int, Int>>()) }

    fun startNewGame(newDiff: Difficulty) {
        difficulty = newDiff
        currentPuzzle = generator.generate(newDiff.toLevel())
        board = createInitialBoard(currentPuzzle, newDiff)
        selectedCell = null
        noteMode = false
        isComplete = false
        errorCells = emptySet()
    }

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

        board = newBoard
        errorCells = errors
        
        // Check if complete
        var complete = true
        for (row in 0..8) {
            for (col in 0..8) {
                if (newBoard[row][col].value == 0 || (row to col) in errors) {
                    complete = false
                    break
                }
            }
            if (!complete) break
        }
        isComplete = complete
    }

    // ── UI ────────────────────────────────────
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A2E))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFF4ECCA3)
                )
            }
            
            Text(
                "SUDOKU",
                color = Color(0xFF4ECCA3),
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 4.sp,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            
            // To balance the layout
            Box(Modifier.size(48.dp))
        }

        // Difficulty Selector
        Row(
            modifier = Modifier.padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Difficulty.entries.forEach { diff ->
                FilterChip(
                    selected = difficulty == diff,
                    onClick = { startNewGame(diff) },
                    label = { Text(diff.name, fontSize = 10.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF4ECCA3),
                        selectedLabelColor = Color(0xFF1A1A2E),
                        containerColor = Color(0xFF16213E),
                        labelColor = Color(0xFF8892B0)
                    )
                )
            }
        }

        if (isComplete) {
            Surface(
                color = Color(0xFF0F3460),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Text(
                    "🎉  Sudoku Solved!",
                    color = Color(0xFF4ECCA3),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                )
            }
        }

        // ── GRID ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .border(3.dp, Color(0xFF4ECCA3), RoundedCornerShape(4.dp))
        ) {
            Column(Modifier.fillMaxSize()) {
                for (row in 0..8) {
                    Row(Modifier.weight(1f).fillMaxWidth()) {
                        for (col in 0..8) {
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

                            BasicSudokuCell(
                                row = row,
                                col = col,
                                cell = cell,
                                isSelected = isSelected,
                                isHighlighted = isHighlighted,
                                isSameValue = isSameValue,
                                isError = isError,
                                level = difficulty.toLevel(),
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                onClick = { selectedCell = if (isSelected) null else row to col }
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── CONTROLS ──
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
                    onClick = { startNewGame(difficulty) },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF8892B0))
                ) { Text("New") }
                
                Button(
                    onClick = {
                        board = Array(9) { r -> Array(9) { c -> 
                            CellState(value = currentPuzzle.solution[r][c], isGiven = true) 
                        } }
                        errorCells = emptySet()
                        isComplete = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F3460))
                ) { Text("Solve", color = Color(0xFF4ECCA3)) }
            }
        }

        Spacer(Modifier.height(12.dp))

        NumberPad(
            onNumber = ::placeNumber,
            board = board,
            selectedCell = selectedCell,
            level = difficulty.toLevel()
        )

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
fun BasicSudokuCell(
    row: Int,
    col: Int,
    cell: CellState,
    isSelected: Boolean,
    isHighlighted: Boolean,
    isSameValue: Boolean,
    isError: Boolean,
    level: Level,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        when {
            isSelected   -> Color(0xFF4ECCA3).copy(alpha = 0.35f)
            isError      -> Color(0xFFE94560).copy(alpha = 0.25f)
            isSameValue  -> Color(0xFF4ECCA3).copy(alpha = 0.15f)
            isHighlighted -> Color(0xFF0F3460)
            else         -> Color(0xFF16213E)
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
                val thickColor = Color(0xFF4ECCA3).copy(alpha = 0.8f)
                val thinColor = Color(0xFF8892B0).copy(alpha = 0.3f)
                val thickPx = 2.dp.toPx()
                val thinPx = 0.5.dp.toPx()

                if (col < 8) {
                    val isThick = (col + 1) % 3 == 0
                    drawLine(
                        color = if (isThick) thickColor else thinColor,
                        start = Offset(size.width, 0f),
                        end = Offset(size.width, size.height),
                        strokeWidth = if (isThick) thickPx else thinPx
                    )
                }

                if (row < 8) {
                    val isThick = (row + 1) % 3 == 0
                    drawLine(
                        color = if (isThick) thickColor else thinColor,
                        start = Offset(0f, size.height),
                        end = Offset(size.width, size.height),
                        strokeWidth = if (isThick) thickPx else thinPx
                    )
                }
            }
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
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
            Box(Modifier.fillMaxSize()) {
                NotesGrid(notes = cell.notes, level = level)
            }
        }
    }
}

private fun createInitialBoard(puzzle: GeneratedPuzzle, difficulty: Difficulty): Array<Array<CellState>> {
    val board = Array(9) { Array(9) { CellState() } }
    
    // Reveal count based on difficulty (inverse of Killer Sudoku logic)
    val revealCount = when(difficulty) {
        Difficulty.EASY -> 40
        Difficulty.MEDIUM -> 32
        Difficulty.HARD -> 26
        Difficulty.EXPERT -> 20
    }
    
    val cells = (0..80).shuffled().take(revealCount)
    for (idx in cells) {
        val r = idx / 9
        val c = idx % 9
        board[r][c] = CellState(value = puzzle.solution[r][c], isGiven = true)
    }
    
    return board
}
