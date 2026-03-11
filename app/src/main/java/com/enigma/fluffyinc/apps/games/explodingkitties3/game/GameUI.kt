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
import androidx.compose.material3.Slider
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
            GameState.PLAYING, GameState.AWAITING_KITTEN_PLACEMENT -> {
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
    // This defensive check prevents crashes if the UI receives a transient
    // invalid state during a complex, multi-stage update.
    if (uiState.players.isEmpty() || uiState.currentPlayerIndex >= uiState.players.size) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Loading Game...")
            CircularProgressIndicator()
        }
        return
    }

    val currentPlayer = uiState.players[uiState.currentPlayerIndex]

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Display other players (AIs) at the top
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            items(uiState.players.filter { it.id != currentPlayer.id }) { player ->
                PlayerStatusCard(player)
            }
        }

        // Deck and Discard pile
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("DECK", style = MaterialTheme.typography.labelMedium)
                Card(modifier = Modifier.size(80.dp, 110.dp)) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Draw (${uiState.deckSize})")
                    }
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("DISCARD", style = MaterialTheme.typography.labelMedium)
                Card(
                    modifier = Modifier.size(80.dp, 110.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiary)
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = uiState.discardPile.lastOrNull()?.name ?: "Empty",
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center,
                            color = Color.White,
                            modifier = Modifier.padding(4.dp)
                        )
                        if (uiState.discardPile.isNullOrEmpty()){
                            Text(
                                text = uiState.discardPile.lastOrNull()?.name ?: "Empty",
                                fontSize = 10.sp,
                                textAlign = TextAlign.Center,
                                color = Color.White,
                                modifier = Modifier.padding(4.dp)
                            )
                        }else{
                            PlayableCard(enabled = false, card = uiState.discardPile.last(), onClick = {})
                        }
                    }
                }
            }
        }

        // Game message and action button
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = uiState.gameMessage,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(8.dp)
                    .background(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            if (currentPlayer.turnsToTake > 1) {
                Text(
                    text = "Turns Left: ${currentPlayer.turnsToTake}",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = onEndTurnAndDraw,
                enabled = currentPlayer.type == PlayerType.HUMAN // Disable button during AI turn
            ) {
                Text("End Turn & Draw Card", fontSize = 16.sp)
            }
        }

        // Current player's hand
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Your Hand (${currentPlayer.hand.size})", style = MaterialTheme.typography.titleMedium)
            if (currentPlayer.isAlive && currentPlayer.type == PlayerType.HUMAN) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                ) {
                    items(currentPlayer.hand) { card ->
                        PlayableCard(
                            card = card,
                            onClick = { onPlayCard(card) },
                            // A card is playable if it's not an exploding kitten or a defuse (defuse is played automatically)
                            enabled = card.type !in listOf(CardType.EXPLODING_KITTEN, CardType.DEFUSE)
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
                    Column {
                        Text("The next 3 cards are:")
                        uiState.futureCards.forEachIndexed { index, card ->
                            Text("${index + 1}. ${card.name}")
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
fun PlayerStatusCard(player: Player) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (player.isAlive) MaterialTheme.colorScheme.surface
            else MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                player.name,
                style = MaterialTheme.typography.titleSmall,
                color = if (player.isAlive) LocalContentColor.current else Color.Gray
            )
            Text(
                if (player.isAlive) "Cards: ${player.hand.size}" else "💀 Exploded",
                fontSize = 12.sp,
                color = if (player.isAlive) LocalContentColor.current else Color.Gray
            )
        }
    }
}

@Composable
fun PlayableCard(card: Card, onClick: () -> Unit, enabled: Boolean) {
    val cardColor = when (card.type) {
        CardType.EXPLODING_KITTEN -> Color(0xFFFF1744)
        CardType.DEFUSE -> Color(0xFF00E676)
        CardType.SKIP -> Color(0xFF2196F3)
        CardType.ATTACK -> Color(0xFFFF5722)
        CardType.SEE_FUTURE -> Color(0xFF9C27B0)
        CardType.SHUFFLE -> Color(0xFFFF9800)
        CardType.NORMAL -> Color(0xFF607D8B)
    }
    Card(
        modifier = Modifier
            .size(80.dp, 110.dp)
            .clickable(enabled = enabled, onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (enabled) cardColor else cardColor.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(if (enabled) 4.dp else 2.dp),
        border = if (enabled) BorderStroke(2.dp, Color.White) else null
    ) {
        if (card.imageId!=null){
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp)
            ){
                Image(
                    painter = painterResource(card.imageId),
                    contentDescription = card.name,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }else{
            Box(
                Modifier.fillMaxSize().padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    card.name,
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 12.sp
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
            TutorialSection("Your Turn", "1. Play as many cards as you'd like from your hand to perform actions.\n2. When you are done playing cards, end your turn by pressing the 'End Turn & Draw Card' button.")
            TutorialSection("Card Types",
                "💣 Exploding Kitten: You lose immediately unless you have a Defuse card.\n\n" +
                        "🛡️ Defuse: If you draw a Kitten, play this card instead of dying. You then get to secretly place the Kitten back into the deck anywhere you like.\n\n" +
                        "⚔️ Attack: Ends your turn without drawing. Force the next player to take two turns. A 'turn' consists of playing cards then drawing one. The victim will have to do this twice.\n\n" +
                        "⏭️ Skip: End your turn without drawing a card. If you were Attacked, this cancels one of the two turns you must take.\n\n" +
                        "🔮 See the Future: Privately look at the top 3 cards of the deck.\n\n" +
                        "🔀 Shuffle: Shuffle the deck.\n\n" +
                        "🐱 Cat Cards: These do nothing on their own. (Advanced Rule: In the full game, you can play pairs to steal cards from others).\n\n" +
                        "🙅 Nope (Advanced Rule): In the full game, this card can be played at any time to cancel another player's action card."
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