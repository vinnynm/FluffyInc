package com.enigma.fluffyinc.apps.games.explodingkitties3.data

import com.enigma.fluffyinc.games.explodingkitties3.data.types.AIDifficulty
import com.enigma.fluffyinc.games.explodingkitties3.data.types.PlayerType
import kotlinx.serialization.Serializable

@Serializable
data class Player(
    val id: Int,
    val name: String,
    val hand: MutableList<Card>,
    val isAlive: Boolean = true,
    val type: PlayerType = PlayerType.HUMAN,
    val aiDifficulty: AIDifficulty = AIDifficulty.MEDIUM,
    val turnsToTake: Int = 1 // New: Tracks turns for Attack cards
)
