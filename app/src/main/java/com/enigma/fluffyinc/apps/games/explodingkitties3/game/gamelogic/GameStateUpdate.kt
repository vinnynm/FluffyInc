package com.enigma.fluffyinc.apps.games.explodingkitties3.game.gamelogic

import com.enigma.fluffyinc.apps.games.explodingkitties3.data.Card
import com.enigma.fluffyinc.apps.games.explodingkitties3.data.Player
import com.enigma.fluffyinc.apps.games.explodingkitties3.data.states.GameState
import kotlinx.serialization.Serializable

@Serializable
data class GameStateUpdate(
    val players: List<Player>,
    val currentPlayerIndex: Int,
    val deck: List<Card>, // Deck is now part of the state update
    val discardPile: List<Card>,
    val gameMessage: String,
    val gameState: GameState
)