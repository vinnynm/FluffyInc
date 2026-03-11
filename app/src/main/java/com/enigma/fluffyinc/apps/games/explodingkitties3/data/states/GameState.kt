package com.enigma.fluffyinc.apps.games.explodingkitties3.data.states

import kotlinx.serialization.Serializable


@Serializable
enum class GameState {
    MENU, SETUP, LOBBY, PLAYING, GAME_OVER, TUTORIAL, AWAITING_KITTEN_PLACEMENT, HANDOFF
}
