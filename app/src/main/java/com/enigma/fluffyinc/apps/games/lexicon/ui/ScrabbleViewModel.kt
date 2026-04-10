package com.enigma.fluffyinc.apps.games.lexicon.ui

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.enigma.fluffyinc.apps.games.lexicon.model.AiDecision
import com.enigma.fluffyinc.apps.games.lexicon.model.AiDifficulty
import com.enigma.fluffyinc.apps.games.lexicon.model.AiOpponent
import com.enigma.fluffyinc.apps.games.lexicon.data.WordDictionaryManager
import com.enigma.fluffyinc.apps.games.lexicon.data.WordLibraryUpdate
import com.enigma.fluffyinc.apps.games.lexicon.model.PlacedTile
import com.enigma.fluffyinc.apps.games.lexicon.model.PlayResult
import com.enigma.fluffyinc.apps.games.lexicon.model.Player
import com.enigma.fluffyinc.apps.games.lexicon.model.ScrabbleGame
import com.enigma.fluffyinc.apps.games.lexicon.model.Tile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ScrabbleGameViewModel(
    private val dictionaryManager: WordDictionaryManager,
    context: Context
) : ViewModel() {

    private val prefs: SharedPreferences = context.getSharedPreferences("lexicon_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()
    private var aiOpponent: AiOpponent? = null

    private val _isVsAi = MutableStateFlow(false)

    private val game = ScrabbleGame()

    // Pending blank tile placement: row, col awaiting letter assignment
    private var pendingBlankRow = -1
    private var pendingBlankCol = -1

    init {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            dictionaryManager.load()
            dictionaryManager.loadLargeDictionary()
            game.updateDictionary(dictionaryManager.dictionary.value)
            game.updateLargeDictionary(dictionaryManager.largeDictionary.value)
            
            val hasSave = prefs.contains("game_state")
            
            _uiState.update {
                it.copy(
                    isLoading = false,
                    isDictionaryLoaded = dictionaryManager.dictionary.value.isNotEmpty(),
                    dictionarySize = dictionaryManager.dictionary.value.size,
                    errorMessage = dictionaryManager.error.value,
                    canResume = hasSave
                )
            }
        }
    }

    private fun saveGame() {
        val saveData = LexiconSaveData(
            board = game.board.map { row -> row.toList() },
            players = game.players.toList(),
            currentPlayer = game.currentPlayer,
            bag = game.bag.toList(),
            consecutiveSkips = game.consecutiveSkips,
            isVsAi = _isVsAi.value,
            aiDifficulty = _uiState.value.aiDifficulty
        )
        prefs.edit().putString("game_state", gson.toJson(saveData)).apply()
        _uiState.update { it.copy(canResume = true) }
    }

    fun resumeGame() {
        val json = prefs.getString("game_state", null) ?: return
        val saveData: LexiconSaveData = gson.fromJson(json, LexiconSaveData::class.java)

        game.reset()
        saveData.board.forEachIndexed { r, row ->
            row.forEachIndexed { c, tile ->
                game.board[r][c] = tile
            }
        }
        game.players.clear()
        game.players.addAll(saveData.players)
        game.currentPlayer = saveData.currentPlayer
        game.bag.clear()
        game.bag.addAll(saveData.bag)
        game.consecutiveSkips = saveData.consecutiveSkips
        
        _isVsAi.value = saveData.isVsAi
        if (saveData.isVsAi && saveData.aiDifficulty != null) {
            aiOpponent = AiOpponent(
                fullDictionary = dictionaryManager.dictionary.value,
                difficulty = saveData.aiDifficulty
            )
        } else {
            aiOpponent = null
        }

        _uiState.update {
            it.copy(
                gameState = GameState.PLAYING,
                isVsAi = saveData.isVsAi,
                aiDifficulty = saveData.aiDifficulty
            )
        }
        updateStateFromGame()
    }

    private fun clearSave() {
        prefs.edit().remove("game_state").apply()
        _uiState.update { it.copy(canResume = false) }
    }

    fun retryLoadingDictionary() {
        viewModelScope.launch {
            dictionaryManager.load()
            game.updateDictionary(dictionaryManager.dictionary.value)
            _uiState.update {
                it.copy(
                    isDictionaryLoaded = dictionaryManager.dictionary.value.isNotEmpty(),
                    dictionarySize = dictionaryManager.dictionary.value.size,
                    errorMessage = dictionaryManager.error.value
                )
            }
        }
    }

    fun startVsAi(player1Name: String, difficulty: AiDifficulty) {
        if (!_uiState.value.isDictionaryLoaded) return
        _isVsAi.value = true
        aiOpponent = AiOpponent(
            fullDictionary = dictionaryManager.dictionary.value,
            difficulty     = difficulty
        )
        game.startGame(player1Name, difficulty.displayName)
        _uiState.update {
            it.copy(
                gameState    = GameState.PLAYING,
                isVsAi       = true,
                aiDifficulty = difficulty
            )
        }
        updateStateFromGame()
        saveGame()
    }

    fun startGame(player1Name: String, player2Name: String) {
        if (!_uiState.value.isDictionaryLoaded) return
        _isVsAi.value = false
        aiOpponent = null
        game.startGame(player1Name, player2Name)
        _uiState.update { it.copy(gameState = GameState.PLAYING, isVsAi = false, aiDifficulty = null) }
        updateStateFromGame()
        saveGame()
    }

    private fun maybeRunAiTurn() {
        val ai = aiOpponent ?: return
        if (!_isVsAi.value) return
        if (game.currentPlayer != 1) return
        if (game.isGameOver) return

        viewModelScope.launch {
            _uiState.update { it.copy(isAiThinking = true) }
            delay(900)

            val decision = withContext(Dispatchers.Default) {
                ai.decideMove(
                    board   = game.board,
                    rack    = game.players[1].rack,
                    bagSize = game.getBagSize()
                )
            }

            when (decision) {
                is AiDecision.PlayWord -> {
                    decision.move.tiles.forEach { t ->
                        game.placeTile(t.row, t.col, t.letter)
                    }
                    val result = game.playWord()
                    when (result) {
                        is PlayResult.Success -> {
                            _uiState.update {
                                it.copy(
                                    lastPlayMessage = "${ai.difficulty.displayName} AI played " +
                                            "${result.words.joinToString(", ")} for +${result.score}",
                                    lastPlayScore = result.score,
                                    isAiThinking = false
                                )
                            }
                            saveGame()
                        }
                        is PlayResult.Error -> {
                            game.skipTurn()
                            _uiState.update {
                                it.copy(lastPlayMessage = "${ai.difficulty.displayName} AI skipped", isAiThinking = false)
                            }
                            saveGame()
                        }
                    }
                }

                is AiDecision.ExchangeTiles -> {
                    game.exchangeTiles()
                    _uiState.update {
                        it.copy(lastPlayMessage = "${ai.difficulty.displayName} AI exchanged tiles", isAiThinking = false)
                    }
                    saveGame()
                }

                is AiDecision.Skip -> {
                    game.skipTurn()
                    _uiState.update {
                        it.copy(lastPlayMessage = "${ai.difficulty.displayName} AI skipped", isAiThinking = false)
                    }
                    saveGame()
                }
            }

            updateStateFromGame()
        }
    }

    private fun updateStateFromGame() {
        _uiState.update {
            it.copy(
                gameState      = if (game.isGameOver) GameState.GAME_OVER else GameState.PLAYING,
                currentPlayer  = game.currentPlayer,
                player1        = game.players[0].copy(),
                player2        = game.players[1].copy(),
                board          = Array(15) { r -> Array(15) { c -> game.board[r][c]?.copy() } },
                placedThisTurn = game.placedThisTurn.toList(),
                bagSize        = game.getBagSize()
            )
        }
    }

    fun selectTile(rackIndex: Int) {
        _uiState.update { state ->
            val current = state.selectedTile
            state.copy(selectedTile = if (current == rackIndex) null else rackIndex)
        }
    }

    /**
     * Place a tile at (row, col).
     * If the selected tile is a blank (?), we pause and ask the player to
     * choose a letter via [confirmBlankLetter] before the tile is committed.
     */
    fun placeTile(row: Int, col: Int) {
        val selectedIdx = _uiState.value.selectedTile ?: return
        val player = game.players[game.currentPlayer]
        if (selectedIdx >= player.rack.size) return

        val letter = player.rack[selectedIdx]

        if (letter == '?') {
            // Remember where the blank will land; show the picker dialog
            pendingBlankRow = row
            pendingBlankCol = col
            _uiState.update { it.copy(
                pendingBlankPickerRow = row,
                pendingBlankPickerCol = col,
                showBlankPicker       = true
            ) }
        } else {
            if (game.placeTile(row, col, letter)) {
                _uiState.update { it.copy(selectedTile = null, lastPlayMessage = "") }
                updateStateFromGame()
            }
        }
    }

    /**
     * If the cell at (row, col) has a tile placed THIS turn, return it to the rack.
     */
    fun onCellClick(row: Int, col: Int) {
        val state = _uiState.value
        val existingPlaced = state.placedThisTurn.find { it.row == row && it.col == col }
        
        if (existingPlaced != null) {
            game.recallTile(row, col)
            updateStateFromGame()
        } else {
            placeTile(row, col)
        }
    }

    /**
     * Called when the player picks a letter for their blank tile.
     * The model places the blank as that letter (with 0 points and isBlank=true).
     */
    fun confirmBlankLetter(chosenLetter: Char) {
        val row = pendingBlankRow
        val col = pendingBlankCol
        if (row < 0 || col < 0) return

        // Place the blank using '?' so the model treats it as a blank tile,
        // then immediately rename it to the chosen letter inside placedThisTurn.
        if (game.placeTile(row, col, '?')) {
            // Find the just-placed blank and swap its display letter
            val idx = game.placedThisTurn.indexOfFirst { it.row == row && it.col == col && it.isBlank }
            if (idx >= 0) {
                val old = game.placedThisTurn[idx]
                game.placedThisTurn[idx] = old.copy(letter = chosenLetter.uppercaseChar())
            }
        }

        pendingBlankRow = -1
        pendingBlankCol = -1
        _uiState.update { it.copy(
            selectedTile          = null,
            showBlankPicker       = false,
            pendingBlankPickerRow = -1,
            pendingBlankPickerCol = -1
        ) }
        updateStateFromGame()
    }

    /** Cancel placing the blank tile (player changed their mind). */
    fun cancelBlankPicker() {
        pendingBlankRow = -1
        pendingBlankCol = -1
        _uiState.update { it.copy(
            showBlankPicker       = false,
            pendingBlankPickerRow = -1,
            pendingBlankPickerCol = -1
        ) }
    }

    fun recallAllTiles() {
        game.recallAllTiles()
        _uiState.update { it.copy(selectedTile = null) }
        updateStateFromGame()
    }

    fun playWord() {
        val result = game.playWord()
        when (result) {
            is PlayResult.Success -> {
                updateStateFromGame()
                _uiState.update {
                    it.copy(
                        lastPlayMessage = "Played ${result.words.joinToString(", ")} for +${result.score}",
                        lastPlayScore   = result.score,
                        selectedTile    = null
                    )
                }
                if (game.isGameOver) {
                    clearSave()
                } else {
                    saveGame()
                    maybeRunAiTurn()
                }
            }
            is PlayResult.Error -> {
                _uiState.update { it.copy(lastPlayMessage = result.message) }
            }
        }
    }

    fun skipTurn() {
        game.skipTurn()
        updateStateFromGame()
        if (game.isGameOver) {
            clearSave()
        } else {
            saveGame()
            maybeRunAiTurn()
        }
    }

    fun exchangeTiles() {
        if (game.exchangeTiles()) {
            updateStateFromGame()
            saveGame()
            maybeRunAiTurn()
        } else {
            _uiState.update { it.copy(lastPlayMessage = "Not enough tiles in bag") }
        }
    }

    fun shuffleRack() { game.shuffleRack(); updateStateFromGame(); saveGame() }

    fun resetGame() {
        game.reset()
        clearSave()
        _uiState.update {
            GameUiState(
                isDictionaryLoaded = it.isDictionaryLoaded,
                dictionarySize = it.dictionarySize,
                gameState = GameState.MENU
            )
        }
    }

    fun clearMessage() { _uiState.update { it.copy(lastPlayMessage = "", lastPlayScore = 0) } }

    fun downloadUpdate() { game.toString() }
}

