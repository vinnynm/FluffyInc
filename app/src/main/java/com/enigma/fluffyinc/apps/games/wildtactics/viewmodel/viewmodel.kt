package com.enigma.fluffyinc.apps.games.wildtactics.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.enigma.fluffyinc.apps.games.wildtactics.*
import com.enigma.fluffyinc.apps.games.wildtactics.ai.*
import com.enigma.fluffyinc.apps.games.wildtactics.data.*
import com.enigma.fluffyinc.apps.games.wildtactics.processor.BlitzEngine
import com.enigma.fluffyinc.apps.games.wildtactics.processor.BlitzEvent
import com.enigma.fluffyinc.apps.games.wildtactics.processor.GameEvent
import com.enigma.fluffyinc.apps.games.wildtactics.processor.GameMode
import com.enigma.fluffyinc.apps.games.wildtactics.processor.GamePhase
import com.enigma.fluffyinc.apps.games.wildtactics.processor.GameState
import com.enigma.fluffyinc.apps.games.wildtactics.processor.KingOfHillEngine
import com.enigma.fluffyinc.apps.games.wildtactics.processor.KingOfHillEvent
import com.enigma.fluffyinc.apps.games.wildtactics.processor.NetworkGameManager
import com.enigma.fluffyinc.apps.games.wildtactics.processor.NetworkState
import com.enigma.fluffyinc.apps.games.wildtactics.processor.WildTacticsGameEngine
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.collections.get
import kotlin.text.get

// ==================== Enhanced ViewModel with AI Integration ====================

class WildTacticsViewModel : ViewModel() {
    // Game Engines
    private var gameEngine: WildTacticsGameEngine? = null
    private var kingEngine: KingOfHillEngine? = null
    private var blitzEngine: BlitzEngine? = null
    private var networkManager: NetworkGameManager? = null
    
    // AI System
    private var aiManager: AIManager? = null
    private var aiThinkingJob: Job? = null
    
    // State Flows
    val gameState = MutableStateFlow<GameState?>(null)
    val gameEvents = MutableStateFlow<List<GameEvent>>(emptyList())
    val networkState = MutableStateFlow<NetworkState>(NetworkState.Disconnected)
    
    // UI State
    var selectedCardIndex by mutableStateOf<Int?>(null)
    var selectedTargetPlayer by mutableStateOf<Int?>(null)
    var selectedAttackCards by mutableStateOf<Set<Int>>(emptySet())
    var showGameModeSelector by mutableStateOf(true)
    var showVictoryDialog by mutableStateOf(false)
    var showNetworkDialog by mutableStateOf(false)
    var showAIThinking by mutableStateOf(false)
    var eventLog by mutableStateOf<List<String>>(emptyList())
    var connectedPlayers by mutableStateOf<List<String>>(emptyList())
    var errorMessage by mutableStateOf<String?>(null)
    var flippedCards by mutableStateOf<Set<Int>>(emptySet())
    
    // Blitz mode specific
    var turnTimer by mutableIntStateOf(30)
    var roundsWon by mutableStateOf<Map<Int, Int>>(emptyMap())
    var cumulativeDamage by mutableStateOf<Map<Int, Int>>(emptyMap())
    
    // King of Hill specific
    var currentKing by mutableStateOf<Int?>(null)
    var crownPoints by mutableStateOf<Map<Int, Int>>(emptyMap())
    
    // AI Debug Info
    var aiDebugInfo by mutableStateOf<String?>(null)
    var showAIDebug by mutableStateOf(false)

    var showTutorial by mutableStateOf(false)

    // Statistics
    var totalTurns by mutableIntStateOf(0)
    var playerWins by mutableIntStateOf(0)
    var aiWins by mutableIntStateOf(0)

