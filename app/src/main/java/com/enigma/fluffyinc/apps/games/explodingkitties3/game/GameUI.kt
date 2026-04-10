package com.enigma.fluffyinc.apps.games.explodingkitties3.game

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.enigma.fluffyinc.apps.games.explodingkitties3.data.Card
import com.enigma.fluffyinc.apps.games.explodingkitties3.data.Player
import com.enigma.fluffyinc.apps.games.explodingkitties3.data.states.GameMode
import com.enigma.fluffyinc.apps.games.explodingkitties3.data.states.GameState
import com.enigma.fluffyinc.apps.games.explodingkitties3.data.states.GameUiState
import com.enigma.fluffyinc.games.explodingkitties3.data.types.AIDifficulty
import com.enigma.fluffyinc.games.explodingkitties3.data.types.CardType
import com.enigma.fluffyinc.games.explodingkitties3.data.types.PlayerType
import com.enigma.fluffyinc.apps.games.explodingkitties3.ui.GameScreen
import kotlin.math.roundToInt

@Composable
fun ExplodingKittens(
    uiState: GameUiState,
    onGameModeSelected: (GameMode) -> Unit,
    onPlayerCountChange: (Int) -> Unit,
    onAIDifficultyChange: (AIDifficulty) -> Unit,
    onStartGame: () -> Unit,
    onBackToMenu: () -> Unit,
    onPlayCard: (Card) -> Unit,
    onEndTurnAndDraw: () -> Unit,
    onShowTutorial: () -> Unit,
    onCloseFuture: () -> Unit,
    onKittenPlaced: (Int) -> Unit,
    onHandoffConfirmed: () -> Unit,
    onHostIpChange: (String) -> Unit,
    onStartHost: () -> Unit,
    onJoinHost: (String) -> Unit,

) {
    /**
    Column(
        modifier = Modifier.fillMaxSize().padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "💣 Exploding Kittens 🐱",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        when (uiState.gameState) {
            GameState.MENU -> MainMenuScreen(onGameModeSelected, onShowTutorial)
            GameState.SETUP -> SetupScreen(uiState, onPlayerCountChange, onAIDifficultyChange, onStartGame, onBackToMenu)
            GameState.TUTORIAL -> TutorialScreen(onBack = onBackToMenu)
            GameState.PLAYING, GameState.AWAITING_KITTEN_PLACEMENT, GameState.NOPE_CHANCE -> {
                GamePlayScreen(uiState, onPlayCard, onEndTurnAndDraw, onCloseFuture)
                if (uiState.gameState == GameState.AWAITING_KITTEN_PLACEMENT) {
                    PlaceKittenDialog(uiState.deckSize, onKittenPlaced)
                }
            }
            GameState.GAME_OVER -> GameOverScreen(uiState.winner, onBackToMenu)
            GameState.HANDOFF -> HandoffScreen(uiState, onHandoffConfirmed)
            else -> {}
        }
    }
     */
    GameScreen(
        uiState = uiState,
        onGameModeSelected = onGameModeSelected,
        onPlayerCountChange = onPlayerCountChange,
        onAIDifficultyChange = onAIDifficultyChange,
        onStartGame = onStartGame,
        onBackToMenu = onBackToMenu,
        onPlayCard = onPlayCard,
        onEndTurnAndDraw = onEndTurnAndDraw,
        onShowTutorial = onShowTutorial,
        onCloseFuture = onCloseFuture,
        onKittenPlaced = onKittenPlaced,
        onHandoffConfirmed = onHandoffConfirmed,
        onHostIpChange = onHostIpChange,
        onStartHost = onStartHost,
        onJoinHost = onJoinHost,
    )
}

