package com.enigma.fluffyinc.apps.games.lexicon.model

/**
import com.enigma.fluffyinc.apps.games.lexicon.ai.AiDifficulty
import com.enigma.fluffyinc.apps.games.lexicon.ai.AiOpponent
import kotlinx.coroutines.flow.MutableStateFlow

// ─────────────────────────────────────────────────────────────────────────────
//  ADD THESE TO ScrabbleGameViewModel.kt
//  (paste inside the class body, and add the new fields to GameUiState)
// ─────────────────────────────────────────────────────────────────────────────


//
  import com.enigma.fluffyinc.apps.games.lexicon.ai.AiDecision
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
  import kotlinx.coroutines.Dispatchers

// ── 2. New private fields inside ScrabbleGameViewModel ───────────────────────

    private var aiOpponent: AiOpponent? = null

    // Player 2 is always the AI when this is true.
    // Expose it in GameUiState so the UI can hide/show controls accordingly.
    private val _isVsAi = MutableStateFlow(false)

// ── 3. Replace / extend startGame() ──────────────────────────────────────────

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

// ── 5. Add to GameUiState data class ─────────────────────────────────────────
//
//   val isVsAi: Boolean = false,
//   val aiDifficulty: AiDifficulty? = null,
//   val isAiThinking: Boolean = false,
//
//  Also update the equals() check to include isVsAi and isAiThinking.

// ── 6. Disable human controls while AI is thinking ───────────────────────────
//
//  In GameScreen (ScrabbleGameApp.kt), gate PLAY / SHUFFLE / EXCHANGE / SKIP
//  buttons and rack tile clicks on:
//
//      enabled = !uiState.isAiThinking && <existing condition>
//
//  Optionally show a "thinking…" indicator:
//
//      if (uiState.isAiThinking) {
//          LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp))
//      }

// ── 7. Menu screen — add difficulty picker before START ───────────────────────
//
//  var selectedDifficulty by remember { mutableStateOf(AiDifficulty.MEDIUM) }
//  var vsAiMode by remember { mutableStateOf(true) }
//
//  // Mode toggle
//  Row { listOf("vs AI", "2 Players").forEachIndexed { i, label ->
//      FilterChip(selected = vsAiMode == (i == 0), onClick = { vsAiMode = i == 0 }, label = { Text(label) })
//  }}
//
//  // Difficulty row (only when vs AI)
//  if (vsAiMode) {
//      Row { AiDifficulty.entries.forEach { d ->
//          FilterChip(selected = selectedDifficulty == d, onClick = { selectedDifficulty = d },
//              label = { Text(d.displayName) })
//      }}
//  }
//
//  // Start button
//  Button(onClick = {
//      if (vsAiMode) viewModel.startVsAi(player1Name, selectedDifficulty)
//      else          viewModel.startGame(player1Name, player2Name)
//  }, enabled = uiState.isDictionaryLoaded) { Text("START GAME") }
*/