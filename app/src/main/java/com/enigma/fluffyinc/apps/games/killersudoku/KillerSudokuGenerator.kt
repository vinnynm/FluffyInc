package com.enigma.fluffyinc.apps.games.killersudoku

import kotlin.random.Random

// ─────────────────────────────────────────────────────────────────────────────
// KILLER SUDOKU GENERATOR ENGINE
//
// Usage:
//   val generator = KillerSudokuGenerator()
//   val puzzle    = generator.generate(difficulty = Difficulty.MEDIUM, level = Level.MEDIUM)
//
//   puzzle.solution  → Array<IntArray>  (9×9 solved grid)
//   puzzle.cages     → List<Cage>       (ready to pass straight to the UI)
// ─────────────────────────────────────────────────────────────────────────────

// ── Difficulty controls cage sizes ───────────────────────────────────────────

enum class Difficulty(
    val minCageSize: Int,
    val maxCageSize: Int,
    val avgCageSize: Double
) {
    EASY   (minCageSize = 1, maxCageSize = 3, avgCageSize = 2.2),
    MEDIUM (minCageSize = 2, maxCageSize = 5, avgCageSize = 3.2),
    HARD   (minCageSize = 2, maxCageSize = 6, avgCageSize = 4.0),
    EXPERT (minCageSize = 3, maxCageSize = 7, avgCageSize = 4.8)
}

// ── Map Level → Difficulty ────────────────────────────────────────────────────
fun Level.toDifficulty() = when (this) {
    Level.EASY   -> Difficulty.EASY
    Level.MEDIUM -> Difficulty.MEDIUM
    Level.HARD   -> Difficulty.HARD
    Level.EXPERT -> Difficulty.EXPERT
}

fun Difficulty.toLevel() = when (this) {
    Difficulty.EASY   -> Level.EASY
    Difficulty.MEDIUM -> Level.MEDIUM
    Difficulty.HARD   -> Level.HARD
    Difficulty.EXPERT -> Level.EXPERT
}

// ── Output ────────────────────────────────────────────────────────────────────

