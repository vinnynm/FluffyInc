package com.enigma.fluffyinc.apps.games.wildtactics.processor

import com.enigma.fluffyinc.apps.games.wildtactics.ai.AIDifficulty
import com.enigma.fluffyinc.apps.games.wildtactics.data.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

// ==================== Game State ====================
data class GameState(
    val players: List<Player>,
    val currentPlayerIndex: Int = 0,
    val deck: List<Card> = emptyList(),
    val discardPile: List<Card> = emptyList(),
    val turnNumber: Int = 1,
    val gamePhase: GamePhase = GamePhase.Draw,
    val gameMode: GameMode = GameMode.SinglePlayer(AIDifficulty.Medium),
    val isGameOver: Boolean = false,
    val winner: Player? = null,
    val errorMessage: String? = null
)

sealed class GamePhase {
    data object Draw : GamePhase()
    data object Play : GamePhase()
    data object Attack : GamePhase()
    data object End : GamePhase()
}

sealed class GameMode {
    data class SinglePlayer(val aiDifficulty: AIDifficulty) : GameMode()
    data object PassAndPlay : GameMode()
}

// ==================== Game Result ====================
sealed class GameResult<out T> {
    data class Success<T>(val data: T) : GameResult<T>()
    data class Error(val message: String) : GameResult<Nothing>()
    
    fun onSuccess(action: (T) -> Unit): GameResult<T> {
        if (this is Success) action(data)
        return this
    }
    
    fun onError(action: (String) -> Unit): GameResult<T> {
        if (this is Error) action(message)
        return this
    }
}

