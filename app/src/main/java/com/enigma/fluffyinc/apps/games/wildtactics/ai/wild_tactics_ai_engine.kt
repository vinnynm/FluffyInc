package com.enigma.fluffyinc.apps.games.wildtactics.ai

import com.enigma.fluffyinc.apps.games.wildtactics.data.*
import com.enigma.fluffyinc.apps.games.wildtactics.processor.*
import kotlinx.coroutines.delay
import kotlin.random.Random

class WildTacticsAI(
    private val difficulty: AIDifficulty
) {
    suspend fun executeTurn(
        state: GameState,
        aiPlayerId: Int,
        engine: WildTacticsGameEngine
    ) {
        // Always work with the latest state from the engine
        val currentState = engine.gameState.value ?: return
        if (currentState.currentPlayerIndex != aiPlayerId || currentState.isGameOver) return

        when (currentState.gamePhase) {
            GamePhase.Draw -> {
                engine.endPhase()
            }
            GamePhase.Play -> {
                val player = currentState.players[aiPlayerId]
                // AI Logic: Play up to 2 best cards
                val handIndices = player.hand.indices.toMutableList()
                repeat(2) {
                    if (handIndices.isNotEmpty()) {
                        // Re-fetch state to get updated hand indices
                        val latestPlayer = engine.gameState.value?.players?.get(aiPlayerId) ?: return@repeat
                        if (latestPlayer.hand.isEmpty()) return@repeat
                        
                        val bestIndex = latestPlayer.hand.indices.maxByOrNull { latestPlayer.hand[it].animal.strength } ?: 0
                        engine.playCard(aiPlayerId, bestIndex)
                        delay(600)
                    }
                }
                engine.endPhase()
            }
            GamePhase.Attack -> {
                val player = engine.gameState.value?.players?.get(aiPlayerId) ?: return
                val opponents = currentState.players.filter { it.id != aiPlayerId && it.isAlive }
                
                if (player.battlefield.isNotEmpty() && opponents.isNotEmpty()) {
                    val target = opponents.random()
                    val attackingIndices = player.battlefield.indices.filter { !player.battlefield[it].hasAttacked }
                    
                    if (attackingIndices.isNotEmpty()) {
                        engine.attack(aiPlayerId, listOf(target.id), attackingIndices)
                        delay(600)
                    }
                }
                engine.endPhase()
            }
            GamePhase.End -> {
                engine.endPhase()
            }
        }
    }
}

class AIManager(
    private val difficulty: AIDifficulty,
    private val gameMode: GameMode
) {
    private val ai = WildTacticsAI(difficulty)
    private var isBusy = false

    suspend fun executeTurn(
        state: GameState,
        aiPlayerId: Int,
        engine: WildTacticsGameEngine
    ) {
        if (isBusy) return
        isBusy = true
        try {
            ai.executeTurn(state, aiPlayerId, engine)
        } finally {
            isBusy = false
        }
    }
}
