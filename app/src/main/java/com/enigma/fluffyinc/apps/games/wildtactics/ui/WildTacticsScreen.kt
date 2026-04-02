package com.enigma.fluffyinc.apps.games.wildtactics.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.enigma.fluffyinc.apps.games.wildtactics.ai.AIDifficulty
import com.enigma.fluffyinc.apps.games.wildtactics.data.*
import com.enigma.fluffyinc.apps.games.wildtactics.processor.*
import com.enigma.fluffyinc.apps.games.wildtactics.viewmodel.WildTacticsViewModel

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
                    colors = listOf(Color(0xFF1B5E20), Color(0xFF4CAF50))
                )
            )
    ) {
        when {
            viewModel.showGameModeSelector -> {
                GameModeSelector(onModeSelected = { mode, players ->
                    viewModel.startGame(mode, players)
                })
            }
            gameState != null -> {
                GameBoard(gameState!!, viewModel)
            }
        }

        // Error handling
        viewModel.errorMessage?.let { error ->
            AlertDialog(
                onDismissRequest = { viewModel.dismissError() },
                title = { Text("Game Error") },
                text = { Text(error) },
                confirmButton = {
                    Button(onClick = { viewModel.dismissError() }) {
                        Text("OK")
                    }
                }
            )
        }

        // Victory Dialog
        if (viewModel.showVictoryDialog) {
            val winner = gameState?.winner
            AlertDialog(
                onDismissRequest = { },
                title = { Text("🎉 Game Over!") },
                text = { Text("${winner?.name ?: "No one"} is the champion of the Wild!") },
                confirmButton = {
                    Button(onClick = { viewModel.resetGame() }) {
                        Text("Return to Menu")
                    }
                }
            )
        }
    }
}

@Composable
fun GameModeSelector(onModeSelected: (GameMode, Int) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "WILD TACTICS",
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(32.dp))
        
        Card(
            modifier = Modifier.padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f))
        ) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Button(
                    onClick = { onModeSelected(GameMode.SinglePlayer(AIDifficulty.Medium), 2) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Single Player (vs AI)")
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { onModeSelected(GameMode.PassAndPlay, 2) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Local Multiplayer (2 Players)")
                }
            }
        }
    }
}

@Composable
fun GameBoard(gameState: GameState, viewModel: WildTacticsViewModel) {
    val currentPlayer = gameState.players[gameState.currentPlayerIndex]
    val isPlayerTurn = !currentPlayer.isAiPlayer

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Opponent Info
        val opponent = gameState.players.first { it.id != gameState.currentPlayerIndex }
        OpponentSection(opponent)

        Spacer(modifier = Modifier.weight(1f))

        // Center: Turn info and Phase info
        GameStatusSection(gameState, viewModel)

        Spacer(modifier = Modifier.weight(1f))

        // Player Section
        PlayerSection(currentPlayer, gameState, viewModel, isPlayerTurn)
    }
}

@Composable
fun OpponentSection(opponent: Player) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AccountCircle, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(opponent.name, color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.weight(1f))
                Text("❤️ ${opponent.lives}", color = Color.Red, fontWeight = FontWeight.Bold)
            }
            
            // Opponent Battlefield
            LazyRow(modifier = Modifier.padding(top = 8.dp)) {
                items(opponent.battlefield) { card ->
                    MiniCard(card)
                }
            }
        }
    }
}

@Composable
fun GameStatusSection(gameState: GameState, viewModel: WildTacticsViewModel) {
    Card(
        modifier = Modifier.fillMaxWidth().wrapContentHeight(),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "Phase: ${gameState.gamePhase::class.simpleName}",
                color = Color.Yellow,
                fontWeight = FontWeight.Bold
            )
            
            val log = viewModel.eventLog.lastOrNull() ?: "Prepare for battle!"
            Text(log, color = Color.White, fontSize = 12.sp, textAlign = TextAlign.Center)

            if (!gameState.players[gameState.currentPlayerIndex].isAiPlayer) {
                Button(
                    onClick = { viewModel.endPhase() },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("End ${gameState.gamePhase::class.simpleName}")
                }
            } else {
                Text("AI is thinking...", color = Color.White, modifier = Modifier.padding(8.dp))
            }
        }
    }
}

@Composable
fun PlayerSection(
    player: Player,
    gameState: GameState,
    viewModel: WildTacticsViewModel,
    isTurn: Boolean
) {
    Column {
        // Battlefield
        Text("Battlefield", color = Color.White, fontSize = 14.sp)
        LazyRow(modifier = Modifier.height(100.dp)) {
            itemsIndexed(player.battlefield) { index, card ->
                val isSelected = index in viewModel.selectedAttackCards
                CardView(
                    card = card,
                    isSelected = isSelected,
                    onClick = {
                        if (isTurn && gameState.gamePhase == GamePhase.Attack) {
                            viewModel.selectedAttackCards = if (isSelected) {
                                viewModel.selectedAttackCards - index
                            } else {
                                viewModel.selectedAttackCards + index
                            }
                        }
                    }
                )
            }
        }

        if (isTurn && gameState.gamePhase == GamePhase.Attack && viewModel.selectedAttackCards.isNotEmpty()) {
            val opponent = gameState.players.first { it.id != player.id }
            Button(
                onClick = { viewModel.attack(opponent.id) },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text("ATTACK!")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Hand
        Text("Your Hand", color = Color.White, fontSize = 14.sp)
        LazyRow(modifier = Modifier.height(150.dp)) {
            itemsIndexed(player.hand) { index, card ->
                val isSelected = viewModel.selectedCardIndex == index
                CardView(
                    card = card,
                    isSelected = isSelected,
                    onClick = {
                        if (isTurn && gameState.gamePhase == GamePhase.Play) {
                            if (isSelected) {
                                viewModel.playCard(index)
                            } else {
                                viewModel.selectedCardIndex = index
                            }
                        }
                    }
                )
            }
        }
        
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("❤️ ${player.lives}", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Spacer(modifier = Modifier.weight(1f))
            Text(player.name, color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun CardView(card: Card, isSelected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(100.dp)
            .padding(4.dp)
            .border(
                width = if (isSelected) 3.dp else 0.dp,
                color = if (isSelected) Color.Yellow else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(8.dp))
                .background(Brush.verticalGradient(listOf(card.color1, card.color2)))
        ) {
            Column(
                modifier = Modifier.padding(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    card.animal.name,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    card.currentStrength.toString(),
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    card.animal.animalType::class.simpleName ?: "",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 10.sp
                )
            }
            if (card.hasAttacked) {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                }
            }
        }
    }
}

@Composable
fun MiniCard(card: Card) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .padding(2.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(Brush.verticalGradient(listOf(card.color1, card.color2))),
        contentAlignment = Alignment.Center
    ) {
        Text(
            card.currentStrength.toString(),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}