    // ==================== Game Initialization ====================
    fun startGame(gameMode: GameMode, playerCount: Int = 2) {
        viewModelScope.launch {
            try {
                cleanup()
                
                // Initialize game engine
                gameEngine = WildTacticsGameEngine(gameMode, playerCount)
                
                // Initialize AI for single player mode
                if (gameMode is GameMode.SinglePlayer) {
                    aiManager = AIManager(gameMode.aiDifficulty, gameMode)
                    addToEventLog("🤖 AI Difficulty: ${gameMode.aiDifficulty.name}")
                }
                
                // Initialize mode-specific engines
                when (gameMode) {
                    is GameMode.KingOfHill -> {
                        kingEngine = KingOfHillEngine(gameEngine!!, playerCount)
                        setupKingOfHillListeners()
                        addToEventLog("👑 King of the Hill mode started!")
                    }
                    is GameMode.Blitz -> {
                        blitzEngine = BlitzEngine(gameEngine!!, playerCount)
                        setupBlitzListeners()
                        addToEventLog("⚡ Blitz mode started! 30 seconds per turn")
                    }
                    is GameMode.LocalMultiplayer -> {
                        networkManager = NetworkGameManager()
                        setupNetworkListeners()
                        addToEventLog("🌐 Network game initialized")
                    }
                    else -> {
                        addToEventLog("🎮 Game started!")
                    }
                }
                
                // Setup game state listeners
                setupGameStateListeners()
                setupGameEventListeners()
                
                showGameModeSelector = false
                totalTurns = 0
                
            } catch (e: Exception) {
                handleError("Failed to start game: ${e.message}")
            }
        }
    }
    
    // ==================== Listener Setup ====================
    private fun setupGameStateListeners() {
        viewModelScope.launch {
            gameEngine?.gameState?.collect { state ->
                state?.let {
                    gameState.value = it
                    
                    if (it.isGameOver) {
                        handleGameOver(it.winner)
                    } else {
                        // Check if it's AI's turn
                        val currentPlayer = it.players[it.currentPlayerIndex]
                        if (currentPlayer.isAiPlayer && !showAIThinking) {
                            handleAITurn(it.currentPlayerIndex)
                        }
                    }
                    
                    errorMessage = it.errorMessage
                }
            }
        }
    }
    
    private fun setupGameEventListeners() {
        viewModelScope.launch {
            gameEngine?.gameEvents?.collect { event ->
                gameEvents.value += event
                addToEventLog(event)
                
                // Handle event-specific UI updates
                when (event) {
                    is GameEvent.PhaseChanged -> {
                        if (event.newPhase == GamePhase.Draw) {
                            totalTurns++
                        }
                    }
                    is GameEvent.Error -> {
                        errorMessage = event.message
                    }
                    else -> {}
                }
            }
        }
    }
    
    private fun setupKingOfHillListeners() {
        viewModelScope.launch {
            kingEngine?.kingEvents?.collect { event ->
                handleKingEvent(event)
            }
        }
    }
    
    private fun setupBlitzListeners() {
        viewModelScope.launch {
            blitzEngine?.blitzEvents?.collect { event ->
                handleBlitzEvent(event)
            }
        }
        
        viewModelScope.launch {
            blitzEngine?.turnTimer?.collect { time ->
                turnTimer = time
            }
        }
        
        // Start first turn timer
        viewModelScope.launch {
            delay(500)
            blitzEngine?.startTurnTimer()
        }
    }
    
    private fun setupNetworkListeners() {
        viewModelScope.launch {
            networkManager?.networkState?.collect { state ->
                networkState.value = state
                
                when (state) {
                    is NetworkState.Connected -> {
                        addToEventLog("✅ Connected to host")
                    }
                    is NetworkState.Hosting -> {
                        addToEventLog("🏠 Hosting on ${state.address}")
                    }
                    is NetworkState.Error -> {
                        errorMessage = state.message
                    }
                    else -> {}
                }
            }
        }
    }
    
