package com.enigma.fluffyinc.apps.games.lexicon.ui.old

/**
class ScrabbleGameViewModel(
    private val dictionaryManager: WordDictionaryManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()
    private var aiOpponent: AiOpponent? = null

    // Player 2 is always the AI when this is true.
    // Expose it in GameUiState so the UI can hide/show controls accordingly.
    private val _isVsAi = MutableStateFlow(false)

    private val game = ScrabbleGame()
    init {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            dictionaryManager.load()                        // ← awaits completion
            game.updateDictionary(dictionaryManager.dictionary.value)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    isDictionaryLoaded = dictionaryManager.dictionary.value.isNotEmpty(),
                    dictionarySize = dictionaryManager.dictionary.value.size,
                    errorMessage = dictionaryManager.error.value
                )
            }
        }
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


    /**
     * Start a game against the AI at the given difficulty.
     * The AI always plays as Player 2.
     */
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
                isVsAi       = true,
                aiDifficulty = difficulty
            )
        }
        updateStateFromGame()
    }

    /** Original two-player (human vs human) start — unchanged. */
    fun startGame(player1Name: String, player2Name: String) {
        if (!_uiState.value.isDictionaryLoaded) return
        _isVsAi.value = false
        aiOpponent = null
        game.startGame(player1Name, player2Name)
        _uiState.update { it.copy(isVsAi = false, aiDifficulty = null) }
        updateStateFromGame()
    }


// ── 4. Trigger AI turn after every human action ───────────────────────────────
//
//      Call   maybeRunAiTurn()   at the END of playWord(), skipTurn(),
//      and exchangeTiles() — just before the closing brace of each.
//
//      Example inside playWord():
//
//          is PlayResult.Success -> {
//              updateStateFromGame()
//              _uiState.update { ... }
//              maybeRunAiTurn()          // ← add this
//          }

    /**
     * If it is now the AI's turn, compute and execute its move on a
     * background thread, then post the result back to the UI.
     */
    private fun maybeRunAiTurn() {
        val ai = aiOpponent ?: return
        if (!_isVsAi.value) return
        if (game.currentPlayer != 1) return   // player 2 = AI
        if (game.isGameOver) return

        viewModelScope.launch {
            // Small delay so the human can see the board update before AI moves
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
                    // Place each tile through the existing game model
                    decision.move.tiles.forEach { t ->
                        game.placeTile(t.row, t.col, t.letter)
                    }
                    val result = game.playWord()
                    when (result) {
                        is PlayResult.Success -> _uiState.update {
                            it.copy(
                                lastPlayMessage = "${ai.difficulty.displayName} AI played " +
                                        "${result.words.joinToString(", ")} for +${result.score}",
                                lastPlayScore   = result.score,
                                isAiThinking    = false
                            )
                        }
                        is PlayResult.Error -> {
                            // Shouldn't normally happen; fall back to skip
                            game.skipTurn()
                            _uiState.update {
                                it.copy(lastPlayMessage = "${ai.difficulty.displayName} AI skipped", isAiThinking = false)
                            }
                        }
                    }
                }

                is AiDecision.ExchangeTiles -> {
                    game.exchangeTiles()
                    _uiState.update {
                        it.copy(lastPlayMessage = "${ai.difficulty.displayName} AI exchanged tiles", isAiThinking = false)
                    }
                }

                is AiDecision.Skip -> {
                    game.skipTurn()
                    _uiState.update {
                        it.copy(lastPlayMessage = "${ai.difficulty.displayName} AI skipped", isAiThinking = false)
                    }
                }
            }

            updateStateFromGame()
        }
    }

    private fun updateStateFromGame() {
        _uiState.update { it.copy(
            gameState = if (game.isGameOver) GameState.GAME_OVER else GameState.PLAYING,
            currentPlayer = game.currentPlayer,
            player1 = game.players[0].copy(),
            player2 = game.players[1].copy(),
            board = Array(15) { r -> Array(15) { c -> game.board[r][c]?.copy() } },
            placedThisTurn = game.placedThisTurn.toList(),
            bagSize = game.getBagSize()
        ) }
    }

    fun selectTile(rackIndex: Int) {
        _uiState.update { state ->
            val current = state.selectedTile
            state.copy(selectedTile = if (current == rackIndex) null else rackIndex)
        }
    }

    fun placeTile(row: Int, col: Int) {
        val selectedIdx = _uiState.value.selectedTile ?: return
        val player = game.players[game.currentPlayer]
        if (selectedIdx < player.rack.size) {
            if (game.placeTile(row, col, player.rack[selectedIdx])) {
                _uiState.update { it.copy(selectedTile = null) }
                updateStateFromGame()
            }
        }
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
                _uiState.update { it.copy(
                    lastPlayMessage = "Played ${result.words.joinToString(", ")} for +${result.score}",
                    lastPlayScore = result.score,
                    selectedTile = null
                ) }
                maybeRunAiTurn()
            }
            is PlayResult.Error -> {
                _uiState.update { it.copy(lastPlayMessage = result.message) }
            }
        }
    }

    fun skipTurn() { 
        game.skipTurn()
        updateStateFromGame()
        maybeRunAiTurn()
    }
    
    fun exchangeTiles() { 
        if (game.exchangeTiles()) {
            updateStateFromGame()
            maybeRunAiTurn()
        } else {
            _uiState.update { it.copy(lastPlayMessage = "Not enough tiles in bag") }
        }
    }
    fun shuffleRack() { game.shuffleRack(); updateStateFromGame() }
    fun resetGame() {
        game.reset()
        _uiState.update {
            GameUiState(
                isDictionaryLoaded = it.isDictionaryLoaded,
                dictionarySize = it.dictionarySize,
                gameState = GameState.MENU
            )
        }
    }
    fun clearMessage() { _uiState.update { it.copy(lastPlayMessage = "", lastPlayScore = 0) } }
    fun downloadUpdate() {
        game.toString()
    }
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
               isAiThinking == other.isAiThinking
    }
    override fun hashCode(): Int = javaClass.hashCode()
}

enum class GameState { MENU, PLAYING, GAME_OVER }

        */