@Composable
fun MainMenuScreen(
    onGameModeSelected: (GameMode) -> Unit,
    onShowTutorial: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        modifier = Modifier.fillMaxSize()
    ) {
        Text("Main Menu", style = MaterialTheme.typography.headlineSmall)

        Button(
            onClick = { onGameModeSelected(GameMode.SINGLE_PLAYER) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Person, null); Spacer(Modifier.width(8.dp)); Text("Single Player vs AI")
        }

        // New Button for Pass and Play
        Button(
            onClick = { onGameModeSelected(GameMode.PASS_AND_PLAY) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.People, null); Spacer(Modifier.width(8.dp)); Text("Pass and Play")
        }

        Button(
            onClick = onShowTutorial,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Info, null); Spacer(Modifier.width(8.dp)); Text("How to Play")
        }
    }
}
@Composable
fun GamePlayScreen(
    uiState: GameUiState,
    onPlayCard: (Card) -> Unit,
    onEndTurnAndDraw: () -> Unit,
    onCloseFuture: () -> Unit
) {
    if (uiState.players.isEmpty() || uiState.currentPlayerIndex >= uiState.players.size) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(16.dp))
                Text("Waiting for Players...", color = MaterialTheme.colorScheme.primary)
            }
        }
        return
    }

    val currentPlayer = uiState.players[uiState.currentPlayerIndex]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Player status bar
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            contentPadding = PaddingValues(horizontal = 8.dp)
        ) {
            items(uiState.players) { player ->
                PlayerStatusCard(player = player, isCurrent = player.id == currentPlayer.id)
            }
        }

        // Central Deck/Discard Area
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Deck
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "DECK",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Card(
                        modifier = Modifier
                            .size(100.dp, 140.dp),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "💣",
                                    fontSize = 24.sp,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                                Text(
                                    "${uiState.deckSize}",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }

                // Discard Pile
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "DISCARD",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Card(
                        modifier = Modifier
                            .size(100.dp, 140.dp),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            if (uiState.discardPile.isEmpty()) {
                                Text(
                                    "EMPTY",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                            } else {
                                PlayableCard(
                                    enabled = false,
                                    card = uiState.discardPile.last(),
                                    onClick = {},
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
            }

            // Game Message / Action Status
            Surface(
                modifier = Modifier
                    .padding(vertical = 16.dp)
                    .fillMaxWidth(0.9f),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                tonalElevation = 4.dp
            ) {
                Text(
                    text = uiState.gameMessage,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp),
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Action controls
        Column(
            modifier = Modifier.padding(bottom = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = onEndTurnAndDraw,
                enabled = currentPlayer.type == PlayerType.HUMAN,
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(56.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
            ) {
                Text("End Turn & Draw Card", style = MaterialTheme.typography.titleMedium)
            }
        }

        // Current player's hand
        val showFullHand = uiState.gameState != GameState.NOPE_CHANCE || currentPlayer.type == PlayerType.HUMAN

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                )
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                if (uiState.gameState == GameState.NOPE_CHANCE) "QUICK! PLAY A NOPE?" else "Your Hand (${currentPlayer.hand.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (uiState.gameState == GameState.NOPE_CHANCE) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (uiState.gameState == GameState.NOPE_CHANCE) {
                // In NOPE_CHANCE, show countdown
                LinearProgressIndicator(
                    progress = { uiState.actionCountdown / 5f },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 8.dp),
                    color = MaterialTheme.colorScheme.error
                )
            }

            if (currentPlayer.isAlive) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(currentPlayer.hand) { card ->
                        val isNope = card.type == CardType.NOPE
                        val canPlay = if (uiState.gameState == GameState.NOPE_CHANCE) isNope else card.type !in listOf(CardType.EXPLODING_KITTEN, CardType.DEFUSE)
                        
                        PlayableCard(
                            card = card,
                            onClick = { onPlayCard(card) },
                            enabled = canPlay && currentPlayer.type == PlayerType.HUMAN
                        )
                    }
                }
            }
        }

        // See the Future Dialog
        if (uiState.showFutureCards) {
            AlertDialog(
                onDismissRequest = onCloseFuture,
                title = { Text("🔮 Future Cards") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        uiState.futureCards.forEachIndexed { index, card ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "${index + 1}",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(end = 12.dp)
                                    )
                                    Text(card.name, style = MaterialTheme.typography.bodyLarge)
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = onCloseFuture) { Text("Got it") }
                }
            )
        }
    }
}

// PlayerStatusCard, PlayableCard, GameOverScreen, etc. would be here
// The following are included for completeness.

