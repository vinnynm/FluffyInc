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

/**
class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState = _uiState.asStateFlow()

    private var deck = mutableListOf<Card>()
    private var discardPile = mutableListOf<Card>()


    private var aiPlayer: AIPlayer? = null


    private val networkManager = NetworkManager(viewModelScope)
    private var isHost: Boolean = false
    private var myPlayerId: Int = UUID.randomUUID().variant()

    var playerId: Int = Random.nextInt(1, 1000)

    init {
        // Observe our own state to trigger AI moves
        // Setup network callbacks
        networkManager.onStateReceived = { stateUpdate -> onStateUpdateReceived(stateUpdate) }
        networkManager.onActionReceived = { gameAction -> onActionReceivedFromClient(gameAction) }

        // Observe UI state to trigger AI moves
        viewModelScope.launch {
            _uiState.onEach { state ->
                // --- FIX: Add guards to ensure we only run AI logic in the correct mode and state ---
                if (state.gameState == GameState.PLAYING &&
                    state.gameMode == GameMode.SINGLE_PLAYER &&
                    state.players.isNotEmpty() &&
                    state.currentPlayerIndex < state.players.size) {

                    val currentPlayer = state.players[state.currentPlayerIndex]
                    // Ensure the AI object exists and it's an AI's turn
                    if (currentPlayer.type == PlayerType.AI && currentPlayer.isAlive && aiPlayer != null) {
                        delay(1500) // AI "thinking" time
                        val move = aiPlayer!!.makeMove(currentPlayer, state.deckSize, state.players)
                        if (move.action == "PLAY" && move.card != null) {
                            onPlayCard(move.card)
                        } else {
                            onEndTurnAndDraw()
                        }
                    }
                }
            }
        // This is necessary for the onEach to execute
        }
    }



    // --- Event Handlers ---

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

    fun onShowTutorial() {
        _uiState.update { it.copy(gameState = GameState.TUTORIAL) }
    }

    fun onPlayerCountChange(newCount: Int) {
        if (newCount in 2..6) { _uiState.update { it.copy(playerCount = newCount) } }
    }

    fun onAIDifficultyChange(difficulty: AIDifficulty) {
        _uiState.update { it.copy(aiDifficulty = difficulty) }
    }

    fun onBackToMenu() = resetGame()

    fun onStartHost() {
        isHost = true
        myPlayerId = 1
        networkManager.startHost()
        _uiState.update { it.copy(connectionStatus = "Hosting on ${_uiState.value.localIP}...") }
    }


    fun onJoinHost(ip: String) {
        isHost = false
        // In a real app, player ID would be assigned by the host
        myPlayerId = 2
        networkManager.connectToHost(ip)
        _uiState.update { it.copy(connectionStatus = "Connecting to $ip...") }
    }

    fun onStartGame() {
        // This setup logic is now only ever run by the host or for local games.
        // Clients will receive the starting state from the host.
        if (!isHost && _uiState.value.gameMode == GameMode.NETWORK_JOIN) return

        // --- FIX: Initialize aiPlayer only for Single Player mode ---
        if (_uiState.value.gameMode == GameMode.SINGLE_PLAYER) {
            aiPlayer = AIPlayer(_uiState.value.aiDifficulty)
        } else {
            aiPlayer = null
        }
        deck = createOfficialDeck(_uiState.value.playerCount)
        val players = mutableListOf<Player>()
        val playerCount = _uiState.value.playerCount

        for (id in 1..playerCount) {
            val hand = mutableListOf(createCard(CardType.DEFUSE))
            repeat(7) { if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                hand.add(deck.removeFirst())
            }else{
                hand.add(deck.removeAt(0))
            }
            }

            val player = when (_uiState.value.gameMode) {
                GameMode.SINGLE_PLAYER -> Player(id = id, name = if (id == 1) "You" else "AI $id", hand = hand, type = if (id == 1) PlayerType.HUMAN else PlayerType.AI)
                else -> Player(id = id, name = "Player $id", hand = hand)
            }
            players.add(player)
        }

        repeat(playerCount - 1) { deck.add(createCard(CardType.EXPLODING_KITTEN)) }
        deck.shuffle()

        discardPile.clear()

        _uiState.update { it.copy(
            gameState = GameState.PLAYING,
            players = players,
            currentPlayerIndex = 0,
            deckSize = deck.size,
            discardPile = emptyList(),
            gameMessage = "The game has begun! ${players[0].name}'s turn."
        )}

        if (isHost) broadcastState()
    }


    /**

    // Add this new function to your GameViewModel
    fun onHandoffConfirmed() {
        val nextPlayerName = _uiState.value.players[_uiState.value.currentPlayerIndex].name
        _uiState.update { it.copy(
            gameState = GameState.PLAYING,
            gameMessage = "It's your turn, $nextPlayerName!"
        )}
    }
    */