data class GameUiState(
    val gameState: GameState = GameState.MENU,
    val currentPlayer: Int = 0,
    val player1: Player = Player("Player 1", 0, emptyList()),
    val player2: Player = Player("Player 2", 0, emptyList()),
    val board: Array<Array<Tile?>> = Array(15) { arrayOfNulls(15) },
    val placedThisTurn: List<PlacedTile> = emptyList(),
    val selectedTile: Int? = null,
    val lastPlayMessage: String = "",
    val lastPlayScore: Int = 0,
    val isDictionaryLoaded: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val dictionarySize: Int = 0,
    val bagSize: Int = 0,
    val updateAvailable: WordLibraryUpdate? = null,
    val isDownloading: Boolean = false,
    val isVsAi: Boolean = false,
    val aiDifficulty: AiDifficulty? = null,
    val isAiThinking: Boolean = false,
    val canResume: Boolean = false,
    // ── Blank tile picker ──────────────────────────────────────────────────────
    val showBlankPicker: Boolean = false,
    val pendingBlankPickerRow: Int = -1,
    val pendingBlankPickerCol: Int = -1,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is GameUiState) return false
        return gameState == other.gameState &&
               currentPlayer == other.currentPlayer &&
               player1 == other.player1 &&
               player2 == other.player2 &&
               board.contentDeepEquals(other.board) &&
               placedThisTurn == other.placedThisTurn &&
               selectedTile == other.selectedTile &&
               lastPlayMessage == other.lastPlayMessage &&
               lastPlayScore == other.lastPlayScore &&
               isDictionaryLoaded == other.isDictionaryLoaded &&
               isLoading == other.isLoading &&
               errorMessage == other.errorMessage &&
               dictionarySize == other.dictionarySize &&
               bagSize == other.bagSize &&
               updateAvailable == other.updateAvailable &&
               isDownloading == other.isDownloading &&
               isVsAi == other.isVsAi &&
               aiDifficulty == other.aiDifficulty &&
               isAiThinking == other.isAiThinking &&
               canResume == other.canResume &&
               showBlankPicker == other.showBlankPicker &&
               pendingBlankPickerRow == other.pendingBlankPickerRow &&
               pendingBlankPickerCol == other.pendingBlankPickerCol
    }

    override fun hashCode(): Int = javaClass.hashCode()
}

data class LexiconSaveData(
    val board: List<List<Tile?>>,
    val players: List<Player>,
    val currentPlayer: Int,
    val bag: List<Char>,
    val consecutiveSkips: Int,
    val isVsAi: Boolean,
    val aiDifficulty: AiDifficulty?
)

enum class GameState { MENU, PLAYING, GAME_OVER }
