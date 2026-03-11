package com.enigma.fluffyinc.apps.games.wildtactics.apexcode

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.enigma.fluffyinc.apps.games.wildtactics.data.*
import com.enigma.fluffyinc.apps.games.wildtactics.*
import com.enigma.fluffyinc.apps.games.wildtactics.ai.AIDifficulty
import com.enigma.fluffyinc.apps.games.wildtactics.processor.BlitzEngine
import com.enigma.fluffyinc.apps.games.wildtactics.processor.BlitzEvent
import com.enigma.fluffyinc.apps.games.wildtactics.processor.GameEvent
import com.enigma.fluffyinc.apps.games.wildtactics.processor.GameMode
import com.enigma.fluffyinc.apps.games.wildtactics.processor.GamePhase
import com.enigma.fluffyinc.apps.games.wildtactics.processor.GameState
import com.enigma.fluffyinc.apps.games.wildtactics.processor.KingOfHillEngine
import com.enigma.fluffyinc.apps.games.wildtactics.processor.KingOfHillEvent
import com.enigma.fluffyinc.apps.games.wildtactics.processor.NetworkGameManager
import com.enigma.fluffyinc.apps.games.wildtactics.processor.NetworkState
import com.enigma.fluffyinc.apps.games.wildtactics.processor.WildTacticsGameEngine
import com.enigma.fluffyinc.apps.games.wildtactics.viewmodel.WildTacticsViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.random.Random
import kotlin.text.get



// ==================== Main Game Screen ====================
@Composable
fun WildTacticsGameScreen(
    viewModel: WildTacticsViewModel = viewModel()
) {
    val gameState by viewModel.gameState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A237E),
                        Color(0xFF0D47A1),
                        Color(0xFF01579B)
                    )
                )
            )
    ) {
        when {
            viewModel.showTutorial -> {
                TutorialScreen(
                    onDismiss = { viewModel.showTutorial = false },
                    onStartTutorial = { mode ->
                        viewModel.showTutorial = false
                        when (mode) {
                            TutorialMode.BASIC_GAME -> {
                                viewModel.startGame(GameMode.SinglePlayer(AIDifficulty.Easy), 2)
                            }

                            TutorialMode.KING_OF_HILL -> {
                                viewModel.startGame(GameMode.KingOfHill(), 3)
                            }

                            TutorialMode.BLITZ -> {
                                viewModel.startGame(GameMode.Blitz(), 2)
                            }
                        }
                    }
                )
            }
            viewModel.showGameModeSelector -> {
                GameModeSelector(
                    onModeSelected = { mode, playerCount ->
                        viewModel.startGame(mode, playerCount)
                    }
                )
            }
            gameState != null -> {
                GameBoard(
                    gameState = gameState!!,
                    viewModel = viewModel
                )
            }
        }

        // Error Snackbar
        viewModel.errorMessage?.let { error ->
            ErrorSnackbar(
                message = error,
                onDismiss = { viewModel.dismissError() }
            )
        }

        // Victory Dialog
        if (viewModel.showVictoryDialog) {
            VictoryDialog(
                winner = gameState?.winner,
                onPlayAgain = { viewModel.resetGame() },
                onExit = { viewModel.resetGame() }
            )
        }
    }
}

// ==================== Error Snackbar ====================
@Composable
fun ErrorSnackbar(
    message: String,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onDismiss),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFD32F2F)
            ),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Error,
                    contentDescription = null,
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = message,
                    color = Color.White,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Dismiss",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

