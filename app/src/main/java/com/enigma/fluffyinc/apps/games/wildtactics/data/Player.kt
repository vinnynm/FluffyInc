package com.enigma.fluffyinc.apps.games.wildtactics.data

data class Player(
    val id: Int,
    val name: String,
    val lives: Int = 100,
    val isAiPlayer: Boolean = false,
    val hand: List<Card> = emptyList(),
    val battlefield: List<Card> = emptyList()
) {
    val isAlive: Boolean get() = lives > 0

    fun takeDamage(amount: Int): Player {
        return copy(lives = (lives - amount).coerceAtLeast(0))
    }

    fun resetForNewTurn(): Player {
        return copy(battlefield = battlefield.map { it.copy(hasAttacked = false) })
    }
}
