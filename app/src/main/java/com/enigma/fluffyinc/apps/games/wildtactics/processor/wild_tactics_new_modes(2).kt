package com.enigma.fluffyinc.apps.games.wildtactics.processor

import androidx.compose.runtime.mutableStateOf
import com.enigma.fluffyinc.apps.games.wildtactics.ai.AIDifficulty
import com.enigma.fluffyinc.apps.games.wildtactics.data.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.math.roundToInt
import kotlin.random.Random

// ==================== Game State ====================
data class GameState(
    val players: List<Player>,
    val currentPlayerIndex: Int = 0,
    val deck: MutableList<Card> = mutableListOf(),
    val discardPile: MutableList<Card> = mutableListOf(),
    val battlefield: MutableMap<Int, List<Card>> = mutableMapOf(),
    val turnNumber: Int = 1,
    val gamePhase: GamePhase = GamePhase.Draw,
    val activeEffects: MutableList<Effect> = mutableListOf(),
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
    data class LocalMultiplayer(val hostId: String, val clientId: String?) : GameMode()
    data class KingOfHill(
        val currentKing: Int? = null,
        val crownPoints: MutableMap<Int, Int> = mutableMapOf(),
        val challengeCount: Int = 0
    ) : GameMode()
    data class Blitz(
        val roundsWon: MutableMap<Int, Int> = mutableMapOf(),
        val currentRound: Int = 1,
        val timeLimit: Int = 30,
        val timeoutCount: MutableMap<Int, Int> = mutableMapOf()
    ) : GameMode()
}


// ==================== Custom Exceptions ====================
sealed class GameException(message: String) : Exception(message) {
    class InvalidPlayerException(playerId: Int) : 
        GameException("Invalid player ID: $playerId")
    class InvalidCardException(cardIndex: Int) : 
        GameException("Invalid card index: $cardIndex")
    class InvalidGamePhaseException(required: GamePhase, current: GamePhase) : 
        GameException("Invalid phase. Required: $required, Current: $current")
    class EmptyDeckException : 
        GameException("Deck is empty and cannot be reshuffled")
    class InvalidAttackException(message: String) : 
        GameException("Invalid attack: $message")
    class GameOverException : 
        GameException("Game is already over")
    class NetworkException(message: String) : 
        GameException("Network error: $message")
    class TimeoutException(playerId: Int) : 
        GameException("Player $playerId timed out")
}

// ==================== Game Result ====================
sealed class GameResult<out T> {
    data class Success<T>(val data: T) : GameResult<T>()
    data class Error(val exception: GameException) : GameResult<Nothing>()
    
    fun onSuccess(action: (T) -> Unit): GameResult<T> {
        if (this is Success) action(data)
        return this
    }
    