fun onEndTurnAndDraw() {
    if (isHost || _uiState.value.gameMode != GameMode.NETWORK_JOIN) {
        executeEndTurnAndDraw()
        if (isHost) broadcastState()
    } else {
        networkManager.sendActionToHost(GameAction("END_TURN", playerId = myPlayerId))
    }
}

    private fun executeEndTurnAndDraw() = viewModelScope.launch {
        val currentPlayer = _uiState.value.players[_uiState.value.currentPlayerIndex]
        if (deck.isEmpty()) {
            _uiState.update { it.copy(gameMessage = "Deck is empty! Game over!") }
            return@launch
        }

        val drawnCard = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            deck.removeFirst()
        } else {
            deck.removeAt(0)
        }

        if (drawnCard.type == CardType.EXPLODING_KITTEN) {
            handleExplodingKittenDraw(currentPlayer, drawnCard)
        } else {
            val newHand = currentPlayer.hand.toMutableList().apply { add(drawnCard) }
            val newPlayers = _uiState.value.players.toMutableList().apply { this[_uiState.value.currentPlayerIndex] = currentPlayer.copy(hand = newHand) }
            _uiState.update { it.copy(players = newPlayers, gameMessage = "${currentPlayer.name} drew a card.") }
            endTurn(skipped=false)
        }
    }


    /**
    fun onPlayCard(card: Card) {
        val currentPlayerIndex = _uiState.value.currentPlayerIndex

        if (isHost || _uiState.value.gameMode == GameMode.PASS_AND_PLAY || _uiState.value.gameMode == GameMode.SINGLE_PLAYER) {
            // Host or local game: Execute logic directly
            var players = _uiState.value.players.toMutableList()
            val currentPlayer = players[currentPlayerIndex]

            val newHand = currentPlayer.hand.toMutableList().apply { remove(card) }
            players[currentPlayerIndex] = currentPlayer.copy(hand = newHand)
            discardPile.add(card)
            _uiState.update { it.copy(players = players, discardPile = discardPile.toList()) }

            when (card.type) {
                CardType.ATTACK -> {
                    var victimIndex = currentPlayerIndex
                    do { victimIndex = (victimIndex + 1) % players.size } while (!players[victimIndex].isAlive)
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
                    _uiState.update { it.copy(showFutureCards = true, futureCards = deck.take(3)) }
                }
                CardType.SHUFFLE -> {
                    deck.shuffle()
                    _uiState.update { it.copy(gameMessage = "The deck has been shuffled!") }
                }
                else -> {}
            }
            if (isHost) broadcastState()
        } else {
            // Client: Send action to host
            networkManager.sendActionToHost(GameAction("PLAY_CARD", card, myPlayerId))
        }

    }
    */

    fun onPlayCard(card: Card) {
    if (isHost || _uiState.value.gameMode != GameMode.NETWORK_JOIN) {
        executePlayCard(card)
        if (isHost) broadcastState()
    } else {
        networkManager.sendActionToHost(GameAction("PLAY_CARD", card, myPlayerId))
    }
}
    fun onHandoffConfirmed() {
        val nextPlayerName = _uiState.value.players[_uiState.value.currentPlayerIndex].name
        _uiState.update { it.copy(
            gameState = GameState.PLAYING,
            gameMessage = "It's your turn, $nextPlayerName!"
        )}
    }
    private fun executePlayCard(card: Card) {
        val currentPlayerIndex = _uiState.value.currentPlayerIndex

        if (isHost || _uiState.value.gameMode == GameMode.PASS_AND_PLAY || _uiState.value.gameMode == GameMode.SINGLE_PLAYER) {
            // Host or local game: Execute logic directly
            var players = _uiState.value.players.toMutableList()
            val currentPlayer = players[currentPlayerIndex]

            val newHand = currentPlayer.hand.toMutableList().apply { remove(card) }
            players[currentPlayerIndex] = currentPlayer.copy(hand = newHand)
            discardPile.add(card)
            _uiState.update { it.copy(players = players, discardPile = discardPile.toList()) }

            when (card.type) {
                CardType.ATTACK -> {
                    var victimIndex = currentPlayerIndex
                    do { victimIndex = (victimIndex + 1) % players.size } while (!players[victimIndex].isAlive)
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
                    _uiState.update { it.copy(showFutureCards = true, futureCards = deck.take(3)) }
                }
                CardType.SHUFFLE -> {
                    deck.shuffle()
                    _uiState.update { it.copy(gameMessage = "The deck has been shuffled!") }
                }
                else -> {}
            }

        }
    }

    fun onKittenPlaced(position: Int) {
        val card = _uiState.value.cardToPlaceBack ?: return
        deck.add(maxOf(0, minOf(position, deck.size)), card)
        _uiState.update { it.copy(gameState = GameState.PLAYING, cardToPlaceBack = null) }
        endTurn(skipped = true)
    }

    fun onCloseFuture() { _uiState.update { it.copy(showFutureCards = false) } }

    private fun handleExplodingKittenDraw(player: Player, kittenCard: Card) {
        val defuseCard = player.hand.find { it.type == CardType.DEFUSE }
        if (defuseCard != null) {
            val newHand = player.hand.toMutableList().apply { remove(defuseCard) }
            val newPlayers = _uiState.value.players.toMutableList().apply { this[_uiState.value.currentPlayerIndex] = player.copy(hand = newHand) }
            discardPile.add(defuseCard)
            _uiState.update { it.copy(gameState = GameState.AWAITING_KITTEN_PLACEMENT, cardToPlaceBack = kittenCard, players = newPlayers, gameMessage = "${player.name} defused a Kitten! Place it back.")}
        } else {
            val newPlayers = _uiState.value.players.toMutableList().apply { this[_uiState.value.currentPlayerIndex] = player.copy(isAlive = false) }
            discardPile.add(kittenCard)
            _uiState.update { it.copy(players = newPlayers, gameMessage = "${player.name} exploded! 💥💀") }
            endTurn(skipped = true)
        }
    }

    // Modify the endTurn function in GameViewModel
    private fun endTurn(skipped: Boolean) {
        if (isHost || _uiState.value.gameMode == GameMode.PASS_AND_PLAY || _uiState.value.gameMode == GameMode.SINGLE_PLAYER) {
            val currentPlayer = _uiState.value.players[_uiState.value.currentPlayerIndex]
            val turnsLeft = currentPlayer.turnsToTake - 1

            var nextPlayerWillTakeOver = false

            if (turnsLeft > 0 && !skipped) {
                // Player has more turns from an attack, does not hand off
                val updatedPlayer = currentPlayer.copy(turnsToTake = turnsLeft)
                val newPlayers = _uiState.value.players.toMutableList().apply { this[_uiState.value.currentPlayerIndex] = updatedPlayer }
                _uiState.update { it.copy(players = newPlayers, gameMessage = "${currentPlayer.name} has $turnsLeft turns left.") }
            } else {
                // Turn is over, find the next player
                nextPlayerWillTakeOver = true
                var nextIndex = _uiState.value.currentPlayerIndex
                do { nextIndex = (nextIndex + 1) % _uiState.value.players.size } while (!_uiState.value.players[nextIndex].isAlive)

                val nextPlayerInitialTurns = if (skipped && turnsLeft > 0) turnsLeft else 1
                val players = _uiState.value.players.toMutableList()
                players[_uiState.value.currentPlayerIndex] = currentPlayer.copy(turnsToTake = nextPlayerInitialTurns)
                _uiState.update { it.copy(players = players, currentPlayerIndex = nextIndex) }
            }

            val alivePlayers = _uiState.value.players.filter { it.isAlive }
            if (alivePlayers.size <= 1 && _uiState.value.gameState == GameState.PLAYING) {
                _uiState.update { it.copy(gameState = GameState.GAME_OVER, winner = alivePlayers.firstOrNull()) }
                return
            }

            // NEW LOGIC: Enter HANDOFF state if it's Pass and Play mode
            if (nextPlayerWillTakeOver && _uiState.value.gameMode == GameMode.PASS_AND_PLAY) {
                val nextPlayerName = _uiState.value.players[_uiState.value.currentPlayerIndex].name
                _uiState.update { it.copy(
                    gameState = GameState.HANDOFF,
                    gameMessage = "Pass the device to $nextPlayerName."
                )}
            }
            if (isHost) broadcastState()
        } else {
            networkManager.sendActionToHost(GameAction("END_TURN", playerId = myPlayerId))
        }

    }


    private fun createOfficialDeck(playerCount: Int): MutableList<Card> {
        val deck = mutableListOf<Card>()
        repeat(4) { deck.add(createCard(CardType.ATTACK)) }
        repeat(4) { deck.add(createCard(CardType.SKIP)) }
        repeat(5) { deck.add(createCard(CardType.SEE_FUTURE)) }
        repeat(4) { deck.add(createCard(CardType.SHUFFLE)) }
        repeat(4) { deck.add(createCard(CardType.NORMAL, "TacoCat")) }
        repeat(4) { deck.add(createCard(CardType.NORMAL, "Hairy Potato Cat")) }
        repeat(4) { deck.add(createCard(CardType.NORMAL, "Cattermelon")) }
        repeat(4) { deck.add(createCard(CardType.NORMAL, "Beard Cat")) }

        val defuseCardsInDeck = if (playerCount <= 2) 2 else 6 - playerCount
        repeat(defuseCardsInDeck) { deck.add(createCard(CardType.DEFUSE)) }
        return deck.shuffled().toMutableList()
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

        }

        val imageId = when(name){
            "TacoCat" -> R.drawable.bugger_kitty2
             "⚔️ Attack"-> R.drawable.godcat
            "💣 Exploding Kitten" -> R.drawable.devil_kitty
            "Diffuse kitty" -> R.drawable.diffuse_kitty1
            "Future kitty" -> R.drawable.rainbowkitty
            "Cattermelon" -> R.drawable.watermelon_kitty
            "Hairy Potato Cat" -> R.drawable.zombiekittie


            else -> {
                if (type==CardType.ATTACK){
                    R.drawable.godcat
                }else{
                    if (type==CardType.SEE_FUTURE){
                        R.drawable.rainbowkitty
                    }else{
                        if (CardType.DEFUSE==type){
                            R.drawable.diffuse_kitty1
                        }else{
                            null
                        }
                    }
                }
            }
        }
        return Card("", "", type, displayName,imageId =imageId)
    }


    // Host-side logic
    private fun onActionReceivedFromClient(action: GameAction) {
        if (!isHost) return

        // Basic validation: Is it this player's turn?
        if (action.playerId == _uiState.value.players[_uiState.value.currentPlayerIndex].id) {
            when (action.actionType) {
                "PLAY_CARD" -> action.card?.let { onPlayCard(it) }
                "END_TURN" -> {
                    endTurn(skipped = false)
                }
            }
            // After any action, broadcast the new truth to all clients
            broadcastState()
        }
    }

    // Client-side logic
    fun sendAction(actionType: String, card: Card? = null) {
        if (isHost) return // Hosts process logic directly
        viewModelScope.launch {
            val action = GameAction(actionType, card, playerId =playerId )
            networkManager.sendActionToHost(action)
        }
    }

    // Client receives an update and overwrites its local state
    private fun onStateUpdateReceived(update: GameStateUpdate) {
        if (isHost) return // Host is the source of truth, ignores its own broadcasts
        _uiState.update { it.copy(
            players = update.players,
            currentPlayerIndex = update.currentPlayerIndex,
            deckSize = update.deckSize,
            discardPile = update.discardPile,
            gameMessage = update.gameMessage,
            gameState = update.gameState
        )}
    }
    private fun broadcastState() {
        if (!isHost) return
        val state = _uiState.value
        val update = GameStateUpdate(
            players = state.players,
            currentPlayerIndex = state.currentPlayerIndex,
            deckSize = state.deckSize,
            discardPile = state.discardPile,
            gameMessage = state.gameMessage,
            gameState = state.gameState
        )
        networkManager.broadcastStateToClients(update)
    }
    private fun resetGame() = _uiState.update { GameUiState() }


    override fun onCleared() {
        super.onCleared()
        networkManager.disconnect()
    }


}

