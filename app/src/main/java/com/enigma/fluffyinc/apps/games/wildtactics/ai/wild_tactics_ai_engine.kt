package com.enigma.fluffyinc.apps.games.wildtactics.ai

import com.enigma.fluffyinc.apps.games.wildtactics.data.AnimalClass
import com.enigma.fluffyinc.apps.games.wildtactics.data.AnimalType
import com.enigma.fluffyinc.apps.games.wildtactics.data.Card
import com.enigma.fluffyinc.apps.games.wildtactics.data.PlayerEffects
import com.enigma.fluffyinc.apps.games.wildtactics.processor.BlitzEngine
import com.enigma.fluffyinc.apps.games.wildtactics.processor.GameMode
import com.enigma.fluffyinc.apps.games.wildtactics.processor.GamePhase
import com.enigma.fluffyinc.apps.games.wildtactics.processor.GameState
import com.enigma.fluffyinc.apps.games.wildtactics.processor.KingOfHillEngine
import com.enigma.fluffyinc.apps.games.wildtactics.processor.WildTacticsGameEngine
import kotlinx.coroutines.delay
import kotlin.random.Random

// ==================== AI Decision System ====================
data class AIDecision(
    val action: AIAction,
    val confidence: Float,
    val reasoning: String
)

sealed class AIAction {
    data class PlayCard(val cardIndex: Int, val targetPlayerId: Int?) : AIAction()
    data class Attack(val defenderIds: List<Int>, val cardIndices: List<Int>) : AIAction()
    data object EndPhase : AIAction()
    data object Pass : AIAction()
    data class ChallengeKing(val kingId: Int) : AIAction()
}

// ==================== AI Personality Traits ====================
data class AIPersonality(
    val aggression: Float,      // 0.0 - 1.0: How likely to attack
    val caution: Float,          // 0.0 - 1.0: How defensive
    val greed: Float,            // 0.0 - 1.0: Resource hoarding
    val cunning: Float,          // 0.0 - 1.0: Trick card usage
    val patience: Float,         // 0.0 - 1.0: Long-term planning
    val adaptability: Float      // 0.0 - 1.0: Strategy adjustment
) {
    companion object {
        fun forDifficulty(difficulty: AIDifficulty): AIPersonality {
            return when (difficulty) {
                AIDifficulty.Easy -> AIPersonality(
                    aggression = 0.3f,
                    caution = 0.2f,
                    greed = 0.6f,
                    cunning = 0.1f,
                    patience = 0.1f,
                    adaptability = 0.2f
                )
                AIDifficulty.Medium -> AIPersonality(
                    aggression = 0.5f,
                    caution = 0.5f,
                    greed = 0.4f,
                    cunning = 0.4f,
                    patience = 0.5f,
                    adaptability = 0.5f
                )
                AIDifficulty.Hard -> AIPersonality(
                    aggression = 0.7f,
                    caution = 0.7f,
                    greed = 0.3f,
                    cunning = 0.7f,
                    patience = 0.7f,
                    adaptability = 0.8f
                )
                AIDifficulty.Expert -> AIPersonality(
                    aggression = 0.85f,
                    caution = 0.9f,
                    greed = 0.2f,
                    cunning = 0.9f,
                    patience = 0.9f,
                    adaptability = 1.0f
                )
            }
        }
    }
}

// ==================== Game State Evaluator ====================
class GameStateEvaluator {
    