// ==================== Game Mode Selector ====================
@Composable
fun GameModeSelector(
    onModeSelected: (GameMode, Int) -> Unit
) {
    var selectedMode by remember { mutableStateOf<GameMode?>(null) }
    var selectedDifficulty by remember { mutableStateOf(AIDifficulty.Medium) }
    var playerCount by remember { mutableIntStateOf(2) }

    val scope = rememberCoroutineScope()

    val columScrollState = remember { ScrollState(0) }

    Column(
        modifier = Modifier
            .scrollable(
                state = columScrollState,
                orientation = Orientation.Vertical,
                flingBehavior = ScrollableDefaults.flingBehavior(

                )
            )
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🦁 WILD TACTICS 🦊",
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 48.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.1f)
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Select Game Mode",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                // Single Player
                GameModeButton(
                    icon = Icons.Default.Person,
                    title = "Single Player",
                    description = "Play against AI",
                    onClick = { selectedMode = GameMode.SinglePlayer(selectedDifficulty) }
                )

                if (selectedMode is GameMode.SinglePlayer) {
                    DifficultySelector(
                        selectedDifficulty = selectedDifficulty,
                        onDifficultySelected = { 
                            selectedDifficulty = it
                            selectedMode = GameMode.SinglePlayer(it)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Pass and Play
                GameModeButton(
                    icon = Icons.Default.PlayArrow,
                    title = "Pass & Play",
                    description = "Local multiplayer on one device",
                    onClick = { selectedMode = GameMode.PassAndPlay }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // King of the Hill
                GameModeButton(
                    icon = Icons.Default.EmojiEvents,
                    title = "King of the Hill",
                    description = "Compete for the crown (3-4 players)",
                    onClick = { 
                        selectedMode = GameMode.KingOfHill()
                        playerCount = 3
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Blitz Mode
                GameModeButton(
                    icon = Icons.Default.Timer,
                    title = "Blitz Mode",
                    description = "Fast-paced with 30s turns (2 players)",
                    onClick = { 
                        selectedMode = GameMode.Blitz()
                        playerCount = 2
                    }
                )

                if (selectedMode != null) {
                    Spacer(modifier = Modifier.height(24.dp))

                    // Player Count Selector (with mode restrictions)
                    when (selectedMode) {
                        is GameMode.Blitz -> {
                            Text(
                                text = "Players: 2 (Fixed)",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 16.sp
                            )
                        }
                        is GameMode.KingOfHill -> {
                            Text(
                                text = "Number of Players: $playerCount",
                                color = Color.White,
                                fontSize = 16.sp
                            )
                            Slider(
                                value = playerCount.toFloat(),
                                onValueChange = { playerCount = it.toInt() },
                                valueRange = 3f..4f,
                                steps = 0,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        else -> {
                            Text(
                                text = "Number of Players: $playerCount",
                                color = Color.White,
                                fontSize = 16.sp
                            )
                            Slider(
                                value = playerCount.toFloat(),
                                onValueChange = { playerCount = it.toInt() },
                                valueRange = 2f..4f,
                                steps = 1,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }

                    Button(
                        onClick = { selectedMode?.let { onModeSelected(it, playerCount) } },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        )
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("START GAME", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun GameModeButton(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.15f)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = description,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun DifficultySelector(
    selectedDifficulty: AIDifficulty,
    onDifficultySelected: (AIDifficulty) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 16.dp)) {
        Text(
            text = "AI Difficulty",
            color = Color.White,
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            AIDifficulty.entries.forEach { difficulty ->
                FilterChip(
                    selected = selectedDifficulty == difficulty,
                    onClick = { onDifficultySelected(difficulty) },
                    label = { Text(difficulty.name) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFFF9800)
                    )
                )
            }
        }
    }
}

// ==================== Game Board ====================
@Composable
fun GameBoard(
    gameState: GameState,
    viewModel: WildTacticsViewModel
) {
    val currentPlayer = gameState.players[gameState.currentPlayerIndex]
    val isPlayerTurn = !currentPlayer.isAiPlayer

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top Section: Opponent Info & Battlefield
        OpponentSection(
            gameState = gameState,
            currentPlayerId = gameState.currentPlayerIndex,
            viewModel = viewModel
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Middle: Game Info & Event Log
        GameInfoPanel(
            gameState = gameState,
            eventLog = viewModel.eventLog,
            viewModel = viewModel,
            onEndPhase = { if (isPlayerTurn) viewModel.endPhase() },
            onResetGame = { viewModel.resetGame() }
        )

        Spacer(modifier = Modifier.weight(1f))

        // Bottom: Current Player Hand & Battlefield
        CurrentPlayerSection(
            gameState = gameState,
            viewModel = viewModel,
            isPlayerTurn = isPlayerTurn
        )
    }
}

// ==================== Game Info Panel ====================
@Composable
fun GameInfoPanel(
    gameState: GameState,
    eventLog: List<String>,
    viewModel: WildTacticsViewModel,
    onEndPhase: () -> Unit,
    onResetGame: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Turn ${gameState.turnNumber}",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Phase: ${gameState.gamePhase::class.simpleName}",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Deck: ${gameState.deck.size} cards",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )
                    
                    // Mode-specific info
                    when (gameState.gameMode) {
                        is GameMode.KingOfHill -> {
                            viewModel.currentKing?.let { kingId ->
                                Text(
                                    text = "👑 King: Player ${kingId + 1} (${viewModel.crownPoints[kingId] ?: 0} pts)",
                                    color = Color(0xFFFFD700),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        is GameMode.Blitz -> {
                            Text(
                                text = "⏱️ Time: ${viewModel.turnTimer}s",
                                color = if (viewModel.turnTimer <= 10) Color.Red else Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Row {
                                viewModel.roundsWon.forEach { (playerId, rounds) ->
                                    Text(
                                        text = "P${playerId + 1}: $rounds | ",
                                        color = Color.White,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                        else -> {}
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Event Log",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    eventLog.forEach { event ->
                        Text(
                            text = "• $event",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 11.sp
                        )
                    }
                }

                Column {
                    Button(
                        onClick = onEndPhase,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF9800)
                        ),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Text("Next Phase")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = onResetGame,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White
                        ),
                        modifier = Modifier.height(40.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Exit", fontSize = 12.sp)
                    }
                    
                    // King of Hill challenge button
                    if (gameState.gameMode is GameMode.KingOfHill && viewModel.currentKing != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { viewModel.challengeKing(gameState.currentPlayerIndex) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFFD700)
                            ),
                            modifier = Modifier.height(40.dp),
                            enabled = viewModel.currentKing != gameState.currentPlayerIndex
                        ) {
                            Text("Challenge", fontSize = 12.sp, color = Color.Black)
                        }
                    }
                }
            }
        }
    }
}

// ==================== Opponent Section ====================
@Composable
fun OpponentSection(
    gameState: GameState,
    currentPlayerId: Int,
    viewModel: WildTacticsViewModel
) {
    val opponents = gameState.players.filterIndexed { index, _ -> index != currentPlayerId }

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(opponents) { opponent ->
            OpponentCard(
                player = opponent,
                battlefield = gameState.battlefield[opponent.id] ?: emptyList(),
                isSelected = viewModel.selectedTargetPlayer == opponent.id,
                isKing = viewModel.currentKing == opponent.id,
                onClick = {
                    viewModel.selectedTargetPlayer = opponent.id
                    if (gameState.gamePhase == GamePhase.Attack) {
                        viewModel.attack(listOf(opponent.id))
                    }
                }
            )
        }
    }
}

@Composable
fun OpponentCard(
    player: Player,
    battlefield: List<Card>,
    isSelected: Boolean,
    isKing: Boolean = false,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "opponent scale"
    )

    Card(
        modifier = Modifier
            .width(180.dp)
            .scale(scale)
            .clickable(onClick = onClick)
            .border(
                width = if (isSelected) 3.dp else if (isKing) 2.dp else 0.dp,
                color = if (isSelected) Color.Yellow else if (isKing) Color(0xFFFFD700) else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF1E88E5).copy(alpha = 0.5f) else Color(0xFF1E88E5).copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = if (player.isAiPlayer) Icons.Default.Computer 
                                  else Icons.Default.Person,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = player.name,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                if (isKing) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "👑", fontSize = 18.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            
            if (isSelected) {
                Text(
                    text = "🎯 SELECTED",
                    color = Color.Yellow,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            Text(
                text = "Hand: ${player.hand.size} cards",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp
            )

            if (battlefield.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Battlefield:",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    items(battlefield) { card ->
                        MiniCardView(card)
                    }
                }
            }

            // Player Effects
            if (player.playerEffects.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                EffectsList(player.playerEffects)
            }
        }
    }
}

@Composable
fun MiniCardView(card: Card) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(card.color1, card.color2)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = card.animal.strength.toString(),
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// ==================== Current Player Section ====================
@Composable
fun CurrentPlayerSection(
    gameState: GameState,
    viewModel: WildTacticsViewModel,
    isPlayerTurn: Boolean
) {
    val currentPlayer = gameState.players[gameState.currentPlayerIndex]
    val battlefield = gameState.battlefield[gameState.currentPlayerIndex] ?: emptyList()

    Column {
        // Battlefield
        if (battlefield.isNotEmpty()) {
            Text(
                text = "Your Battlefield",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                itemsIndexed(battlefield) { index, card ->
                    val isSelected = index in viewModel.selectedAttackCards
                    AnimatedCard(
                        card = card,
                        isSelected = isSelected,
                        isFlipped = false,
                        onClick = {
                            if (gameState.gamePhase == GamePhase.Attack) {
                                viewModel.selectedAttackCards = if (isSelected) {
                                    viewModel.selectedAttackCards - index
                                } else {
                                    viewModel.selectedAttackCards + index
                                }
                            }
                        },
                        showDetails = true
                    )
                }
            }
        }

        // Player Hand
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF2E7D32).copy(alpha = 0.3f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = currentPlayer.name,
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (viewModel.currentKing == gameState.currentPlayerIndex) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "👑", fontSize = 24.sp)
                        }
                    }
                    
                    if (!isPlayerTurn) {
                        Text(
                            text = "AI is thinking...",
                            color = Color.Yellow,
                            fontSize = 14.sp,
                            fontStyle = FontStyle.Italic
                        )
                    }
                }

                if (currentPlayer.playerEffects.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    EffectsList(currentPlayer.playerEffects)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action buttons for selected card
                if (viewModel.selectedCardIndex != null && isPlayerTurn) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val selectedCard = currentPlayer.hand.getOrNull(viewModel.selectedCardIndex!!)
                        val needsTarget = selectedCard?.animal?.animalType is AnimalType.Trickster
                        
                        if (gameState.gamePhase == GamePhase.Play) {
                            Button(
                                onClick = { 
                                    viewModel.selectedCardIndex?.let { index ->
                                        viewModel.playCard(index, viewModel.selectedTargetPlayer)
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF4CAF50)
                                ),
                                enabled = !needsTarget || viewModel.selectedTargetPlayer != null
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (needsTarget && viewModel.selectedTargetPlayer == null) 
                                        "Select Target" 
                                    else 
                                        "Play Card",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            OutlinedButton(
                                onClick = { 
                                    viewModel.selectedCardIndex = null
                                    viewModel.selectedTargetPlayer = null
                                },
                                modifier = Modifier.weight(0.5f),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
                
                // Attack button for selected battlefield cards
                if (viewModel.selectedAttackCards.isNotEmpty() && gameState.gamePhase == GamePhase.Attack && isPlayerTurn) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { 
                                viewModel.selectedTargetPlayer?.let { targetId ->
                                    viewModel.attack(listOf(targetId))
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFF5722)
                            ),
                            enabled = viewModel.selectedTargetPlayer != null
                        ) {
                            Icon(Icons.Default.Bolt, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (viewModel.selectedTargetPlayer == null)
                                    "Select Target"
                                else
                                    "Attack (${viewModel.selectedAttackCards.size} cards)",
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        OutlinedButton(
                            onClick = { 
                                viewModel.selectedAttackCards = emptySet()
                                viewModel.selectedTargetPlayer = null
                            },
                            modifier = Modifier.weight(0.5f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.White
                            )
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(20.dp))
                        }
                    }
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(currentPlayer.hand) { index, card ->
                        val isSelected = viewModel.selectedCardIndex == index
                        val isFlipped = index in viewModel.flippedCards
                        AnimatedCard(
                            card = card,
                            isSelected = isSelected,
                            isFlipped = isFlipped,
                            onClick = {
                                if (isPlayerTurn && gameState.gamePhase == GamePhase.Play) {
                                    // Toggle selection
                                    viewModel.selectedCardIndex = if (isSelected) null else index
                                }
                            },
                            showDetails = true,
                            enabled = isPlayerTurn && gameState.gamePhase == GamePhase.Play
                        )
                    }
                }
            }
        }
    }
}

// ==================== Animated Card with Flip ====================
@Composable
fun AnimatedCard(
    card: Card,
    isSelected: Boolean = false,
    isFlipped: Boolean = false,
    onClick: () -> Unit = {},
    showDetails: Boolean = false,
    enabled: Boolean = true
) {
    var cardRotation by remember { mutableStateOf(0f) }
    
    LaunchedEffect(isFlipped) {
        if (isFlipped) {
            // Flip animation
            cardRotation = 0f
            while (cardRotation < 180f) {
                cardRotation += 18f
                delay(16)
            }
            cardRotation = 180f
            delay(300)
            cardRotation = 360f
        } else {
            cardRotation = 0f
        }
    }
    
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.15f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "card scale"
    )
    
    val elevation by animateFloatAsState(
        targetValue = if (isSelected) 12f else 4f,
        label = "card elevation"
    )

    Card(
        modifier = Modifier
            .width(120.dp)
            .height(160.dp)
            .scale(scale)
            .graphicsLayer {
                rotationY = cardRotation
                cameraDistance = 12f * density
            }
            .clickable(enabled = enabled, onClick = onClick)
            .border(
                width = if (isSelected) 4.dp else 1.dp,
                color = if (isSelected) Color.Yellow else Color.White.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            )
            .alpha(if (enabled) 1f else 0.6f),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(elevation.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    rotationY = if (cardRotation > 90f && cardRotation < 270f) 180f else 0f
                }
                .background(
                    Brush.verticalGradient(
                        colors = listOf(card.color1, card.color2)
                    )
                )
        ) {
            if (cardRotation <= 90f || cardRotation >= 270f) {
                // Front of card
                CardFront(card = card, showDetails = showDetails)
            } else {
                // Back of card (flipped view)
                CardBack()
            }
        }
    }
}

@Composable
fun CardFront(card: Card, showDetails: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = card.animal.name,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 2
        )

        Spacer(modifier = Modifier.weight(1f))

        // Strength
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(Color.Black.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = card.animal.strength.toString(),
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Type indicator
        val typeIcon = when (card.animal.animalType) {
            is AnimalType.Predator -> "⚔️"
            is AnimalType.Trickster -> "🎭"
            AnimalType.CounterAnimal -> "🛡️"
            else -> ""
        }
        Text(
            text = typeIcon,
            fontSize = 20.sp
        )

        // Rarity
        val rarityColor = when (card.animal.animalClass) {
            AnimalClass.Common -> Color.Gray
            AnimalClass.Rare -> Color.Blue
            AnimalClass.Epic -> Color.Magenta
            AnimalClass.Legendary -> Color(0xFFFFD700)
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(rarityColor)
        )
    }
}