    fun onError(action: (GameException) -> Unit): GameResult<T> {
        if (this is Error) action(exception)
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
    
    private var isInitialized = false

    init {
        try {
            validatePlayerCount(playerCount)
            _gameState.value = createInitialState()
            isInitialized = true
        } catch (e: Exception) {
            handleError(e, "Initialization failed")
        }
    }

    private fun validatePlayerCount(count: Int) {
        when (initialGameMode) {
            is GameMode.Blitz -> {
                if (count != 2) throw IllegalArgumentException("Blitz mode requires exactly 2 players")
            }
            is GameMode.KingOfHill -> {
                if (count !in 3..4) throw IllegalArgumentException("King of Hill requires 3-4 players")
            }
            else -> {
                if (count !in 2..4) throw IllegalArgumentException("Player count must be 2-4")
            }
        }
    }

    private fun createInitialState(): GameState {
        return try {
            val players = List(playerCount) { index ->
                Player(
                    id = index,
                    name = if (index == 0) "Player 1" else "Player ${index + 1}",
                    isAiPlayer = when (initialGameMode) {
                        is GameMode.SinglePlayer -> index > 0
                        else -> false
                    }
                )
            }

            val deck = createDeck().shuffled().toMutableList()
            
            if (deck.size < playerCount * 5) {
                throw GameException.EmptyDeckException()
            }
            
            // Deal initial hands
            players.forEach { player ->
                val cardsToTake = if (initialGameMode is GameMode.Blitz) 3 else 5
                repeat(cardsToTake) {
                    if (deck.isNotEmpty()) {
                        player.hand.add(deck.removeAt(0))
                    }
                }
            }

            GameState(
                players = players,
                deck = deck,
                gameMode = initialGameMode
            )
        } catch (e: Exception) {
            throw IllegalStateException("Failed to create initial state: ${e.message}", e)
        }
    }

    private fun createDeck(): List<Card> {
        return try {
            val animals = listOf(
                Animal.Lion, Animal.Puma, Animal.Leopard, Animal.Tiger, Animal.Jaguar,
                Animal.Wolf, Animal.Bear, Animal.Fox, Animal.Eagle, Animal.Falcon,
                Animal.Adder, Animal.Cobra, Animal.Monkey, Animal.Elephant, Animal.Rhino,
                Animal.Bunny, Animal.Rabbit, Animal.Hare
            )
            
            val multiplier = if (initialGameMode is GameMode.Blitz) {
                // Simplified deck for Blitz
                when {
                    else -> 3
                }
            } else {
                when {
                    else -> 1
                }
            }
            
            animals.flatMapIndexed { index, animal ->
                val count = when (animal.animalClass) {
                    AnimalClass.Common -> 8 * multiplier
                    AnimalClass.Rare -> 5 * multiplier
                    AnimalClass.Epic -> 3 * multiplier
                    AnimalClass.Legendary -> 1 * multiplier
                }
                List(count) { Card(id = index * 100 + it, animal = animal) }
            }
        } catch (e: Exception) {
            throw IllegalStateException("Failed to create deck: ${e.message}", e)
        }
    }

    // ==================== Game Actions with Error Handling ====================
    suspend fun playCard(playerId: Int, cardIndex: Int, targetPlayerId: Int? = null): GameResult<Unit> {
        return withErrorHandling {
            val currentState = requireGameState()
            
            validateGameNotOver(currentState)
            validatePlayerTurn(currentState, playerId)
            validateGamePhase(currentState, GamePhase.Play)
            
            val player = getPlayer(currentState, playerId)
            validateCardIndex(player, cardIndex)

            val card = player.hand[cardIndex]
            player.hand.removeAt(cardIndex)

            // Apply King of Hill bonus
            val strengthBonus = if (initialGameMode is GameMode.KingOfHill) {
                val kingMode = initialGameMode
                if (kingMode.currentKing == playerId) 5 else 0
            } else 0

            val effectiveCard = if (strengthBonus > 0) {
                card.animal.strength += strengthBonus
                Card(
                    card.id,
                    animal = card.animal
                )
            } else card

            // Add to battlefield
            val battlefield = currentState.battlefield.toMutableMap()
            battlefield[playerId] = (battlefield[playerId] ?: emptyList()) + effectiveCard

            _gameState.value = currentState.copy(battlefield = battlefield)
            _gameEvents.emit(GameEvent.CardPlayed(playerId, card))

            // Handle card effects
            handleCardEffect(card, playerId, targetPlayerId)
        }
    }

    suspend fun attack(attackerId: Int, defenderIds: List<Int>, attackingCardIndices: List<Int>): GameResult<Unit> {
        return withErrorHandling {
            val currentState = requireGameState()
            
            validateGameNotOver(currentState)
            validatePlayerTurn(currentState, attackerId)
            validateGamePhase(currentState, GamePhase.Attack)

            if (defenderIds.isEmpty()) {
                throw GameException.InvalidAttackException("No defenders specified")
            }

            val attackerCards = currentState.battlefield[attackerId]?.filterIndexed { index, _ ->
                index in attackingCardIndices
            } ?: throw GameException.InvalidAttackException("No attacker cards found")

            if (attackerCards.isEmpty()) {
                throw GameException.InvalidAttackException("No valid attacking cards")
            }

            defenderIds.forEach { defenderId ->
                validatePlayerId(currentState, defenderId)
                if (defenderId == attackerId) {
                    throw GameException.InvalidAttackException("Cannot attack yourself")
                }
                
                val defenderCards = currentState.battlefield[defenderId] ?: emptyList()
                resolveAttack(attackerId, defenderId, attackerCards, defenderCards)
            }

            _gameEvents.emit(GameEvent.AttackCompleted(attackerId, defenderIds))
        }
    }

    private suspend fun resolveAttack(
        attackerId: Int,
        defenderId: Int,
        attackerCards: List<Card>,
        defenderCards: List<Card>
    ) {
        try {
            val currentState = requireGameState()
            val attacker = getPlayer(currentState, attackerId)
            val defender = getPlayer(currentState, defenderId)

            var totalDamage = attackerCards.sumOf { it.animal.strength }

            // Apply attack effects
            attackerCards.forEach { card ->
                when (val animalType = card.animal.animalType) {
                    is AnimalType.Predator -> {
                        when (animalType.attack) {
                            Attack.PoisonousBite -> {
                                defender.playerEffects[PlayerEffects.Poisoned(
                                    poisonDamage = card.animal.strength
                                )] = 3
                                totalDamage= card.animal.strength
                            }
                            Attack.ShockAndAwe -> {
                                totalDamage += defender.lives
                            }

                            Attack.Bite -> {
                                defender.playerEffects.forEach { effects ->
                                    when(effects.key){
                                        PlayerEffects.Ambush -> {
                                            defender.lives - (card.animal.strength*5)
                                        }
                                        is PlayerEffects.Bleeding -> {
                                            defender.lives - (card.animal.strength* 2)
                                        }
                                        PlayerEffects.Blind -> {
                                            totalDamage= (3* card.animal.strength)
                                        }
                                        PlayerEffects.MonkeySee -> {
                                            resolveAttack(
                                                attackerId = defenderId,
                                                defenderId = attackerId,
                                                attackerCards = listOf(card),
                                                defenderCards = defender.hand.filter {card ->
                                                    card.animal.animalType== AnimalType.CounterAnimal
                                                }
                                            )
                                        }
                                        is PlayerEffects.Poisoned -> {
                                            totalDamage=(card.animal.strength * 2.5f).roundToInt()
                                        }
                                        PlayerEffects.ShieldOfSavannah -> {
                                            totalDamage= ((card.animal.strength* 2)-10)
                                            defender.playerEffects.remove(PlayerEffects.ShieldOfSavannah)
                                        }

                                        PlayerEffects.ToxicBlood -> {
                                            if (attacker.playerEffects.containsKey(PlayerEffects.Poisoned())) {
                                                attacker.playerEffects.replace(
                                                    PlayerEffects.Poisoned(
                                                        (card.animal.strength / 2).toFloat()
                                                            .roundToInt()
                                                    ),
                                                    attacker.playerEffects[PlayerEffects.Poisoned()]!! + 3
                                                )
                                            }
                                            totalDamage= card.animal.strength
                                        }
                                        else->{}
                                    }
                                }
                            }
                            Attack.Kick -> {
                                defender.playerEffects.forEach{effects ->
                                    when(effects.key){
                                        PlayerEffects.MonkeySee -> {
                                            resolveAttack(
                                                attackerId = defenderId,
                                                defenderId = attackerId,
                                                attackerCards = listOf(card),
                                                defenderCards = defender.hand.filter {card ->
                                                    card.animal.animalType== AnimalType.CounterAnimal
                                                }
                                            )
                                            defender.playerEffects.remove(PlayerEffects.MonkeySee)
                                            totalDamage= card.animal.strength
                                        }
                                        PlayerEffects.ShieldOfSavannah -> {
                                            totalDamage= ((card.animal.strength)-10)
                                            defender.playerEffects.remove(PlayerEffects.ShieldOfSavannah)
                                        }
                                        else ->  defender.lives -= ((card.animal.strength))
                                    }
                                }
                            }
                            Attack.Pounce -> {
                                defender.playerEffects.forEach {effects ->
                                    when(effects.key){
                                        PlayerEffects.Ambush -> {
                                            totalDamage=(card.animal.strength*7.5).roundToInt()
                                        }
                                        is PlayerEffects.Bleeding -> {
                                            totalDamage= (card.animal.strength* 4)
                                        }
                                        PlayerEffects.Blind -> {
                                            totalDamage= (card.animal.strength* 4)
                                        }
                                        PlayerEffects.MonkeySee -> {
                                            resolveAttack(
                                                attackerId = defenderId,
                                                defenderId = attackerId,
                                                attackerCards = listOf(card),
                                                defenderCards = defender.hand.filter {card ->
                                                    card.animal.animalType== AnimalType.CounterAnimal
                                                }
                                            )
                                            totalDamage= (card.animal.strength)
                                        }
                                        is PlayerEffects.Poisoned -> {
                                            totalDamage= (card.animal.strength * 2.5f).roundToInt()
                                        }
                                        PlayerEffects.ShieldOfSavannah -> {
                                            totalDamage= ((card.animal.strength* 2)-10)
                                            defender.playerEffects.remove(PlayerEffects.ShieldOfSavannah)
                                        }

                                        PlayerEffects.ToxicBlood -> {
                                            if (attacker.playerEffects.containsKey(PlayerEffects.Poisoned())) {
                                                attacker.playerEffects.replace(
                                                    PlayerEffects.Poisoned(
                                                        (card.animal.strength / 2).toFloat()
                                                            .roundToInt()
                                                    ),
                                                    attacker.playerEffects[PlayerEffects.Poisoned()]!! + 3
                                                )
                                            }
                                            totalDamage= (card.animal.strength*1.5).roundToInt()
                                        }
                                        else->{}
                                    }
                                }

                                val effectChance = Random.nextInt(0,99)
                                when(effectChance){
                                    in 0..25 ->{
                                        defender.playerEffects
                                    }
                                    in 0..88->{
                                        defender.playerEffects.putIfAbsent(PlayerEffects.Bleeding(5),2)

                                    }
                                    else -> {
                                        defender.playerEffects.putIfAbsent(PlayerEffects.Bleeding(card.animal.strength),5)
                                        defender.playerEffects.putIfAbsent(PlayerEffects.Blind,5)

                                    }
                                }
                            }
                            Attack.Rush -> {
                                totalDamage=((Random.nextFloat()*card.animal.strength).roundToInt()+ card.animal.strength)
                            }
                            Attack.Slash -> {
                                totalDamage=card.animal.strength
                                defender.playerEffects.putIfAbsent(PlayerEffects.Bleeding(card.animal.strength),5)
                            }
                        }
                    }
                    is AnimalType.Trickster -> {
                        handleTrick(card, attackerId, defenderId)
                    }
                    else -> {}
                }
            }

            // Counter defense
            val counterValue = defenderCards.sumOf { it.animal.strength }
            val netDamage = maxOf(0, totalDamage - counterValue)

            if (netDamage > 0) {
                _gameEvents.emit(GameEvent.PlayerDamaged(defenderId, netDamage))
                
                // Remove defender cards
                val battlefield = currentState.battlefield.toMutableMap()
                battlefield[defenderId] = emptyList()
                _gameState.value = currentState.copy(battlefield = battlefield)
            }

            // Check for game over
            checkGameOver()
        } catch (e: Exception) {
            handleError(e, "Attack resolution failed")
        }
    }

    private suspend fun handleCardEffect(card: Card, playerId: Int, targetPlayerId: Int?) {
        try {
            when (val animalType = card.animal.animalType) {
                is AnimalType.Trickster -> {
                    when (animalType.trick) {
                        Trick.Steal -> {
                            targetPlayerId?.let { targetId ->
                                stealCard(playerId, targetId)
                            }
                        }
                        Trick.ReversalOfFate -> {
                            reverseFate(playerId)
                        }
                        Trick.ForceAttack -> {
                            targetPlayerId?.let {
                                _gameEvents.emit(GameEvent.ForcedAttack(it))
                            }
                        }
                        Trick.Ambush -> {
                            val state = requireGameState()
                            val player = getPlayer(state, playerId)
                            player.playerEffects[PlayerEffects.Ambush] = 2
                        }
                    }
                }
                AnimalType.CounterAnimal -> {
                    val state = requireGameState()
                    val player = getPlayer(state, playerId)
                    card.animal.animalTrickCounter.forEach { counter ->
                        when (counter) {
                            Counter.ShieldOfSavannah -> player.playerEffects[PlayerEffects.ShieldOfSavannah] = 2
                            Counter.SwordOfHorus -> player.playerEffects[PlayerEffects.SwordOfHorus] = 2
                            Counter.ToxicBlood -> player.playerEffects[PlayerEffects.ToxicBlood] = 2
                            else -> {}
                        }
                    }
                }
                else -> {}
            }
        } catch (e: Exception) {
            handleError(e, "Card effect handling failed")
        }
    }

    private suspend fun stealCard(stealerId: Int, targetId: Int) {
        try {
            val currentState = requireGameState()
            val target = getPlayer(currentState, targetId)
            val stealer = getPlayer(currentState, stealerId)

            if (target.hand.isNotEmpty()) {
                val stolenCard = target.hand.random()
                target.hand.remove(stolenCard)
                stealer.hand.add(stolenCard)
                _gameEvents.emit(GameEvent.CardStolen(stealerId, targetId, stolenCard))
            }
        } catch (e: Exception) {
            handleError(e, "Card steal failed")
        }
    }

    private fun reverseFate(playerId: Int) {
        try {
            val currentState = requireGameState()
            val battlefield = currentState.battlefield.toMutableMap()
            
            // Swap battlefield positions
            val playerCards = battlefield[playerId] ?: emptyList()
            battlefield.keys.forEach { otherId ->
                if (otherId != playerId) {
                    val temp = battlefield[otherId] ?: emptyList()
                    battlefield[otherId] = playerCards
                    battlefield[playerId] = temp
                }
            }
            
            _gameState.value = currentState.copy(battlefield = battlefield)
        } catch (e: Exception) {
            handleError(e, "Reverse fate failed")
        }
    }

    private fun handleTrick(card: Card, playerId: Int, targetId: Int) {
        // Trick handling logic with error handling
        try {
            // Implementation here
        } catch (e: Exception) {
            handleError(e, "Trick handling failed")
        }
    }

    suspend fun drawCard(playerId: Int): GameResult<Unit> {
        return withErrorHandling {
            val currentState = requireGameState()
            validatePlayerId(currentState, playerId)
            
            if (currentState.deck.isEmpty()) {
                // Reshuffle discard pile
                if (currentState.discardPile.isEmpty()) {
                    throw GameException.EmptyDeckException()
                }
                currentState.deck.addAll(currentState.discardPile.shuffled())
                currentState.discardPile.clear()
            }

            if (currentState.deck.isNotEmpty()) {
                val card = currentState.deck.removeAt(0)
                val player = getPlayer(currentState, playerId)
                player.hand.add(card)
                _gameEvents.emit(GameEvent.CardDrawn(playerId, card))
            }
        }
    }

    suspend fun endPhase(): GameResult<Unit> {
        return withErrorHandling {
            val currentState = requireGameState()
            validateGameNotOver(currentState)
            
            val nextPhase = when (currentState.gamePhase) {
                GamePhase.Draw -> {
                    drawCard(currentState.currentPlayerIndex)
                    GamePhase.Play
                }
                GamePhase.Play -> GamePhase.Attack
                GamePhase.Attack -> GamePhase.End
                GamePhase.End -> {
                    endTurn()
                    return@withErrorHandling
                }
            }

            _gameState.value = currentState.copy(gamePhase = nextPhase)
            _gameEvents.emit(GameEvent.PhaseChanged(nextPhase))
        }
    }

    private suspend fun endTurn() {
        try {
            val currentState = requireGameState()
            val currentPlayer = getPlayer(currentState, currentState.currentPlayerIndex)
            
            // Apply end-of-turn effects
            currentPlayer.runTheTurn()
            
            // Apply ongoing effects
            applyOngoingEffects(currentPlayer)

            val nextPlayerIndex = (currentState.currentPlayerIndex + 1) % currentState.players.size
            _gameState.value = currentState.copy(
                currentPlayerIndex = nextPlayerIndex,
                gamePhase = GamePhase.Draw,
                turnNumber = if (nextPlayerIndex == 0) currentState.turnNumber + 1 else currentState.turnNumber
            )

            _gameEvents.emit(GameEvent.TurnEnded(currentState.currentPlayerIndex, nextPlayerIndex))

            // Handle AI turn
            val nextPlayer = getPlayer(currentState, nextPlayerIndex)
            if (nextPlayer.isAiPlayer) {
                handleAITurn(nextPlayerIndex)
            }
        } catch (e: Exception) {
            handleError(e, "End turn failed")
        }
    }

    private suspend fun applyOngoingEffects(player: Player) {
        try {
            player.playerEffects.forEach { (effect, _) ->
                when (effect) {
                    is PlayerEffects.Poisoned -> {
                        _gameEvents.emit(GameEvent.PlayerDamaged(player.id, damage = effect.poisonDamage))
                    }
                    is PlayerEffects.Bleeding -> {
                        _gameEvents.emit(GameEvent.PlayerDamaged(player.id, damage = effect.bleedingDamage))
                    }
                    else -> {}
                }
            }
        } catch (e: Exception) {
            handleError(e, "Applying ongoing effects failed")
        }
    }

    // ==================== AI System ====================
    private suspend fun handleAITurn(aiPlayerId: Int) {
        try {
            val currentState = requireGameState()
            val difficulty = when (val mode = currentState.gameMode) {
                is GameMode.SinglePlayer -> mode.aiDifficulty
                else -> AIDifficulty.Medium
            }

            delay(1000) // Simulate thinking

            when (difficulty) {
                AIDifficulty.Easy -> executeEasyAI(aiPlayerId)
                AIDifficulty.Medium -> executeMediumAI(aiPlayerId)
                AIDifficulty.Hard -> executeHardAI(aiPlayerId)
                AIDifficulty.Expert -> executeExpertAI(aiPlayerId)
            }
        } catch (e: Exception) {
            handleError(e, "AI turn failed")
            // Skip AI turn on error
            endPhase()
        }
    }

    private suspend fun executeEasyAI(aiPlayerId: Int) {
        try {
            endPhase() // Draw
            
            val state = requireGameState()
            val player = getPlayer(state, aiPlayerId)
            
            if (player.hand.isNotEmpty() && Random.nextFloat() > 0.3f) {
                val randomCard = Random.nextInt(player.hand.size)
                playCard(aiPlayerId, randomCard)
            }
            
            endPhase() // Move to attack
            
            if (Random.nextFloat() > 0.5f) {
                val targets = state.players.indices.filter { it != aiPlayerId }
                if (targets.isNotEmpty()) {
                    val battlefield = state.battlefield[aiPlayerId] ?: emptyList()
                    if (battlefield.isNotEmpty()) {
                        attack(aiPlayerId, listOf(targets.random()), battlefield.indices.toList())
                    }
                }
            }
            
            endPhase() // End turn
            endPhase()
        } catch (e: Exception) {
            handleError(e, "Easy AI execution failed")
        }
    }

    private suspend fun executeMediumAI(aiPlayerId: Int) {
        try {
            endPhase() // Draw
            
            val state = requireGameState()
            val player = getPlayer(state, aiPlayerId)
            
            // Play strongest cards
            val sortedHand = player.hand.withIndex()
                .sortedByDescending { it.value.animal.strength }
                .take(2)
            
            sortedHand.forEach { _ ->
                if (player.hand.isNotEmpty()) {
                    playCard(aiPlayerId, 0) // Index shifts after removal
                    delay(500)
                }
            }
            
            endPhase() // Move to attack
            
            // Attack weakest opponent
            val opponents = state.players.indices.filter { it != aiPlayerId }
            val weakestOpponent = opponents.minByOrNull { opponentId ->
                state.battlefield[opponentId]?.size ?: 0
            }
            
            weakestOpponent?.let { targetId ->
                val battlefield = state.battlefield[aiPlayerId] ?: emptyList()
                if (battlefield.isNotEmpty()) {
                    attack(aiPlayerId, listOf(targetId), battlefield.indices.toList())
                }
            }
            
            endPhase() // End turn
            endPhase()
        } catch (e: Exception) {
            handleError(e, "Medium AI execution failed")
        }
    }

    private suspend fun executeHardAI(aiPlayerId: Int) {
        try {
            endPhase() // Draw
            
            val state = requireGameState()
            val player = getPlayer(state, aiPlayerId)
            
            // Strategic play based on card types
            val predators = player.hand.filter { 
                it.animal.animalType is AnimalType.Predator 
            }
            val tricksters = player.hand.filter { 
                it.animal.animalType is AnimalType.Trickster 
            }
            val counters = player.hand.filter { 
                it.animal.animalType == AnimalType.CounterAnimal 
            }
            
            // Play counters first for defense
            counters.take(1).forEach { _ ->
                val index = player.hand.indexOfFirst { it.animal.animalType == AnimalType.CounterAnimal }
                if (index >= 0) playCard(aiPlayerId, index)
            }
            
            // Then tricksters
            tricksters.take(1).forEach { _ ->
                val index = player.hand.indexOfFirst { it.animal.animalType is AnimalType.Trickster }
                if (index >= 0) {
                    val targets = state.players.indices.filter { it != aiPlayerId }
                    playCard(aiPlayerId, index, targets.firstOrNull())
                }
            }
            
            // Then strongest predators
            predators.sortedByDescending { it.animal.strength }.take(2).forEach { _ ->
                val index = player.hand.indexOfFirst { it.animal.animalType is AnimalType.Predator }
                if (index >= 0) playCard(aiPlayerId, index)
            }
            
            endPhase() // Move to attack
            
            // Calculate optimal attack target
            val opponents = state.players.indices.filter { it != aiPlayerId }
            val targetScores = opponents.map { opponentId ->
                val defenseStrength = state.battlefield[opponentId]
                    ?.sumOf { it.animal.strength } ?: 0
                val myAttackStrength = state.battlefield[aiPlayerId]
                    ?.sumOf { it.animal.strength } ?: 0
                
                opponentId to (myAttackStrength - defenseStrength)
            }
            
            val bestTarget = targetScores.maxByOrNull { it.second }
            
            bestTarget?.let { (targetId, score) ->
                if (score > 0) {
                    val battlefield = state.battlefield[aiPlayerId] ?: emptyList()
                    if (battlefield.isNotEmpty()) {
                        attack(aiPlayerId, listOf(targetId), battlefield.indices.toList())
                    }
                }
            }
            
            endPhase() // End turn
            endPhase()
        } catch (e: Exception) {
            handleError(e, "Hard AI execution failed")
        }
    }

    private suspend fun executeExpertAI(aiPlayerId: Int) {
        try {
            // Advanced AI with combo detection and optimal play
            executeHardAI(aiPlayerId) // For now, same as hard
            // TODO: Implement Monte Carlo Tree Search or similar advanced AI
        } catch (e: Exception) {
            handleError(e, "Expert AI execution failed")
        }
    }

    private suspend fun checkGameOver() {
        try {
            val currentState = requireGameState()
            val alivePlayers = currentState.players.filter { it.isAlive }
            
            if (alivePlayers.size == 1) {
                _gameState.value = currentState.copy(
                    isGameOver = true,
                    winner = alivePlayers.first()
                )
                _gameEvents.emit(GameEvent.GameOver(alivePlayers.first()))
            }
        } catch (e: Exception) {
            handleError(e, "Check game over failed")
        }
    }

    // ==================== Validation Methods ====================
    private fun requireGameState(): GameState {
        return _gameState.value ?: throw IllegalStateException("Game not initialized")
    }

    private fun validateGameNotOver(state: GameState) {
        if (state.isGameOver) {
            throw GameException.GameOverException()
        }
    }

    private fun validatePlayerTurn(state: GameState, playerId: Int) {
        if (state.currentPlayerIndex != playerId) {
            throw GameException.InvalidPlayerException(playerId)
        }
    }

    private fun validateGamePhase(state: GameState, required: GamePhase) {
        if (state.gamePhase != required) {
            throw GameException.InvalidGamePhaseException(required, state.gamePhase)
        }
    }

    private fun validatePlayerId(state: GameState, playerId: Int) {
        if (playerId !in state.players.indices) {
            throw GameException.InvalidPlayerException(playerId)
        }
    }

    private fun getPlayer(state: GameState, playerId: Int): Player {
        validatePlayerId(state, playerId)
        return state.players[playerId]
    }

    private fun validateCardIndex(player: Player, cardIndex: Int) {
        if (cardIndex !in player.hand.indices) {
            throw GameException.InvalidCardException(cardIndex)
        }
    }

    // ==================== Error Handling ====================
    private suspend fun <T> withErrorHandling(block: suspend () -> T): GameResult<T> {
        return try {
            GameResult.Success(block())
        } catch (e: GameException) {
            handleError(e, "Game action failed")
            GameResult.Error(e)
        } catch (e: Exception) {
            val gameException = GameException.InvalidAttackException(e.message ?: "Unknown error")
            handleError(gameException, "Unexpected error")
            GameResult.Error(gameException)
        }
    }

    private fun handleError(exception: Exception, context: String) {
        val errorMessage = "$context: ${exception.message}"
        println("ERROR - $errorMessage")
        
        scope.launch {
            try {
                _gameEvents.emit(GameEvent.Error(errorMessage))
                
                val currentState = _gameState.value
                currentState?.let {
                    _gameState.value = it.copy(errorMessage = errorMessage)
                }
            } catch (e: Exception) {
                println("Failed to emit error event: ${e.message}")
            }
        }
    }

    fun cleanup() {
        try {
            scope.cancel()
            isInitialized = false
        } catch (e: Exception) {
            println("Cleanup failed: ${e.message}")
        }
    }
}



// ==================== Game Events ====================
sealed class GameEvent {
    data class CardPlayed(val playerId: Int, val card: Card) : GameEvent()
    data class CardDrawn(val playerId: Int, val card: Card) : GameEvent()
    data class AttackCompleted(val attackerId: Int, val defenderIds: List<Int>) : GameEvent()
    data class PlayerDamaged(val playerId: Int, val damage: Int) : GameEvent()
    data class CardStolen(val stealerId: Int, val victimId: Int, val card: Card) : GameEvent()
    data class ForcedAttack(val playerId: Int) : GameEvent()
    data class PhaseChanged(val newPhase: GamePhase) : GameEvent()
    data class TurnEnded(val previousPlayerId: Int, val nextPlayerId: Int) : GameEvent()
    data class GameOver(val winner: Player) : GameEvent()
    data class Error(val message: String) : GameEvent()
}

// ==================== King of the Hill Mode ====================
class KingOfHillEngine(
    private val baseEngine: WildTacticsGameEngine,
    private val playerCount: Int = 4
) {
    private val _kingEvents = MutableSharedFlow<KingOfHillEvent>()
    val kingEvents: SharedFlow<KingOfHillEvent> = _kingEvents.asSharedFlow()
    
    private var currentKing: Int? = null
    private val crownPoints = mutableMapOf<Int, Int>()
    private var consecutiveChallenges = 0
    
    init {
        repeat(playerCount) { crownPoints[it] = 0 }
    }
    
    suspend fun challengeKing(challengerId: Int): GameResult<Unit> {
        return try {
            val state = baseEngine.gameState.value ?: throw IllegalStateException("No game state")
            
            if (state.isGameOver) {
                throw GameException.GameOverException()
            }
            
            if (currentKing == null) {
                crownKing(challengerId)
                return GameResult.Success(Unit)
            }
            
            if (challengerId == currentKing) {
                return GameResult.Error(
                    GameException.InvalidAttackException("Cannot challenge yourself as king")
                )
            }
            
            val challengerStrength = state.battlefield[challengerId]
                ?.sumOf { it.animal.strength } ?: 0
            val kingStrength = state.battlefield[currentKing!!]
                ?.sumOf { it.animal.strength } ?: 0
            
            // King has Crown Shield (absorb first 5 damage)
            val effectiveDamage = maxOf(0, challengerStrength - 5)
            
            _kingEvents.emit(
                KingOfHillEvent.ChallengeIssued(
                    challengerId, 
                    currentKing!!, 
                    challengerStrength, 
                    kingStrength
                )
            )
            
            if (effectiveDamage > kingStrength) {
                dethroneKing(challengerId)
                consecutiveChallenges = 0
            } else {
                consecutiveChallenges++
                _kingEvents.emit(KingOfHillEvent.KingSurvived(currentKing!!, consecutiveChallenges))
                
                if (consecutiveChallenges >= 3) {
                    val winner = state.players[currentKing!!]
                    _kingEvents.emit(KingOfHillEvent.GameOver(winner, "3 Consecutive Challenges"))
                }
            }
            
            GameResult.Success(Unit)
        } catch (e: GameException) {
            GameResult.Error(e)
        } catch (e: Exception) {
            GameResult.Error(GameException.InvalidAttackException(e.message ?: "Challenge failed"))
        }
    }
    
    private suspend fun crownKing(newKingId: Int) {
        currentKing = newKingId
        consecutiveChallenges = 0
        _kingEvents.emit(KingOfHillEvent.NewKing(newKingId, "Player ${newKingId + 1}"))
    }
    
    private suspend fun dethroneKing(newKingId: Int) {
        val oldKing = currentKing!!
        crownKing(newKingId)
        _kingEvents.emit(KingOfHillEvent.Dethroned(oldKing, newKingId))
    }
    
    suspend fun awardCrownPoints() {
        try {
            currentKing?.let { kingId ->
                crownPoints[kingId] = (crownPoints[kingId] ?: 0) + 1
                
                if (crownPoints[kingId]!! >= 50) {
                    val state = baseEngine.gameState.value
                    state?.let {
                        _kingEvents.emit(
                            KingOfHillEvent.GameOver(
                                it.players[kingId], 
                                "50 Crown Points"
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            println("Award crown points failed: ${e.message}")
        }
    }
    
    fun getCrownPoints() = crownPoints.toMap()
    fun getCurrentKing() = currentKing
}

sealed class KingOfHillEvent {
    data class NewKing(val playerId: Int, val playerName: String) : KingOfHillEvent()
    data class Dethroned(val oldKing: Int, val newKing: Int) : KingOfHillEvent()
    data class ChallengeIssued(
        val challengerId: Int, 
        val kingId: Int, 
        val challengerStrength: Int,
        val kingStrength: Int
    ) : KingOfHillEvent()
    data class KingSurvived(val kingId: Int, val consecutiveChallenges: Int) : KingOfHillEvent()
    data class GameOver(val winner: Player, val reason: String) : KingOfHillEvent()
}

// ==================== Blitz Mode ====================
class BlitzEngine(
    private val baseEngine: WildTacticsGameEngine,
    private val playerCount: Int = 2
) {
    private val _blitzEvents = MutableSharedFlow<BlitzEvent>()
    val blitzEvents: SharedFlow<BlitzEvent> = _blitzEvents.asSharedFlow()
    
    private val _turnTimer = MutableStateFlow(30)
    val turnTimer: StateFlow<Int> = _turnTimer.asStateFlow()
    
    private val roundsWon = mutableMapOf<Int, Int>()
    private val timeoutCount = mutableMapOf<Int, Int>()
    private val cumulativeDamage = mutableMapOf<Int, Int>()
    private var currentRound = 1
    private var timerJob: Job? = null
    
    init {
        if (playerCount != 2) {
            throw IllegalArgumentException("Blitz mode requires exactly 2 players")
        }
        repeat(playerCount) {
            roundsWon[it] = 0
            timeoutCount[it] = 0
            cumulativeDamage[it] = 0
        }
    }
    
    suspend fun startTurnTimer(): GameResult<Unit> {
        return try {
            timerJob?.cancel()
            _turnTimer.value = 30
            
            val state = baseEngine.gameState.value ?: throw IllegalStateException("No game state")
            _blitzEvents.emit(BlitzEvent.TurnStarted(state.currentPlayerIndex, 30))
            
            timerJob = CoroutineScope(Dispatchers.Default).launch {
                while (_turnTimer.value > 0) {
                    delay(1000)
                    tickTimer()
                }
            }
            
            GameResult.Success(Unit)
        } catch (e: Exception) {
            GameResult.Error(GameException.InvalidAttackException("Timer start failed: ${e.message}"))
        }
    }
    
    private suspend fun tickTimer() {
        try {
            val current = _turnTimer.value
            if (current > 0) {
                _turnTimer.value = current - 1
                
                if (current == 10) {
                    val state = baseEngine.gameState.value ?: return
                    _blitzEvents.emit(BlitzEvent.TimeWarning(state.currentPlayerIndex, 10))
                }
                
                if (current == 1) {
                    handleTimeout()
                }
            }
        } catch (e: Exception) {
            println("Timer tick failed: ${e.message}")
        }
    }
    
    private suspend fun handleTimeout() {
        try {
            val state = baseEngine.gameState.value ?: return
            val playerId = state.currentPlayerIndex
            
            timeoutCount[playerId] = (timeoutCount[playerId] ?: 0) + 1
            
            val player = state.players.getOrNull(playerId) ?: return
            if (player.hand.isNotEmpty()) {
                val discardedCard = player.hand.removeAt(Random.nextInt(player.hand.size))
                _blitzEvents.emit(BlitzEvent.Timeout(playerId, discardedCard))
            }
            
            if (timeoutCount[playerId]!! >= 3) {
                val opponent = state.players.firstOrNull { it.id != playerId }
                opponent?.let {
                    _blitzEvents.emit(BlitzEvent.AutoForfeit(playerId, it))
                    endGame(it.id, "Opponent timed out 3 times")
                }
            } else {
                baseEngine.endPhase()
            }
        } catch (e: Exception) {
            println("Timeout handling failed: ${e.message}")
        }
    }
    
    suspend fun recordDamage(attackerId: Int, defenderId: Int, damage: Int): GameResult<Unit> {
        return try {
            if (damage < 0) {
                throw IllegalArgumentException("Damage cannot be negative")
            }
            
            cumulativeDamage[defenderId] = (cumulativeDamage[defenderId] ?: 0) + damage
            _blitzEvents.emit(BlitzEvent.DamageDealt(attackerId, defenderId, damage))
            
            if (cumulativeDamage[defenderId]!! >= 50) {
                endGame(attackerId, "50 cumulative damage dealt")
            } else {
                checkRoundEnd(attackerId)
            }
            
            GameResult.Success(Unit)
        } catch (e: Exception) {
            GameResult.Error(GameException.InvalidAttackException("Damage recording failed: ${e.message}"))
        }
    }
    
    private suspend fun checkRoundEnd(attackerId: Int) {
        try {
            roundsWon[attackerId] = (roundsWon[attackerId] ?: 0) + 1
            _blitzEvents.emit(BlitzEvent.RoundWon(attackerId, roundsWon[attackerId]!!))
            
            if (roundsWon[attackerId]!! >= 3) {
                endGame(attackerId, "Won 3 rounds")
            } else {
                startNewRound()
            }
        } catch (e: Exception) {
            println("Round end check failed: ${e.message}")
        }
    }
    
    private suspend fun startNewRound() {
        try {
            currentRound++
            _blitzEvents.emit(BlitzEvent.NewRound(currentRound))
            // Note: Actual state reset would be handled by the UI/ViewModel
        } catch (e: Exception) {
            println("New round start failed: ${e.message}")
        }
    }
    
    private suspend fun endGame(winnerId: Int, reason: String) {
        try {
            timerJob?.cancel()
            val state = baseEngine.gameState.value ?: return
            val winner = state.players.getOrNull(winnerId) ?: return
            _blitzEvents.emit(BlitzEvent.GameOver(winner, reason))
        } catch (e: Exception) {
            println("End game failed: ${e.message}")
        }
    }
    
    fun stopTimer() {
        timerJob?.cancel()
    }
    
    fun getRoundsWon() = roundsWon.toMap()
    fun getCumulativeDamage() = cumulativeDamage.toMap()
}

sealed class BlitzEvent {
    data class TurnStarted(val playerId: Int, val timeLimit: Int) : BlitzEvent()
    data class TimeWarning(val playerId: Int, val secondsLeft: Int) : BlitzEvent()
    data class Timeout(val playerId: Int, val discardedCard: Card) : BlitzEvent()
    data class AutoForfeit(val loserId: Int, val winner: Player) : BlitzEvent()
    data class DamageDealt(val attackerId: Int, val defenderId: Int, val damage: Int) : BlitzEvent()
    data class RoundWon(val playerId: Int, val totalRounds: Int) : BlitzEvent()
    data class NewRound(val roundNumber: Int) : BlitzEvent()
    data class GameOver(val winner: Player, val reason: String) : BlitzEvent()
}

// ==================== Network Manager ====================
class NetworkGameManager {
    private val _networkState = MutableStateFlow<NetworkState>(NetworkState.Disconnected)
    val networkState: StateFlow<NetworkState> = _networkState.asStateFlow()
    
    private var connectionJob: Job? = null

    suspend fun hostGame(port: Int = 8888): GameResult<String> {
        return try {
            if (port !in 1024..65535) {
                throw IllegalArgumentException("Invalid port number: $port")
            }
            
            // Implement WiFi hosting logic
            val address = "192.168.1.100:$port"
            _networkState.value = NetworkState.Hosting("192.168.1.100", port)
            GameResult.Success(address)
        } catch (e: Exception) {
            val error = GameException.NetworkException("Failed to host game: ${e.message}")
            _networkState.value = NetworkState.Error(error.message!!)
            GameResult.Error(error)
        }
    }

    suspend fun joinGame(hostAddress: String, port: Int = 8888): GameResult<Unit> {
        return try {
            if (hostAddress.isBlank()) {
                throw IllegalArgumentException("Host address cannot be empty")
            }
            if (port !in 1024..65535) {
                throw IllegalArgumentException("Invalid port number: $port")
            }
            
            // Implement WiFi joining logic
            _networkState.value = NetworkState.Connecting(hostAddress, port)
            
            // Simulate connection attempt
            delay(1000)
            
            _networkState.value = NetworkState.Connected(hostAddress, port)
            GameResult.Success(Unit)
        } catch (e: Exception) {
            val error = GameException.NetworkException("Failed to join game: ${e.message}")
            _networkState.value = NetworkState.Error(error.message!!)
            GameResult.Error(error)
        }
    }

    suspend fun sendGameAction(action: NetworkAction): GameResult<Unit> {
        return try {
            val state = _networkState.value
            if (state !is NetworkState.Connected && state !is NetworkState.Hosting) {
                throw GameException.NetworkException("Not connected to network")
            }
            
            // Send action to other device
            GameResult.Success(Unit)
        } catch (e: Exception) {
            GameResult.Error(GameException.NetworkException("Failed to send action: ${e.message}"))
        }
    }

    suspend fun disconnect(): GameResult<Unit> {
        return try {
            connectionJob?.cancel()
            _networkState.value = NetworkState.Disconnected
            GameResult.Success(Unit)
        } catch (e: Exception) {
            GameResult.Error(GameException.NetworkException("Disconnect failed: ${e.message}"))
        }
    }
}

sealed class NetworkState {
    data object Disconnected : NetworkState()
    data class Connecting(val hostAddress: String, val port: Int) : NetworkState()
    data class Hosting(val address: String, val port: Int) : NetworkState()
    data class Connected(val hostAddress: String, val port: Int) : NetworkState()
    data class Error(val message: String) : NetworkState()
}

sealed class NetworkAction {
    data class PlayCard(val playerId: Int, val cardIndex: Int, val targetPlayerId: Int?) : NetworkAction()
    data class Attack(val attackerId: Int, val defenderIds: List<Int>, val cardIndices: List<Int>) : NetworkAction()
    data class EndPhase(val playerId: Int) : NetworkAction()
}

// ==================== Utility Extensions ====================
fun GameMode.toDisplayString(): String = when (this) {
    is GameMode.SinglePlayer -> "Single Player (${aiDifficulty.name})"
    is GameMode.PassAndPlay -> "Pass & Play"
    is GameMode.LocalMultiplayer -> "Local WiFi"
    is GameMode.KingOfHill -> "King of the Hill"
    is GameMode.Blitz -> "Blitz Mode"
}

fun GameMode.requiresTimer(): Boolean = this is GameMode.Blitz

fun GameMode.maxPlayers(): Int = when (this) {
    is GameMode.Blitz -> 2
    is GameMode.KingOfHill -> 4
    else -> 4
}

fun GameMode.minPlayers(): Int = when (this) {
    is GameMode.KingOfHill -> 3
    else -> 2
}