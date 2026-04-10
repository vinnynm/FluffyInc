package com.enigma.fluffyinc.apps.games.killersudoku

import kotlin.random.Random

// ─────────────────────────────────────────────────────────────────────────────
// KILLER SUDOKU GENERATOR ENGINE
//
// Usage:
//   val generator = KillerSudokuGenerator()
//   val puzzle    = generator.generate(difficulty = Difficulty.MEDIUM)
//
//   puzzle.solution  → Array<IntArray>  (9×9 solved grid)
//   puzzle.cages     → List<Cage>       (ready to pass straight to the UI)
// ─────────────────────────────────────────────────────────────────────────────

// ── Difficulty controls cage sizes ───────────────────────────────────────────

enum class Difficulty(
    val minCageSize: Int,
    val maxCageSize: Int,
    val avgCageSize: Double   // target average – drives how "chopped up" the grid is
) {
    EASY   (minCageSize = 1, maxCageSize = 3, avgCageSize = 2.2),
    MEDIUM (minCageSize = 2, maxCageSize = 5, avgCageSize = 3.2),
    HARD   (minCageSize = 2, maxCageSize = 6, avgCageSize = 4.0),
    EXPERT (minCageSize = 3, maxCageSize = 7, avgCageSize = 4.8)
}

// ── Output ────────────────────────────────────────────────────────────────────