    fun evaluatePosition(state: GameState, aiPlayerId: Int): Float {
        var score = 0f
        
        val aiPlayer = state.players[aiPlayerId]
        val opponents = state.players.filter { it.id != aiPlayerId }
        
        // Hand size advantage
        score += aiPlayer.hand.size * 5f
        
        // Battlefield strength
        val aiStrength = state.battlefield[aiPlayerId]?.sumOf { it.animal.strength } ?: 0
        score += aiStrength * 3f
        
        // Relative strength to opponents
        opponents.forEach { opponent ->
            val oppStrength = state.battlefield[opponent.id]?.sumOf { it.animal.strength } ?: 0
            score += (aiStrength - oppStrength) * 2f
        }
        
        // Card quality in hand
        aiPlayer.hand.forEach { card ->
            score += when (card.animal.animalClass) {
                AnimalClass.Legendary -> 15f
                AnimalClass.Epic -> 10f
                AnimalClass.Rare -> 5f
                AnimalClass.Common -> 2f
            }
        }
        
        // Strategic card types
        val tricksterCount = aiPlayer.hand.count { it.animal.animalType is AnimalType.Trickster }
        val counterCount = aiPlayer.hand.count { it.animal.animalType == AnimalType.CounterAnimal }
        score += tricksterCount * 8f + counterCount * 6f
        
        // Opponent threat assessment
        opponents.forEach { opponent ->
            if (opponent.hand.size > aiPlayer.hand.size + 2) {
                score -= 10f // Opponent has card advantage
            }
        }
        
        return score
    }
    
    fun evaluateCardValue(card: Card, context: GameContext): Float {
        var value = card.animal.strength.toFloat()
        
        // Rarity bonus
        value += when (card.animal.animalClass) {
            AnimalClass.Legendary -> 20f
            AnimalClass.Epic -> 12f
            AnimalClass.Rare -> 6f
            AnimalClass.Common -> 0f
        }
        
        // Type effectiveness
        when (card.animal.animalType) {
            is AnimalType.Predator -> {
                value += 8f
                if (context.opponentHasWeakDefense) value += 5f
            }
            is AnimalType.Trickster -> {
                value += 10f
                if (context.needsUtility) value += 8f
            }
            AnimalType.CounterAnimal -> {
                value += 7f
                if (context.underAttack) value += 10f
            }
            else -> {}
        }
        
        return value
    }
    
    fun evaluateAttackOutcome(
        attackerCards: List<Card>,
        defenderCards: List<Card>,
        attackerEffects: Map<PlayerEffects, Int>,
        defenderEffects: Map<PlayerEffects, Int>
    ): AttackOutcome {
        var attackPower = attackerCards.sumOf { it.animal.strength }.toFloat()
        var defensePower = defenderCards.sumOf { it.animal.strength }.toFloat()
        
        // Apply effects
        if (PlayerEffects.SwordOfHorus in attackerEffects) attackPower *= 1.5f
        if (PlayerEffects.ShieldOfSavannah in defenderEffects) defensePower *= 1.3f
        if (PlayerEffects.Poisoned() in defenderEffects) attackPower += 5f
        
        val netDamage = maxOf(0f, attackPower - defensePower)
        val success = netDamage > 0
        val risk = defensePower / maxOf(1f, attackPower)
        
        return AttackOutcome(
            expectedDamage = netDamage,
            successProbability = if (success) 0.8f else 0.2f,
            riskLevel = risk,
            worthwhile = netDamage > defensePower * 0.5f
        )
    }
}

data class GameContext(
    val opponentHasWeakDefense: Boolean,
    val needsUtility: Boolean,
    val underAttack: Boolean,
    val isWinning: Boolean,
    val isLosing: Boolean
)

data class AttackOutcome(
    val expectedDamage: Float,
    val successProbability: Float,
    val riskLevel: Float,
    val worthwhile: Boolean
)