@Composable
fun CardBack() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF4A148C),
                        Color(0xFF1A237E)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🦁",
                fontSize = 48.sp
            )
            Text(
                text = "WILD",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "TACTICS",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ==================== Effects List ====================
@Composable
fun EffectsList(effects: Map<PlayerEffects, Int>) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        effects.forEach { (effect, turns) ->
            val (icon, color) = when (effect) {
                is PlayerEffects.Poisoned -> "☠️" to Color(0xFF4CAF50)
                is PlayerEffects.Bleeding -> "🩸" to Color.Red
                PlayerEffects.ShieldOfSavannah -> "🛡️" to Color.Cyan
                PlayerEffects.SwordOfHorus -> "⚔️" to Color(0xFFFFD700)
                PlayerEffects.ToxicBlood -> "🧪" to Color.Green
                PlayerEffects.Ambush -> "🌙" to Color.DarkGray
                PlayerEffects.MonkeySee -> "🙈" to Color(0xFFFF9800)
                PlayerEffects.Blind -> "👁️" to Color.Black
            }
            
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = color.copy(alpha = 0.3f),
                border = BorderStroke(1.dp, color)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = icon, fontSize = 16.sp)
                    Text(
                        text = " $turns",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// ==================== Victory Dialog ====================
@Composable
fun VictoryDialog(
    winner: Player?,
    onPlayAgain: () -> Unit,
    onExit: () -> Unit
) {
    var showConfetti by remember { mutableStateOf(true) }
    
    LaunchedEffect(Unit) {
        delay(5000)
        showConfetti = false
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Confetti effect
        if (showConfetti) {
            repeat(20) { index ->
                ConfettiParticle(index)
            }
        }
        
        AlertDialog(
            onDismissRequest = {},
            title = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "🏆",
                        fontSize = 64.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "VICTORY!",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "${winner?.name ?: "Player"} wins!",
                        fontSize = 24.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "🎉 Congratulations! 🎉",
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = onPlayAgain,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Play Again", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = onExit,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.ExitToApp, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Exit to Menu", fontSize = 16.sp)
                }
            }
        )
    }
}

