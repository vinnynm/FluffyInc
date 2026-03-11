package com.enigma.fluffyinc.apps.games.explodingkitties3.game.gamelogic

import com.enigma.fluffyinc.apps.games.explodingkitties3.data.Card
import com.enigma.fluffyinc.apps.games.explodingkitties3.data.Player
import com.enigma.fluffyinc.games.explodingkitties3.data.types.AIDifficulty
import com.enigma.fluffyinc.games.explodingkitties3.data.types.CardType
import kotlin.random.Random
// --- AI Player Logic ---
class AIPlayer(private val difficulty: AIDifficulty) {
    data class AIMove(val action: String, val card: Card?)

    fun makeMove(player: Player, deckSize: Int, allPlayers: List<Player>): AIMove {
        val playableCards = player.hand.filter {
            it.type in listOf(CardType.SKIP, CardType.ATTACK, CardType.SEE_FUTURE, CardType.SHUFFLE)
        }
        return when (difficulty) {
            AIDifficulty.EASY -> makeEasyMove(playableCards)
            AIDifficulty.MEDIUM -> makeMediumMove(player, deckSize, playableCards)
            AIDifficulty.HARD -> makeHardMove(player, deckSize, allPlayers, playableCards)
        }
    }

    private fun makeEasyMove(playableCards: List<Card>): AIMove {
        return if (playableCards.isNotEmpty() && Random.nextFloat() < 0.3f) {
            AIMove("PLAY", playableCards.random())
        } else {
            AIMove("DRAW", null)
        }
    }

    private fun makeMediumMove(player: Player, deckSize: Int, playableCards: List<Card>): AIMove {
        val defuseCount = player.hand.count { it.type == CardType.DEFUSE }
        playableCards.find { it.type == CardType.SEE_FUTURE }?.let {
            if (defuseCount <= 1) return AIMove("PLAY", it)
        }
        playableCards.find { it.type == CardType.ATTACK }?.let {
            if (player.hand.size > 6) return AIMove("PLAY", it)
        }
        playableCards.find { it.type == CardType.SHUFFLE }?.let {
            if (deckSize < 5) return AIMove("PLAY", it)
        }
        return AIMove("DRAW", null)
    }

    private fun makeHardMove(player: Player, deckSize: Int, allPlayers: List<Player>, playableCards: List<Card>): AIMove {
        val defuseCount = player.hand.count { it.type == CardType.DEFUSE }
        val alivePlayers = allPlayers.count { it.isAlive }
        val explodingKittensLeft = allPlayers.size - 1 // Simplified assumption
        val riskLevel = if (deckSize > 0) explodingKittensLeft.toFloat() / deckSize else 0f

        if (riskLevel > 0.2f && defuseCount == 0) {
            playableCards.find { it.type == CardType.SKIP }?.let { return AIMove("PLAY", it) }
            playableCards.find { it.type == CardType.SEE_FUTURE }?.let { return AIMove("PLAY", it) }
        }
        if (defuseCount >= 2 || riskLevel < 0.1f) {
            playableCards.find { it.type == CardType.ATTACK }?.let { return AIMove("PLAY", it) }
        }
        if (alivePlayers <= 3) {
            playableCards.find { it.type == CardType.ATTACK }?.let { return AIMove("PLAY", it) }
        }
        return AIMove("DRAW", null)
    }
}