// ==================== Main Game Engine ====================
class WildTacticsGameEngine(
    private val initialGameMode: GameMode,
    private val playerCount: Int = 2
) {
    private val _gameState = MutableStateFlow<GameState?>(null)
    val gameState: StateFlow<GameState?> = _gameState.asStateFlow()

    private val _gameEvents = MutableSharedFlow<GameEvent>()
    val gameEvents: SharedFlow<GameEvent> = _gameEvents.asSharedFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    init {
        scope.launch {
            val initialState = createInitialState()
            _gameState.value = initialState
            // Start the first turn properly
            drawCard(0)
            endPhase()
        }
    }

    private fun createInitialState(): GameState {
        var currentDeck = createDeck().shuffled()
        
        val players = List(playerCount) { index ->
            val hand = currentDeck.take(5)
            currentDeck = currentDeck.drop(5)
            Player(
                id = index,
                name = if (initialGameMode is GameMode.SinglePlayer && index > 0) "AI Opponent" else "Player ${index + 1}",
                isAiPlayer = initialGameMode is GameMode.SinglePlayer && index > 0,
                hand = hand
            )
        }

        return GameState(
            players = players,
            deck = currentDeck,
            gameMode = initialGameMode,
            gamePhase = GamePhase.Draw
        )
    }

    private fun createDeck(): List<Card> {
        val animals = Animal.getAll()
        val deck = mutableListOf<Card>()
        var idCounter = 0
        
        animals.forEach { animal ->
            val count = when (animal.animalClass) {
                AnimalClass.Common -> 6
                AnimalClass.Rare -> 4
                AnimalClass.Epic -> 2
                AnimalClass.Legendary -> 1
            }
            repeat(count) {
                deck.add(Card(id = idCounter++, animal = animal))
            }
        }
        return deck
    }

    suspend fun drawCard(playerId: Int): GameResult<Unit> {
        val currentState = _gameState.value ?: return GameResult.Error("No game state")
        if (currentState.deck.isEmpty()) return GameResult.Error("Deck is empty")
        
        val card = currentState.deck.first()
        val newDeck = currentState.deck.drop(1)
        
        val newPlayers = currentState.players.map { player ->
            if (player.id == playerId) {
                player.copy(hand = player.hand + card)
            } else {
                player
            }
        }

        _gameState.value = currentState.copy(players = newPlayers, deck = newDeck)
        _gameEvents.emit(GameEvent.CardDrawn(playerId, card))
        return GameResult.Success(Unit)
    }

    suspend fun playCard(playerId: Int, cardIndex: Int): GameResult<Unit> {
        val currentState = _gameState.value ?: return GameResult.Error("No game state")
        if (currentState.currentPlayerIndex != playerId) return GameResult.Error("Not your turn")
        if (currentState.gamePhase != GamePhase.Play) return GameResult.Error("Must be in Play phase")

        val player = currentState.players[playerId]
        if (cardIndex !in player.hand.indices) return GameResult.Error("Invalid card")

        val card = player.hand[cardIndex]
        val newHand = player.hand.toMutableList().apply { removeAt(cardIndex) }
        val newBattlefield = player.battlefield + card
        
        val newPlayers = currentState.players.map { p ->
            if (p.id == playerId) {
                p.copy(hand = newHand, battlefield = newBattlefield)
            } else {
                p
            }
        }

        _gameState.value = currentState.copy(players = newPlayers)
        _gameEvents.emit(GameEvent.CardPlayed(playerId, card))
        return GameResult.Success(Unit)
    }

    suspend fun attack(attackerId: Int, defenderIds: List<Int>, attackingCardIndices: List<Int>): GameResult<Unit> {
        val currentState = _gameState.value ?: return GameResult.Error("No game state")
        if (currentState.gamePhase != GamePhase.Attack) return GameResult.Error("Must be in Attack phase")

        val attacker = currentState.players[attackerId]
        val defenderId = defenderIds.firstOrNull() ?: return GameResult.Error("No target")
        val defender = currentState.players[defenderId]
        
        val attackingCardsWithIndices = attackingCardIndices.mapNotNull { index ->
            attacker.battlefield.getOrNull(index)?.let { index to it }
        }
        if (attackingCardsWithIndices.isEmpty()) return GameResult.Error("No cards selected")

        var totalDamage = 0
        val usedAttackerIndices = mutableSetOf<Int>()
        
        attackingCardsWithIndices.forEach { (index, card) ->
            if (!card.hasAttacked) {
                totalDamage += card.currentStrength
                usedAttackerIndices.add(index)
            }
        }

        if (totalDamage == 0) return GameResult.Error("No valid attackers")

        // Counter Logic: Counter animals reduce damage and are consumed
        var damageToDeal = totalDamage
        val countersUsedIndices = mutableListOf<Int>()
        
        defender.battlefield.forEachIndexed { index, card ->
            if (card.animal.animalType == AnimalType.CounterAnimal && damageToDeal > 0) {
                damageToDeal = (damageToDeal - card.currentStrength).coerceAtLeast(0)
                countersUsedIndices.add(index)
                scope.launch {
                    _gameEvents.emit(GameEvent.CounterTriggered(defenderId, card))
                }
            }
        }

        val updatedDefender = defender.copy(
            lives = (defender.lives - damageToDeal).coerceAtLeast(0),
            battlefield = defender.battlefield.filterIndexed { index, _ -> index !in countersUsedIndices }
        )

        // Attacker cleanup: Remove non-counter animals that attacked
        val updatedAttackerBattlefield = attacker.battlefield.mapIndexedNotNull { index, card ->
            if (index in usedAttackerIndices) {
                if (card.animal.animalType == AnimalType.CounterAnimal) {
                    card.copy(hasAttacked = true)
                } else {
                    null // Predators/Tricksters leave after attacking
                }
            } else {
                card
            }
        }
        
        val updatedAttacker = attacker.copy(battlefield = updatedAttackerBattlefield)

        val newPlayers = currentState.players.map { p ->
            when (p.id) {
                attackerId -> updatedAttacker
                defenderId -> updatedDefender
                else -> p
            }
        }

        _gameState.value = currentState.copy(players = newPlayers)
        
        if (damageToDeal > 0) {
            _gameEvents.emit(GameEvent.PlayerDamaged(defenderId, damageToDeal))
        }

        _gameEvents.emit(GameEvent.AttackCompleted(attackerId, listOf(defenderId)))
        checkGameOver()
        
        return GameResult.Success(Unit)
    }

    suspend fun endPhase(): GameResult<Unit> {
        val currentState = _gameState.value ?: return GameResult.Error("No game state")
        
        var newPlayers = currentState.players
        // When ending Attack phase, remove any remaining Predators/Tricksters
        if (currentState.gamePhase == GamePhase.Attack) {
            newPlayers = currentState.players.map { p ->
                if (p.id == currentState.currentPlayerIndex) {
                    p.copy(battlefield = p.battlefield.filter { it.animal.animalType == AnimalType.CounterAnimal })
                } else {
                    p
                }
            }
        }

        val nextPhase = when (currentState.gamePhase) {
            GamePhase.Draw -> GamePhase.Play
            GamePhase.Play -> GamePhase.Attack
            GamePhase.Attack -> GamePhase.End
            GamePhase.End -> {
                startNextTurn()
                return GameResult.Success(Unit)
            }
        }

        _gameState.value = currentState.copy(players = newPlayers, gamePhase = nextPhase)
        _gameEvents.emit(GameEvent.PhaseChanged(nextPhase))
        return GameResult.Success(Unit)
    }

    private suspend fun startNextTurn() {
        val currentState = _gameState.value ?: return
        val nextIndex = (currentState.currentPlayerIndex + 1) % currentState.players.size
        
        val newPlayers = currentState.players.map { p ->
            if (p.id == currentState.currentPlayerIndex) {
                // Double check only counter animals remain
                p.copy(
                    battlefield = p.battlefield.filter { it.animal.animalType == AnimalType.CounterAnimal }
                        .map { it.copy(hasAttacked = false) }
                )
            } else {
                p
            }
        }

        _gameState.value = currentState.copy(
            players = newPlayers,
            currentPlayerIndex = nextIndex,
            gamePhase = GamePhase.Draw,
            turnNumber = if (nextIndex == 0) currentState.turnNumber + 1 else currentState.turnNumber
        )

        _gameEvents.emit(GameEvent.TurnEnded(currentState.currentPlayerIndex, nextIndex))
        drawCard(nextIndex)
        endPhase()
    }

    private suspend fun checkGameOver() {
        val currentState = _gameState.value ?: return
        val alivePlayers = currentState.players.filter { it.isAlive }
        
        if (alivePlayers.size <= 1) {
            val winner = alivePlayers.firstOrNull()
            _gameState.value = currentState.copy(isGameOver = true, winner = winner)
            winner?.let { _gameEvents.emit(GameEvent.GameOver(it)) }
        }
    }

    fun cleanup() {
        scope.cancel()
    }
}

// ==================== Game Events ====================
sealed class GameEvent {
    data class CardPlayed(val playerId: Int, val card: Card) : GameEvent()
    data class CardDrawn(val playerId: Int, val card: Card) : GameEvent()
    data class AttackCompleted(val attackerId: Int, val defenderIds: List<Int>) : GameEvent()
    data class PlayerDamaged(val playerId: Int, val damage: Int) : GameEvent()
    data class PhaseChanged(val newPhase: GamePhase) : GameEvent()
    data class TurnEnded(val previousPlayerId: Int, val nextPlayerId: Int) : GameEvent()
    data class GameOver(val winner: Player) : GameEvent()
    data class Error(val message: String) : GameEvent()
    data class CounterTriggered(val playerId: Int, val card: Card) : GameEvent()
}
