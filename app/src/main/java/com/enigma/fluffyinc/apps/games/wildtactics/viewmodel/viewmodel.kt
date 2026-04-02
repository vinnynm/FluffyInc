package com.enigma.fluffyinc.apps.games.wildtactics.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enigma.fluffyinc.apps.games.wildtactics.ai.AIManager
import com.enigma.fluffyinc.apps.games.wildtactics.processor.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WildTacticsViewModel : ViewModel() {
    private var gameEngine: WildTacticsGameEngine? = null
    private var aiManager: AIManager? = null

    private val _gameState = MutableStateFlow<GameState?>(null)
    val gameState = _gameState.asStateFlow()

    var selectedCardIndex by mutableStateOf<Int?>(null)
    var selectedTargetPlayer by mutableStateOf<Int?>(null)
    var selectedAttackCards by mutableStateOf<Set<Int>>(emptySet())
    var showGameModeSelector by mutableStateOf(true)
    var showVictoryDialog by mutableStateOf(false)
    var eventLog by mutableStateOf<List<String>>(emptyList())
    var errorMessage by mutableStateOf<String?>(null)
    var flippedCards by mutableStateOf<Set<Int>>(emptySet())
    var showTutorial by mutableStateOf(false)

    fun startGame(gameMode: GameMode, playerCount: Int = 2) {
        viewModelScope.launch {
            gameEngine = WildTacticsGameEngine(gameMode, playerCount)
            
            if (gameMode is GameMode.SinglePlayer) {
                aiManager = AIManager(gameMode.aiDifficulty, gameMode)
            }

            // Observe Game State
            launch {
                gameEngine?.gameState?.collect { state ->
                    _gameState.value = state
                    if (state?.isGameOver == true) {
                        showVictoryDialog = true
                    }
                    
                    // Trigger AI if it's AI turn
                    if (state != null && state.players[state.currentPlayerIndex].isAiPlayer && !state.isGameOver) {
                        handleAiTurn(state)
                    }
                }
            }

            // Observe Game Events
            launch {
                gameEngine?.gameEvents?.collect { event ->
                    addToEventLog(event)
                }
            }

            showGameModeSelector = false
        }
    }

    private suspend fun handleAiTurn(state: GameState) {
        delay(1000) // Delay for UI to update
        aiManager?.executeTurn(state, state.currentPlayerIndex, gameEngine!!)
    }

    fun playCard(cardIndex: Int) {
        viewModelScope.launch {
            val result = gameEngine?.playCard(_gameState.value?.currentPlayerIndex ?: 0, cardIndex)
            result?.onSuccess {
                selectedCardIndex = null
            }?.onError {
                errorMessage = it
            }
        }
    }

    fun attack(targetId: Int) {
        viewModelScope.launch {
            val state = _gameState.value ?: return@launch
            val result = gameEngine?.attack(
                state.currentPlayerIndex,
                listOf(targetId),
                selectedAttackCards.toList()
            )
            result?.onSuccess {
                selectedAttackCards = emptySet()
                selectedTargetPlayer = null
            }?.onError {
                errorMessage = it
            }
        }
    }

    fun endPhase() {
        viewModelScope.launch {
            gameEngine?.endPhase()?.onError {
                errorMessage = it
            }
        }
    }

    private fun addToEventLog(event: GameEvent) {
        val message = when (event) {
            is GameEvent.CardPlayed -> "Player ${event.playerId + 1} played ${event.card.animal.name}"
            is GameEvent.CardDrawn -> "Player ${event.playerId + 1} drew a card"
            is GameEvent.AttackCompleted -> "Player ${event.attackerId + 1} attacked!"
            is GameEvent.PlayerDamaged -> "Player ${event.playerId + 1} took ${event.damage} damage"
            is GameEvent.PhaseChanged -> "Phase: ${event.newPhase::class.simpleName}"
            is GameEvent.TurnEnded -> "Turn ended for Player ${event.previousPlayerId + 1}"
            is GameEvent.GameOver -> "Game Over! ${event.winner.name} wins!"
            is GameEvent.Error -> "Error: ${event.message}"
            is GameEvent.CounterTriggered -> "Counter triggered for Player ${event.playerId + 1}"

        }
        eventLog = (eventLog + message).takeLast(5)
    }

    fun dismissError() {
        errorMessage = null
    }

    fun resetGame() {
        gameEngine?.cleanup()
        gameEngine = null
        aiManager = null
        _gameState.value = null
        showGameModeSelector = true
        showVictoryDialog = false
        eventLog = emptyList()
        selectedCardIndex = null
        selectedAttackCards = emptySet()
    }
}
