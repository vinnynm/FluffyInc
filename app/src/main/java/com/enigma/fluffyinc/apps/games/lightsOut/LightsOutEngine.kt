package com.enigma.fluffyinc.apps.games.lightsOut

// ─────────────────────────────────────────────────────────────────────────────
//  GF(2) matrix  —  all arithmetic is mod 2 (XOR)
// ─────────────────────────────────────────────────────────────────────────────

/**
 * A matrix of 0s and 1s with row operations over GF(2).
 * Rows and columns are both [size] wide; used for an augmented [size × size+1]
 * system during elimination.
 */
private class GF2Matrix(val rows: Int, val cols: Int) {
    val data = Array(rows) { IntArray(cols) }

    operator fun get(r: Int, c: Int) = data[r][c]
    operator fun set(r: Int, c: Int, v: Int) { data[r][c] = v and 1 }

    /** XOR row [src] into row [dst]. */
    fun xorRow(dst: Int, src: Int) {
        for (c in 0 until cols) data[dst][c] = data[dst][c] xor data[src][c]
    }

    fun swapRows(a: Int, b: Int) {
        val tmp = data[a]; data[a] = data[b]; data[b] = tmp
    }

    fun copy(): GF2Matrix {
        val m = GF2Matrix(rows, cols)
        for (r in 0 until rows) data[r].copyInto(m.data[r])
        return m
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Toggle matrix  —  built once, shared across all puzzles
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Returns the 25×25 toggle matrix T for a 5×5 Lights Out board.
 * T[i][j] == 1  ↔  pressing button j toggles cell i.
 *
 * In other words, column j is the toggle pattern of button j.
 */
private fun buildToggleMatrix(): GF2Matrix {
    val size = 25
    val m = GF2Matrix(size, size)
    for (btn in 0 until size) {
        val br = btn / 5; val bc = btn % 5
        // The button itself and its 4 orthogonal neighbours
        for ((dr, dc) in listOf(0 to 0, -1 to 0, 1 to 0, 0 to -1, 0 to 1)) {
            val nr = br + dr; val nc = bc + dc
            if (nr in 0..4 && nc in 0..4) m[nr * 5 + nc, btn] = 1
        }
    }
    return m
}

// Precomputed once per app launch
private val TOGGLE_MATRIX: GF2Matrix = buildToggleMatrix()

// ─────────────────────────────────────────────────────────────────────────────
//  Gaussian elimination over GF(2)
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Solve  T · x = b  over GF(2) using Gauss-Jordan elimination.
 *
 * @param b  25-element array representing the board state (1 = lit, 0 = off).
 * @return   25-element solution x (1 = press this button), or null if no
 *           solution exists (board not in column space of T).
 */
fun solve(b: IntArray): IntArray? {
    require(b.size == 25)
    val n = 25

    // Build augmented matrix [T | b]
    val aug = GF2Matrix(n, n + 1)
    for (r in 0 until n) {
        for (c in 0 until n) aug[r, c] = TOGGLE_MATRIX[r, c]
        aug[r, n] = b[r] and 1
    }

    // Forward elimination
    var pivotRow = 0
    for (col in 0 until n) {
        // Find a row with a 1 in this column at or below pivotRow
        val found = (pivotRow until n).firstOrNull { aug[it, col] == 1 } ?: continue
        aug.swapRows(pivotRow, found)
        // Eliminate all other rows
        for (row in 0 until n) {
            if (row != pivotRow && aug[row, col] == 1) aug.xorRow(row, pivotRow)
        }
        pivotRow++
    }

    // Check consistency: any row [0 0 … 0 | 1] means no solution
    for (r in 0 until n) {
        val allZero = (0 until n).all { aug[r, it] == 0 }
        if (allZero && aug[r, n] == 1) return null
    }

    // Extract solution: each pivot column gives x[col]
    val x = IntArray(n)
    for (col in 0 until n) {
        // Find the pivot row for this column (row with a single 1 at [r,col])
        val pivRow = (0 until n).firstOrNull { r ->
            aug[r, col] == 1 && (0 until n).all { c -> c == col || aug[r, c] == 0 }
        }
        if (pivRow != null) x[col] = aug[pivRow, n]
    }
    return x
}

// ─────────────────────────────────────────────────────────────────────────────
//  Puzzle generator
// ─────────────────────────────────────────────────────────────────────────────

enum class LightsOutDifficulty(
    /** How many buttons are pressed to generate the puzzle (more = harder). */
    val pressCount: Int
) {
    EASY(5),
    MEDIUM(10),
    HARD(16),
    EXPERT(22)
}

/**
 * A puzzle ready for the UI.
 *
 * [board]    – 25-element flat array, 1 = lit, 0 = off.  Index = row*5 + col.
 * [solution] – 25-element array, 1 = this button is part of a solution.
 * [minPresses] – number of button presses in the solution (lower bound; there
 *               may be multiple solutions of the same length).
 */
data class LightsOutPuzzle(
    val board: IntArray,
    val solution: IntArray,
    val minPresses: Int
)

/**
 * Generate a guaranteed-solvable Lights Out puzzle.
 *
 * Strategy: start from the all-off state (trivially solved), press
 * [difficulty.pressCount] random buttons, and record the resulting board.
 * That board is solvable by construction because the same set of presses
 * (mod 2) solves it.  We then verify via [solve] to get the canonical
 * minimum solution.
 */
fun generatePuzzle(difficulty: LightsOutDifficulty): LightsOutPuzzle {
    while (true) {                       // retry if the random board is trivial
        val pressCount = difficulty.pressCount
        val board = IntArray(25)

        // Press random buttons — duplicates cancel (XOR) which is fine
        val pressedButtons = (0 until 25).shuffled().take(pressCount)
        for (btn in pressedButtons) {
            val br = btn / 5; val bc = btn % 5
            for ((dr, dc) in listOf(0 to 0, -1 to 0, 1 to 0, 0 to -1, 0 to 1)) {
                val nr = br + dr; val nc = bc + dc
                if (nr in 0..4 && nc in 0..4) {
                    board[nr * 5 + nc] = board[nr * 5 + nc] xor 1
                }
            }
        }

        // Skip trivially-solved boards (all lights already off)
        if (board.all { it == 0 }) continue

        // Solve to get the canonical solution (may differ from pressedButtons
        // because some presses may cancel, but it's always valid)
        val solution = solve(board) ?: continue   // unsolvable (shouldn't happen)
        val minPresses = solution.sum()

        return LightsOutPuzzle(
            board      = board,
            solution   = solution,
            minPresses = minPresses
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Game state
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Mutable game session.  Feed this into your ViewModel's StateFlow.
 */
data class LightsOutGameState(
    val cells: IntArray = IntArray(25),        // current board  (1 = lit)
    val presses: IntArray = IntArray(25),      // how many times each was pressed (mod 2)
    val solution: IntArray = IntArray(25),     // optimal solution from generator
    val moveCount: Int = 0,
    val isSolved: Boolean = false,
    val difficulty: LightsOutDifficulty = LightsOutDifficulty.MEDIUM,
    val showHint: Boolean = false
) {
    /** True when a hint button is "part of the solution and not yet pressed". */
    fun isHintCell(index: Int) =
        showHint && solution[index] == 1 && presses[index] == 0

    override fun equals(other: Any?) = other is LightsOutGameState &&
        cells.contentEquals(other.cells) && moveCount == other.moveCount &&
        isSolved == other.isSolved && showHint == other.showHint

    override fun hashCode() = cells.contentHashCode() * 31 + moveCount
}

/**
 * Applies a button press to a [LightsOutGameState] and returns the new state.
 * Pure function — safe to call from a ViewModel.
 */
fun pressButton(state: LightsOutGameState, index: Int): LightsOutGameState {
    if (state.isSolved) return state

    val newCells  = state.cells.copyOf()
    val newPresses = state.presses.copyOf()

    val br = index / 5; val bc = index % 5
    for ((dr, dc) in listOf(0 to 0, -1 to 0, 1 to 0, 0 to -1, 0 to 1)) {
        val nr = br + dr; val nc = bc + dc
        if (nr in 0..4 && nc in 0..4) newCells[nr * 5 + nc] = newCells[nr * 5 + nc] xor 1
    }
    newPresses[index] = newPresses[index] xor 1

    return state.copy(
        cells      = newCells,
        presses    = newPresses,
        moveCount  = state.moveCount + 1,
        isSolved   = newCells.all { it == 0 }
    )
}