// ==================== Main AI Engine ====================
class WildTacticsAI(
    private val difficulty: AIDifficulty,
    private val gameMode: GameMode
) {
    private val personality = AIPersonality.forDifficulty(difficulty)
    private val evaluator = GameStateEvaluator()
    private val moveHistory = mutableListOf<AIAction>()
    private var turnCount = 0
    
    suspend fun executeTurn(
        state: GameState,
        aiPlayerId: Int,
        engine: WildTacticsGameEngine
    ) {
        try {
            turnCount++
            delay(calculateThinkingTime())
            
            val context = analyzeGameContext(state, aiPlayerId)
            
            when (state.gamePhase) {
                GamePhase.Draw -> {
                    engine.endPhase()
                }
                GamePhase.Play -> {
                    executePlayPhase(state, aiPlayerId, context, engine)
                }
                GamePhase.Attack -> {
                    executeAttackPhase(state, aiPlayerId, context, engine)
                }
                GamePhase.End -> {
                    engine.endPhase()
                    engine.endPhase() // Complete the turn
                }
            }
        } catch (e: Exception) {
            println("AI Error: ${e.message}")
            engine.endPhase()
        }
    }
    
    private fun calculateThinkingTime(): Long {
        return when (difficulty) {
            AIDifficulty.Easy -> Random.nextLong(500, 1000)
            AIDifficulty.Medium -> Random.nextLong(800, 1500)
            AIDifficulty.Hard -> Random.nextLong(1200, 2000)
            AIDifficulty.Expert -> Random.nextLong(1500, 2500)
        }
    }
    
    private fun analyzeGameContext(state: GameState, aiPlayerId: Int): GameContext {
        val aiPlayer = state.players[aiPlayerId]
        val opponents = state.players.filter { it.id != aiPlayerId }
        
        val aiStrength = state.battlefield[aiPlayerId]?.sumOf { it.animal.strength } ?: 0
        val avgOpponentStrength = opponents.map { 
            state.battlefield[it.id]?.sumOf { card -> card.animal.strength } ?: 0 
        }.average()
        
        val totalOpponentCards = opponents.sumOf { it.hand.size }
        val positionScore = evaluator.evaluatePosition(state, aiPlayerId)
        
        return GameContext(
            opponentHasWeakDefense = avgOpponentStrength < aiStrength * 0.7,
            needsUtility = aiPlayer.hand.none { it.animal.animalType is AnimalType.Trickster },
            underAttack = avgOpponentStrength > aiStrength * 1.3,
            isWinning = positionScore > 50f,
            isLosing = positionScore < -30f
        )
    }
    
    private suspend fun executePlayPhase(
        state: GameState,
        aiPlayerId: Int,
        context: GameContext,
        engine: WildTacticsGameEngine
    ) {
        val decisions = evaluatePlayOptions(state, aiPlayerId, context)
        val cardsToPlay = selectCardsToPlay(decisions, context)
        
        cardsToPlay.forEach { decision ->
            when (val action = decision.action) {
                is AIAction.PlayCard -> {
                    delay(300)
                    val result = engine.playCard(aiPlayerId, action.cardIndex, action.targetPlayerId)
                    result.onSuccess {
                        moveHistory.add(action)
                    }
                }
                else -> {}
            }
        }
        
        delay(500)
        engine.endPhase()
    }
    
    private fun evaluatePlayOptions(
        state: GameState,
        aiPlayerId: Int,
        context: GameContext
    ): List<AIDecision> {
        val player = state.players[aiPlayerId]
        val decisions = mutableListOf<AIDecision>()
        
        player.hand.forEachIndexed { index, card ->
            val value = evaluator.evaluateCardValue(card, context)
            var confidence = value / 50f // Normalize to 0-1
            
            // Adjust confidence based on personality
            when (card.animal.animalType) {
                is AnimalType.Predator -> {
                    confidence *= (1f + personality.aggression)
                }
                is AnimalType.Trickster -> {
                    confidence *= (1f + personality.cunning)
                }
                AnimalType.CounterAnimal -> {
                    confidence *= (1f + personality.caution)
                }
                else -> {}
            }
            
            // Strategic adjustments
            if (context.isLosing && card.animal.animalType is AnimalType.Predator) {
                confidence *= 1.3f // Aggression when behind
            }
            
            if (context.isWinning && card.animal.animalType == AnimalType.CounterAnimal) {
                confidence *= 1.2f // Defensive when ahead
            }
            
            // Difficulty-based randomness
            confidence += when (difficulty) {
                AIDifficulty.Easy -> Random.nextFloat() * 0.3f - 0.15f
                AIDifficulty.Medium -> Random.nextFloat() * 0.2f - 0.1f
                AIDifficulty.Hard -> Random.nextFloat() * 0.1f - 0.05f
                AIDifficulty.Expert -> Random.nextFloat() * 0.05f - 0.025f
            }
            
            val targetPlayer = selectTarget(state, aiPlayerId, card)
            
            decisions.add(AIDecision(
                action = AIAction.PlayCard(index, targetPlayer),
                confidence = confidence.coerceIn(0f, 1f),
                reasoning = buildReasoningString(card, confidence, context)
            ))
        }
        
        return decisions.sortedByDescending { it.confidence }
    }
    
    private fun selectCardsToPlay(
        decisions: List<AIDecision>,
        context: GameContext
    ): List<AIDecision> {
        val threshold = when (difficulty) {
            AIDifficulty.Easy -> 0.3f
            AIDifficulty.Medium -> 0.5f
            AIDifficulty.Hard -> 0.6f
            AIDifficulty.Expert -> 0.7f
        }
        
        val maxCards = when {
            context.isLosing -> 3 // Play more when desperate
            context.isWinning -> 1 // Conserve when ahead
            else -> 2
        }
        
        return decisions.filter { it.confidence >= threshold }.take(maxCards)
    }
    
    private suspend fun executeAttackPhase(
        state: GameState,
        aiPlayerId: Int,
        context: GameContext,
        engine: WildTacticsGameEngine
    ) {
        val attackDecision = evaluateAttackOptions(state, aiPlayerId, context)
        
        if (attackDecision != null && attackDecision.confidence > getAttackThreshold()) {
            when (val action = attackDecision.action) {
                is AIAction.Attack -> {
                    delay(500)
                    val result = engine.attack(aiPlayerId, action.defenderIds, action.cardIndices)
                    result.onSuccess {
                        moveHistory.add(action)
                    }
                }
                else -> {}
            }
        }
        
        delay(300)
        engine.endPhase()
    }
    
    private fun evaluateAttackOptions(
        state: GameState,
        aiPlayerId: Int,
        context: GameContext
    ): AIDecision? {
        val aiCards = state.battlefield[aiPlayerId] ?: return null
        if (aiCards.isEmpty()) return null
        
        val opponents = state.players.filter { it.id != aiPlayerId && it.isAlive }
        if (opponents.isEmpty()) return null
        
        val aiPlayer = state.players[aiPlayerId]
        val attackOptions = mutableListOf<Pair<List<Int>, AttackOutcome>>()
        
        opponents.forEach { opponent ->
            val defenderCards = state.battlefield[opponent.id] ?: emptyList()
            
            // Evaluate different attack combinations
            val outcome = evaluator.evaluateAttackOutcome(
                aiCards,
                defenderCards,
                aiPlayer.playerEffects,
                opponent.playerEffects
            )
            
            attackOptions.add(
                Pair(listOf(opponent.id), outcome)
            )
        }
        
        // Select best attack
        val bestAttack = attackOptions.maxByOrNull { (_, outcome) ->
            outcome.expectedDamage * outcome.successProbability - outcome.riskLevel * personality.caution
        } ?: return null
        
        val (targets, outcome) = bestAttack
        
        // Calculate confidence
        var confidence = outcome.successProbability * personality.aggression
        
        if (outcome.worthwhile) confidence *= 1.3f
        if (context.isLosing) confidence *= 1.2f // Desperate attacks
        if (context.isWinning && outcome.riskLevel > 0.5f) confidence *= 0.7f // Risk averse when ahead
        
        // Difficulty adjustments
        confidence += when (difficulty) {
            AIDifficulty.Easy -> Random.nextFloat() * 0.4f - 0.2f
            AIDifficulty.Medium -> Random.nextFloat() * 0.3f - 0.15f
            AIDifficulty.Hard -> Random.nextFloat() * 0.2f - 0.1f
            AIDifficulty.Expert -> Random.nextFloat() * 0.1f - 0.05f
        }
        
        return AIDecision(
            action = AIAction.Attack(targets, aiCards.indices.toList()),
            confidence = confidence.coerceIn(0f, 1f),
            reasoning = "Attack with ${aiCards.size} cards, expected damage: ${outcome.expectedDamage.toInt()}"
        )
    }
    
    private fun getAttackThreshold(): Float {
        return when (difficulty) {
            AIDifficulty.Easy -> 0.2f
            AIDifficulty.Medium -> 0.4f
            AIDifficulty.Hard -> 0.5f
            AIDifficulty.Expert -> 0.6f
        }
    }
    
    private fun selectTarget(state: GameState, aiPlayerId: Int, card: Card): Int? {
        val opponents = state.players.filter { it.id != aiPlayerId && it.isAlive }
        if (opponents.isEmpty()) return null
        
        return when (card.animal.animalType) {
            is AnimalType.Trickster -> {
                // Target player with most cards
                opponents.maxByOrNull { it.hand.size }?.id
            }
            else -> {
                // Target weakest opponent
                opponents.minByOrNull { opponent ->
                    state.battlefield[opponent.id]?.sumOf { it.animal.strength } ?: 0
                }?.id
            }
        }
    }
    
    private fun buildReasoningString(card: Card, confidence: Float, context: GameContext): String {
        return buildString {
            append("${card.animal.name} (${card.animal.strength})")
            append(" - Confidence: ${(confidence * 100).toInt()}%")
            if (context.isLosing) append(" [DESPERATE]")
            if (context.isWinning) append(" [AHEAD]")
        }
    }
}