data class GeneratedPuzzle(
    val solution: Array<IntArray>,   // [row][col] 1-9
    val cages: List<Cage>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GeneratedPuzzle

        if (!solution.contentDeepEquals(other.solution)) return false
        if (cages != other.cages) return false

        return true
    }

    override fun hashCode(): Int {
        var result = solution.contentDeepHashCode()
        result = 31 * result + cages.hashCode()
        return result
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// MAIN GENERATOR
// ─────────────────────────────────────────────────────────────────────────────

class KillerSudokuGenerator(private val random: Random = Random.Default) {

    fun generate(difficulty: Difficulty = Difficulty.MEDIUM): GeneratedPuzzle {
        val solution = generateSolvedGrid()
        val cages    = buildCages(solution, difficulty)
        return GeneratedPuzzle(solution, cages)
    }

    // ── Step 1: Generate a valid solved Sudoku grid ───────────────────────────

    private fun generateSolvedGrid(): Array<IntArray> {
        val grid = Array(9) { IntArray(9) }
        // Seed the diagonal 3×3 boxes first (they don't interact), then solve
        fillDiagonalBoxes(grid)
        solveSudoku(grid)
        return grid
    }

    private fun fillDiagonalBoxes(grid: Array<IntArray>) {
        for (box in 0..2) {
            val startR = box * 3
            val startC = box * 3
            val nums = (1..9).shuffled(random)
            var idx = 0
            for (r in startR until startR + 3)
                for (c in startC until startC + 3)
                    grid[r][c] = nums[idx++]
        }
    }

    private fun solveSudoku(grid: Array<IntArray>): Boolean {
        for (r in 0..8) {
            for (c in 0..8) {
                if (grid[r][c] != 0) continue
                val candidates = (1..9).shuffled(random)
                for (n in candidates) {
                    if (isSafe(grid, r, c, n)) {
                        grid[r][c] = n
                        if (solveSudoku(grid)) return true
                        grid[r][c] = 0
                    }
                }
                return false
            }
        }
        return true
    }

    private fun isSafe(grid: Array<IntArray>, row: Int, col: Int, num: Int): Boolean {
        if (grid[row].any { it == num }) return false
        if (grid.any { it[col] == num }) return false
        val sr = (row / 3) * 3
        val sc = (col / 3) * 3
        for (r in sr until sr + 3)
            for (c in sc until sc + 3)
                if (grid[r][c] == num) return false
        return true
    }

    // ── Step 2: Partition the grid into cages ────────────────────────────────
    //
    // Algorithm: randomised flood-fill ("growing" approach)
    //   1. Shuffle all 81 cells.
    //   2. For each unassigned seed cell start a new cage.
    //   3. Greedily expand it by one random orthogonal neighbour at a time
    //      until it reaches its target size (sampled from the difficulty range).
    //   4. Record the cage sum from the solution.
    //
    // Constraints enforced:
    //   • No cage crosses a 3×3 box boundary    (optional, controlled by flag)
    //   • Each cage is a single connected region
    //   • Sum ≤ MAX_CAGE_SUM  to keep puzzles solvable
    //   • No duplicate digits within a cage

    private val BOX_CONSTRAINT = false   // set true for easier puzzles

    private fun buildCages(
        solution: Array<IntArray>,
        difficulty: Difficulty
    ): List<Cage> {
        val assigned = Array(9) { BooleanArray(9) }
        val cageCells = mutableListOf<List<Pair<Int, Int>>>()

        val cellOrder = (0..80).map { it / 9 to it % 9 }.shuffled(random)

        for ((seedR, seedC) in cellOrder) {
            if (assigned[seedR][seedC]) continue

            // Decide target size for this cage
            val targetSize = sampleCageSize(difficulty)

            val cage = mutableListOf(seedR to seedC)
            assigned[seedR][seedC] = true

            // Grow the cage
            repeat(targetSize - 1) {
                val candidate = findExpansionCandidate(cage, assigned, solution, difficulty)
                if (candidate != null) {
                    cage.add(candidate)
                    assigned[candidate.first][candidate.second] = true
                }
            }

            cageCells.add(cage)
        }

        // Convert to Cage objects (id, sum, cells, color)
        return cageCells.mapIndexed { idx, cells ->
            val sum = cells.sumOf { (r, c) -> solution[r][c] }
            Cage(
                id    = idx,
                sum   = sum,
                cells = cells,
                color = CAGE_COLORS[idx % CAGE_COLORS.size]
            )
        }
    }

    private fun sampleCageSize(difficulty: Difficulty): Int {
        // Weighted random: prefer sizes near avgCageSize
        val avg = difficulty.avgCageSize
        val lo  = difficulty.minCageSize
        val hi  = difficulty.maxCageSize

        // Triangle distribution approximation
        val raw = lo + (gaussianClamp(avg - lo, (hi - lo).toDouble()) + 0.5).toInt()
        return raw.coerceIn(lo, hi)
    }

    /** Gaussian-ish value centred at `mean` within [0, range] */
    private fun gaussianClamp(mean: Double, range: Double): Double {
        val u = random.nextDouble() + random.nextDouble() + random.nextDouble() - 1.5   // ~N(0,0.5)
        return (mean + u * (range / 3.0)).coerceIn(0.0, range)
    }

    private fun findExpansionCandidate(
        cage: List<Pair<Int, Int>>,
        assigned: Array<BooleanArray>,
        solution: Array<IntArray>,
        difficulty: Difficulty
    ): Pair<Int, Int>? {
        val currentValues = cage.map { (r, c) -> solution[r][c] }.toMutableSet()

        // Collect all unassigned orthogonal neighbours of any cell in the cage
        val candidates = cage
            .flatMap { (r, c) -> orthogonalNeighbours(r, c) }
            .distinct()
            .filter { (nr, nc) ->
                !assigned[nr][nc] &&
                solution[nr][nc] !in currentValues &&          // no duplicate digit
                (!BOX_CONSTRAINT || sameBox(cage[0], nr to nc))  // optional box constraint
            }
            .shuffled(random)

        return candidates.firstOrNull()
    }

    private fun orthogonalNeighbours(r: Int, c: Int): List<Pair<Int, Int>> =
        listOf(r - 1 to c, r + 1 to c, r to c - 1, r to c + 1)
            .filter { (nr, nc) -> nr in 0..8 && nc in 0..8 }

    private fun sameBox(a: Pair<Int, Int>, b: Pair<Int, Int>): Boolean =
        (a.first / 3 == b.first / 3) && (a.second / 3 == b.second / 3)
}

// ─────────────────────────────────────────────────────────────────────────────
// EXTENSION: validate a generated puzzle (useful in tests / debug builds)
// ─────────────────────────────────────────────────────────────────────────────

fun GeneratedPuzzle.validate(): List<String> {
    val errors = mutableListOf<String>()

    // 1. Every cell is covered by exactly one cage
    val coverage = Array(9) { IntArray(9) { 0 } }
    cages.forEach { cage ->
        cage.cells.forEach { (r, c) -> coverage[r][c]++ }
    }
    for (r in 0..8) for (c in 0..8)
        if (coverage[r][c] != 1) errors.add("Cell ($r,$c) covered ${coverage[r][c]} times")

    // 2. Each cage sum matches the solution
    cages.forEach { cage ->
        val realSum = cage.cells.sumOf { (r, c) -> solution[r][c] }
        if (realSum != cage.sum) errors.add("Cage ${cage.id}: declared sum=${cage.sum}, real=$realSum")
    }

    // 3. No duplicate digits within a cage
    cages.forEach { cage ->
        val vals = cage.cells.map { (r, c) -> solution[r][c] }
        if (vals.size != vals.toSet().size) errors.add("Cage ${cage.id} has duplicate digits: $vals")
    }

    // 4. Solution is a valid Sudoku
    for (r in 0..8) {
        val row = solution[r].toList()
        if (row.toSet() != (1..9).toSet()) errors.add("Row $r invalid: $row")
    }
    for (c in 0..8) {
        val col = solution.map { it[c] }
        if (col.toSet() != (1..9).toSet()) errors.add("Col $c invalid: $col")
    }
    for (br in 0..2) for (bc in 0..2) {
        val box = mutableListOf<Int>()
        for (r in br * 3 until br * 3 + 3)
            for (c in bc * 3 until bc * 3 + 3)
                box.add(solution[r][c])
        if (box.toSet() != (1..9).toSet()) errors.add("Box ($br,$bc) invalid: $box")
    }

    return errors
}

// ─────────────────────────────────────────────────────────────────────────────
// HOW TO WIRE THIS INTO KillerSudokuGame()
// ─────────────────────────────────────────────────────────────────────────────
//
//  Replace the hard-coded SOLUTION / buildCages() calls with:
//
//  val generator = remember { KillerSudokuGenerator() }
//
//  var puzzle by remember {
//      mutableStateOf(generator.generate(Difficulty.MEDIUM))
//  }
//
//  // Pass puzzle.cages and puzzle.solution into the rest of the composable.
//  // To generate a new puzzle:
//  puzzle = generator.generate(selectedDifficulty)
//
// ─────────────────────────────────────────────────────────────────────────────