@Composable
fun PlayerStatusCard(player: Player, isCurrent: Boolean = false) {
    val borderColor = if (isCurrent) MaterialTheme.colorScheme.primary else Color.Transparent
    val borderWidth = if (isCurrent) 2.dp else 0.dp

    Card(
        modifier = Modifier
            .padding(4.dp)
            .width(100.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(borderWidth, borderColor),
        colors = CardDefaults.cardColors(
            containerColor = if (player.isAlive) {
                if (isCurrent) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant
            } else MaterialTheme.colorScheme.errorContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isCurrent) 8.dp else 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = if (player.type == PlayerType.HUMAN) Icons.Default.Person else Icons.Default.People,
                contentDescription = null,
                tint = if (player.isAlive) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.size(24.dp)
            )
            Text(
                player.name,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                color = if (player.isAlive) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                if (player.isAlive) "${player.hand.size} Cards" else "💀 EXPLODED",
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = if (player.isAlive) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onErrorContainer
            )
            if (player.isAlive && player.turnsToTake > 1) {
                Text(
                    "Turns: ${player.turnsToTake}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun PlayableCard(
    card: Card,
    onClick: () -> Unit,
    enabled: Boolean,
    isSelected: Boolean = false,
    modifier: Modifier = Modifier
) {
    val cardColor = when (card.type) {
        CardType.EXPLODING_KITTEN -> Color(0xFFFF1744)
        CardType.DEFUSE -> Color(0xFF00E676)
        CardType.SKIP -> Color(0xFF2196F3)
        CardType.ATTACK -> Color(0xFFFF5722)
        CardType.SEE_FUTURE -> Color(0xFF9C27B0)
        CardType.SHUFFLE -> Color(0xFFFF9800)
        CardType.NORMAL -> Color(0xFF607D8B)
        CardType.NOPE -> Color(0xFF424242)
    }

    Card(
        modifier = modifier
            .size(85.dp, 120.dp)
            .clickable(enabled = enabled, onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (enabled) cardColor else cardColor.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 12.dp else if (enabled) 6.dp else 2.dp),
        border = BorderStroke(
            width = if (isSelected) 3.dp else 1.dp,
            color = if (isSelected) Color.Yellow else Color.White.copy(alpha = 0.5f)
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Card Content
            if (card.imageId != null) {
                Image(
                    painter = painterResource(card.imageId),
                    contentDescription = card.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp)
                )
            }

            // Card Overlay / Name
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
                            startY = 60f
                        )
                    )
                    .padding(4.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Text(
                    text = card.name,
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 11.sp,
                    maxLines = 2
                )
            }

            if (!enabled && card.type != CardType.EXPLODING_KITTEN) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.2f))
                )
            }
        }
    }
}

@Composable
fun PlaceKittenDialog(deckSize: Int, onKittenPlaced: (Int) -> Unit) {
    var sliderPosition by remember { mutableFloatStateOf(0f) }
    val position = sliderPosition.roundToInt()

    AlertDialog(
        onDismissRequest = { /* Disallow dismissing */ },
        title = { Text("Hide the Exploding Kitten!") },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Place the kitten back into the Draw Pile.", Modifier.padding(bottom = 16.dp))
                if (deckSize > 0) {
                    Slider(value = sliderPosition, onValueChange = { sliderPosition = it }, steps = deckSize, valueRange = 0f..deckSize.toFloat())
                    val positionText = when(position) {
                        0 -> "Top of the Deck"
                        deckSize -> "Bottom of the Deck"
                        else -> "Position $position from the top"
                    }
                    Text(positionText, fontWeight = FontWeight.Bold)
                } else {
                    Text("Deck is empty. Placing on top.")
                }
            }
        },
        confirmButton = {
            Button(onClick = { onKittenPlaced(position) }) { Text("Place Kitten") }
        }
    )
}