    // ==================== AI Turn Management ====================
    private fun handleAITurn(aiPlayerId: Int) {
        aiThinkingJob?.cancel()
        aiThinkingJob = viewModelScope.launch {
            try {
                showAIThinking = true
                val state = gameState.value ?: return@launch
                
                addToEventLog("🤖 AI is thinking...")
                
                if (showAIDebug) {
                    aiDebugInfo = "AI analyzing position for Player ${aiPlayerId + 1}..."
                }
                
                // Execute AI turn through AI Manager
                aiManager?.executeTurn(
                    state = state,
                    aiPlayerId = aiPlayerId,
                    engine = gameEngine!!,
                    kingEngine = kingEngine,
                    blitzEngine = blitzEngine
                )
                
                if (showAIDebug) {
                    aiDebugInfo = "AI completed turn"
                }
                
            } catch (e: Exception) {
                handleError("AI turn failed: ${e.message}")
                // Force end phase to prevent softlock
                gameEngine?.endPhase()
            } finally {
                showAIThinking = false
            }
        }
    }
    
    // ==================== Player Actions ====================
    fun playCard(cardIndex: Int, targetPlayerId: Int? = null) {
        viewModelScope.launch {
            try {
                val state = gameState.value ?: return@launch
                
                if (state.players[state.currentPlayerIndex].isAiPlayer) {
                    addToEventLog("⚠️ Wait for AI to finish")
                    return@launch
                }
                
                // Trigger flip animation
                flippedCards = flippedCards + cardIndex
                
                val result = gameEngine?.playCard(
                    state.currentPlayerIndex,
                    cardIndex,
                    targetPlayerId
                )
                
                result?.onError { error ->
                    errorMessage = error.message
                    flippedCards = flippedCards - cardIndex
                }?.onSuccess {
                    viewModelScope.launch {
                        delay(600)
                    }
                     // Wait for flip animation
                    flippedCards = flippedCards - cardIndex
                    selectedCardIndex = null
                    selectedTargetPlayer = null
                    
                    if (showAIDebug) {
                        aiDebugInfo = "Card played successfully"
                    }
                }
                
            } catch (e: Exception) {
                handleError("Failed to play card: ${e.message}")
            }
        }
    }

    fun attack(defenderIds: List<Int>) {
        viewModelScope.launch {
            try {
                val state = gameState.value ?: return@launch
                
                if (state.players[state.currentPlayerIndex].isAiPlayer) {
                    addToEventLog("⚠️ Wait for AI to finish")
                    return@launch
                }
                
                if (selectedAttackCards.isEmpty()) {
                    errorMessage = "Select cards to attack with"
                    return@launch
                }
                
                val result = gameEngine?.attack(
                    state.currentPlayerIndex,
                    defenderIds,
                    selectedAttackCards.toList()
                )
                
                result?.onError { error ->
                    errorMessage = error.message
                }?.onSuccess {
                    selectedAttackCards = emptySet()
                    
                    // Record damage for blitz mode
                    blitzEngine?.let { blitz ->
                        val damage = 10 // Calculate actual damage
                        viewModelScope.launch {
                            blitz.recordDamage(state.currentPlayerIndex, defenderIds.first(), damage)
                        }

                    }
                }
                
            } catch (e: Exception) {
                handleError("Attack failed: ${e.message}")
            }
        }
    }

    fun endPhase() {
        viewModelScope.launch {
            try {
                val state = gameState.value ?: return@launch
                
                if (state.players[state.currentPlayerIndex].isAiPlayer) {
                    addToEventLog("⚠️ Wait for AI to finish")
                    return@launch
                }
                
                val result = gameEngine?.endPhase()
                
                result?.onError { error ->
                    errorMessage = error.message
                }?.onSuccess {
                    // Handle mode-specific phase end logic
                    when (state.gameMode) {
                        is GameMode.Blitz -> {
                            if (state.gamePhase == GamePhase.End) {
                                viewModelScope.launch {
                                    blitzEngine?.startTurnTimer()
                                }

                            }
                        }
                        is GameMode.KingOfHill -> {
                            if (state.gamePhase == GamePhase.End) {
                                viewModelScope.launch {
                                    kingEngine?.awardCrownPoints()
                                }

                            }
                        }
                        else -> {}
                    }
                }
                
            } catch (e: Exception) {
                handleError("Failed to end phase: ${e.message}")
            }
        }
    }
    
