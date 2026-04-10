package com.enigma.fluffyinc.apps.games.explodingkitties3.data.states

import com.enigma.fluffyinc.apps.games.explodingkitties3.data.Card
import com.enigma.fluffyinc.apps.games.explodingkitties3.data.Player
import com.enigma.fluffyinc.games.explodingkitties3.data.types.AIDifficulty
import kotlinx.serialization.Serializable


@Serializable
data class GameUiState(
    val gameState: GameState = GameState.MENU,
    val players: List<Player> = emptyList(),
    val currentPlayerIndex: Int = 0,

    // --- NEW: Deck and Discard Pile are now part of the savable state ---
    val deck: List<Card> = emptyList(),
    val discardPile: List<Card> = emptyList(),

    val gameMessage: String = "Welcome to Exploding Kittens!",
    val showFutureCards: Boolean = false,
    val futureCards: List<Card> = emptyList(),
    val winner: Player? = null,
    val gameMode: GameMode? = null,
    val playerCount: Int = 2,
    val aiDifficulty: AIDifficulty = AIDifficulty.MEDIUM,
    val hostIP: String = "",
    val connectionStatus: String = "",
    val localIP: String = "",
    val connectedPlayers: List<String> = emptyList(),
    val cardToPlaceBack: Card? = null,

    // --- NEW: Nope Stack Logic ---
    val pendingAction: Card? = null,
    val nopeCount: Int = 0,
    val actionCountdown: Int = 0, // In seconds or steps
    val victimId: Int? = null, // For targeted actions

    // --- NEW: Flag to show/hide the resume button on the UI ---
    val hasSavedGame: Boolean = false
) {
    val deckSize: Int = deck.size
}