*/
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
                        onEndTurnAndDraw()
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
            repeat(7) { if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                hand.add(deck.removeFirst())
            }else{
                hand.add(deck.removeAt(0))
            } }
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
            // Re-initialize non-serializable parts if necessary
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
        if (isHost || _uiState.value.gameMode != GameMode.NETWORK_JOIN) {
            executePlayCard(card)
            if (isHost) broadcastState()
        } else {
            networkManager.sendActionToHost(GameAction("PLAY_CARD", card, myPlayerId))
        }
    }

    fun onEndTurnAndDraw() {
        if (isHost || _uiState.value.gameMode != GameMode.NETWORK_JOIN) {
            executeEndTurnAndDraw()
            if (isHost) broadcastState()
        } else {
            networkManager.sendActionToHost(GameAction("END_TURN", playerId = myPlayerId))
        }
    }

    fun onHandoffConfirmed() {
        val nextPlayerName = _uiState.value.players[_uiState.value.currentPlayerIndex].name
        _uiState.update { it.copy(
            gameState = GameState.PLAYING,
            gameMessage = "It's your turn, $nextPlayerName!"
        )}
    }

    fun onKittenPlaced(position: Int) {
        val state = _uiState.value
        val card = state.cardToPlaceBack ?: return
        val newDeck = state.deck.toMutableList()
        newDeck.add(maxOf(0, minOf(position, newDeck.size)), card)
        _uiState.update { it.copy(gameState = GameState.PLAYING, cardToPlaceBack = null, deck = newDeck) }
        endTurn(skipped = true)
        if (isHost) broadcastState()
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

    private fun onActionReceivedFromClient(action: GameAction) {
        if (!isHost) return
        if (action.playerId == _uiState.value.players[_uiState.value.currentPlayerIndex].id) {
            when (action.actionType) {
                "PLAY_CARD" -> action.card?.let { executePlayCard(it) }
                "END_TURN" -> executeEndTurnAndDraw()
            }
            broadcastState()
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

        when (card.type) {
            CardType.ATTACK -> {
                var victimIndex = currentPlayerIndex
                do { victimIndex = (victimIndex + 1) % players.size } while (!players[victimIndex].isAlive)
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
            }else{
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
        repeat(4) { deck.add(createCard(CardType.NORMAL, "TacoCat")) }
        repeat(4) { deck.add(createCard(CardType.NORMAL, "Hairy Potato Cat")) }
        repeat(4) { deck.add(createCard(CardType.NORMAL, "Cattermelon")) }
        repeat(4) { deck.add(createCard(CardType.NORMAL, "Beard Cat")) }

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


            else -> {
                if (type==CardType.ATTACK){
                    R.drawable.godcat
                }else{
                    if (type==CardType.SEE_FUTURE){
                        R.drawable.rainbowkitty
                    }else{
                        if (CardType.DEFUSE==type){
                            R.drawable.diffuse_kitty1
                        }else{
                            null
                        }
                    }
                }
            }
        }
        return Card("", "", type, displayName,imageId =imageId)
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

}