    fun challengeKing(challengerId: Int) {
        viewModelScope.launch {
            try {
                val state = gameState.value ?: return@launch
                
                if (state.players[challengerId].isAiPlayer) {
                    return@launch
                }
                
                val result = kingEngine?.challengeKing(challengerId)
                
                result?.onError { error ->
                    errorMessage = error.message
                }?.onSuccess {
                    addToEventLog("⚔️ You challenged the King!")
                }
                
            } catch (e: Exception) {
                handleError("Challenge failed: ${e.message}")
            }
        }
    }
    
    // ==================== Event Handlers ====================
    private fun handleKingEvent(event: KingOfHillEvent) {
        when (event) {
            is KingOfHillEvent.NewKing -> {
                currentKing = event.playerId
                addToEventLog("👑 ${event.playerName} is now the King!")
                
                if (showAIDebug) {
                    aiDebugInfo = "New king crowned: Player ${event.playerId + 1}"
                }
            }
            is KingOfHillEvent.Dethroned -> {
                addToEventLog("⚔️ King dethroned! Player ${event.newKing + 1} takes the crown!")
            }
            is KingOfHillEvent.ChallengeIssued -> {
                addToEventLog("⚡ Player ${event.challengerId + 1} challenges the King! (${event.challengerStrength} vs ${event.kingStrength})")
            }
            is KingOfHillEvent.KingSurvived -> {
                addToEventLog("🛡️ King survives! (${event.consecutiveChallenges} consecutive challenges)")
            }
            is KingOfHillEvent.GameOver -> {
                addToEventLog("🏆 ${event.winner.name} wins! (${event.reason})")
                showVictoryDialog = true
            }
        }
        
        kingEngine?.let {
            crownPoints = it.getCrownPoints()
            currentKing = it.getCurrentKing()
        }
    }
    
    private fun handleBlitzEvent(event: BlitzEvent) {
        when (event) {
            is BlitzEvent.TurnStarted -> {
                addToEventLog("⏱️ Player ${event.playerId + 1}'s turn (${event.timeLimit}s)")
            }
            is BlitzEvent.TimeWarning -> {
                addToEventLog("⚠️ ${event.secondsLeft} seconds left!")
            }
            is BlitzEvent.Timeout -> {
                addToEventLog("❌ Player ${event.playerId + 1} timed out! Card discarded")
            }
            is BlitzEvent.AutoForfeit -> {
                addToEventLog("🚫 Player ${event.loserId + 1} auto-forfeited (3 timeouts)")
            }
            is BlitzEvent.DamageDealt -> {
                addToEventLog("💥 ${event.damage} damage dealt!")
            }
            is BlitzEvent.RoundWon -> {
                addToEventLog("✨ Player ${event.playerId + 1} wins round ${event.totalRounds}!")
            }
            is BlitzEvent.NewRound -> {
                addToEventLog("🔄 Round ${event.roundNumber} begins!")
            }
            is BlitzEvent.GameOver -> {
                addToEventLog("🏆 ${event.winner.name} wins! (${event.reason})")
                showVictoryDialog = true
            }
        }
        
        blitzEngine?.let {
            roundsWon = it.getRoundsWon()
            cumulativeDamage = it.getCumulativeDamage()
        }
    }
    
    private fun handleGameOver(winner: Player?) {
        showVictoryDialog = true
        
        winner?.let {
            if (it.isAiPlayer) {
                aiWins++
                addToEventLog("🤖 AI wins!")
            } else {
                playerWins++
                addToEventLog("🎉 You win!")
            }
        }
        
        // Cleanup AI
        aiThinkingJob?.cancel()
        showAIThinking = false
    }
    