@Composable
fun TutorialScreen(onBack: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back to Menu") }
            Text("How to Play", style = MaterialTheme.typography.headlineSmall)
        }
        Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp)) {
            TutorialSection("The Goal", "If you draw an Exploding Kitten, you lose. You win by being the last player left alive!")
            TutorialSection("Setup", "Each player starts with 1 Defuse card and 7 random cards. The deck is then seeded with one fewer Exploding Kittens than there are players.")
            TutorialSection("Your Turn", "1. Play as many cards as you'd like from your hand to perform actions.\n2. When you are done playing cards, end your turn by drawing a card. You win by being the last player left alive!")
            TutorialSection("Card Types",
                "💣 Exploding Kitten: You lose immediately unless you have a Defuse card.\n\n" +
                        "🛡️ Defuse: If you draw a Kitten, play this card instead of dying. You then get to secretly place the Kitten back into the deck anywhere you like.\n\n" +
                        "⚔️ Attack: Ends your turn without drawing. Force the next player to take two turns.\n\n" +
                        "⏭️ Skip: End your turn without drawing a card. If you were Attacked, this cancels one of the two turns you must take.\n\n" +
                        "🔮 See the Future: Privately look at the top 3 cards of the deck.\n\n" +
                        "🔀 Shuffle: Shuffle the deck.\n\n" +
                        "🐱 Cat Cards: Play a pair of matching cat cards to steal a random card from another player.\n\n" +
                        "🙅 Nope: Play this card at any time to cancel another player's action card. Any number of Nopes can be played on top of each other. An odd number of Nopes cancels the original action; an even number allows it."
            )
        }
    }
}

@Composable
fun TutorialSection(title: String, text: String) {
    Text(title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 16.dp, bottom = 4.dp))
    Text(text, style = MaterialTheme.typography.bodyLarge)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupScreen(
    uiState: GameUiState,
    onPlayerCountChange: (Int) -> Unit,
    onAIDifficultyChange: (AIDifficulty) -> Unit,
    onStartGame: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
            }
            Text(text = "Game Setup", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.padding(horizontal = 24.dp))
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // This text clarifies that the player count includes the human player + AIs
                Text(
                    text = if (uiState.gameMode == GameMode.SINGLE_PLAYER)
                        "Total Players (You + AI)"
                    else
                        "Number of Players:"
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { onPlayerCountChange(uiState.playerCount - 1) },
                        enabled = uiState.playerCount > 2
                    ) { Text("-") }

                    Text(
                        text = uiState.playerCount.toString(),
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Button(
                        onClick = { onPlayerCountChange(uiState.playerCount + 1) },
                        enabled = uiState.playerCount < 5 // Max players set to 5
                    ) { Text("+") }
                }

                if (uiState.gameMode == GameMode.SINGLE_PLAYER) {
                    HorizontalDivider(
                        Modifier.padding(vertical = 8.dp),
                        DividerDefaults.Thickness,
                        DividerDefaults.color
                    )
                    Text("AI Difficulty:")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AIDifficulty.entries.forEach { difficulty ->
                            FilterChip(
                                onClick = { onAIDifficultyChange(difficulty) },
                                label = { Text(difficulty.name.replaceFirstChar { it.uppercase() }) },
                                selected = uiState.aiDifficulty == difficulty
                            )
                        }
                    }
                    Text(
                        text = when (uiState.aiDifficulty) {
                            AIDifficulty.EASY -> "🟢 Easy: Random plays, good for beginners"
                            AIDifficulty.MEDIUM -> "🟡 Medium: Some strategy, balanced gameplay"
                            AIDifficulty.HARD -> "🔴 Hard: Advanced strategy, challenging opponent"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }
        Button(
            onClick = onStartGame,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Start Game", fontSize = 18.sp)
        }
        TextButton(onClick = onBack) {
            Text("Back to Menu")
        }
    }
}

@Composable
fun GameOverScreen(winner: Player?, onResetGame: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically), modifier = Modifier.fillMaxSize()) {
        Text("🎉 Game Over! 🎉", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
        winner?.let { Text("${it.name} Wins!", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.secondary) }
        Button(onClick = onResetGame, modifier = Modifier.padding(top = 16.dp)) { Text("Play Again", fontSize = 18.sp) }
    }
}


// New Composable for the Handoff Screen
@Composable
fun HandoffScreen(
    uiState: GameUiState,
    onConfirmHandoff: () -> Unit
) {
    val nextPlayerName = uiState.players.getOrNull(uiState.currentPlayerIndex)?.name ?: "the next player"

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Pass the device to",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = nextPlayerName,
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(vertical = 16.dp)
        )
        Button(
            onClick = onConfirmHandoff,
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Text("I am $nextPlayerName, show my hand!", fontSize = 16.sp)
        }
    }
}