// ==================== Mode-Specific AI ====================

// King of the Hill AI
class KingOfHillAI(
    private val baseAI: WildTacticsAI,
    private val difficulty: AIDifficulty
) {
    private val personality = AIPersonality.forDifficulty(difficulty)
    
    suspend fun evaluateKingChallenge(
        state: GameState,
        aiPlayerId: Int,
        kingId: Int,
        kingEngine: KingOfHillEngine
    ): Boolean {
        if (aiPlayerId == kingId) return false
        
        val aiStrength = state.battlefield[aiPlayerId]?.sumOf { it.animal.strength } ?: 0
        val kingStrength = state.battlefield[kingId]?.sumOf { it.animal.strength } ?: 0
        
        // King has crown shield (5 damage absorption)
        val effectiveStrength = maxOf(0, aiStrength - 5)
        
        var challengeValue = (effectiveStrength - kingStrength).toFloat()
        
        // Strategic considerations
        val crownPoints = kingEngine.getCrownPoints()[kingId] ?: 0
        if (crownPoints > 30) challengeValue += 20f // High urgency if king close to winning
        
        // Personality influence
        challengeValue *= personality.aggression
        if (kingStrength > aiStrength) challengeValue *= personality.caution * 0.5f // Risky attack
        
        val threshold = when (difficulty) {
            AIDifficulty.Easy -> -5f
            AIDifficulty.Medium -> 0f
            AIDifficulty.Hard -> 3f
            AIDifficulty.Expert -> 5f
        }
        
        return challengeValue > threshold
    }
    
    fun evaluateKingBehavior(state: GameState, aiPlayerId: Int): KingStrategy {
        val opponentCount = state.players.count { it.id != aiPlayerId && it.isAlive }
        val aiStrength = state.battlefield[aiPlayerId]?.sumOf { it.animal.strength } ?: 0
        
        return when {
            aiStrength < 10 -> KingStrategy.DEFENSIVE // Build up defenses
            opponentCount > 2 -> KingStrategy.BALANCED // Multiple threats
            personality.aggression > 0.7f -> KingStrategy.AGGRESSIVE // Crush opposition
            else -> KingStrategy.BALANCED
        }
    }
}