    private fun addToEventLog(message: String) {
        eventLog = (eventLog + message).takeLast(5)
    }
    
    private fun addToEventLog(event: GameEvent) {
        val message = when (event) {
            is GameEvent.CardPlayed -> "🎴 Player ${event.playerId + 1} played ${event.card.animal.name}"
            is GameEvent.CardDrawn -> "📥 Player ${event.playerId + 1} drew a card"
            is GameEvent.AttackCompleted -> "⚔️ Player ${event.attackerId + 1} attacked!"
            is GameEvent.PlayerDamaged -> "💥 Player ${event.playerId + 1} took ${event.damage} damage"
            is GameEvent.CardStolen -> "🎭 Player ${event.stealerId + 1} stole a card!"
            is GameEvent.PhaseChanged -> "📍 Phase: ${event.newPhase::class.simpleName}"
            is GameEvent.TurnEnded -> "🔄 Player ${event.nextPlayerId + 1}'s turn"
            is GameEvent.GameOver -> "🏆 ${event.winner.name} wins!"
            is GameEvent.Error -> "❌ Error: ${event.message}"
            else -> ""
        }
        if (message.isNotEmpty()) {
            addToEventLog(message)
        }
    }
    
    private fun handleError(message: String) {
        errorMessage = message
        addToEventLog("❌ $message")
    }
    
    // ==================== Network Functions ====================
    fun hostMultiplayerGame(port: Int = 8888) {
        viewModelScope.launch {
            try {
                val result = networkManager?.hostGame(port)
                result?.onSuccess { address ->
                    addToEventLog("🏠 Hosting on $address")
                }?.onError { error ->
                    errorMessage = error.message
                }
            } catch (e: Exception) {
                handleError("Failed to host: ${e.message}")
            }
        }
    }
    
    fun joinMultiplayerGame(hostAddress: String, port: Int = 8888) {
        viewModelScope.launch {
            try {
                val result = networkManager?.joinGame(hostAddress, port)
                result?.onSuccess {
                    addToEventLog("✅ Connected to $hostAddress")
                }?.onError { error ->
                    errorMessage = error.message
                }
            } catch (e: Exception) {
                handleError("Failed to connect: ${e.message}")
            }
        }
    }
    
    fun disconnectMultiplayer() {
        viewModelScope.launch {
            try {
                val result = networkManager?.disconnect()
                result?.onSuccess {
                    addToEventLog("🔌 Disconnected from game")
                }
            } catch (e: Exception) {
                handleError("Failed to disconnect: ${e.message}")
            }
        }
    }
    
    // ==================== UI State Management ====================
    fun toggleAIDebug() {
        showAIDebug = !showAIDebug
        if (showAIDebug) {
            addToEventLog("🔍 AI Debug enabled")
        } else {
            aiDebugInfo = null
        }
    }
    
    fun dismissError() {
        errorMessage = null
    }
    
    fun resetGame() {
        cleanup()
        gameState.value = null
        gameEvents.value = emptyList()
        eventLog = emptyList()
        selectedCardIndex = null
        selectedTargetPlayer = null
        selectedAttackCards = emptySet()
        showGameModeSelector = true
        showVictoryDialog = false
        showNetworkDialog = false
        showAIThinking = false
        showAIDebug = false
        errorMessage = null
        aiDebugInfo = null
        flippedCards = emptySet()
        currentKing = null
        crownPoints = emptyMap()
        turnTimer = 30
        roundsWon = emptyMap()
        cumulativeDamage = emptyMap()
        totalTurns = 0
    }
    
    fun quitToMenu() {
        resetGame()
    }
    
