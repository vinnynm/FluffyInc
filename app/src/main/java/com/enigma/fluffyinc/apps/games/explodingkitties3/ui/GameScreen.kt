package com.enigma.fluffyinc.apps.games.explodingkitties3.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.enigma.fluffyinc.apps.games.explodingkitties3.data.Card
import com.enigma.fluffyinc.apps.games.explodingkitties3.data.states.GameMode
import com.enigma.fluffyinc.apps.games.explodingkitties3.data.states.GameState
import com.enigma.fluffyinc.apps.games.explodingkitties3.data.states.GameUiState
import com.enigma.fluffyinc.games.explodingkitties3.data.types.AIDifficulty
import com.enigma.fluffyinc.apps.games.explodingkitties3.game.GameOverScreen
import com.enigma.fluffyinc.apps.games.explodingkitties3.game.GamePlayScreen
import com.enigma.fluffyinc.apps.games.explodingkitties3.game.HandoffScreen
import com.enigma.fluffyinc.apps.games.explodingkitties3.game.PlaceKittenDialog
import com.enigma.fluffyinc.apps.games.explodingkitties3.game.SetupScreen
import com.enigma.fluffyinc.apps.games.explodingkitties3.game.TutorialScreen

// This is the main entry point for the UI, which routes to other screens
// based on the game state.
@Composable
fun GameScreen(
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
    // New handlers for the network lobby
    onHostIpChange: (String) -> Unit,
    onStartHost: () -> Unit,
    onJoinHost: (String) -> Unit
) {
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
            GameState.LOBBY -> NetworkLobbyScreen(
                uiState = uiState,
                onHostIpChange = onHostIpChange,
                onStartHost = onStartHost,
                onJoinHost = onJoinHost,
                onStartGame = onStartGame,
                onBack = onBackToMenu
            )
            GameState.TUTORIAL -> TutorialScreen(onBack = onBackToMenu)
            GameState.PLAYING, GameState.AWAITING_KITTEN_PLACEMENT -> {
                GamePlayScreen(uiState, onPlayCard, onEndTurnAndDraw, onCloseFuture)
                if (uiState.gameState == GameState.AWAITING_KITTEN_PLACEMENT) {
                    PlaceKittenDialog(uiState.deckSize, onKittenPlaced)
                }
            }
            GameState.HANDOFF -> HandoffScreen(uiState = uiState, onConfirmHandoff = onHandoffConfirmed)
            GameState.GAME_OVER -> GameOverScreen(uiState.winner, onBackToMenu)
        }
    }
}



// --- Updated Main Menu Screen ---
@Composable
fun MainMenuScreen(onGameModeSelected: (GameMode) -> Unit, onShowTutorial: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        modifier = Modifier.fillMaxSize()
    ) {
        Text("Main Menu", style = MaterialTheme.typography.headlineSmall)

        Button(onClick = { onGameModeSelected(GameMode.SINGLE_PLAYER) }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.Person, null); Spacer(Modifier.width(8.dp)); Text("Single Player vs AI")
        }

        Button(onClick = { onGameModeSelected(GameMode.PASS_AND_PLAY) }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.People, null); Spacer(Modifier.width(8.dp)); Text("Pass and Play")
        }

        // --- NEW: Buttons for WiFi Multiplayer ---
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp),
            thickness = DividerDefaults.Thickness,
            color = DividerDefaults.color
        )

        Button(onClick = { onGameModeSelected(GameMode.NETWORK_HOST) }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.Wifi, null); Spacer(Modifier.width(8.dp)); Text("Host WiFi Game")
        }

        Button(onClick = { onGameModeSelected(GameMode.NETWORK_JOIN) }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.WifiTethering, null); Spacer(Modifier.width(8.dp)); Text("Join WiFi Game")
        }

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp),
            thickness = DividerDefaults.Thickness,
            color = DividerDefaults.color
        )

        Button(onClick = onShowTutorial, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.Info, null); Spacer(Modifier.width(8.dp)); Text("How to Play")
        }
    }
}


// --- NEW: Network Lobby Screen ---
@Composable
fun NetworkLobbyScreen(
    uiState: GameUiState,
    onHostIpChange: (String) -> Unit,
    onStartHost: () -> Unit,
    onJoinHost: (String) -> Unit,
    onStartGame: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
            Text(
                text = if (uiState.gameMode == GameMode.NETWORK_HOST) "Host Lobby" else "Join Game",
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(Modifier.width(48.dp)) // Balance the back button
        }

        if (uiState.connectionStatus.isNotEmpty()) {
            Text(
                uiState.connectionStatus,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.secondary,
                textAlign = TextAlign.Center
            )
        }

        // --- HOST VIEW ---
        if (uiState.gameMode == GameMode.NETWORK_HOST) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Your IP Address:", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = uiState.localIP,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Button(onClick = onStartHost) {
                        Text("Start Hosting")
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        thickness = DividerDefaults.Thickness,
                        color = DividerDefaults.color
                    )

                    Text("Connected Players: ${uiState.connectedPlayers.size + 1}")
                    LazyColumn {
                        items(uiState.connectedPlayers) { player ->
                            Text(player)
                        }
                    }

                    // The host can start the game once at least one other player has joined.
                    Button(
                        onClick = onStartGame,
                        enabled = uiState.connectedPlayers.isNotEmpty()
                    ) {
                        Text("Start Game")
                    }
                }
            }
        }
        // --- CLIENT (JOIN) VIEW ---
        else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.hostIP,
                        onValueChange = onHostIpChange,
                        label = { Text("Enter Host IP Address") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )
                    Button(onClick = { onJoinHost.invoke(uiState.hostIP) }) {
                        Text("Connect to Host")
                    }
                    Text(
                        "Waiting for the host to start the game...",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