enum class KingStrategy {
    DEFENSIVE,   // Focus on counter cards and defense
    BALANCED,    // Mix of attack and defense
    AGGRESSIVE   // Eliminate threats aggressively
}

// Blitz Mode AI
class BlitzModeAI(
    private val baseAI: WildTacticsAI,
    private val difficulty: AIDifficulty
) {
    private var decisionTimeMs = 0L
    
    suspend fun executeBlitzTurn(
        state: GameState,
        aiPlayerId: Int,
        engine: WildTacticsGameEngine,
        timeRemaining: Int
    ) {
        val startTime = System.currentTimeMillis()
        
        // Faster decision making in blitz mode
        val thinkTime = when (difficulty) {
            AIDifficulty.Easy -> 300L
            AIDifficulty.Medium -> 500L
            AIDifficulty.Hard -> 800L
            AIDifficulty.Expert -> 1000L
        }
        
        delay(thinkTime)
        
        // Quick play strategy
        val player = state.players[aiPlayerId]
        if (player.hand.isNotEmpty()) {
            // Play strongest card immediately
            val bestCard = player.hand.withIndex().maxByOrNull { (_, card) ->
                card.animal.strength + when (card.animal.animalClass) {
                    AnimalClass.Legendary -> 20
                    AnimalClass.Epic -> 10
                    AnimalClass.Rare -> 5
                    AnimalClass.Common -> 0
                }
            }
            
            bestCard?.let { (index, _) ->
                engine.playCard(aiPlayerId, index)
                delay(200)
            }
        }
        
        engine.endPhase() // To attack phase
        delay(200)
        
        // Quick attack if possible
        val battlefield = state.battlefield[aiPlayerId]
        if (battlefield != null && battlefield.isNotEmpty()) {
            val opponents = state.players.filter { it.id != aiPlayerId }
            if (opponents.isNotEmpty()) {
                engine.attack(aiPlayerId, listOf(opponents.first().id), battlefield.indices.toList())
            }
        }
        
        delay(200)
        engine.endPhase() // End turn
        
        decisionTimeMs = System.currentTimeMillis() - startTime
    }
    
    fun getAverageDecisionTime() = decisionTimeMs
}