data class GeneratedPuzzle(
    val solution: Array<IntArray>,
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

    /**
     * Generate a complete puzzle for the given [Level].
     * Pass [level] to automatically select difficulty and cage colour palette.
     */
    fun generate(
        level: Level = Level.EASY,
        difficulty: Difficulty = level.toDifficulty()
    ): GeneratedPuzzle {
        val solution = generateSolvedGrid()
        val cages    = buildCages(solution, difficulty, level)
        return GeneratedPuzzle(solution, cages)
    }

    // ── Step 1: Valid solved Sudoku grid ─────────────────────────────────────

    private fun generateSolvedGrid(): Array<IntArray> {
        val grid = Array(9) { IntArray(9) }
        fillDiagonalBoxes(grid)
        solveSudoku(grid)
        return grid
    }

    private fun fillDiagonalBoxes(grid: Array<IntArray>) {
        for (box in 0..2) {
            val startR = box * 3; val startC = box * 3
            val nums = (1..9).shuffled(random); var idx = 0
            for (r in startR until startR + 3)
                for (c in startC until startC + 3)
                    grid[r][c] = nums[idx++]
        }
    }

    private fun solveSudoku(grid: Array<IntArray>): Boolean {
        for (r in 0..8) for (c in 0..8) {
            if (grid[r][c] != 0) continue
            for (n in (1..9).shuffled(random)) {
                if (isSafe(grid, r, c, n)) {
                    grid[r][c] = n
                    if (solveSudoku(grid)) return true
                    grid[r][c] = 0
                }
            }
            return false
        }
        return true
    }

    private fun isSafe(grid: Array<IntArray>, row: Int, col: Int, num: Int): Boolean {
        if (grid[row].any { it == num }) return false
        if (grid.any { it[col] == num }) return false
        val sr = (row / 3) * 3; val sc = (col / 3) * 3
        for (r in sr until sr + 3) for (c in sc until sc + 3)
            if (grid[r][c] == num) return false
        return true
    }

    // ── Step 2: Partition the grid into cages ────────────────────────────────

    private val BOX_CONSTRAINT = false

    private fun buildCages(
        solution: Array<IntArray>,
        difficulty: Difficulty,
        level: Level
    ): List<Cage> {
        val assigned   = Array(9) { BooleanArray(9) }
        val cageCells  = mutableListOf<List<Pair<Int, Int>>>()
        val colors     = cageColorsForLevel(level)
        val cellOrder  = (0..80).map { it / 9 to it % 9 }.shuffled(random)

        for ((seedR, seedC) in cellOrder) {
            if (assigned[seedR][seedC]) continue
            val targetSize = sampleCageSize(difficulty)
            val cage = mutableListOf(seedR to seedC)
            assigned[seedR][seedC] = true

            repeat(targetSize - 1) {
                val candidate = findExpansionCandidate(cage, assigned, solution, difficulty)
                if (candidate != null) {
                    cage.add(candidate)
                    assigned[candidate.first][candidate.second] = true
                }
            }
            cageCells.add(cage)
        }

        return cageCells.mapIndexed { idx, cells ->
            val sum = cells.sumOf { (r, c) -> solution[r][c] }
            Cage(
                id    = idx,
                sum   = sum,
                cells = cells,
                color = colors[idx % colors.size]
            )
        }
    }

    private fun sampleCageSize(difficulty: Difficulty): Int {
        val avg = difficulty.avgCageSize
        val lo  = difficulty.minCageSize
        val hi  = difficulty.maxCageSize
        val raw = lo + (gaussianClamp(avg - lo, (hi - lo).toDouble()) + 0.5).toInt()
        return raw.coerceIn(lo, hi)
    }

    private fun gaussianClamp(mean: Double, range: Double): Double {
        val u = random.nextDouble() + random.nextDouble() + random.nextDouble() - 1.5
        return (mean + u * (range / 3.0)).coerceIn(0.0, range)
    }

    private fun findExpansionCandidate(
        cage: List<Pair<Int, Int>>,
        assigned: Array<BooleanArray>,
        solution: Array<IntArray>,
        difficulty: Difficulty
    ): Pair<Int, Int>? {
        val currentValues = cage.map { (r, c) -> solution[r][c] }.toMutableSet()
        val candidates = cage
            .flatMap { (r, c) -> orthogonalNeighbours(r, c) }
            .distinct()
            .filter { (nr, nc) ->
                !assigned[nr][nc] &&
                solution[nr][nc] !in currentValues &&
                (!BOX_CONSTRAINT || sameBox(cage[0], nr to nc))
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
// VALIDATION
// ─────────────────────────────────────────────────────────────────────────────

fun GeneratedPuzzle.validate(): List<String> {
    val errors = mutableListOf<String>()

    val coverage = Array(9) { IntArray(9) { 0 } }
    cages.forEach { cage -> cage.cells.forEach { (r, c) -> coverage[r][c]++ } }
    for (r in 0..8) for (c in 0..8)
        if (coverage[r][c] != 1) errors.add("Cell ($r,$c) covered ${coverage[r][c]} times")

    cages.forEach { cage ->
        val realSum = cage.cells.sumOf { (r, c) -> solution[r][c] }
        if (realSum != cage.sum) errors.add("Cage ${cage.id}: declared sum=${cage.sum}, real=$realSum")
    }

    cages.forEach { cage ->
        val vals = cage.cells.map { (r, c) -> solution[r][c] }
        if (vals.size != vals.toSet().size) errors.add("Cage ${cage.id} has duplicate digits: $vals")
    }

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
// HOW TO WIRE INTO KillerSudokuGame()
// ─────────────────────────────────────────────────────────────────────────────
//
//  val generator = remember { KillerSudokuGenerator() }
//
//  var puzzle by remember {
//      mutableStateOf(generator.generate(level = Level.MEDIUM))
//  }
//
//  // To generate a new puzzle for a chosen level:
//  puzzle = generator.generate(level = selectedLevel)
//
// ─────────────────────────────────────────────────────────────────────────────
