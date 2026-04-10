package com.enigma.fluffyinc.apps.games.explodingkitties3.game

import android.app.Application
import android.content.Context
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.enigma.fluffyinc.R
import com.enigma.fluffyinc.apps.games.explodingkitties3.data.Card
import com.enigma.fluffyinc.apps.games.explodingkitties3.data.Player
import com.enigma.fluffyinc.apps.games.explodingkitties3.data.states.GameMode
import com.enigma.fluffyinc.apps.games.explodingkitties3.data.states.GameState
import com.enigma.fluffyinc.apps.games.explodingkitties3.data.states.GameUiState
import com.enigma.fluffyinc.games.explodingkitties3.data.types.AIDifficulty
import com.enigma.fluffyinc.games.explodingkitties3.data.types.CardType
import com.enigma.fluffyinc.games.explodingkitties3.data.types.PlayerType
import com.enigma.fluffyinc.apps.games.explodingkitties3.game.gamelogic.AIPlayer
import com.enigma.fluffyinc.apps.games.explodingkitties3.game.gamelogic.GameAction
import com.enigma.fluffyinc.apps.games.explodingkitties3.game.gamelogic.GameStateUpdate
import com.enigma.fluffyinc.games.explodingkitties3.game.processors.NetworkManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private const val PREFS_NAME = "ExplodingKittensPrefs"
private const val SAVED_GAME_KEY = "SavedGame"

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState = _uiState.asStateFlow()

    private val networkManager = NetworkManager(viewModelScope)
    private var isHost: Boolean = false
    private var myPlayerId: Int = 1
    private var aiPlayer: AIPlayer? = null
    private val sharedPreferences = application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    init {
        setupNetworkCallbacks()
        checkForSavedGame()

        // AI turn logic
        _uiState.onEach { state ->
            if (state.gameState == GameState.PLAYING && state.gameMode == GameMode.SINGLE_PLAYER && state.players.isNotEmpty() && state.currentPlayerIndex < state.players.size) {
                val currentPlayer = state.players[state.currentPlayerIndex]
                if (currentPlayer.type == PlayerType.AI && currentPlayer.isAlive && aiPlayer != null) {
                    delay(1500)
                    val move = aiPlayer!!.makeMove(currentPlayer, state.deck.size, state.players)
                    if (move.action == "PLAY" && move.card != null) {
                        onPlayCard(move.card)
                    } else {
                        executeEndTurnAndDraw()
                    }
                }
            }
        }.launchIn(viewModelScope)
    }

    private fun setupNetworkCallbacks() {
        networkManager.onStateReceived = { onStateUpdateReceived(it) }
        networkManager.onActionReceived = { onActionReceivedFromClient(it) }
        networkManager.onClientConnected = { ip ->
            val currentPlayers = _uiState.value.connectedPlayers.toMutableList()
            currentPlayers.add(ip)
            _uiState.update { it.copy(connectedPlayers = currentPlayers, connectionStatus = "$ip has joined!") }
        }
        networkManager.onClientDisconnected = { ip ->
            _uiState.update { it.copy(gameState = GameState.GAME_OVER, gameMessage = "$ip has disconnected. Game over.") }
        }
        networkManager.onHostDisconnected = {
            _uiState.update { it.copy(gameState = GameState.GAME_OVER, gameMessage = "Host has disconnected. Game over.") }
        }
    }

    // --- Event Handlers from UI ---

    fun onGameModeSelected(mode: GameMode) {
        aiPlayer = null
        isHost = (mode == GameMode.NETWORK_HOST)
        myPlayerId = if (isHost) 1 else 2
        _uiState.update { it.copy(
            gameMode = mode,
            gameState = if (mode == GameMode.NETWORK_HOST || mode == GameMode.NETWORK_JOIN) GameState.LOBBY else GameState.SETUP,
            localIP = if (mode == GameMode.NETWORK_HOST) networkManager.getLocalIPAddress(getApplication()) else ""
        )}
    }

    fun onPlayerCountChange(newCount: Int) {
        if (newCount in 2..5) {
            _uiState.update { it.copy(playerCount = newCount) }
        }
    }

    fun onAIDifficultyChange(difficulty: AIDifficulty) {
        _uiState.update { it.copy(aiDifficulty = difficulty) }
    }

    fun onStartHost() {
        isHost = true
        myPlayerId = 1
        networkManager.startHost()
        _uiState.update { it.copy(connectionStatus = "Hosting on ${_uiState.value.localIP}...") }
    }

    fun onJoinHost(ip: String) {
        if (ip.isBlank()) {
            _uiState.update { it.copy(connectionStatus = "IP Address cannot be empty.") }
            return
        }
        isHost = false
        myPlayerId = 2
        networkManager.connectToHost(ip)
        _uiState.update { it.copy(connectionStatus = "Connecting to $ip...") }
    }

    fun onStartGame() {
        if (!isHost && _uiState.value.gameMode == GameMode.NETWORK_JOIN) return
        clearSavedGame()

        if (_uiState.value.gameMode == GameMode.SINGLE_PLAYER) {
            aiPlayer = AIPlayer(_uiState.value.aiDifficulty)
        }

        val playerCount = _uiState.value.playerCount
        val deck = createOfficialDeck(playerCount).toMutableList()
        val players = (1..playerCount).map { id ->
            val hand = mutableListOf(createCard(CardType.DEFUSE))
            repeat(7) {
                if (deck.isNotEmpty()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                        hand.add(deck.removeFirst())
                    } else {
                        hand.add(deck.removeAt(0))
                    }
                }
            }
            when (_uiState.value.gameMode) {
                GameMode.SINGLE_PLAYER -> Player(id = id, name = if (id == 1) "You" else "AI $id", hand = hand, type = if (id == 1) PlayerType.HUMAN else PlayerType.AI)
                else -> Player(id = id, name = "Player $id", hand = hand)
            }
        }
        repeat(playerCount - 1) { deck.add(createCard(CardType.EXPLODING_KITTEN)) }
        deck.shuffle()

        _uiState.update { it.copy(
            gameState = GameState.PLAYING,
            players = players,
            deck = deck,
            discardPile = emptyList(),
            currentPlayerIndex = 0,
            gameMessage = "The game has begun! ${players[0].name}'s turn."
        )}

        if (isHost) broadcastState()
    }

    fun onResumeGame() {
        val savedJson = sharedPreferences.getString(SAVED_GAME_KEY, null)
        if (savedJson != null) {
            val savedState = Json.decodeFromString<GameUiState>(savedJson)
            if (savedState.gameMode == GameMode.SINGLE_PLAYER) {
                aiPlayer = AIPlayer(savedState.aiDifficulty)
            }
            _uiState.value = savedState
        }
    }

    fun onExitGame() {
        val state = _uiState.value
        if ((state.gameMode == GameMode.SINGLE_PLAYER || state.gameMode == GameMode.PASS_AND_PLAY) && state.gameState == GameState.PLAYING) {
            saveGameState()
        } else {
            clearSavedGame()
        }
        resetGame()
    }

    private fun saveGameState() {
        val stateJson = Json.encodeToString(_uiState.value)
        sharedPreferences.edit().putString(SAVED_GAME_KEY, stateJson).apply()
    }

    private fun checkForSavedGame() {
        val hasSavedGame = sharedPreferences.contains(SAVED_GAME_KEY)
        _uiState.update { it.copy(hasSavedGame = hasSavedGame) }
    }

    private fun clearSavedGame() {
        sharedPreferences.edit().remove(SAVED_GAME_KEY).apply()
    }

    fun onPlayCard(card: Card) {
        val state = _uiState.value
        if (state.gameState == GameState.NOPE_CHANCE) {
            if (card.type == CardType.NOPE) {
                executeNope(card)
            }
            return
        }

        if (isHost || state.gameMode != GameMode.NETWORK_JOIN) {
            executePlayCard(card)
            if (isHost) broadcastState()
        } else {
            networkManager.sendActionToHost(GameAction("PLAY_CARD", card, myPlayerId))
        }
    }

    private var actionCountdownJob: kotlinx.coroutines.Job? = null

    private fun startNopeTimer(card: Card) {
        actionCountdownJob?.cancel()
        _uiState.update { it.copy(
            gameState = GameState.NOPE_CHANCE,
            pendingAction = card,
            nopeCount = 0,
            actionCountdown = 5,
            gameMessage = "Playing ${card.name}... Any Nopes?"
        )}

        actionCountdownJob = viewModelScope.launch {
            while (_uiState.value.actionCountdown > 0) {
                delay(1000)
                _uiState.update { it.copy(actionCountdown = it.actionCountdown - 1) }
            }
            resolvePendingAction()
        }
    }

    private fun executeNope(card: Card) {
        val state = _uiState.value
        val players = state.players.toMutableList()
        val playerWhoNopedIndex = players.indexOfFirst { p -> p.hand.any { it.id == card.id } }
        if (playerWhoNopedIndex == -1) return

        val player = players[playerWhoNopedIndex]
        val newHand = player.hand.toMutableList().apply { remove(card) }
        players[playerWhoNopedIndex] = player.copy(hand = newHand)

        val newDiscard = state.discardPile.toMutableList().apply { add(card) }
        val newNopeCount = state.nopeCount + 1

        _uiState.update { it.copy(
            players = players,
            discardPile = newDiscard,
            nopeCount = newNopeCount,
            actionCountdown = 5,
            gameMessage = "NOPE! (Stack: $newNopeCount)"
        )}
    }

    private fun resolvePendingAction() {
        actionCountdownJob?.cancel()
        val state = _uiState.value
        val card = state.pendingAction ?: return
        val isCanceled = state.nopeCount % 2 != 0

        _uiState.update { it.copy(gameState = GameState.PLAYING, pendingAction = null, nopeCount = 0) }

        if (isCanceled) {
            _uiState.update { it.copy(gameMessage = "${card.name} was NOPED!") }
        } else {
            applyCardEffect(card)
        }
    }

    private fun executePlayCard(card: Card) {
        val state = _uiState.value
        val currentPlayerIndex = state.currentPlayerIndex
        val players = state.players.toMutableList()
        val currentPlayer = players[currentPlayerIndex]

        val newHand = currentPlayer.hand.toMutableList().apply { remove(card) }
        players[currentPlayerIndex] = currentPlayer.copy(hand = newHand)
        val newDiscard = state.discardPile.toMutableList().apply { add(card) }

        _uiState.update { it.copy(players = players, discardPile = newDiscard) }

        if (card.type == CardType.NORMAL) {
            handleCatCardPlay(card, currentPlayerIndex)
        } else if (card.type == CardType.NOPE) {
            _uiState.update { it.copy(gameMessage = "You can't Nope nothing!") }
        } else {
            startNopeTimer(card)
        }
    }

    private fun handleCatCardPlay(card: Card, playerIndex: Int) {
        val state = _uiState.value
        val player = state.players[playerIndex]
        val secondCard = player.hand.find { it.name == card.name }

        if (secondCard != null) {
            val newHand = player.hand.toMutableList().apply { remove(secondCard) }
            val players = state.players.toMutableList()
            players[playerIndex] = player.copy(hand = newHand)
            val newDiscard = state.discardPile.toMutableList().apply { add(secondCard) }

            _uiState.update { it.copy(
                players = players,
                discardPile = newDiscard,
                gameMessage = "${player.name} played a pair of ${card.name}s! Stealing a card..."
            )}
            startStealingLogic(playerIndex)
        } else {
            _uiState.update { it.copy(gameMessage = "${player.name} played a ${card.name}. (Need a pair to steal)") }
        }
    }

    private fun startStealingLogic(thiefIndex: Int) {
        val state = _uiState.value
        var victimIndex = (thiefIndex + 1) % state.players.size
        while (!state.players[victimIndex].isAlive) {
            victimIndex = (victimIndex + 1) % state.players.size
        }

        val victim = state.players[victimIndex]
        if (victim.hand.isNotEmpty()) {
            val stolenCard = victim.hand.random()
            val newVictimHand = victim.hand.toMutableList().apply { remove(stolenCard) }
            val thief = state.players[thiefIndex]
            val newThiefHand = thief.hand.toMutableList().apply { add(stolenCard) }

            val players = state.players.toMutableList()
            players[victimIndex] = victim.copy(hand = newVictimHand)
            players[thiefIndex] = thief.copy(hand = newThiefHand)

            _uiState.update { it.copy(
                players = players,
                gameMessage = "${thief.name} stole a card from ${victim.name}!"
            )}
        }
    }

    private fun applyCardEffect(card: Card) {
        val state = _uiState.value
        val players = state.players.toMutableList()
        val currentPlayerIndex = state.currentPlayerIndex
        val currentPlayer = players[currentPlayerIndex]

        when (card.type) {
            CardType.ATTACK -> {
                var victimIndex = (currentPlayerIndex + 1) % players.size
                while (!players[victimIndex].isAlive) {
                    victimIndex = (victimIndex + 1) % players.size
                }
                val victim = players[victimIndex]
                players[victimIndex] = victim.copy(turnsToTake = victim.turnsToTake + 1)
                _uiState.update { it.copy(players = players, gameMessage = "${currentPlayer.name} attacked ${victim.name}!") }
                endTurn(skipped = true)
            }
            CardType.SKIP -> {
                _uiState.update { it.copy(gameMessage = "${currentPlayer.name} skipped their turn.") }
                endTurn(skipped = false)
            }
            CardType.SEE_FUTURE -> {
                _uiState.update { it.copy(showFutureCards = true, futureCards = state.deck.take(3)) }
            }
            CardType.SHUFFLE -> {
                _uiState.update { it.copy(deck = state.deck.shuffled(), gameMessage = "The deck has been shuffled!") }
            }
            else -> {}
        }
    }

    private fun executeEndTurnAndDraw() {
        viewModelScope.launch {
            val state = _uiState.value
            if (state.deck.isEmpty()) {
                endTurn(skipped = true)
                return@launch
            }

            val currentPlayer = state.players[state.currentPlayerIndex]
            val newDeck = state.deck.toMutableList()
            val drawnCard = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                newDeck.removeFirst()
            } else {
                newDeck.removeAt(0)
            }

            if (drawnCard.type == CardType.EXPLODING_KITTEN) {
                handleExplodingKittenDraw(currentPlayer, drawnCard)
            } else {
                val newHand = currentPlayer.hand.toMutableList().apply { add(drawnCard) }
                val newPlayers = state.players.toMutableList().apply { this[state.currentPlayerIndex] = currentPlayer.copy(hand = newHand) }
                _uiState.update { it.copy(players = newPlayers, deck = newDeck, gameMessage = "${currentPlayer.name} drew a card.") }
                endTurn(skipped = false)
            }
        }
    }

    private fun handleExplodingKittenDraw(player: Player, kittenCard: Card) {
        val state = _uiState.value
        val defuseCard = player.hand.find { it.type == CardType.DEFUSE }
        if (defuseCard != null) {
            val newHand = player.hand.toMutableList().apply { remove(defuseCard) }
            val newPlayers = state.players.toMutableList().apply { this[state.currentPlayerIndex] = player.copy(hand = newHand) }
            val newDiscard = state.discardPile.toMutableList().apply { add(defuseCard) }
            _uiState.update { it.copy(gameState = GameState.AWAITING_KITTEN_PLACEMENT, cardToPlaceBack = kittenCard, players = newPlayers, discardPile = newDiscard, gameMessage = "${player.name} defused a Kitten! Place it back.")}
        } else {
            val newPlayers = state.players.toMutableList().apply { this[state.currentPlayerIndex] = player.copy(isAlive = false) }
            val newDiscard = state.discardPile.toMutableList().apply { add(kittenCard) }
            _uiState.update { it.copy(players = newPlayers, discardPile = newDiscard, gameMessage = "${player.name} exploded! 💥💀") }
            endTurn(skipped = true)
        }
    }

    private fun endTurn(skipped: Boolean) {
        val state = _uiState.value
        var nextPlayerWillTakeOver = false
        val currentPlayer = state.players[state.currentPlayerIndex]
        val turnsLeft = currentPlayer.turnsToTake - 1

        if (turnsLeft > 0 && !skipped) {
            val updatedPlayer = currentPlayer.copy(turnsToTake = turnsLeft)
            val newPlayers = state.players.toMutableList().apply { this[state.currentPlayerIndex] = updatedPlayer }
            _uiState.update { it.copy(players = newPlayers, gameMessage = "${currentPlayer.name} has $turnsLeft turns left.") }
        } else {
            nextPlayerWillTakeOver = true
            var nextIndex = state.currentPlayerIndex
            if (state.players.any { it.isAlive }) {
                do { nextIndex = (nextIndex + 1) % state.players.size } while (!state.players[nextIndex].isAlive)
            }

            val nextPlayerInitialTurns = if (skipped && turnsLeft > 0) turnsLeft else 1
            val players = state.players.toMutableList()
            players[nextIndex] = players[nextIndex].copy(turnsToTake = nextPlayerInitialTurns)
            _uiState.update { it.copy(players = players, currentPlayerIndex = nextIndex, gameMessage = "${players[nextIndex].name}'s turn.") }
        }

        val alivePlayers = _uiState.value.players.filter { it.isAlive }
        if (alivePlayers.size <= 1 && _uiState.value.gameState == GameState.PLAYING) {
            _uiState.update { it.copy(gameState = GameState.GAME_OVER, winner = alivePlayers.firstOrNull()) }
            return
        }

        if (nextPlayerWillTakeOver && state.gameMode == GameMode.PASS_AND_PLAY) {
            val nextPlayerName = _uiState.value.players[_uiState.value.currentPlayerIndex].name
            _uiState.update { it.copy(gameState = GameState.HANDOFF, gameMessage = "Pass the device to $nextPlayerName.")}
        }
        if (isHost) broadcastState()
    }

    private fun broadcastState() {
        if (!isHost) return
        val state = _uiState.value
        val update = GameStateUpdate(
            players = state.players,
            currentPlayerIndex = state.currentPlayerIndex,
            deck = state.deck,
            discardPile = state.discardPile,
            gameMessage = state.gameMessage,
            gameState = state.gameState
        )
        networkManager.broadcastStateToClients(update)
    }

    private fun createOfficialDeck(playerCount: Int): List<Card> {
        val deck = mutableListOf<Card>()
        repeat(4) { deck.add(createCard(CardType.ATTACK)) }
        repeat(4) { deck.add(createCard(CardType.SKIP)) }
        repeat(5) { deck.add(createCard(CardType.SEE_FUTURE)) }
        repeat(4) { deck.add(createCard(CardType.SHUFFLE)) }
        repeat(5) { deck.add(createCard(CardType.NOPE)) }

        repeat(4) { deck.add(createCard(CardType.NORMAL, "TacoCat")) }
        repeat(4) { deck.add(createCard(CardType.NORMAL, "Hairy Potato Cat")) }
        repeat(4) { deck.add(createCard(CardType.NORMAL, "Cattermelon")) }
        repeat(4) { deck.add(createCard(CardType.NORMAL, "Beard Cat")) }
        repeat(4) { deck.add(createCard(CardType.NORMAL, "Rainbow-ralphing Cat")) }

        val defuseCardsInDeck = if (playerCount <= 2) 2 else 6 - playerCount
        repeat(defuseCardsInDeck) { deck.add(createCard(CardType.DEFUSE)) }
        return deck.shuffled()
    }

    private fun createCard(type: CardType, name: String? = null): Card {
        val cardName = name ?: type.name.lowercase().replaceFirstChar { it.titlecase() }
        val displayName = when (type) {
            CardType.EXPLODING_KITTEN -> "💣 Exploding Kitten"
            CardType.DEFUSE -> "🛡️ Defuse"
            CardType.SKIP -> "⏭️ Skip"
            CardType.ATTACK -> "⚔️ Attack"
            CardType.SEE_FUTURE -> "🔮 See Future"
            CardType.SHUFFLE -> "🔀 Shuffle"
            CardType.NORMAL -> "🐱 $cardName"
            CardType.NOPE -> "🙅 Nope"
        }

        val imageId = when(name){
            "TacoCat" -> R.drawable.bugger_kitty2
            "⚔️ Attack"-> R.drawable.godcat
            "💣 Exploding Kitten" -> R.drawable.devil_kitty
            "Diffuse kitty" -> R.drawable.diffuse_kitty1
            "Future kitty" -> R.drawable.rainbowkitty
            "Cattermelon" -> R.drawable.watermelon_kitty
            "Hairy Potato Cat" -> R.drawable.zombiekittie
            "Beard Cat" -> R.drawable.beard_kitty
            "Rainbow-ralphing Cat" -> R.drawable.rainbowkitty
            "🙅 Nope" -> R.drawable.bugger_kitty2
            else -> {
                when (type) {
                    CardType.ATTACK -> R.drawable.godcat
                    CardType.SEE_FUTURE -> R.drawable.rainbowkitty
                    CardType.DEFUSE -> R.drawable.diffuse_kitty1
                    else -> null
                }
            }
        }
        return Card(suit = "", rank = "", type = type, name = displayName, imageId = imageId)
    }

    private fun resetGame() {
        networkManager.disconnect()
        aiPlayer = null
        _uiState.update { GameUiState(hasSavedGame = sharedPreferences.contains(SAVED_GAME_KEY)) }
    }

    override fun onCleared() {
        super.onCleared()
        networkManager.disconnect()
    }

    fun onShowTutorial() {
        _uiState.update { it.copy(gameState = GameState.TUTORIAL) }
    }

    fun onBackToMenu() = resetGame()
    fun onCloseFuture() { _uiState.update { it.copy(showFutureCards = false) } }
    fun onEndTurnAndDraw() = executeEndTurnAndDraw()

    private fun onActionReceivedFromClient(action: GameAction) {
        if (!isHost) return

        val state = _uiState.value
        val currentPlayer = state.players[state.currentPlayerIndex]
        if (action.playerId == currentPlayer.id) {
            when (action.actionType) {
                "PLAY_CARD" -> action.card?.let { onPlayCard(it) }
                "END_TURN" -> executeEndTurnAndDraw()
            }
            broadcastState()
        }
    }

    private fun onStateUpdateReceived(update: GameStateUpdate) {
        if (isHost) return
        _uiState.update { it.copy(
            players = update.players,
            currentPlayerIndex = update.currentPlayerIndex,
            deck = update.deck,
            discardPile = update.discardPile,
            gameMessage = update.gameMessage,
            gameState = update.gameState
        )}
    }

    fun onKittenPlaced(position: Int) {
        val card = _uiState.value.cardToPlaceBack ?: return
        val newDeck = _uiState.value.deck.toMutableList()
        newDeck.add(maxOf(0, minOf(position, newDeck.size)), card)
        _uiState.update { it.copy(gameState = GameState.PLAYING, cardToPlaceBack = null, deck = newDeck) }
        endTurn(skipped = true)
    }

    fun onHandoffConfirmed() {
        val nextPlayerName = _uiState.value.players[_uiState.value.currentPlayerIndex].name
        _uiState.update { it.copy(
            gameState = GameState.PLAYING,
            gameMessage = "It's your turn, $nextPlayerName!"
        )}
    }
}