// ==================== AI Manager ====================
class AIManager(
    private val difficulty: AIDifficulty,
    private val gameMode: GameMode
) {
    private val baseAI = WildTacticsAI(difficulty, gameMode)
    private val kingAI = KingOfHillAI(baseAI, difficulty)
    private val blitzAI = BlitzModeAI(baseAI, difficulty)
    
    suspend fun executeTurn(
        state: GameState,
        aiPlayerId: Int,
        engine: WildTacticsGameEngine,
        kingEngine: KingOfHillEngine? = null,
        blitzEngine: BlitzEngine? = null
    ) {
        when (gameMode) {
            is GameMode.Blitz -> {
                blitzEngine?.let { blitz ->
                    blitzAI.executeBlitzTurn(
                        state,
                        aiPlayerId,
                        engine,
                        blitz.turnTimer.value
                    )
                }
            }
            is GameMode.KingOfHill -> {
                baseAI.executeTurn(state, aiPlayerId, engine)
                
                // Evaluate king challenge after turn
                kingEngine?.let { king ->
                    king.getCurrentKing()?.let { kingId ->
                        if (kingAI.evaluateKingChallenge(state, aiPlayerId, kingId, king)) {
                            delay(500)
                            king.challengeKing(aiPlayerId)
                        }
                    }
                }
            }
            else -> {
                baseAI.executeTurn(state, aiPlayerId, engine)
            }
        }
    }
}