    // ==================== Statistics ====================
    fun getGameStats(): GameStats {
        return GameStats(
            totalGames = playerWins + aiWins,
            playerWins = playerWins,
            aiWins = aiWins,
            winRate = if (playerWins + aiWins > 0) {
                (playerWins.toFloat() / (playerWins + aiWins)) * 100
            } else 0f,
            totalTurns = totalTurns
        )
    }
    
    fun resetStats() {
        playerWins = 0
        aiWins = 0
        totalTurns = 0
        addToEventLog("📊 Statistics reset")
    }
    
    // ==================== Cleanup ====================
    private fun cleanup() {
        aiThinkingJob?.cancel()
        blitzEngine?.stopTimer()
        gameEngine?.cleanup()
        
        viewModelScope.launch {
            networkManager?.disconnect()
        }
        
        gameEngine = null
        kingEngine = null
        blitzEngine = null
        networkManager = null
        aiManager = null
    }

    override fun onCleared() {
        super.onCleared()
        cleanup()
    }
}


/**
// ==================== ViewModel ====================
class WildTacticsViewModel : ViewModel() {
    private var gameEngine: WildTacticsGameEngine? = null
    private var kingEngine: KingOfHillEngine? = null
    private var blitzEngine: BlitzEngine? = null
    private var networkManager: NetworkGameManager? = null

    val gameState = MutableStateFlow<GameState?>(null)
    val gameEvents = MutableStateFlow<List<GameEvent>>(emptyList())
    val networkState = MutableStateFlow<NetworkState>(NetworkState.Disconnected)

    var selectedCardIndex by mutableStateOf<Int?>(null)
    var selectedTargetPlayer by mutableStateOf<Int?>(null)
    var selectedAttackCards by mutableStateOf<Set<Int>>(emptySet())
    var showGameModeSelector by mutableStateOf(true)

    var showTutorial by mutableStateOf(false)
    var showVictoryDialog by mutableStateOf(false)
    var showNetworkDialog by mutableStateOf(false)
    var eventLog by mutableStateOf<List<String>>(emptyList())
    var connectedPlayers by mutableStateOf<List<String>>(emptyList())
    var errorMessage by mutableStateOf<String?>(null)
    var flippedCards by mutableStateOf<Set<Int>>(emptySet())

    // Blitz mode specific
    var turnTimer by mutableIntStateOf(30)
    var roundsWon by mutableStateOf<Map<Int, Int>>(emptyMap())

    // King of Hill specific
    var currentKing by mutableStateOf<Int?>(null)
    var crownPoints by mutableStateOf<Map<Int, Int>>(emptyMap())

    fun startGame(gameMode: GameMode, playerCount: Int = 2) {
        viewModelScope.launch {
            try {
                cleanup()

                gameEngine = WildTacticsGameEngine(gameMode, playerCount)

                when (gameMode) {
                    is GameMode.KingOfHill -> {
                        kingEngine = KingOfHillEngine(gameEngine!!, playerCount)
                        viewModelScope.launch {
                            kingEngine?.kingEvents?.collect { event ->
                                handleKingEvent(event)
                            }
                        }
                    }
                    is GameMode.Blitz -> {
                        blitzEngine = BlitzEngine(gameEngine!!, playerCount)
                        viewModelScope.launch {
                            blitzEngine?.blitzEvents?.collect { event ->
                                handleBlitzEvent(event)
                            }
                        }
                        viewModelScope.launch {
                            blitzEngine?.turnTimer?.collect { time ->
                                turnTimer = time
                            }
                        }
                        blitzEngine?.startTurnTimer()
                    }
                    is GameMode.LocalMultiplayer -> {
                        networkManager = NetworkGameManager()
                        viewModelScope.launch {
                            networkManager?.networkState?.collect { state ->
                                networkState.value = state
                            }
                        }
                    }
                    else -> {}
                }

                viewModelScope.launch {
                    gameEngine?.gameState?.collect { state ->
                        state?.let {
                            gameState.value = it
                            if (it.isGameOver) {
                                showVictoryDialog = true
                            }
                            errorMessage = it.errorMessage
                        }
                    }
                }

                viewModelScope.launch {
                    gameEngine?.gameEvents?.collect { event ->
                        gameEvents.value += event
                        addToEventLog(event)
                    }
                }

                showGameModeSelector = false
            } catch (e: Exception) {
                errorMessage = "Failed to start game: ${e.message}"
            }
        }
    }

    private fun handleKingEvent(event: KingOfHillEvent) {
        when (event) {
            is KingOfHillEvent.NewKing -> {
                currentKing = event.playerId
                addToEventLog("👑 ${event.playerName} is now the King!")
            }
            is KingOfHillEvent.Dethroned -> {
                addToEventLog("⚔️ King dethroned! Player ${event.newKing + 1} takes the crown!")
            }
            is KingOfHillEvent.ChallengeIssued -> {
                addToEventLog("⚡ Player ${event.challengerId + 1} challenges the King!")
            }
            is KingOfHillEvent.KingSurvived -> {
                addToEventLog("🛡️ King survives! (${event.consecutiveChallenges} challenges)")
            }
            is KingOfHillEvent.GameOver -> {
                addToEventLog("🏆 ${event.winner.name} wins! (${event.reason})")
                showVictoryDialog = true
            }
        }
        kingEngine?.let {
            crownPoints = it.getCrownPoints()
            currentKing = it.getCurrentKing()
        }
    }

    private fun handleBlitzEvent(event: BlitzEvent) {
        when (event) {
            is BlitzEvent.TurnStarted -> {
                addToEventLog("⏱️ Player ${event.playerId + 1}'s turn (${event.timeLimit}s)")
            }
            is BlitzEvent.TimeWarning -> {
                addToEventLog("⚠️ ${event.secondsLeft} seconds left!")
            }
            is BlitzEvent.Timeout -> {
                addToEventLog("❌ Player ${event.playerId + 1} timed out!")
            }
            is BlitzEvent.RoundWon -> {
                addToEventLog("✨ Player ${event.playerId + 1} wins round ${event.totalRounds}!")
            }
            is BlitzEvent.GameOver -> {
                addToEventLog("🏆 ${event.winner.name} wins! (${event.reason})")
                showVictoryDialog = true
            }
            else -> {}
        }
        blitzEngine?.let {
            roundsWon = it.getRoundsWon()
        }
    }

    private fun addToEventLog(message: String) {
        eventLog = (eventLog + message).takeLast(5)
    }

    private fun addToEventLog(event: GameEvent) {
        val message = when (event) {
            is GameEvent.CardPlayed -> "🎴 Player ${event.playerId + 1} played ${event.card.animal.name}"
            is GameEvent.CardDrawn -> "📥 Player ${event.playerId + 1} drew a card"
            is GameEvent.AttackCompleted -> "⚔️ Player ${event.attackerId + 1} attacked!"
            is GameEvent.PlayerDamaged -> "💥 Player ${event.playerId + 1} took ${event.damage} damage"
            is GameEvent.CardStolen -> "🎭 Player ${event.stealerId + 1} stole a card!"
            is GameEvent.PhaseChanged -> "📍 Phase: ${event.newPhase::class.simpleName}"
            is GameEvent.TurnEnded -> "🔄 Player ${event.nextPlayerId + 1}'s turn"
            is GameEvent.GameOver -> "🏆 ${event.winner.name} wins!"
            is GameEvent.Error -> "❌ Error: ${event.message}"
            else -> ""
        }
        if (message.isNotEmpty()) {
            addToEventLog(message)
        }
    }

    fun playCard(cardIndex: Int, targetPlayerId: Int? = null) {
        viewModelScope.launch {
            gameState.value?.let { state ->
                val result = gameEngine?.playCard(state.currentPlayerIndex, cardIndex, targetPlayerId)
                result?.onError { error ->
                    errorMessage = error.message
                }?.onSuccess {
                    // Trigger flip animation
                    flippedCards = flippedCards + cardIndex
                    viewModelScope.launch {
                        delay(600)
                    }

                    flippedCards = flippedCards - cardIndex

                    selectedCardIndex = null
                    selectedTargetPlayer = null
                }
            }
        }
    }

    fun attack(defenderIds: List<Int>) {
        viewModelScope.launch {
            gameState.value?.let { state ->
                if (selectedAttackCards.isNotEmpty()) {
                    val result = gameEngine?.attack(
                        state.currentPlayerIndex,
                        defenderIds,
                        selectedAttackCards.toList()
                    )
                    result?.onError { error ->
                        errorMessage = error.message
                    }?.onSuccess {
                        selectedAttackCards = emptySet()
                    }
                }
            }
        }
    }

    fun endPhase() {
        viewModelScope.launch {
            val result = gameEngine?.endPhase()
            result?.onError { error ->
                errorMessage = error.message
            }

            // Handle mode-specific logic
            when (gameState.value?.gameMode) {
                is GameMode.Blitz -> {
                    if (gameState.value?.gamePhase == GamePhase.End) {
                        blitzEngine?.startTurnTimer()
                    }
                }
                is GameMode.KingOfHill -> {
                    if (gameState.value?.gamePhase == GamePhase.End) {
                        kingEngine?.awardCrownPoints()
                    }
                }
                else -> {}
            }
        }
    }

    fun challengeKing(challengerId: Int) {
        viewModelScope.launch {
            val result = kingEngine?.challengeKing(challengerId)
            result?.onError { error ->
                errorMessage = error.message
            }
        }
    }

    fun resetGame() {
        cleanup()
        gameState.value = null
        gameEvents.value = emptyList()
        eventLog = emptyList()
        selectedCardIndex = null
        selectedTargetPlayer = null
        selectedAttackCards = emptySet()
        showGameModeSelector = true
        showVictoryDialog = false
        showNetworkDialog = false
        errorMessage = null
        currentKing = null
        crownPoints = emptyMap()
        turnTimer = 30
        roundsWon = emptyMap()
    }

    private fun cleanup() {
        gameEngine?.cleanup()
        blitzEngine?.stopTimer()
        viewModelScope.launch {
            networkManager?.disconnect()
        }
        gameEngine = null
        kingEngine = null
        blitzEngine = null
        networkManager = null
    }

    override fun onCleared() {
        super.onCleared()
        cleanup()
    }

    fun dismissError() {
        errorMessage = null
    }
}
*/

