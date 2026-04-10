package com.enigma.fluffyinc.apps.games.killersudoku

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class SudokuSaveData(
    val difficulty: Difficulty,
    val solution: List<List<Int>>,
    val board: List<List<CellState>>,
    val isComplete: Boolean
)

class SudokuViewModel : ViewModel() {
    private val gson = Gson()
    private val PREFS_NAME = "sudoku_prefs"
    private val KEY_SAVE_STATE = "saved_game"

    var hasSavedGame by mutableStateOf(false)
        private set

    // State for the active game
    var board by mutableStateOf<Array<Array<CellState>>>(Array(9) { Array(9) { CellState() } })
    var currentPuzzle by mutableStateOf<GeneratedPuzzle?>(null)
    var difficulty by mutableStateOf(Difficulty.MEDIUM)
    var isComplete by mutableStateOf(false)
    var errorCells by mutableStateOf(setOf<Pair<Int, Int>>())
    var selectedCell by mutableStateOf<Pair<Int, Int>?>(null)
    var noteMode by mutableStateOf(false)

    fun checkSavedGame(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        hasSavedGame = prefs.contains(KEY_SAVE_STATE)
    }

    fun saveGame(context: Context) {
        val puzzle = currentPuzzle ?: return
        if (isComplete) {
            clearSave(context)
            return
        }

        val saveData = SudokuSaveData(
            difficulty = difficulty,
            solution = puzzle.solution.map { it.toList() },
            board = board.map { it.toList() },
            isComplete = isComplete
        )

        val json = gson.toJson(saveData)
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_SAVE_STATE, json)
            .apply()
        hasSavedGame = true
    }

    fun loadGame(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_SAVE_STATE, null) ?: return false

        return try {
            val type = object : TypeToken<SudokuSaveData>() {}.type
            val saveData: SudokuSaveData = gson.fromJson(json, type)

            this.difficulty = saveData.difficulty
            this.isComplete = saveData.isComplete
            
            // Reconstruct puzzle
            val solutionArray = saveData.solution.map { it.toIntArray() }.toTypedArray()
            this.currentPuzzle = GeneratedPuzzle(solutionArray, emptyList()) // Cages not needed for basic Sudoku
            
            // Reconstruct board
            this.board = saveData.board.map { it.toTypedArray() }.toTypedArray()
            
            // Re-validate errors
            validateBoard()
            
            true
        } catch (e: Exception) {
            false
        }
    }

    fun clearSave(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_SAVE_STATE)
            .apply()
        hasSavedGame = false
    }

    fun startNewGame(newDiff: Difficulty, generator: KillerSudokuGenerator) {
        difficulty = newDiff
        val puzzle = generator.generate(newDiff)
        currentPuzzle = puzzle
        board = createInitialBoard(puzzle, newDiff)
        selectedCell = null
        noteMode = false
        isComplete = false
        errorCells = emptySet()
    }

    fun placeNumber(num: Int, context: Context) {
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

        board = newBoard
        validateBoard()
        checkCompletion()
        saveGame(context)
    }

    private fun validateBoard() {
        val errors = mutableSetOf<Pair<Int, Int>>()
        for (row in 0..8) for (col in 0..8) {
            val v = board[row][col].value
            if (v == 0) continue
            if (!isRowValid(board, row) || !isColValid(board, col) ||
                !isBoxValid(board, row, col)) {
                errors.add(row to col)
            }
        }
        errorCells = errors
    }

    private fun checkCompletion() {
        var complete = true
        for (row in 0..8) {
            for (col in 0..8) {
                if (board[row][col].value == 0 || (row to col) in errorCells) {
                    complete = false
                    break
                }
            }
            if (!complete) break
        }
        isComplete = complete
    }

    private fun createInitialBoard(puzzle: GeneratedPuzzle, difficulty: Difficulty): Array<Array<CellState>> {
        val board = Array(9) { Array(9) { CellState() } }
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
}