@Composable
fun ConfettiParticle(index: Int) {
    var offsetY by remember { mutableFloatStateOf(0f) }
    var offsetX by remember { mutableFloatStateOf(Random.nextInt(-200, 200).toFloat()) }
    var rotation by remember { mutableFloatStateOf(0f) }
    
    LaunchedEffect(Unit) {
        while (offsetY < 1000f) {
            offsetY += 5f
            rotation += 10f
            delay(16)
        }
    }
    
    val colors = listOf(
        Color.Red, Color.Blue, Color.Green, Color.Yellow,
        Color.Magenta, Color.Cyan, Color(0xFFFFD700)
    )
    
    Box(
        modifier = Modifier
            .offset(x = offsetX.dp, y = offsetY.dp)
            .size(12.dp)
            .rotate(rotation)
            .background(colors[index % colors.size], CircleShape)
    )
}



// ==================== Helper Extensions ====================
val Card.color1: Color
    get() = when (animal.animalClass) {
        AnimalClass.Common -> Color(0xFF546E7A)
        AnimalClass.Rare -> Color(0xFF1976D2)
        AnimalClass.Epic -> Color(0xFF7B1FA2)
        AnimalClass.Legendary -> Color(0xFFFF6F00)
    }

val Card.color2: Color
    get() = when (animal.animalClass) {
        AnimalClass.Common -> Color(0xFF263238)
        AnimalClass.Rare -> Color(0xFF0D47A1)
        AnimalClass.Epic -> Color(0xFF4A148C)
        AnimalClass.Legendary -> Color(0xFFE65100)
    }