package com.enigma.fluffyinc.apps.games.explodingkitties3.game.gamelogic

import com.enigma.fluffyinc.apps.games.explodingkitties3.data.Card
import kotlinx.serialization.Serializable

@Serializable
data class GameAction(
    val actionType: String, // e.g., "PLAY_CARD", "END_TURN"
    val card: Card? = null, // The card being played
    val playerId: Int
)