// ==================== Data Classes ====================
data class GameStats(
    val totalGames: Int,
    val playerWins: Int,
    val aiWins: Int,
    val winRate: Float,
    val totalTurns: Int
)

// ==================== ViewModel Factory (Optional) ====================
class WildTacticsViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WildTacticsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WildTacticsViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

// ==================== Extensions for UI ====================
fun GameState.getCurrentPlayerName(): String {
    return players[currentPlayerIndex].name
}

fun GameState.isPlayersTurn(playerId: Int): Boolean {
    return currentPlayerIndex == playerId && !players[currentPlayerIndex].isAiPlayer
}

fun GameState.getOpponents(playerId: Int): List<Player> {
    return players.filter { it.id != playerId && it.isAlive }
}

fun GameState.getBattlefieldStrength(playerId: Int): Int {
    return battlefield[playerId]?.sumOf { it.animal.strength } ?: 0
}

fun GameMode.getDisplayName(): String = when (this) {
    is GameMode.SinglePlayer -> "Single Player (${aiDifficulty.name})"
    is GameMode.PassAndPlay -> "Pass & Play"
    is GameMode.LocalMultiplayer -> "Local WiFi"
    is GameMode.KingOfHill -> "King of the Hill"
    is GameMode.Blitz -> "Blitz Mode"
}